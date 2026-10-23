package crux.ast.types;

import crux.ast.SymbolTable.Symbol;
import crux.ast.*;
import crux.ast.traversal.NodeVisitor;
import crux.ast.traversal.NullNodeVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class will associate types with the AST nodes from Stage 2
 */
public final class TypeChecker {
  private final ArrayList<String> errors = new ArrayList<>();

  public ArrayList<String> getErrors() {
    return errors;
  }

  Symbol currentFunctionSymbol;
  Type currentFunctionReturnType;
  boolean lastStatementReturns;
  boolean hasBreak;

  public void check(DeclarationList ast) {
    var inferenceVisitor = new TypeInferenceVisitor();
    inferenceVisitor.visit(ast);
  }

  /**
   * Helper function, should be used to add error into the errors array
   */
  private void addTypeError(Node n, String message) {
    errors.add(String.format("TypeError%s[%s]", n.getPosition(), message));
  }

  /**
   * Helper function, should be used to record Types if the Type is an ErrorType then it will call
   * addTypeError
   */
  private void setNodeType(Node n, Type ty) {
    ((BaseNode) n).setType(ty);
    if (ty.getClass() == ErrorType.class) {
      var error = (ErrorType) ty;
      addTypeError(n, error.getMessage());
    }
  }

  /**
   * Helper to retrieve Type from the map
   */
  public Type getType(Node n) {
    return ((BaseNode) n).getType();
  }

  /**
   * This calls will visit each AST node and try to resolve it's type with the help of the
   * symbolTable.
   */
  private final class TypeInferenceVisitor extends NullNodeVisitor<Void> {

    // Set node type to type of the variable type
    @Override
    public Void visit(VarAccess varAccess) {
      Symbol symbol = varAccess.getSymbol();
      Type type = symbol.getType();
      setNodeType(varAccess, type);
      return null;
    }

    @Override
    public Void visit(ArrayDeclaration arrayDeclaration) {

      Symbol symbol = arrayDeclaration.getSymbol();
      Type type = ((ArrayType) symbol.getType()).getBase();
      //Type type = symbol.getType();
      // Verify that the array type is either IntType or BoolType
      if (!(type.toString().equals("int") || type.toString().equals("bool"))) {
        addTypeError(arrayDeclaration, "wrong type in array\n");

      }
      lastStatementReturns = false;
      return null;
    }

    @Override
    public Void visit(Assignment assignment) {
      for (Node child : assignment.getChildren()) {
        child.accept(this);

      }

      Type locationType = getType(assignment.getLocation());
      Type valueType = getType(assignment.getValue());
      locationType.assign(valueType);
      setNodeType(assignment, locationType);

      return null;
    }

    @Override
    public Void visit(Break brk) {
      hasBreak = true;
      lastStatementReturns = false;
      return null;
    }

    @Override
    public Void visit(Call call) {
      TypeList x = new TypeList();
      for (Node argument : call.getArguments()) {
        argument.accept(this);
        x.append(getType(argument));


      }
      FuncType calleeType = (FuncType) call.getCallee().getType();
      Type CC = calleeType.call(x);
      setNodeType(call, CC);

      lastStatementReturns = false;


      return null;
    }

    @Override
    public Void visit(Continue cont) {
      for (Node child : cont.getChildren()) {
        child.accept(this);
      }
      return null;
    }

    @Override
    public Void visit(DeclarationList declarationList) {
      for (Node child : declarationList.getChildren()) {
        child.accept(this);
      }
      return null;
    }

    @Override
    public Void visit(FunctionDefinition functionDefinition) {
      currentFunctionSymbol =  functionDefinition.getSymbol();
      currentFunctionReturnType  = ((FuncType) currentFunctionSymbol.getType()).getRet();

      if (currentFunctionSymbol.getName().equals("main")){
        if (!(currentFunctionReturnType.toString().equals("void") && functionDefinition.getParameters().isEmpty())){
          addTypeError(functionDefinition, "functionDefinition error: main not correct\n");
          return null;
        }
      }
      for (Symbol parameter : functionDefinition.getParameters()) {
        if (!(parameter.getType() instanceof IntType
                || parameter.getType() instanceof BoolType)){
          addTypeError(functionDefinition, "functionDefinition error: parameter not type int or bool.\n");
          return null;
        }
      }
      functionDefinition.getStatements().accept(this);
      if (lastStatementReturns && currentFunctionReturnType.toString().equals("void")){
        addTypeError(functionDefinition, "functionDefinition error2\n");
      }

      return null;
    }

    @Override
    public Void visit(IfElseBranch ifElseBranch) {
      Expression condition = ifElseBranch.getCondition();
      condition.accept(this);
      if (!(getType(condition) instanceof BoolType)) {
        addTypeError(condition, "Condition in if-else statement must be boolean");
      }

      ifElseBranch.getThenBlock().accept(this);
      StatementList elseBlock = ifElseBranch.getElseBlock();
      if (elseBlock != null) {
        elseBlock.accept(this);
      }
      return null;
    }

    @Override
    public Void visit(ArrayAccess access) {
      access.getIndex().accept(this);

      Type indexType = access.getBase().getType();
      Type x = ((ArrayType)access.getBase().getType()).index(indexType);
      setNodeType(access, x);
      return null;
    }

    @Override
    public Void visit(LiteralBool literalBool) {
      // Set node type to new bool type
      setNodeType(literalBool, new BoolType());
      return null;
    }

    @Override
    public Void visit(LiteralInt literalInt) {
      // Set node type to new Int type
      setNodeType(literalInt, new IntType());
      return null;
    }

    @Override
    public Void visit(Loop loop) {
      hasBreak = false;
      for (Node child : loop.getBody().getChildren()) {
        child.accept(this);
        if (!hasBreak && !lastStatementReturns){
          addTypeError(child, "ERROR: infinite loop\n");
        }
      }

      return null;
    }

    @Override
    public Void visit(OpExpr op) {
      // visit left and right (right is null if op == LOGIC_NOT)
      op.getLeft().accept(this);
      if (op.getRight() != null){
        op.getRight().accept(this);
      }


      // Get type of left and right
      Type leftType = getType(op.getLeft());
      if (leftType instanceof FuncType){
        leftType = ((FuncType)leftType).getRet();
      }

      Type rightType = new VoidType();
      if (op.getRight() != null) {
        rightType = getType(op.getRight());
        if (rightType instanceof FuncType) {
          rightType = ((FuncType) rightType).getRet();
        }
      }

      Type resultType = null;

      switch(op.getOp()) {
        case ADD:
          resultType = leftType.add(rightType);
          break;
        case SUB:
          resultType = leftType.sub(rightType);
          break;
        case LOGIC_AND:
          resultType = leftType.and(rightType);
          break;
        case LOGIC_OR:
          resultType = leftType.or(rightType);
          break;
        case MULT:
          resultType = leftType.mul(rightType);
          break;
        case DIV:
          resultType = leftType.div(rightType);
          break;
        case LOGIC_NOT:
          resultType = leftType.not();
          break;
        case GE:
          resultType = leftType.compare(rightType);
          break;
        case LE:
          resultType = leftType.compare(rightType);
          break;
        case NE:
          resultType = leftType.compare(rightType);
          break;
        case EQ:
          resultType = leftType.compare(rightType);
          break;
        case GT:
          resultType = leftType.compare(rightType);
          break;
        case LT:
          resultType = leftType.compare(rightType);
          break;
        default:
          break;

      }
      // Update AST node type (resultType)
      setNodeType(op, resultType);
      return null;
    }

    @Override
    public Void visit(Return ret) {

      ret.getValue().accept(this);
      if (!getType(ret.getValue()).equivalent(currentFunctionReturnType)) {
        addTypeError(ret, "return types don't match\n");
      }
      lastStatementReturns = true;
      return null;
    }

    @Override
    public Void visit(StatementList statementList) {
      lastStatementReturns = false;
      for (Node child : statementList.getChildren()) {
        child.accept(this);
        if (!lastStatementReturns){
          hasBreak = true;
        }
      }

      return null;
    }

    @Override
    public Void visit(VariableDeclaration variableDeclaration) {
      Symbol symbol = variableDeclaration.getSymbol();
      Type type = symbol.getType();

      // Verify that the array type is either IntType or BoolType
      if (!(type.toString().equals("int") || type.toString().equals("bool"))) {
        // Set last statement returns to false
        addTypeError(variableDeclaration, "variable declared wrong type\n");
      }
      lastStatementReturns = false;
      return null;
    }
  }
}

