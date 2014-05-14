package moc.type;

public final class ClassType extends AbstractType<ClassType> {
    String name;

    public ClassType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    @Override
    public boolean constructsFrom(Type autre) {
        return false;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Type other) {
        return other == this;
    }

    @Override
    public boolean isClass() {
        return true;
    }
}

