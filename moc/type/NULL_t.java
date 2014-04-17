package moc.type;

/** The type of the NULL constant. May be assigned to any pointer, unless C, it
 * is not juste `(void*)0`.
 */
public class NULL_t implements DTYPE {
    public int getSize() {
        return 0;
    }

    public String toString() {
        return "NULL_t";
    }

    @Override
    public boolean constructsFrom(DTYPE other) {
        return other instanceof NULL_t;
    }

    @Override
    public boolean comparableWith(DTYPE other, String operator) {
        return other instanceof NULL_t || other instanceof POINTER;
    }

    @Override
    public boolean testable() {
        return true;
    }
}

