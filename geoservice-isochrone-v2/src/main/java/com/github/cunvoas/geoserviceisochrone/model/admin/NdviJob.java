package com.github.cunvoas.geoserviceisochrone.model.admin;

import java.util.Date;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputedId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Travail de récupération NDVI pour un parc et une année de référence.
 *
 * <p>File d'attente persistante : {@code ComputeParkAreaV2} ne fait plus l'appel
 * fournisseur, il dépose une ligne {@code TO_PROCESS}. Un traitement planifié par
 * lots consomme la file et stocke la réponse dans {@code ndvi_result}.</p>
 *
 * <p>Le NDVI Copernicus est lissé sur plusieurs années : un appel par parc suffit.
 * La mesure est conservée avec sa période pour permettre une reventilation ultérieure.</p>
 */
@Data
@EqualsAndHashCode(of = {"annee", "id"})
@Entity(name = "ndvi_job")
@IdClass(ParkAreaComputedId.class)
public class NdviJob {

	/** Identifiant de la zone de parc ({@code park_area.id}). */
	@Id
	@Column(name = "id")
	private Long id;

	/** Année de référence de la ligne {@code park_area_computed}. */
	@Id
	@Column(name = "annee", length = 4)
	private Integer annee;

	/** Date de mise en file d'attente. */
	@Column(name = "demand")
	private Date demand = new Date();

	/** Date du dernier traitement. */
	@Column(name = "processed")
	private Date processed;

	/** Statut du traitement. */
	@Column(name = "status")
	private ComputeJobStatusEnum status = ComputeJobStatusEnum.TO_PROCESS;

	/** Relance forcée : ignore le NDVI existant et la règle du un appel par an. */
	@Column(name = "force")
	private boolean force;
}
