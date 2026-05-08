package com.github.cunvoas.geoserviceisochrone.model.admin;

import java.util.Date;

import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entité représentant un travail de calcul pour un IRIS donné et une année.
 * <p>
 * Permet de suivre l'état d'avancement du traitement pour chaque IRIS (demandé, traité, etc.).
 * Stocke la date de demande, de traitement, le statut et le code INSEE associé.
 * </p>
 * @author cunvoas
 */
@Data
@EqualsAndHashCode(of = {"annee", "iris"})
@Entity(name = "compute_iris_job")
@IdClass(IrisId.class)
public class ComputeIrisJob {

	/** Année de la donnée. */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	/** Identifiant IRIS. */
	@Id
	@Column(name="iris",length=30)
	private String iris;
	/** Date de demande du traitement. */
	@Column(name="demand")
	private Date demand = new Date();
	/** Date de traitement effectif. */
	@Column(name="processed")
	private Date processed;
	/** Statut du traitement. */
	@Column(name="status")
	private ComputeJobStatusEnum status = ComputeJobStatusEnum.TO_PROCESS;
	/** Code INSEE de la commune. */
	@Column(name="insee",length=5)
	private String codeInsee;
}