package moc.type;

public class VOID_t implements DTYPE {
    public int getSize() {
        return 0;
    }

    public String toString() {
        return "void";
    }

    public boolean constructsFrom(DTYPE autre) {
        return false;
    }
}
