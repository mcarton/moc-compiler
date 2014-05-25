package moc.type;

public final class IdType extends AbstractType<IdType> {
    public IdType() {
    }

    public String toString() {
        return "id";
    }

    @Override
    public boolean constructsFrom(Type other) {
        return other.isId() || other.isNull()
            || (other.isPointer() && ((Pointer)other).getPointee().isClass());
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
        return other.isId();
    }

    @Override
    public boolean isId() {
        return true;
    }
}

