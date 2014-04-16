package moc.type;

public class VOID_t implements DTYPE {
    public int getSize() {
        return 0;
    }

    public String toString() {
        return "void";
    }

    @Override
    public boolean constructsFrom(DTYPE autre) {
        return false;
    }

    @Override
    public boolean comparableWith(DTYPE other, String operator) {
        return false;
    }
}

