package moc.type;

public class Array extends AbstractType<Array> {
    private Type pointee;
    private int nbElements;

    public Array(Type pointee, int nbElements) {
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

    /**
     * An array can be constructed from an other array iff it has the same size
     * and the its pointee can be constructed from the other array's pointee.
     */
    @Override
    public boolean constructsFrom(Type other) {
        return other instanceof Array
            && pointee.constructsFrom(((Array)other).pointee)
            && ((Array)other).nbElements == nbElements;
    }

    /**
     * Two arrays are comparable for <code>==</code> and <code>!=</code> iff
     * theirs pointees are comparable and are not comparable with other
     * operators.
     */
    @Override
    public boolean comparableWith(Type other, String operator) {
        return (operator.equals("==") || operator.equals("!="))
            && other instanceof Array
            && pointee.comparableWith(((Array)other).pointee, operator);
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}

