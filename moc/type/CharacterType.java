package moc.type;

public final class CharacterType extends AbstractType<CharacterType> {
    public CharacterType() {
    }

    public String toString() {
        return "Char";
    }

    public boolean castsFrom(Type other) {
        return constructsFrom(other)
            || other instanceof IntegerType;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Type other) {
        return other instanceof CharacterType;
    }
}

