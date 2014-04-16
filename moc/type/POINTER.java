package moc.type;

public class POINTER implements DTYPE {
    private int size;
    private DTYPE pointee;

    public POINTER(int size, DTYPE pointee) {
        this.size = size;
        this.pointee = pointee;
    }

    public int getSize() {
        return size;
    }

    public String toString() {
        return pointee.toString() + "*";
    }

    public DTYPE getPointee() {
        return pointee;
    }

    public boolean constructsFrom(DTYPE autre) {
        return autre instanceof POINTER && ((POINTER)autre).pointee.constructsFrom(pointee)
            || autre instanceof NULL_t;
    }
}

