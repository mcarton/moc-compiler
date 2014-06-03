package moc.gc.tam;

import moc.gc.ILocation;

/**
 * This class describes a memory address (offset from a register).
 * If the register is null, the location is an offset from the self pointer
 * (that is, an attribute location).
 */
public class Location implements ILocation {
    private int dep;
    private String reg;

    /**
     * Attribute location = offset / self.
     */
    public Location(int dep) {
        this(dep, null);
    }

    /**
     * Variable or parameter location = address = offset / registre.
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
        return dep + "[" + (reg == null ? "*attributes*" : reg) + ']';
    }
}

