package com.github.cunvoas.geoserviceisochrone.service.project;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NeighborsHelper {

	/**
	 * Trouve les N carrés voisins d'un carré donné selon la sensité.
	 * 
	 * @param idInspire identifiant du carré central
	 * @param annee année de référence
	 * @return liste des carrés voisins (max 24 ou 143 selon densité)
	 */
	public static List<InseeCarre200mOnlyShape> findNeighbors(InseeCarre200mOnlyShape projectCarre, List<InseeCarre200mOnlyShape> carreShapes, Integer urbanDistance) {
		List<InseeCarre200mOnlyShape> neighbors = new ArrayList<>();
		
		// Récupérer le centroïde du carré central
		Geometry centreGeom = projectCarre.getGeoShape();
		Coordinate centroid = centreGeom.getCentroid().getCoordinate();
		
		
		for (InseeCarre200mOnlyShape carreShape : carreShapes) {
			// calcul de la distance au centroïde mètres
			Double distance = 1_000 * DistanceHelper.crowFlyDistance(
					centroid.y, 
					centroid.x,
					carreShape.getGeoShape().getCentroid().getY(),
					carreShape.getGeoShape().getCentroid().getX());
			
			// +121m pour le périmètre vs le centroïde
			// la vrai distance est entre 100 et 100 x racine(2) soit environ 141m
			// on prend la moitiée
			if (distance<urbanDistance+121) {
				neighbors.add(carreShape);
			}
		}
		log.info("Trouvé {} voisins pour du carré {}", neighbors.size(), projectCarre.getIdInspire());
		return neighbors;
	}
}
