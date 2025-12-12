package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.service.solver.sort.ProposalSortStrategy;
import com.github.cunvoas.geoserviceisochrone.service.solver.sort.ProposalSortStrategyFactory;
import com.github.cunvoas.geoserviceisochrone.service.solver.sort.ProposalSortStrategyFactory.Type;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractComputationtrategy implements ProposalComputationStrategy {


	public static final double AT_LEAST_PARK_SURFACE = 1_000; // m²
	public static final double MIN_PARK_SURFACE = 650; // m²
	public static final double CARRE_SIZE = 200; // mètres (200m x 200m)	
	public static final double CARRE_SURFACE = 40_000; // m²
	
	

	/**
	 * Trie les propositions par déficit décroissant
	 * @param carreMap
	 * @return
	 * test: 268566.01
	 */
	public List<ParkProposalWork> sortProposalsByDeficit(Map<String, ParkProposalWork> carreMap) {
		ProposalSortStrategy strategy = ProposalSortStrategyFactory.create(Type.DEFICIT);
		return strategy.sort(carreMap);
	}
	
	
	/**
	 * Trie les propositions par déficit décroissant
	 * @param carreMap
	 * @return
	 * test: xxxs
	 */
	public List<ParkProposalWork> sortProposalsByPersona(Map<String, ParkProposalWork> carreMap) {
		ProposalSortStrategy strategy = ProposalSortStrategyFactory.create(Type.PERSONA);
		return strategy.sort(carreMap);
	}
	
	
	
	/**
	 * Trouve les N carrés voisins d'un carré donné selon la sensité.
	 * 
	 * @param idInspire identifiant du carré central
	 * @param annee année de référence
	 * @return liste des carrés voisins (max 24 ou 143 selon densité)
	 */
	public List<ParkProposalWork> findNeighbors(String idInspire, Map<String, ParkProposalWork> carreMap, Integer urbanDistance) {
		List<ParkProposalWork> neighbors = new ArrayList<>();
		
		// Récupérer le carré central
		ParkProposalWork centre = carreMap.get(idInspire);
		
		if (centre == null) {
			log.warn("Carré central {} introuvable dans la carte des carrés", idInspire);
			return neighbors;
		}
		
		// Récupérer le centroïde du carré central
		Geometry centreGeom = centre.getCentre();
		Coordinate centreCentroid = centreGeom.getCentroid().getCoordinate();
		
		// Rechercher les carrés dans cette zone
		for (Map.Entry<String, ParkProposalWork> parkProposal : carreMap.entrySet()) {
			if (!parkProposal.getKey().equals(idInspire)) {
				Double distance = 1_000 * DistanceHelper.crowFlyDistance(
						centreCentroid.y, 
						centreCentroid.x,
						parkProposal.getValue().getCentre().getCentroid().getY(),
						parkProposal.getValue().getCentre().getCentroid().getX());
				
				if (distance<urbanDistance+100) { // +100m pour le périmètre vs le centroïde
					neighbors.add(parkProposal.getValue());
				}
			}
		}

		log.info("Trouvé {} voisins pour le carré {}", neighbors.size(), idInspire);
		return neighbors;
	}
}
