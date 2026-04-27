package com.github.cunvoas.geoserviceisochrone.model.admin;
	
/**
 * Enumération représentant le statut d'un travail de calcul (ComputeJob).
 * Permet de suivre l'état d'avancement du traitement.
 */
public enum ComputeJobStatusEnum {
	/**
	 * Traitement à effectuer (demande en attente).
	 */
	TO_PROCESS("TO_PROCESS"),  // 0 process requested
	/**
	 * Traitement en cours.
	 */
	IN_PROCESS("IN_PROCESS"),  // 1 process started
	/**
	 * Traitement terminé avec succès.
	 */
	PROCESSED("PROCESSED"),   // 2 process done
	/**
	 * Traitement en erreur.
	 */
	IN_ERROR("IN_ERROR"),   // 3 process in error
	;
	
	/**
	 * Valeur du statut.
	 */
	private String status;

	/**
	 * Constructeur.
	 * @param status valeur du statut
	 */
	ComputeJobStatusEnum(String status) {
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