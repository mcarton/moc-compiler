package moc.tds;

import moc.gc.Location;
import moc.type.Type;

/**
 * This class describes a local var: address and type
 */
public class InfoVar implements Info {
    /**
     * var type
     */
    protected Type type;

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

    public Type getType() {
        return type;
    }

    /**
     * A var has a type and a location for its value
     */
    public InfoVar(Type t, Location l) {
        type = t;
        loc = l;
    }

    @Override
    public String toString() {
        return "InfoVar [type=" + type + ", loc=" + loc + "]";
    }
}
