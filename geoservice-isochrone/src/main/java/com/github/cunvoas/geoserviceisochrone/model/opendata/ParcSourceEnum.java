package com.github.cunvoas.geoserviceisochrone.model.opendata;
	
/**
 * Enum Source du parc.
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
