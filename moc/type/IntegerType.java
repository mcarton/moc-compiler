package moc.type;

public class IntegerType extends AbstractType<IntegerType> {
    public IntegerType() {
    }

    public String toString() {
        return "Int";
    }

    @Override
    public boolean constructsFrom(Type autre) {
        return autre instanceof IntegerType;
    }
     
    @Override
    public boolean comparableWith(Type other, String operator) {
        return other instanceof IntegerType;
    }

    @Override
    public boolean testable() {
        return true;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

