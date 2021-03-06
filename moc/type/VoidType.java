package moc.type;

public final class VoidType extends AbstractType<VoidType> {
    public VoidType() {
    }

    public String toString() {
        return "void";
    }

    @Override
    public boolean constructsFrom(Type other) {
        return false;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Type other) {
        return other.isVoid();
    }

    @Override
    public boolean isVoid() {
        return true;
    }
}

