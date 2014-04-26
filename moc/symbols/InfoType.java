package moc.symbols;

import moc.type.Type;

/**
 * This class describes a type.
 */
public class InfoType implements Info {
    private Type type;

    public InfoType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "InfoType [type=" + type + "]";
    }
}
