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

    public boolean constructsFrom(DTYPE autre) {
        return autre instanceof NULL_t;
    }
}

