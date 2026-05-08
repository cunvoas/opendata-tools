package com.github.cunvoas.geoserviceisochrone.model.analytics;
	
/**
 * Enumération représentant le Seuil OMS.
 * Permet de qualifer les population en unifiant.
 */
public enum StatsSeuilOmsEnum {
	/**
	 * Insufisant au seuil OMS (<10 ou <25 selon la densité).
	 */
	INSUFFISANT("INSUFFISANT"),  // 0 INSUFFISANT
	/**
	 * Seuil minimul OMS (10<12 ou 25<45 selon la densité).
	 */
	MINIMUM("MINIMUM"),  // 1 MINIMUM
	/**
	 * Seuil préconsé OMS (>12 ou >45 selon la densité).
	 */
	PRECONISE("PRECONISE"),   // 2 PRECONISE
	;
	
	/**
	 * Valeur du statut.
	 */
	private String status;

	/**
	 * Constructeur.
	 * @param status valeur du statut
	 */
	StatsSeuilOmsEnum(String status) {
		this.status = status;
	}
		
	/**
	 * Retourne la valeur du statut.
	 * @return valeur du statut
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Retourne la valeur du statut sous forme de chaîne.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getStatus();
	}
}