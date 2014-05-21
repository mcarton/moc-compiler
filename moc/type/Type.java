package moc.type;

public interface Type {
    /**
     * True if we can write `aa = bb` with aa of type `this` and bb of type
     * `other`. It is not necessary to have `bb = aa`.  For example a NullType
     * can be affected to a Int*, but the opposite is false.
     */
    public boolean constructsFrom(Type other);

    /**
     * True iff we can write `(ThisType)other`.
     */
    public boolean castsFrom(Type other);

    /**
     * True iff we can test the type (ie.\ in if (...)).
     */
    public boolean testable();

    /**
     * True iff both types are equals.
     */
    public boolean equals(Type other);

    public abstract <R> R visit(TypeVisitor<R> visitor);

    public boolean isArray();
    public boolean isBool();
    public boolean isClass();
    public boolean isChar();
    public boolean isInt();
    public boolean isNull();
    public boolean isPointer();
    public boolean isVoid();
    public boolean isConstant();
    public void setConstant(boolean constant);
}
