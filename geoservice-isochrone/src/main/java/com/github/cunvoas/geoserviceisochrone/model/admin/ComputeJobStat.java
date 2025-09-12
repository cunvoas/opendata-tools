package com.github.cunvoas.geoserviceisochrone.model.admin;

import lombok.Data;

/**
 * Agrégat pour les statistiques des travaux de calcul (ComputeJob).
 * Permet de regrouper le nombre de jobs par statut et code INSEE.
 */
@Data
public class ComputeJobStat {
	
	/**
	 * Nombre de jobs.
	 */
	private Integer nb;
	
	/**
	 * Statut du job.
	 */
	private ComputeJobStatusEnum status = ComputeJobStatusEnum.TO_PROCESS;

	/**
	 * Code INSEE de la commune.
	 */
	private String codeInsee;
}