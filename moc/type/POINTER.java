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

    public String getName() {
        return pointee.getName() + "*";
    }

    public DTYPE getPointee() {
        return pointee;
    }

    public boolean compareTo(DTYPE autre) {
        return autre instanceof POINTER && ((POINTER)autre).pointee.compareTo(pointee)
            || autre instanceof NULL_t;
    }
}

