package moc.type;

public class Array extends AbstractType<Array> {
    private Type pointee;
    private int nbElements;

    public Array(Type pointee, int nbElements) {
        this.size = pointee.getSize()*nbElements;
        this.pointee = pointee;
        this.nbElements = nbElements;
    }

    public String toString() {
        return pointee.toString() + "[" + nbElements + "]";
    }

    public Type getPointee() {
        return pointee;
    }

    public int getNbElements() {
        return nbElements;
    }

    @Override
    public boolean constructsFrom(Type other) {
        return other instanceof Array
            && ((Array)other).pointee.constructsFrom(pointee)
            && ((Array)other).nbElements == nbElements;
    }
      
    @Override
    public boolean comparableWith(Type other, String operator) {
        return (operator.equals("==") || operator.equals("!="))
            && other instanceof Array;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

