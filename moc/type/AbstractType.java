package moc.type;

public abstract class AbstractType<T extends DTYPE> implements DTYPE {
    protected int size;

    public int getSize() {
        return size;
    }

    public boolean testable() {
        return false;
    }

    /* Common C++, does not seem to work in Java
    @SuppressWarnings("unchecked")
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit((T)this);
    }
    */
}

