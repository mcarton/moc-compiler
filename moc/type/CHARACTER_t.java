package moc.type;

public class CHARACTER_t extends AbstractType<CHARACTER_t> {
    public CHARACTER_t(int size) {
        this.size = size;
    }

    public String toString() {
        return "Char";
    }

    @Override
    public boolean constructsFrom(DTYPE other) {
        return other instanceof CHARACTER_t;
    }
    
    @Override
    public boolean comparableWith(DTYPE other, String operator) {
        return other instanceof CHARACTER_t;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

