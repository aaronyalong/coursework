package crux.backend;

import crux.ast.SymbolTable.Symbol;
import crux.ir.*;
import crux.ir.insts.*;
import crux.printing.IRValueFormatter;

import java.util.*;

/**
 * Convert the CFG into Assembly Instructions
 */
public final class CodeGen extends InstVisitor {
  private final Program p;
  private final CodePrinter out;

  // Keep track and localVar
  private HashMap<Variable, Integer> varIndexMap = new HashMap<Variable,Integer>();
  private int varIndex = 0;


  public CodeGen(Program p) {
    this.p = p;
    // Do not change the file name that is outputted or it will
    // break the grader!

    out = new CodePrinter("a.s");
  }

  /**
   * It should allocate space for globals call genCode for each Function
   */
  public void genCode() {
    for (Iterator<GlobalDecl> glob_it = p.getGlobals(); glob_it.hasNext();) {
      GlobalDecl g = glob_it.next();
      //
      out.printCode(".comm " + g.getSymbol().getName() + ", " + g.getNumElement().getValue()*8 + ", 8");
    }

    int count[] = new int[1];
    for (Iterator<Function> func_it = p.getFunctions(); func_it.hasNext();) {
      Function f = func_it.next();
      genCode(f, count);
    }

    out.close();
  }

  private String getSymbol(Symbol var){ return null;}

  Integer getStackSlot(LocalVar v) {
    if (!varIndexMap.containsKey(v)) {
      varIndex++;
      varIndexMap.put(v, varIndex);
    }
    return varIndexMap.get(v);
  }

  private void genCode(Function f, int[] count){
    // Assign labels to jump targets
    HashMap<Instruction, String> labels = f.assignLabels(count);

    // Declare function and label
    out.printCode(".globl " + f.getName());
    out.printLabel(f.getName() + ":");

    // Print prologue such that stack is 16 byte aligned
    int numSlots = f.getNumTempVars() + f.getNumTempAddressVars();
    if (numSlots % 2 != 0){
      numSlots += 1;
    }
    out.printCode("enter $(8 * " + numSlots + "), $0");

    // Move arguments from registers to local variables
    List<LocalVar> args = f.getArguments();
    String[] registers = {"%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"};
    for (int i = 0; i < args.size(); i++) {
      int offset = getStackSlot(args.get(i));
      out.printCode("movq " + registers[i] + ", -" + (offset * 8) + "(%rbp)");
    }

    // Generate Code for function body by traversing the instruction graph
    if (f.getStart() != null) {
      Stack<Instruction> stack = new Stack<>();
      HashSet<Instruction> visited = new HashSet<>();
      stack.push(f.getStart());

      while (!stack.isEmpty()) {
        Instruction inst = stack.pop();
        if (visited.contains(inst)) continue;
        visited.add(inst);

        if (labels.containsKey(inst)) {
          out.printLabel(labels.get(inst) + ":");
        }

        // Visit
        inst.accept(this);

        // Push next instructions to stack
        for (int i = inst.numNext() - 1; i >= 0; i--) {
          stack.push(inst.getNext(i));
        }
      }
    }

    // Print leave and ret at the end of each CFG
    out.printCode("leave");
    out.printCode("ret");
  }


  public void visit(AddressAt i) {

  }
  public void visit(BinaryOperator i) {
    LocalVar dest = i.getDst();         // Destination variable
    LocalVar lhs = i.getLeftOperand();  // Left-hand side operand
    LocalVar rhs = i.getRightOperand(); // Right-hand side operand

    int destSlot = getStackSlot(dest);
    int lhsSlot = getStackSlot(lhs);
    int rhsSlot = getStackSlot(rhs);

    // Move left operand into %r10
    out.printCode("movq -" + (lhsSlot * 8) + "(%rbp), %r10");

    // Depending on the operation, process the right operand
    switch (i.getOperator()) {
      case Add:
        out.printCode("addq -" + (rhsSlot * 8) + "(%rbp), %r10");
        break;
      case Sub:
        out.printCode("subq -" + (rhsSlot * 8) + "(%rbp), %r10");
        break;
      case Mul:
        out.printCode("imulq -" + (rhsSlot * 8) + "(%rbp), %r10");
        break;
      case Div:
        // Division
        out.printCode("movq %r10, %rax");
        out.printCode("cqto");
        out.printCode("idivq -" + (rhsSlot * 8) + "(%rbp)");
        out.printCode("movq %rax, %r10");
        break;
    }

    // Store the result from %r10 back into the destination variable's location
    out.printCode("movq %r10, -" + (destSlot * 8) + "(%rbp)");
  }

  public void visit(CompareInst i) {
    // Example: Compare -16(%rbp) >  -24(%rbp) and store result in -8(%rbp)
    //    movq $0, %rax
    //    movq $1, %r10
    //    movq -16(%rbp), %r11
    //    cmp -24(%rbp), %r11
    //    cmovg %r10, %rax
    //    movq %rax, -8(%rbp)
    //Example: Jump to L3 if -16(%rbp) is true
    //    movq -16(%rbp), %r10
    //    cmp $1, %r10
    //    je L3
  }

    public void visit(CopyInst i) {
      LocalVar dst = i.getDstVar();
      Value src = i.getSrcValue();

      int dstSlot = getStackSlot(dst);

      if (src instanceof LocalVar) {                                    // If the source is a LocalVar, copy from its stack location
        int srcSlot = getStackSlot((LocalVar) src);
        out.printCode("movq -" + (srcSlot * 8) + "(%rbp), %rax");  // Move from source to %rax
      }
      // Check if Integer Constant
      else if (src instanceof IntegerConstant){                        // If the source is a constant or other type of Value
        out.printCode("movq $" + ((IntegerConstant) src).getValue() + ", %rax");       // Move immediate value to %rax
      }
      // Check if Boolean Constant
      else if (src instanceof BooleanConstant) {
        boolean bool = ((BooleanConstant) src).getValue();
        int value;
        if (bool){
          value = 1;
        }
        else {
          value = 0;
        }
        out.printCode("movq $" + value + ", %rax");
      }
      out.printCode("movq %rax, -" + (dstSlot * 8) + "(%rbp)");  // Move from %rax to destination
    }


  public void visit(JumpInst i) {
    //Conditional jump to the then block (see Slides 19 and 20).
    //Visit the else block (which you should already be doing if you are following Slide 22 since it is edge 0).
    //Next, visit the then block (the jmp should be added automatically at the end of the then block if you are following Slide 22).
  }

  public void visit(LoadInst i) {
    LocalVar dst = i.getDst();
    AddressVar srcAddress = i.getSrcAddress();
    int dstSlot = getStackSlot(dst);

    out.printCode("movq 0(" + srcAddress.getName() + "), %rax");
    out.printCode("movq %rax, -" + (dstSlot * 8) + "(%rbp)");
  }

  public void visit(NopInst i) {
    out.printCode(" // NOP");
  }

  public void visit(StoreInst i) {
    LocalVar srcValue = i.getSrcValue();
    AddressVar destAddress = i.getDestAddress();

    int srcSlot = getStackSlot(srcValue);

    out.printCode("movq -" + (srcSlot * 8) + "(%rbp), %rax");
    out.printCode("movq %rax, (" + destAddress.getName() + ")");  // Store value in %rax to destAddress
  }

  public void visit(ReturnInst i) {
    if (i.getReturnValue() != null) {
      LocalVar retValue = i.getReturnValue();
      int retSlot = getStackSlot(retValue);
      out.printCode("movq -" + (retSlot * 8) + "(%rbp), %rax");  // Move the value to %rax
    }
    out.printCode("leave");
    out.printCode("ret");
  }

  public void visit(CallInst i) {
    Symbol callee = i.getCallee();
    int counter = 0;
    for (LocalVar param : i.getParams()) {
      String name = param.getName();
      int slot = getStackSlot(param);
      switch(counter){
        case 0:
          out.printCode("movq " + slot*-8 + "(%rbp)" + ", %rdi");
          counter++;
          break;
        case 1:
          out.printCode("movq " + slot*-8 + "(%rbp)" +", %rsi");
          counter++;
          break;
        case 2:
          out.printCode("movq " + slot*-8 + "(%rbp)" +", %rdx");
          counter++;
          break;
        case 3:
          out.printCode("movq " + slot*-8 + "(%rbp)" +", %rcx");
          counter++;
          break;
        case 4:
          out.printCode("movq " + slot*-8 + "(%rbp)" +", %r8");
          counter++;
          break;
        case 5:
          out.printCode("movq " + slot*-8 + "(%rbp)" +", %r9");
          counter++;
          break;

      }
    }
    out.printCode("call " + callee.getName());

  }

  public void visit(UnaryNotInst i) {
    // Just subtract the value from $1.
    //movq $1, %r11
    //subq %r11, VAL
  }
}
