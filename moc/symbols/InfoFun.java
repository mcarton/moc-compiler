package moc.symbols;

import moc.gc.Location;
import moc.type.FunctionType;

/**
 * This class describes a function.
 */
public class InfoFun implements Info {
    private FunctionType type;

    public FunctionType getType() {
        return type;
    }

    public InfoFun(FunctionType t) {
        type = t;
    }

    @Override
    public String toString() {
        return "InfoFun [type=" + type + "]";
    }
}
