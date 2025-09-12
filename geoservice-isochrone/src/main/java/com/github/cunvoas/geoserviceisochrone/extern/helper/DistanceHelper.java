package com.github.cunvoas.geoserviceisochrone.extern.helper;

import org.locationtech.jts.geom.Point;

/**
 * Classe utilitaire pour le calcul de la distance à vol d'oiseau entre deux points géographiques.
 * Utilise la formule de Haversine.
 * @see https://fr.wikipedia.org/wiki/Formule_de_haversine

 */
public class DistanceHelper {
	
	private static final int HALF_CIRCLE_IN_DEGREE = 180;
	private static final int EARTH_RADIUS_KM = 6371;

	/**
	 * Calcule la distance à vol d'oiseau (en kilomètres) entre deux points.
	 * @param point1 premier point (JTS Point)
	 * @param point2 second point (JTS Point)
	 * @return distance en kilomètres
	 */
	public static Double crowFlyDistance(Point point1, Point point2) {
		return crowFlyDistance(point1.getY(), point1.getX(), point2.getY(), point2.getX());
	}
	
	/**
	 * Calcule la distance à vol d'oiseau (en kilomètres) entre deux coordonnées géographiques.
	 * @param vLat1 latitude du point de départ
	 * @param vLon1 longitude du point de départ
	 * @param vLat2 latitude du point d'arrivée
	 * @param vLon2 longitude du point d'arrivée
	 * @return distance en kilomètres
	 */
	public static Double crowFlyDistance(Double vLat1, Double vLon1, Double vLat2, Double vLon2) {
		Double dLat = toRad(vLat2 - vLat1);
		Double dLon = toRad(vLon2 - vLon1);
		Double lat1 = toRad(vLat1);
		Double lat2 = toRad(vLat2);

		Double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				 + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		return EARTH_RADIUS_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}

	/**
	 * Convertit un angle en degrés en radians.
	 * @param degree angle en degrés
	 * @return angle en radians
	 */
	private static Double toRad(Double degree) {
		return degree * Math.PI / HALF_CIRCLE_IN_DEGREE;
	}
}