package moc.tds;

import moc.gc.Location;
import moc.type.DFUNCTIONTYPE;

/**
 * This class describes a function.
 */
public class INFOFUN implements INFO {
    private DFUNCTIONTYPE type;

    public DFUNCTIONTYPE getType() {
        return type;
    }

    public INFOFUN(DFUNCTIONTYPE t) {
        type = t;
    }

    @Override
    public String toString() {
        return "INFOFUN [type=" + type + "]";
    }
}
