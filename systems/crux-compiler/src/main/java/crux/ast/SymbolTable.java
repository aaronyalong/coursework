package crux.ast;

import com.sun.jdi.CharType;
import crux.ast.Position;
import crux.ast.types.*;


import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Symbol table will map each symbol from Crux source code to its declaration or appearance in the
 * source. The symbol table is made up of scopes, Each scope is a map which maps an identifier to
 * it's symbol. Scopes are inserted to the table starting from the first scope (Global Scope). The
 * Global scope is the first scope in each Crux program and it contains all the built in functions
 * and names. The symbol table is an ArrayList of scops.
 */
public final class SymbolTable {

  /**
   * Symbol is used to record the name and type of names in the code. Names include function names,
   * global variables, global arrays, and local variables.
   */
  static public final class Symbol implements java.io.Serializable {
    static final long serialVersionUID = 12022L;
    private final String name;
    private final Type type;
    private final String error;

    /**
     * @param name String
     * @param type the Type
     */
    private Symbol(String name, Type type) {
      this.name = name;
      this.type = type;
      this.error = null;
    }

    private Symbol(String name, String error) {
      this.name = name;
      this.type = null;
      this.error = error;
    }

    /**
     * @return String the name
     */
    public String getName() {
      return name;
    }

    /**
     * @return the type
     */
    public Type getType() {
      return type;
    }

    @Override
    public String toString() {
      if (error != null) {
        return String.format("Symbol(%s:%s)", name, error);
      }
      return String.format("Symbol(%s:%s)", name, type);
    }

    public String toString(boolean includeType) {
      if (error != null) {
        return toString();
      }
      return includeType ? toString() : String.format("Symbol(%s)", name);
    }
  }

  private final PrintStream err;
  private final ArrayList<Map<String, Symbol>> symbolScopes = new ArrayList<>();

  private boolean encounteredError = false;

  SymbolTable(PrintStream err) {
    this.err = err;
    enter();
    add(null, "readInt", new FuncType(new TypeList(), new IntType()));
    add(null, "readChar", new FuncType(new TypeList(), new IntType()));
    add(null, "printBool", new FuncType(TypeList.of(new BoolType()), new VoidType()));
    add(null, "printInt", new FuncType(TypeList.of(new IntType()), new VoidType()));
    add(null, "printChar", new FuncType(TypeList.of(new IntType()), new VoidType()));
    add(null, "println", new FuncType(new TypeList(), new VoidType()));
  }

  boolean hasEncounteredError() {
    return encounteredError;
  }

  /**
   * Called to tell symbol table we entered a new scope.
   */

  void enter() {
    symbolScopes.add(new HashMap<>());
  }

  /**
   * Called to tell symbol table we are exiting a scope.
   */

  void exit() {
    if (!symbolScopes.isEmpty()) {
      symbolScopes.remove(symbolScopes.size() - 1);
    }
  }

  /**
   * Insert a symbol to the table at the most recent scope. if the name already exists in the
   * current scope that's a declareation error.
   */
  Symbol add(Position pos, String name, Type type) {
    Map<String, Symbol> recentScope = symbolScopes.get(symbolScopes.size() - 1);
    if (recentScope.containsKey(name)) {
      err.printf("Declaration error: %s already declared in the current scope.%n", name);
      encounteredError = true;
      return null;
    } else {
      Symbol symbol = new Symbol(name, type);
      recentScope.put(name, symbol);
      return symbol;
    }
  }

  /**
   * lookup a name in the SymbolTable, if the name not found in the table it shouold encounter an
   * error and return a symbol with ResolveSymbolError error. if the symbol is found then return it.
   */
  Symbol lookup(Position pos, String name) {
    var symbol = find(name);
    if (symbol == null) {
      err.printf("ResolveSymbolError%s[Could not find %s.]%n", pos, name);
      encounteredError = true;
      return new Symbol(name, "ResolveSymbolError");
    } else {
      return symbol;
    }
  }

  /**
   * Try to find a symbol in the table starting form the most recent scope.
   */
  private Symbol find(String name) {
    // Start from the last added scope and move backward
    for (int i = symbolScopes.size() - 1; i >= 0; i--) {
      Symbol symbol = symbolScopes.get(i).get(name);
      if (symbol != null) {
        return symbol;
      }
    }
    return null;
  }

}