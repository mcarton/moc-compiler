package moc.tds;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import moc.type.DTYPE;

/**
 * A hierarchical symbols table.
 */
public class TDS {
    private static final long serialVersionUID = 0x5a7a79a;
    private HashMap<String, INFO> map = new HashMap<String, INFO>();
    private TDS mother;
    private INFOFUN currentFunction;

    /**
     * Constructor for a symbols table without mother.
     */
    public TDS() {
        this(null);
    }

    /**
     * Constructor for a TDS daughter of p.
     */
    public TDS(TDS p) {
        mother = p;
        currentFunction = p == null ? null : p.currentFunction;
    }

    /**
     * Look for n in the current TDS only.
     */
    public INFO localSearch(String n) {
        return map.get(n);
    }

    /**
     * Look for n in the current TDS and its ancestors.
     */
    public INFO globalSearch(String n) {
        INFO i = localSearch(n);
        if (i == null && mother != null) {
            return mother.globalSearch(n);
        }
        return i;
    }

    /**
     * Add n and its info i in the TDS.
     */
    public void insert(String n, INFO i) {
        map.put(n, i);
    }

    /**
     * Set the current function.
     */
    public void setCurrentFunction(String n, INFOFUN fun) {
        currentFunction = fun;
        mother.insert(n, fun);
    }

    /**
     * Return whether we can return type from the current function.
     */
    public boolean canReturn(DTYPE type) {
        return currentFunction.getType().getReturnType().constructsFrom(type);
    }

    public String toString() {
        return toString(true);
    }

    public String toString(boolean printTSD) {
        return (mother != null
            ? map.toString() + " with mother=" + mother.toString(false)
            : map.toString()) + " and currentFunction=" + currentFunction;
    }
}
