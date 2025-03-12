package com.github.cunvoas.geoserviceisochrone.model.opendata;
	
/**
 * Enum Source du parc.
 */
public enum ParcStatusPrefEnum {
	NO_MATCH("NO_MATCH"), 	  // 0 no match with opendata park
	TO_QUALIFY("TO_QUALIFY"), // 1 pre-computed during process
	CANCEL("CANCEL"),		  // 2 refused as a park by Aut'MEL
	VALID("VALID"),			  // 3 granted as a park by Aut'MEL
	PROCESSED("PROCESSED")	  // 4 granted and processes
	;
	
	private String status;

	ParcStatusPrefEnum(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
	
	@Override
	public String toString() {
		return this.getStatus();
	}
}
