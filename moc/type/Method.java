package moc.type;

import java.util.Vector;

public class Method {
    Type returnType;
    Vector<Selector> selectors = new Vector<>();
    boolean isStatic;

    public Method(Type returnType, Vector<Selector> selectors, boolean isStatic) {
        this.returnType = returnType;
        this.selectors = selectors;
        this.isStatic = isStatic;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Vector<Selector> getSelectors() {
        return selectors;
    }
}

