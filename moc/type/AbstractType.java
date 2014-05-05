package moc.type;

public abstract class AbstractType<T extends Type> implements Type {
    /**
     * Default implementation where type constructs from the other if they are
     * equal.
     */
    public boolean constructsFrom(Type other) {
        return equals(other);
    }

    /**
     * Default implementation where type casts from the other if it is
     * construtible from it.
     */
    public boolean castsFrom(Type other) {
        return constructsFrom(other);
    }

    /**
     * Default implementation where type is comparable only to itself.
     */
    public boolean comparableWith(Type other) {
        return equals(other);
    }

    /**
     * Default implementation where the type is not testable.
     */
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

