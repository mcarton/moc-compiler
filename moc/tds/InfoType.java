package moc.tds;

import moc.type.Type;

/**
 * This class describes a type.
 */
public class InfoType implements Info {
    private Type type;

    public Type getType() {
        return type;
    }

    public InfoType(Type t) {
        type = t;
    }

    @Override
    public String toString() {
        return "InfoType [type=" + type + "]";
    }
}
