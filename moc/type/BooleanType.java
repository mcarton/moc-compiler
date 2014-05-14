package moc.type;

public final class BooleanType extends AbstractType<BooleanType> {
    public BooleanType() {
    }

    public String toString() {
        return "Bool";
    }

    @Override
    public boolean constructsFrom(Type other) {
        return equals(other) || other.isInt();
    }

    @Override
    public boolean castsFrom(Type other) {
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
    public boolean equals(Type autre) {
        return autre.isBool();
    }

    @Override
    public boolean isBool() {
        return true;
    }
}

