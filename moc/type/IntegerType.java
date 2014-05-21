package moc.type;

public final class IntegerType extends AbstractType<IntegerType> {
    public IntegerType() {
    }

    public String toString() {
        return "Int";
    }

    @Override
    public boolean constructsFrom(Type other) {
        return equals(other) || other.isBool();
    }

    @Override
    public boolean castsFrom(Type other) {
        return constructsFrom(other) || other.isChar();
    }

    @Override
    public boolean testable() {
        return true; // ints are testable since they are the boolean type
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Type autre) {
        return autre.isInt();
    }

    @Override
    public boolean isInt() {
        return true;
    }
}

