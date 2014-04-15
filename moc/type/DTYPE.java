package moc.type;

public interface DTYPE
{
    /**
     * The size of the data type: depends on the machine
     */
    public int getSize();

    public String getName();

    /**
     * Compatibility function with the other type
     */
    public boolean compareTo(DTYPE autre);
}
