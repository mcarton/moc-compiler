package moc.type;

public class ARRAY implements DTYPE {
    private int size;
    private DTYPE pointee;
    private int nbElements;

    public ARRAY(int size, DTYPE pointee, int nbElements) {
        this.size = size;
        this.pointee = pointee;
        this.nbElements = nbElements;
    }

    public int getSize() {
        return size;
    }

    public String toString() {
        return pointee.toString() + "[" + nbElements + "]";
    }

    public DTYPE getPointee() {
        return pointee;
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
    public boolean testable() {
        return false;
    }
}

