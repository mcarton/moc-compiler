package moc.type;

public class VOID_t extends AbstractType<VOID_t> {
    public VOID_t() {
        this.size = 0;
    }

    public String toString() {
        return "void";
    }

    @Override
    public boolean constructsFrom(DTYPE autre) {
        return false;
    }

    @Override
    public boolean comparableWith(DTYPE other, String operator) {
        return false;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

