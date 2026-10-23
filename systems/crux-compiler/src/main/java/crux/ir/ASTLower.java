package crux.ir;

import crux.ast.SymbolTable.Symbol;
import crux.ast.*;
import crux.ast.OpExpr.Operation;
import crux.ast.traversal.NodeVisitor;
import crux.ast.types.*;
import crux.ir.insts.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class InstPair {
  private Instruction start;
  private Instruction end;
  private LocalVar value;

  public InstPair(Instruction start, Instruction end, LocalVar value) {
    this.start = start;
    this.end = end;
    this.value = value;

  }
  public InstPair(Instruction start, Instruction end){
    this.start = start;
    this.end = end;
    this.value = null;
  }

  public InstPair(Instruction start){
    this.start = start;
    this.end = start;
    this.value = null;
  }

  Instruction getStart(){
    return this.start;
  }
  Instruction getEnd(){
    return this.end;
  }
  LocalVar getValue(){
    return this.value;
  }
  void setStart(Instruction start1){
    this.start = start1;
  }

  void setEnd(Instruction end1){
    this.end = end1;
  }
  void setValue(LocalVar val){
    this.value = val;
  }

}


/**
 * Convert AST to IR and build the CFG
 */
public final class ASTLower implements NodeVisitor<InstPair> {
  private Program mCurrentProgram = null;
  private Function mCurrentFunction = null;
  private Instruction mCurrentLoopHeader = null;
  private Instruction mCurrentLoopExit = null;

  private Map<Symbol, LocalVar> mCurrentLocalVarMap = null;

  /**
   * A constructor to initialize member variables
   */
  public ASTLower() {
  }

  public Program lower(DeclarationList ast) {
    visit(ast);
    return mCurrentProgram;

  }

  @Override
  public InstPair visit(DeclarationList declarationList) {
    mCurrentProgram = new Program();
    for (Node child : declarationList.getChildren()) {
      child.accept(this);
    }
    return null;
  }

  /**
   * This visitor should create a Function instance for the functionDefinition node, add parameters
   * to the localVarMap, add the function to the program, and init the function start Instruction.
   */
  @Override
  public InstPair visit(FunctionDefinition functionDefinition) {
    String name = functionDefinition.getSymbol().getName();
    FuncType FT = (FuncType) functionDefinition.getSymbol().getType();
    mCurrentFunction = new Function(name, FT);
    List<LocalVar> localvars = new ArrayList<>();
    mCurrentLocalVarMap = new HashMap<Symbol, LocalVar>();
    for (Symbol parameter : functionDefinition.getParameters()) {
      LocalVar varr = mCurrentFunction.getTempVar(parameter.getType());
      localvars.add(varr);
      mCurrentLocalVarMap.put(parameter, varr);
    }
    mCurrentFunction.setArguments(localvars);
    mCurrentProgram.addFunction(mCurrentFunction);

    InstPair pair = functionDefinition.getStatements().accept(this); // is this visiting the current function's body?
    if (pair != null){
      mCurrentFunction.setStart(pair.getStart());
    }
    mCurrentFunction = null;
    mCurrentLocalVarMap = null;
    return null;
  }

  @Override
  public InstPair visit(StatementList statementList) {
    Instruction start = new NopInst();
    InstPair result = new InstPair(start, null);

    for (Node child : statementList.getChildren()) {
      InstPair accept = child.accept(this);

      Instruction INS_start = accept.getStart();
      Instruction INS_end = accept.getEnd();

      start.setNext(0, INS_start);
      start = INS_end;
    }

    result.setEnd(start);
    return result;
  }

  /**
   * Declarations, could be either local or Global
   */
  @Override
  public InstPair visit(VariableDeclaration variableDeclaration) {
      if (mCurrentFunction == null){
        long val = 1;
        IntegerConstant IC = IntegerConstant.get(mCurrentProgram, val);
        GlobalDecl NewGlobalDecl = new GlobalDecl(variableDeclaration.getSymbol(), IC);
        mCurrentProgram.addGlobalVar(NewGlobalDecl);
      } else {

        LocalVar var = mCurrentFunction.getTempVar(variableDeclaration.getSymbol().getType());
        mCurrentLocalVarMap.put(variableDeclaration.getSymbol(), var);

      }
    return new InstPair(new NopInst());
  }

  /**
   * Create a declaration for array and connected it to the CFG
   */
  @Override
  public InstPair visit(ArrayDeclaration arrayDeclaration) {
    long val = ((ArrayType)arrayDeclaration.getSymbol().getType()).getExtent();
    IntegerConstant IC = IntegerConstant.get(mCurrentProgram, val);
    GlobalDecl newGlobal = new GlobalDecl(arrayDeclaration.getSymbol(), IC);
    mCurrentProgram.addGlobalVar(newGlobal);
    return new InstPair(new NopInst());
  }

  /**
   * LookUp the name in the map(s). For globals, we should do a load to get the value to load into a
   * LocalVar.
   */
  @Override
  public InstPair visit(VarAccess name) {
    if (mCurrentLocalVarMap.containsKey(name.getSymbol())){ //local variable
      LocalVar value = mCurrentLocalVarMap.get(name.getSymbol());
      Instruction inst = new NopInst();
      return new InstPair(inst, inst, value);
    } else { //global variable
      AddressVar av = mCurrentFunction.getTempAddressVar(name.getSymbol().getType());
      LocalVar LV = mCurrentFunction.getTempVar(name.getSymbol().getType());

      AddressAt AT = new AddressAt(av, name.getSymbol());

      Instruction LS = new LoadInst(LV, av);
      AT.setNext(0, LS);
      return new InstPair(AT, LS, LV);
    }
  }

  /**
   * If the location is a VarAccess to a LocalVar, copy the value to it. If the location is a
   * VarAccess to a global, store the value. If the location is ArrayAccess, store the value.
   */
  @Override
  public InstPair visit(Assignment assignment) {
    //local varAccess: rhs -> copyInst
    //global varAccess: AddressAt & store
    //arrayAcces: AddressAt & store
    if (assignment.getLocation() instanceof VarAccess){ // it is a variable
      if (mCurrentLocalVarMap.containsKey(((VarAccess) assignment.getLocation()).getSymbol())){ //then it is a local var
        InstPair rightside = assignment.getValue().accept(this);
        CopyInst copy = new CopyInst(mCurrentLocalVarMap.get(((VarAccess) assignment.getLocation()).getSymbol()), rightside.getValue());
        rightside.getEnd().setNext(0, copy);
        return new InstPair(rightside.getStart(), copy);

      } else { // it is a global varAccess
        AddressVar AV = mCurrentFunction.getTempAddressVar(((VarAccess) assignment.getLocation()).getType());
        AddressAt AddrAt = new AddressAt(AV, ((VarAccess) assignment.getLocation()).getSymbol());
        InstPair rightside = assignment.getValue().accept(this);
        AddrAt.setNext(0, rightside.getStart());
        StoreInst StoreInstr = new StoreInst(rightside.getValue(),AV);
        rightside.getEnd().setNext(0,StoreInstr);

        return new InstPair(AddrAt, StoreInstr);
      }
    }
    else if (assignment.getLocation() instanceof ArrayAccess){ //it is an array
      Expression index = ((ArrayAccess) assignment.getLocation()).getIndex();
      InstPair visitIndex = index.accept(this);
      InstPair rightside = assignment.getValue().accept(this);
      Type arrType = ((ArrayType)((ArrayAccess) assignment.getLocation()).getBase().getType()).getBase();
      AddressVar AV = mCurrentFunction.getTempAddressVar(arrType);
      AddressAt AddrAt = new AddressAt(AV,((ArrayAccess) assignment.getLocation()).getBase(), visitIndex.getValue());
      visitIndex.getEnd().setNext(0, AddrAt);
      AddrAt.setNext(0, rightside.getStart());
      StoreInst store = new StoreInst(rightside.getValue(), AV);
      rightside.getEnd().setNext(0, store);
      return new InstPair(visitIndex.getStart(), store);
    }

    return null;
  }

  /**
   * Lower a Call.
   */
  @Override
  public InstPair visit(Call call) {
    Instruction first = new NopInst();
    InstPair result = new InstPair(first, null);
    List<LocalVar> args = new ArrayList<>();

    InstPair prev = null;
    for (Expression argument : call.getArguments()) {

      InstPair accept = argument.accept(this);
      if (prev != null){
        prev.getEnd().setNext(0, accept.getStart());
      } else {
        result.setStart(accept.getStart());
      }
      prev = accept;
      args.add(accept.getValue());
    }
    Type returnType = ((FuncType) call.getCallee().getType()).getRet();

    CallInst callInst = null;
    if (returnType.getClass() != VoidType.class){ //if its not void
      LocalVar temp = mCurrentFunction.getTempVar(returnType);
      result.setValue(temp); //set return type
      callInst = new CallInst(temp, call.getCallee(), args);
    } else {
      callInst = new CallInst(call.getCallee(), args); //else no return type
    }

    if (prev != null){
      prev.getEnd().setNext(0, callInst);
    }
    if (prev == null){
      result.getStart().setNext(0, callInst);
    }

    result.setEnd(callInst);
    return result;
  }

  /**
   * Handle operations like arithmetics and comparisons. Also handle logical operations (and,
   * or, not).
   */
  @Override
  public InstPair visit(OpExpr operation) {

    Expression LHS = operation.getLeft();
    Expression RHS = operation.getRight();
    String oper = "";
    if (RHS != null && LHS != null){
      InstPair lhs = LHS.accept(this);
      InstPair rhs = RHS.accept(this);
      oper = operation.getOp().name();
      BinaryOperator.Op bin = null;
      CompareInst.Predicate pred = null;
      switch(oper){
        case "ADD":
          bin = BinaryOperator.Op.Add;
          break;
        case "SUB":
          bin = BinaryOperator.Op.Sub;
          break;
        case "MULT":
          bin = BinaryOperator.Op.Mul;
          break;
        case "DIV":
          bin = BinaryOperator.Op.Div;
          break;
        case "GT":
          pred = CompareInst.Predicate.GT;
          break;
        case "GE":
          pred = CompareInst.Predicate.GE;
          break;
        case "LE":
          pred = CompareInst.Predicate.LE;
          break;
        case "LT":
          pred = CompareInst.Predicate.LT;
          break;
        case "EQ":
          pred = CompareInst.Predicate.EQ;
          break;
        case "NE":
          pred = CompareInst.Predicate.NE;
          break;
        case "LOGIC_OR":
          bin = null;
          pred = null;
          break;
        case "LOGIC_AND":
          bin = null;
          pred = null;
          break;
      }
      if (bin != null){
        lhs.getEnd().setNext(0, rhs.getStart());
        LocalVar dest = mCurrentFunction.getTempVar(operation.getType());
        BinaryOperator BO = new BinaryOperator(bin, dest, lhs.getValue(), rhs.getValue() );
        rhs.getEnd().setNext(0, BO);
        return new InstPair(lhs.getStart(), BO, BO.getDst());
      } else if (pred != null){
        lhs.getEnd().setNext(0, rhs.getStart());
        LocalVar dest = mCurrentFunction.getTempVar(operation.getType());
        CompareInst CI = new CompareInst(dest, pred, lhs.getValue(), rhs.getValue());
        rhs.getEnd().setNext(0, CI);
        return new InstPair(lhs.getStart(), CI, CI.getDst());
      }
      if (pred == null && bin == null){
        //AND or OR
        if (oper.equals("LOGIC_OR")){
          JumpInst JI = new JumpInst(lhs.getValue());

          lhs.getEnd().setNext(0, JI);

          JI.setNext(0, rhs.getStart());
          LocalVar LV0 = mCurrentFunction.getTempVar(rhs.getValue().getType());
          CopyInst CI0 = new CopyInst(LV0, rhs.getValue());
          rhs.getEnd().setNext(0, CI0);
          NopInst NO0 = new NopInst();
          CI0.setNext(0, NO0);

          CopyInst CI1 = new CopyInst(LV0, lhs.getValue());
          JI.setNext(1, CI1);
          CI1.setNext(0, NO0);

          return new InstPair(lhs.getStart(), NO0, LV0);
        }
        if (oper.equals("LOGIC_AND")){
          JumpInst JI = new JumpInst(lhs.getValue());
          lhs.getEnd().setNext(0, JI);

          JI.setNext(1, rhs.getStart());

          LocalVar LV0 = mCurrentFunction.getTempVar(rhs.getValue().getType());
          CopyInst CI0 = new CopyInst(LV0, rhs.getValue());
          rhs.getEnd().setNext(0, CI0);

          NopInst NO0 = new NopInst();
          CI0.setNext(0, NO0);


          CopyInst CI1 = new CopyInst(LV0, lhs.getValue());
          JI.setNext(0, CI1);
          CI1.setNext(0, NO0);

          return new InstPair(lhs.getStart(), NO0, LV0);
        }
      }



    } else if (RHS == null && LHS != null){
      //logical not
      InstPair lhs = LHS.accept(this);
      LocalVar dest = mCurrentFunction.getTempVar(operation.getType());
      UnaryNotInst UN = new UnaryNotInst(dest, lhs.getValue());
      lhs.getEnd().setNext(0, UN);
      return new InstPair(lhs.getStart(), UN, dest);

    }
    return null;

  }


  /**
   * It should compute the address into the array, do the load, and return the value in a LocalVar.
   */
  @Override
  public InstPair visit(ArrayAccess access) {
    //global variable
    Type arrType = ((ArrayType)access.getBase().getType()).getBase();
    AddressVar av = mCurrentFunction.getTempAddressVar(arrType);
    LocalVar LV = mCurrentFunction.getTempVar(arrType);
    InstPair ind = access.getIndex().accept(this);
    AddressAt AT = new AddressAt(av, access.getBase(), ind.getValue());
    ind.getEnd().setNext(0, AT);
    Instruction LS = new LoadInst(LV, av);
    AT.setNext(0, LS);
    return new InstPair(ind.getStart(), LS, LV);

  }

  /**
   * Copy the literal into a tempVar
   */
  @Override
  public InstPair visit(LiteralBool literalBool) {
    LocalVar localVar = mCurrentFunction.getTempVar(literalBool.getType());
    BooleanConstant val = BooleanConstant.get(mCurrentProgram, literalBool.getValue());
    CopyInst CI = new CopyInst(localVar, val);
    return new InstPair(CI, CI, localVar);
  }

  /**
   * Copy the literal into a tempVar
   */
  @Override
  public InstPair visit(LiteralInt literalInt) {
    LocalVar localVar = mCurrentFunction.getTempVar(literalInt.getType());
    IntegerConstant val = IntegerConstant.get(mCurrentProgram, literalInt.getValue());
    CopyInst CI = new CopyInst(localVar, val);
    return new InstPair(CI, CI, localVar);

  }

  /**
   * Lower a Return.
   */
  @Override
  public InstPair visit(Return ret) {

    InstPair r = ret.getValue().accept(this);
    ReturnInst RI = new ReturnInst(r.getValue());
    r.getEnd().setNext(0, RI);

    return new InstPair(r.getStart(), RI);
  }

  /**
   * Break Node
   */
  @Override
  public InstPair visit(Break brk) {
    Instruction breakInst = new NopInst();
    breakInst.setNext(0, mCurrentLoopExit);
    return new InstPair(breakInst, new NopInst());
  }

  @Override
  public InstPair visit(Continue cont) {
    Instruction continueInst = new NopInst();
    continueInst.setNext(0, mCurrentLoopHeader);
    return new InstPair(continueInst, new NopInst());
  }


  /**
   * Implement If Then Else statements.
   */
  @Override
  public InstPair visit(IfElseBranch ifElseBranch) {

    InstPair condition = ifElseBranch.getCondition().accept(this);

    JumpInst jumpInst = new JumpInst(condition.getValue());
    condition.getEnd().setNext(0, jumpInst);

    InstPair thenBlock = ifElseBranch.getThenBlock().accept(this);
    jumpInst.setNext(1, thenBlock.getStart());

    InstPair elseBlock = null;
    if (ifElseBranch.getElseBlock() != null) {
      elseBlock = ifElseBranch.getElseBlock().accept(this);
      jumpInst.setNext(0, elseBlock.getStart());
    } else {
      Instruction nopElse = new NopInst();
      jumpInst.setNext(0, nopElse);
      elseBlock = new InstPair(nopElse, nopElse);
    }

    NopInst mergeNop = new NopInst();
    thenBlock.getEnd().setNext(0, mergeNop);
    elseBlock.getEnd().setNext(0, mergeNop);

    return new InstPair(condition.getStart(), mergeNop);
  }


  /**
   * Implement loops.
   */
  @Override
  public InstPair visit(Loop loop) {
    Instruction previousLoopHeader = mCurrentLoopHeader;
    Instruction previousLoopExit = mCurrentLoopExit;

    Instruction header = new NopInst();
    Instruction exit = new NopInst();

    mCurrentLoopHeader = header;
    mCurrentLoopExit = exit;

    InstPair bodyPair = loop.getBody().accept(this);
    if (bodyPair != null) {
      header.setNext(0, bodyPair.getStart());
      bodyPair.getEnd().setNext(0, header);
    } else {
      header.setNext(0, header);
    }

    mCurrentLoopHeader = previousLoopHeader;
    mCurrentLoopExit = previousLoopExit;

    return new InstPair(header, exit);
  }
}
