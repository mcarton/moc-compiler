package moc.tds;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A hierarchical symbols table
 */
public class TDS {
    private static final long serialVersionUID = 0x5a7a79a;
    private HashMap<String, INFO> map = new HashMap<String, INFO>();
    private TDS mother;

    /**
     * Constructor for a symbols table without mother
     */
    public TDS() {
        this(null);
    }

    /**
     * Constructor for a TDS daughter of p
     */
    public TDS(TDS p) {
        mother = p;
    }

    /**
     * Look for n in the current TDS only
     */
    public INFO localSearch(String n) {
        return map.get(n);
    }

    /**
     * Look for n in the current TDS and its ancestors
     */
    public INFO globalSearch(String n) {
        INFO i = localSearch(n);
        if (i == null && mother != null) {
            return mother.globalSearch(n);
        }
        return i;
    }

    /**
     * Add n ad its info i in the TDS
     */
    public void insert(String n, INFO i) {
        map.put(n, i);
    }
}
