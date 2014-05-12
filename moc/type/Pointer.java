package moc.type;

public final class Pointer extends AbstractType<Pointer> {
    private Type pointee;

    public Pointer(Type pointee) {
        this.pointee = pointee;
    }

    public String toString() {
        return pointee.toString() + "*";
    }

    public Type getPointee() {
        return pointee;
    }

    /** A pointer can be constructed from another compatible pointer (the same
     *  pointee or a pointee that extends the current pointee), from an array
     *  of the same pointee or from the {@link NullType}.
     */
    @Override
    public boolean constructsFrom(Type other) {
        // TODO:moc: inheritance
        return other.isPointer() && ((Pointer)other).pointee.constructsFrom(pointee)
            || other.isArray()   && ((Array)other).getPointee().constructsFrom(pointee)
            || other.isNull();
    }

    @Override
    public boolean castsFrom(Type other) {
        // TODO:moc: inheritance
        return constructsFrom(other) || other.isPointer();
    }

    @Override
    public boolean testable() {
        return true;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Type other) {
        return other.isPointer()
            && ((Pointer)other).getPointee().equals(getPointee());
    }

    @Override
    public boolean isPointer() {
        return true;
    }
}

