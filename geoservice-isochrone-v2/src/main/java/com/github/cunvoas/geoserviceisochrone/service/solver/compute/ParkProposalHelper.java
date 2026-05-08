package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParkProposalHelper {

	/**
	 * Trouve les N carrés voisins d'un carré donné selon la sensité.
	 * 
	 * @param idInspire identifiant du carré central
	 * @param annee année de référence
	 * @return liste des carrés voisins (max 24 ou 143 selon densité)
	 */
	public static List<ParkProposalWork> findNeighbors(String idInspire, Map<String, ParkProposalWork> carreMap, Integer urbanDistance) {
		List<ParkProposalWork> neighbors = new ArrayList<>();
		
		// Récupérer le carré central
		ParkProposalWork centre = carreMap.get(idInspire);
		
		if (centre == null) {
			log.warn("Carré central {} introuvable dans la carte des carrés", idInspire);
			return neighbors;
		}
		
		// Récupérer le centroïde du carré central
		Geometry centreGeom = centre.getCentre();
		Coordinate centroid = centreGeom.getCentroid().getCoordinate();
		
		// Rechercher les carrés dans cette zone
		for (Map.Entry<String, ParkProposalWork> parkProposal : carreMap.entrySet()) {
			if (!parkProposal.getKey().equals(idInspire)) {
				// calcul de la distance au centroïde mètres
				Double distance = 1_000 * DistanceHelper.crowFlyDistance(
						centroid.y, 
						centroid.x,
						parkProposal.getValue().getCentre().getCentroid().getY(),
						parkProposal.getValue().getCentre().getCentroid().getX());
				
				if (distance<urbanDistance+100) { // +100m pour le périmètre vs le centroïde
					neighbors.add(parkProposal.getValue());
				}
			}
		}

		if (neighbors.size()>24) {
			log.warn("Nombre de voisins ({}) supérieur à 24 pour le carré {} en zone urbaine", neighbors.size(), idInspire);
			
		}
		log.info("Trouvé {} voisins pour le carré {}", neighbors.size(), idInspire);
		return neighbors;
	}
}
