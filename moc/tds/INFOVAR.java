package moc.tds;

import moc.gc.Location;
import moc.type.DTYPE;

/**
 * This class describes a local var: address and type
 */
public class INFOVAR implements INFO {
    /**
     * var type
     */
    protected DTYPE type;

    /**
     * Represents a memory location: depends on the machine
     */
    protected Location loc;

    public Location getLoc() {
        return loc;
    }

    public int getSize() {
        return getType().getSize();
    }

    public DTYPE getType() {
        return type;
    }

    /**
     * A var has a type and a location for its value
     */
    public INFOVAR(DTYPE t, Location l) {
        type = t;
        loc = l;
    }

    @Override
    public String toString() {
        return "INFOVAR [type=" + type + ", loc=" + loc + "]";
    }
}
