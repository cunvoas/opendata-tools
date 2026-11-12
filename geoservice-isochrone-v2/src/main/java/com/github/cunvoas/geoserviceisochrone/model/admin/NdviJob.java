package com.github.cunvoas.geoserviceisochrone.model.admin;

import java.util.Date;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviResultObjectType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Travail de récupération NDVI pour un objet NDVI (parc, carreau 200m, ...).
 *
 * <p>File d'attente persistante : {@code ComputeParkAreaV2} ne fait plus l'appel
 * fournisseur, il dépose une ligne {@code TO_PROCESS}. Un traitement planifié par
 * lots consomme la file et stocke la réponse dans {@code ndvi_result}.</p>
 *
 * <p>Le NDVI Copernicus est lissé sur plusieurs années : un appel par parc suffit.
 * La mesure est conservée avec sa période pour permettre une reventilation ultérieure.</p>
 *
 * <p>Clé technique {@code job_id}. Pas de NULL dans la clé : {@code id} vaut {@link #NO_ID}
 * si la ligne porte un {@code id_inspire}, {@code idInspire} vaut {@link #NO_ID_INSPIRE}
 * si elle porte un {@code id}. L'unicité du triplet est portée par la base.</p>
 */
@Data
@EqualsAndHashCode(of = {"objectType", "id", "idInspire"})
@Entity(name = "ndvi_job")
public class NdviJob {

	/** Sentinelle de {@link #id} pour une ligne carreau 200m. */
	public static final Long NO_ID = -1L;

	/** Sentinelle de {@link #idInspire} pour une zone de parc. */
	public static final String NO_ID_INSPIRE = "";

	/** Identifiant technique de la demande. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "job_id")
	private Long jobId;

	/** Identifiant de la zone de parc ({@code park_area.id}). */
	@Column(name = "id", nullable = false)
	private Long id = NO_ID;

	/** Code Inspire du carreau 200m ({@code NdviResultObjectType.CARRE_200M}). */
	@Column(name = "id_inspire", nullable = false, length = 30)
	private String idInspire = NO_ID_INSPIRE;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "object_type", nullable = false, length = 30)
	private NdviResultObjectType objectType;
	
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
