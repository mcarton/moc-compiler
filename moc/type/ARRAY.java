package moc.type;

public class ARRAY extends AbstractType<ARRAY> {
    private DTYPE pointee;
    private int nbElements;

    public ARRAY(DTYPE pointee, int nbElements) {
        this.size = pointee.getSize()*nbElements;
        this.pointee = pointee;
        this.nbElements = nbElements;
    }

    public String toString() {
        return pointee.toString() + "[" + nbElements + "]";
    }

    public DTYPE getPointee() {
        return pointee;
    }

    public int getNbElements() {
        return nbElements;
    }

    @Override
    public boolean constructsFrom(DTYPE other) {
        return other instanceof ARRAY
            && ((ARRAY)other).pointee.constructsFrom(pointee)
            && ((ARRAY)other).nbElements == nbElements;
    }
      
    @Override
    public boolean comparableWith(DTYPE other, String operator) {
        return (operator.equals("==") || operator.equals("!="))
            && other instanceof ARRAY;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

