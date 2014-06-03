package moc.type;

import moc.type.*;

/** A visitor to get the size of types.
 */
public abstract class SizeVisitor implements TypeVisitor<Integer> {
    protected abstract int baseClassSize();

    public Integer visit(ClassType what) {
        int size = 0;
        for (Attributes att : what.getAttributes()) {
            size += att.type.visit(this);
        }
        if (what.hasSuper()) {
            size += visit(what.getSuper());
        }
        else {
            size += baseClassSize(); // vtable
        }
        return size;
    }

    public Integer visit(Array what) {
        return what.getPointee().visit(this) * what.getNbElements();
    }

    public Integer visit(VoidType what) {
        return 0;
    }
}

