package com.github.cunvoas.geoserviceisochrone.extern.helper;

/**
 * Crow flies distance calculator.
 * @see https://fr.wikipedia.org/wiki/Formule_de_haversine
 */
public class DistanceHelper {
	
	private static final int HALF_CIRCLE_IN_DEGREE = 180;
	private static final int EARTH_RADIUS_KM = 6371;

	/**
	 * This method takes in latitude and longitude of two location and returns the distance between them as the crow flies (in km).
	 * @param vLat1 start latitide
	 * @param vLon1 start longitude
	 * @param vLat2 end latitide
	 * @param vLon2 end longitude
	 * @return distance in km
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
	 *  Converts numeric degrees to radians/
	 *  @param angle in degrees
	 * @return angle in radians
	 */
	private static Double toRad(Double degree) {
		return degree * Math.PI / HALF_CIRCLE_IN_DEGREE;
	}
}
