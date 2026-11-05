package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client;

import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

/**
 * Interface d'appel au service de l'IGN.
 * @author cunvoas
 */
public interface IsoChroneClientService {

	/**
	 * //point=3.1069023679917662,50.62485026020619
	 * @return
	 */
	String getIsoChrone(Coordinate coordinate, String duration);

}