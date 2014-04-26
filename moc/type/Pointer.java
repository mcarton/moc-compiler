package moc.type;

public class Pointer extends AbstractType<Pointer> {
    private Type pointee;

    public Pointer(int size, Type pointee) {
        this.size = size;
        this.pointee = pointee;
    }

    public String toString() {
        return pointee.toString() + "*";
    }

    public Type getPointee() {
        return pointee;
    }

    @Override
    public boolean constructsFrom(Type other) {
        return other instanceof Pointer && ((Pointer)other).pointee.constructsFrom(pointee)
            || other instanceof Array   && ((Array)other).getPointee().constructsFrom(pointee)
            || other instanceof NullType;
    }
      
    @Override
    public boolean comparableWith(Type other, String operator) {
        return (   operator.equals("==") || operator.equals("!="))
            && (other instanceof Pointer || other instanceof NullType);
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

