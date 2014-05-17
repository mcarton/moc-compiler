package moc.type;

import java.util.Vector;

public class Method {
    Vector<String> selectors = new Vector<>();
    TypeList types = new TypeList();
    boolean isStatic;

    public Method(Vector<String> selectors, TypeList types, boolean isStatic) {
        this.selectors = selectors;
        this.types = types;
        this.isStatic = isStatic;
    }
}

