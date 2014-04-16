package moc.type;

public interface DTYPE {
    /**
     * The size of the data type: depends on the machine.
     */
    public int getSize();

    /**
     * Compatibility function with the other type.
     * a.compareTo(b) == true iff we can write `aa = bb` with aa of type a and
     * bb of type b. It is not necessary to have `bb = aa`.
     * For example a NULL_t can be affected to a int*, but the opposite is
     * false.
     */
    public boolean compareTo(DTYPE autre);
}
