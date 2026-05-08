package com.github.cunvoas.geoserviceisochrone.model.admin;

import java.util.Date;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Représente un travail de calcul pour une maille INSEE (carre 200m) et une année.
 * Permet de suivre l'état d'avancement du traitement pour chaque maille.
 */
@Data
@EqualsAndHashCode(of = {"annee", "idInspire"})
@Entity(name = "compute_job")
@IdClass(InseeCarre200mComputedId.class)
public class ComputeJob {

	/**
	 * Année de la donnée.
	 */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	
	/**
	 * Identifiant Inspire de la maille INSEE (carre 200m).
	 */
	@Id
	@Column(name="idInspire",length=30)
	private String idInspire;
	
	/**
	 * Date de demande du traitement.
	 */
	@Column(name="demand")
	private Date demand = new Date();

	/**
	 * Date de traitement effectif.
	 */
	@Column(name="processed")
	private Date processed;

	/**
	 * Statut du traitement.
	 */
	@Column(name="status")
	private ComputeJobStatusEnum status = ComputeJobStatusEnum.TO_PROCESS;

	/**
	 * Code INSEE de la commune.
	 */
	@Column(name="insee",length=5)
	private String codeInsee;
}