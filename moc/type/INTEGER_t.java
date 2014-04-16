package moc.type;

public class INTEGER_t implements DTYPE {
    private int size;

    public INTEGER_t(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public String toString() {
        return "int";
    }

    public boolean compareTo(DTYPE autre) {
        return autre instanceof INTEGER_t;
    }
}
