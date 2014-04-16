package moc.type;

public class CHARACTER_t implements DTYPE {
    private int size;

    public CHARACTER_t(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public String toString() {
        return "char";
    }

    public boolean constructsFrom(DTYPE autre) {
        return autre instanceof CHARACTER_t;
    }
}
