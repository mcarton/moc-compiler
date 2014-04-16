package moc.type;

public class VOID_t implements DTYPE {
    public int getSize() {
        return 0;
    }

    public String getName() {
        return "void";
    }

    public boolean compareTo(DTYPE autre) {
        return autre instanceof VOID_t;
    }
}
