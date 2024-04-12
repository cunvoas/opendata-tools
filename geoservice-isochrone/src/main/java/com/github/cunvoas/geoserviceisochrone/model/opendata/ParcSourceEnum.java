package com.github.cunvoas.geoserviceisochrone.model.opendata;
	
/**
 * Source du parc.
 */
public enum ParcSourceEnum {
	OPENDATA("opendata"),
	PREFECTURE("prefecture"),
	AUTMEL("autmel");
	

	private String source;

	ParcSourceEnum(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}
	
	@Override
	public String toString() {
		return this.getSource();
	}
}
