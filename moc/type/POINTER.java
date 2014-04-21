package moc.type;

public class POINTER extends AbstractType<POINTER> {
    private DTYPE pointee;

    public POINTER(int size, DTYPE pointee) {
        this.size = size;
        this.pointee = pointee;
    }

    public String toString() {
        return pointee.toString() + "*";
    }

    public DTYPE getPointee() {
        return pointee;
    }

    @Override
    public boolean constructsFrom(DTYPE other) {
        return other instanceof POINTER && ((POINTER)other).pointee.constructsFrom(pointee)
            || other instanceof ARRAY   && ((ARRAY)other).getPointee().constructsFrom(pointee)
            || other instanceof NULL_t;
    }
      
    @Override
    public boolean comparableWith(DTYPE other, String operator) {
        return (   operator.equals("==") || operator.equals("!="))
            && (other instanceof POINTER || other instanceof NULL_t);
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

