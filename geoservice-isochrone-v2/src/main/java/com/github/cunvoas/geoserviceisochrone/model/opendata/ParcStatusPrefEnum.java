package com.github.cunvoas.geoserviceisochrone.model.opendata;
	
/**
 * Enumération des statuts d'un parc issu de la préfecture.
 * <p>
 * Permet de qualifier l'état d'un parc selon la préfecture :
 * <ul>
 *   <li>Pas de correspondance</li>
 *   <li>À qualifier</li>
 *   <li>Annulé</li>
 *   <li>Validé</li>
 *   <li>Traité</li>
 * </ul>
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