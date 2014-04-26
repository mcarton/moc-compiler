package moc.symbols;

import moc.gc.Location;
import moc.type.Type;

/**
 * This class describes a local var: type and location.
 */
public class InfoVar implements Info {
    private Type type;
    private Location loc;

    /**
     * An InfoVar has a type and a location for its value.
     */
    public InfoVar(Type type, Location location) {
        this.type = type;
        this.loc = location;
    }

    public Type getType() {
        return type;
    }

    public Location getLoc() {
        return loc;
    }

    @Override
    public String toString() {
        return "InfoVar [type=" + type + ", loc=" + loc + "]";
    }
}
