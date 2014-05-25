package moc.symbols;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import moc.gc.*;
import moc.type.*;

/**
 * A hierarchical symbols table.
 */
public class SymbolTable {
    private static final long serialVersionUID = 0x5a7a79a;
    private HashMap<String, Info> map = new HashMap<String, Info>();
    private SymbolTable mother;
    private Type returnType;
    private ClassType classType; // null if we are not in a method

    /**
     * Constructor for a symbols table without mother.
     */
    public SymbolTable() {
        this(null);
    }

    /**
     * Constructor for a SymbolTable daughter of mother.
     */
    public SymbolTable(SymbolTable mother) {
        this.mother = mother;
        returnType = mother == null ? null : mother.getReturnType();
    }

    /**
     * Look for n in the current SymbolTable only.
     */
    public Info localSearch(String n) {
        return map.get(n);
    }

    /**
     * Look for n in the current SymbolTable and its ancestors.
     */
    public Info globalSearch(String n) {
        Info i = localSearch(n);
        if (i == null && mother != null) {
            return mother.globalSearch(n);
        }
        return i;
    }

    /**
     * Add a variable to the SymbolTable.
     */
    public void insertVar(String n, Type t, ILocation location) {
        map.put(n, new InfoVar(t, location));
    }

    /**
     * Add a function to the SymbolTable.
     */
    public void insertFun(String n, FunctionType i) {
        map.put(n, new InfoFun(i));
    }

    /**
     * Add a type alias to the SymbolTable.
     */
    public void insertType(String n, Type t) {
        map.put(n, new InfoType(t));
    }

    /**
     * Set the current function.
     */
    public void setCurrentFunction(String name, FunctionType function) {
        returnType = function.getReturnType();
        mother.insertFun(name, function);
    }

    /**
     * Set the current method.
     */
    public void setCurrentMethod(Method method) {
        returnType = method.getReturnType();
        classType = method.getClassType();
    }

    public Type getReturnType() {
        return returnType;
    }

    public ClassType getClassType() {
        return classType;
    }

    public String toString() {
        return toString(true);
    }

    public String toString(boolean printTSD) {
        return (mother != null
            ? map.toString() + " with mother=" + mother.toString(false)
            : map.toString()) + " and returnType=" + returnType;
    }
}

