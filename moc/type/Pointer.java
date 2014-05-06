package moc.type;

public class Pointer extends AbstractType<Pointer> {
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
        return other instanceof Pointer && ((Pointer)other).pointee.constructsFrom(pointee)
            || other instanceof Array   && ((Array)other).getPointee().constructsFrom(pointee)
            || other instanceof NullType;
    }

    @Override
    public boolean castsFrom(Type other) {
        // TODO:moc: inheritance
        return constructsFrom(other) || other instanceof Pointer;
    }

    /** A pointer is comparable with other pointers and the {@link NullType}.
     */
    @Override
    public boolean comparableWith(Type other) {
        return other instanceof Pointer || other instanceof NullType;
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
        return other instanceof Pointer
            && ((Pointer)other).getPointee().equals(getPointee());
    }
}

