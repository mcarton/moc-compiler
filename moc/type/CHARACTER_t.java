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
        return "Char";
    }

    @Override
    public boolean constructsFrom(DTYPE other) {
        return other instanceof CHARACTER_t;
    }
    
    @Override
    public boolean comparableWith(DTYPE other, String operator) {
        return other instanceof CHARACTER_t;
    }

    @Override
    public boolean testable() {
        return false;
    }
}

