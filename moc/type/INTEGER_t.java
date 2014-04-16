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

    @Override
    public boolean constructsFrom(DTYPE autre) {
        return autre instanceof INTEGER_t;
    }
     
    @Override
    public boolean comparableWith(DTYPE other, String operator) {
        return other instanceof INTEGER_t;
    }
}
