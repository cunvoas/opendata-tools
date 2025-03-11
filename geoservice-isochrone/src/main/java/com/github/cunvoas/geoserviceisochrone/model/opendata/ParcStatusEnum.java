package com.github.cunvoas.geoserviceisochrone.model.opendata;
	
/**
 * Enum Source du parc.
 */
public enum ParcStatusEnum {
	TO_QUALIFY("TO_QUALIFY"),	// 0
	REJETED("REJETED"),			// 1 refused as a park by Aut'MEL
	VALIDATED("VALIDATED")		// 2 granted as a park by Aut'MEL
	;
	
	private String status;

	ParcStatusEnum(String status) {
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
