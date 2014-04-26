package moc.symbols;

import moc.type.FunctionType;

/**
 * This class describes a function.
 */
public class InfoFun implements Info {
    private FunctionType type;

    public InfoFun(FunctionType type) {
        this.type = type;
    }

    public FunctionType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "InfoFun [type=" + type + "]";
    }
}

