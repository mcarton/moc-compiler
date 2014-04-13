package moc.type;

public interface DTYPE {
	/**
	 * La taille du type de donnee : depend de la machine
	 * @return
	 */
	public int getTaille();

	/**
	 * @return
	 */
	public String getNom();

	/**
	 * Fonction de compatibilié avec l'autre type
	 * @param autre
	 * @return
	 */
	public boolean compareTo(DTYPE autre);

	public String toString();
}
