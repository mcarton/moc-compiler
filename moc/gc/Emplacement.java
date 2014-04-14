package moc.gc;

/**
 * Cette classe decrit une adresse mémoire (déplacement par rapport à un
 * registre).
 */
public class Emplacement {
    private int dep;
    private Register reg;

    public int getDep() {
        return dep;
    }

    public Register getReg() {
        return reg;
    }

    @Override
    public String toString() {
        return "[" + dep + "/" + reg + "]";
    }

    /**
     * Emplacement = adresse = deplacement / registre.
     */
    public Emplacement(int dep, Register reg) {
        this.dep = dep;
        this.reg = reg;
    }
}

