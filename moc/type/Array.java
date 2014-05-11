package moc.type;

public final class Array extends AbstractType<Array> {
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
        return other.isArray()
            && pointee.constructsFrom(((Array)other).pointee)
            && ((Array)other).nbElements == nbElements;
    }

    @Override
    public <R> R visit(TypeVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Type other) {
        return other.isArray()
            && ((Array)other).getNbElements() == getNbElements()
            && ((Array)other).getPointee().equals(getPointee());
    }

    @Override
    public boolean isArray() {
        return true;
    }
}

