package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.service.solver.helper.ParkProposalHelper;
import com.github.cunvoas.geoserviceisochrone.service.solver.sort.ProposalSortStrategy;
import com.github.cunvoas.geoserviceisochrone.service.solver.sort.ProposalSortStrategyFactory;
import com.github.cunvoas.geoserviceisochrone.service.solver.sort.ProposalSortStrategyFactory.Type;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractComputationtrategy implements ProposalComputationStrategy {


	public static final double AT_LEAST_PARK_SURFACE = 1_000; // m²
	public static final double MIN_PARK_SURFACE = 350; // m²
	public static final double CARRE_SIZE = 333; // mètres (200m x 200m)	
	public static final double CARRE_SURFACE = 40_000; // m²
	public static final int SQUARE_DISTANCE = 120; // marge en mètres entre demi-coté et demi-diagonale
	

	/**
	 * Trie les propositions par déficit décroissant
	 * @param squaresOnTerritoryMap
	 * @return
	 * test: 268566.01
	 */
	public List<ParkProposalWork> sortProposalsByDeficit(Map<String, ParkProposalWork> squaresOnTerritoryMap) {
		ProposalSortStrategy strategy = ProposalSortStrategyFactory.create(Type.DEFICIT);
		return strategy.sort(squaresOnTerritoryMap);
	}
	
	
	/**
	 * Trie les propositions par déficit décroissant
	 * @param squaresOnTerritoryMap
	 * @return
	 * test: xxxs
	 */
	public List<ParkProposalWork> sortProposalsByPersona(Map<String, ParkProposalWork> squaresOnTerritoryMap) {
		ProposalSortStrategy strategy = ProposalSortStrategyFactory.create(Type.PERSONA);
		return strategy.sort(squaresOnTerritoryMap);
	}
	
	
	
	/**
	 * Trie les propositions par impact humain total decroissant : manque de surface × population.
	 * Priorite aux carreaux ou le deficit est a la fois grand et concerne beaucoup d'habitants.
	 * @param squaresOnTerritoryMap
	 * @return
	 */
	public List<ParkProposalWork> sortProposalsByMissingPopulation(Map<String, ParkProposalWork> squaresOnTerritoryMap) {
		ProposalSortStrategy strategy = ProposalSortStrategyFactory.create(Type.MISSING_POPULATION);
		return strategy.sort(squaresOnTerritoryMap);
	}


	/**
	 * Trouve les N carrés voisins d'un carré donné selon la sensité.
	 * 
	 * @param idInspire identifiant du carré central
	 * @param annee année de référence
	 * @return liste des carrés voisins (max 24 ou 143 selon densité)
	 */
	public List<ParkProposalWork> findNeighbors(String idInspire, Map<String, ParkProposalWork> squaresOnTerritoryMap, Integer urbanDistance) {
		return ParkProposalHelper.findNeighbors(idInspire, squaresOnTerritoryMap, urbanDistance);
	}
	
}
