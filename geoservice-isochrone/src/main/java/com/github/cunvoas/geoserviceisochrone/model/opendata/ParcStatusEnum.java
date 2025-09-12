package com.github.cunvoas.geoserviceisochrone.model.opendata;
	
/**
 * Enumération des statuts d'un parc (opendata).
 * <p>
 * Permet de qualifier l'état d'un parc :
 * <ul>
 *   <li>À qualifier</li>
 *   <li>Rejeté</li>
 *   <li>Validé</li>
 * </ul>
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