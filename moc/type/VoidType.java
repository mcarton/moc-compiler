package moc.type;

public class VoidType extends AbstractType<VoidType> {
    public VoidType() {
        this.size = 0;
    }

    public String toString() {
        return "void";
    }

    @Override
    public boolean constructsFrom(Type autre) {
        return false;
    }

    @Override
    public boolean comparableWith(Type other, String operator) {
        return false;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

