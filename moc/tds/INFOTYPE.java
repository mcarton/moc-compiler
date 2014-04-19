package moc.tds;

import moc.type.DTYPE;

/**
 * This class describes a type.
 */
public class INFOTYPE implements INFO {
    private DTYPE type;

    public DTYPE getType() {
        return type;
    }

    public INFOTYPE(DTYPE t) {
        type = t;
    }

    @Override
    public String toString() {
        return "INFOTYPE [type=" + type + "]";
    }
}
