package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.util.List;

import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationTypeAlgo;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entité de métadonnées pour une simulation de proposition de parcs.
 * <p>
 * Regroupe les paramètres de la simulation (année, commune, algorithme) et les résultats globaux
 * (nombre de parcs proposés, surface totale), ainsi que la liste des propositions détaillées.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"id"})
@Entity(name = "park_proposal_meta")
public class ParkProposalMeta {

	/** Identifiant technique unique (clé primaire). */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	/** Année de la simulation (identifiant métier). */
    private Integer annee;

	/** Code INSEE de la commune concernée. */
    private String insee;

	/** Type d'algorithme de calcul utilisé pour la proposition. */
    @Enumerated(EnumType.STRING)
    private ProposalComputationTypeAlgo typeAlgo;
    
    /** Nombre de parcs proposés par la simulation. */
    private Integer numberOfParks = 0;

	/** Surface totale des parcs proposés (m²). */
    private Integer totalSurfaceOfParks = 0;
    
    /** Liste des propositions de parcs calculées pour cette simulation. */
    @OneToMany(mappedBy = "idMeta", fetch = FetchType.LAZY)
    private List<ParkProposal> proposals;
    
}