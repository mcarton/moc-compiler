package moc.tds;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Une TDS hiérarchique.
 * 
 * @author marcel
 */
public class TDS {
    private static final long serialVersionUID = 0x5a7a79a;
    private HashMap<String, INFO> map;
    private TDS mother;

    /**
     * Constructeur pour une TDS sans parente.
     */
    public TDS() {
        this(null);
    }

    /**
     * Constructeur pour une TDS fille de p.
     */
    public TDS(TDS p) {
        mother = p;
    }

    /**
     * Recherche de n dans la TDS courante uniquement.
     */
    public INFO localSearch(String n) {
        return map.get(n);
    }

    /**
     * Recherche de n dans la TDS courante et ses parentes.
     */
    public INFO globalSearch(String n) {
        INFO i = localSearch(n);
        if (i == null && mother != null) {
                return mother.globalSearch(n);
        }
        return i;
    }

    /**
     * Ajoute le nom n et son information i dans la TDS.
     */
    public void insert(String n, INFO i) {
        map.put(n, i);
    }
}

