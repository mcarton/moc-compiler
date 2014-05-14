package moc.gc.tam;

/**
 * This class describes a memory address (offset from a register)
 */
public class Location implements moc.gc.Location {
    private int dep;
    private Register reg;

    /**
     * Location = address = offset / registre.
     */
    public Location(int dep, Register reg) {
        this.dep = dep;
        this.reg = reg;
    }

    public int getDep() {
        return dep;
    }

    public Register getReg() {
        return reg;
    }

    @Override
    public String toString() {
        return " "+ dep +"["+reg+"]";
    }
}

