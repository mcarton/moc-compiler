package moc.type;

import java.util.Vector;

public class Method {
    Vector<Selector> selectors = new Vector<>();
    boolean isStatic;

    public Method(Vector<Selector> selectors, boolean isStatic) {
        this.selectors = selectors;
        this.isStatic = isStatic;
    }
}

