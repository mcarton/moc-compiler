package moc.type;

public interface Type {
    /**
     * True iff we can write `aa = bb` with aa of type `this` and bb of type
     * `other`. It is not necessary to have `bb = aa`.  For example a NullType
     * can be affected to a Int*, but the opposite is false.
     */
    public boolean constructsFrom(Type other);

    /**
     * True iff we can compare the two types with the given operator.
     */
    public boolean comparableWith(Type other, String operator);

    /**
     * True iff we can test the type (ie. in if (...)).
     */
    public boolean testable();

    public abstract <R> R visit(TypeVisitor<R> visitor);
}

