package moc.type;

public class CharacterType extends AbstractType<CharacterType> {
    public CharacterType() {
    }

    public String toString() {
        return "Char";
    }

    @Override
    public boolean constructsFrom(Type other) {
        return other instanceof CharacterType;
    }
    
    @Override
    public boolean comparableWith(Type other, String operator) {
        return other instanceof CharacterType;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

