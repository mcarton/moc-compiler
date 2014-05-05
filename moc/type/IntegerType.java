package moc.type;

public class IntegerType extends AbstractType<IntegerType> {
    public IntegerType() {
    }

    public String toString() {
        return "Int";
    }

    public boolean castsFrom(Type other) {
        return constructsFrom(other)
            || other instanceof CharacterType;
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
        return autre instanceof IntegerType;
    }
}

