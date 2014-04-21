package moc.type;

public interface DTYPE {
    /**
     * The size of the data type: depends on the machine.
     */
    public int getSize();

    /**
     * True iff we can write `aa = bb` with aa of type `this` and bb of type
     * `autre`. It is not necessary to have `bb = aa`.  For example a NULL_t
     * can be affected to a int*, but the opposite is false.
     */
    public boolean constructsFrom(DTYPE other);

    /**
     * True iff we can compare the two types with the given operator.
     */
    public boolean comparableWith(DTYPE other, String operator);

    /**
     * True iff we can test the type (ie. in if(...)).
     */
    public boolean testable();

    public abstract <R> R visit(TypeVisitor<R> visitor);
}

