package com.github.cunvoas.geoserviceisochrone.model.opendata;
	
/**
 * Enumération des sources de données pour les parcs.
 * <p>
 * Permet d'identifier l'origine des données :
 * <ul>
 *   <li>Opendata</li>
 *   <li>Préfecture</li>
 *   <li>Aut'MEL</li>
 * </ul>
 */
public enum ParcSourceEnum {
	OPENDATA("opendata"),
	PREFECTURE("prefecture"),
	AUTMEL("autmel");
	

	private String source;

	/**
	 * Constructor.
	 * @param source source
	 */
	ParcSourceEnum(String source) {
		this.source = source;
	}

	/**
	 * @return source
	 */
	public String getSource() {
		return source;
	}
	
	/**
	 * @see java.lang.Object.toString()
	 */
	@Override
	public String toString() {
		return this.getSource();
	}
}