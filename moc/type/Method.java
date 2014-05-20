package moc.type;

import java.util.Vector;

public class Method {
    ClassType classType;
    Type returnType;
    Vector<Selector> selectors = new Vector<>();
    boolean isStatic;

    public Method(
        ClassType classType, Type returnType,
        Vector<Selector> selectors, boolean isStatic
    ) {
        this.classType = classType;
        this.returnType = returnType;
        this.selectors = selectors;
        this.isStatic = isStatic;
    }

    public ClassType getClassType() {
        return classType;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Vector<Selector> getSelectors() {
        return selectors;
    }
}

