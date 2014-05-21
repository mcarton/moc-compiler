package moc.symbols;

import moc.gc.ILocation;
import moc.type.Type;

/**
 * This class describes a local var: type and location.
 */
public class InfoVar implements Info {
    private Type type;
    private ILocation loc;

    /**
     * An InfoVar has a type and a location for its value.
     */
    public InfoVar(Type type, ILocation location) {
        this.type = type;
        this.loc = location;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ILocation getLoc() {
        return loc;
    }

    @Override
    public String toString() {
        return "InfoVar [type=" + type + ", loc=" + loc + "]";
    }
}
