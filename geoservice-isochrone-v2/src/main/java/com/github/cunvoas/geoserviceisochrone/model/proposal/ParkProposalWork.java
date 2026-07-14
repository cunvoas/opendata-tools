package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Point;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;
import com.github.cunvoas.geoserviceisochrone.service.solver.helper.ProposalComputationTypeAlgo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entité de travail intermédiaire pour le calcul des propositions de parcs par carreau de 200m.
 * <p>
 * Contient les données de surface manquante, population accessante et locale, surface accessible,
 * ainsi que les valeurs après ajout du nouveau parc proposé.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"annee", "idInspire"})
@Entity(name = "park_proposal_work")
@IdClass(ParkProposalWorkId.class)
public class ParkProposalWork {

	/** Année de la donnée. */
	@Id
	@Column(name="annee",length=4)
    private Integer annee;
	
	/** Identifiant Inspire du carreau de 200m. */
	@Id
	@Column(name="idInspire",length=30)
	private String idInspire;
	
	@Id 
	/** Identifiant du type d'algo (ParkProposalMeta). */
	@Enumerated(EnumType.STRING)
	@Column(name="type_algo",length=30)
	private ProposalComputationTypeAlgo typeAlgo;

	/** Indique si le carreau est en zone dense. */
	@Column(name="dense")
	private Boolean isDense;

	/** Point central du carreau. */
	@Column(name="centre")
	private Point centre;

	/** Population totale du carreau. */
	@Column(name="local_pop", precision = 12, scale = 2)
	private BigDecimal localPopulation;
	
	/** Population ayant accès aux parcs dans le carreau. */
	@Column(name="access_pop", precision = 12, scale = 2)
	private BigDecimal accessingPopulation;
	
	/**
	 * Surface manquante pour atteindre le seuil OMS.
	 * Formule : (Seuil OMS – MAX(0, surface disponible - seuil OMS)) * Nb habitants ayant accès.
	 */
	@Column(name="miss_surf", precision = 12, scale = 2)
	private BigDecimal missingSurface;

	/** Surface de parc accessible depuis le carreau (m²). */
	@Column(name="access_surf", precision = 12, scale = 2)
	private BigDecimal accessingSurface;
	
	/** Surface de parc par habitant (m²/hab). */
	@Column(name="surf_per_capita", precision = 12, scale = 2)
	private BigDecimal surfacePerCapita;
	

	/** Nouvelle surface manquante après proposition. */
	@Column(name="new_miss_surf", precision = 12, scale = 2)
	private BigDecimal newMissingSurface;
	
	/** Nouvelle surface de parc après proposition (m²). */
	@Column(name="new_surf", precision = 12, scale = 2)
	private BigDecimal newAccessingSurface;

	/** Nouvelle surface de parc par habitant après proposition (m²/hab). */
	@Column(name="new_surf_per_capita", precision = 12, scale = 2)
	private BigDecimal newSurfacePerCapita;

	
}