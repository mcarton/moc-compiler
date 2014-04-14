package moc.type;

public interface DTYPE {
    /**
     * La taille du type de donnee : depend de la machine.
     */
    public int getSize();

    public String getName();

    /**
     * Fonction de compatibilié avec l'autre type.
     */
    public boolean compareTo(DTYPE autre);
}

