package moc.gc.tam;

/**
 * This class describes a memory address (offset from a register)
 */
public class Location implements moc.gc.Location {
    private int dep;
    private String reg;

    /**
     * Location = address = offset / registre.
     */
    public Location(int dep, String reg) {
        this.dep = dep;
        this.reg = reg;
    }

    public int getDep() {
        return dep;
    }

    public String getReg() {
        return reg;
    }

    @Override
    public String toString() {
        return dep + "[" + reg + ']';
    }
}

