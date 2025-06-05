package com.github.cunvoas.geoserviceisochrone.service.park;

import org.locationtech.jts.geom.Geometry;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;

/**
 * Interface d'abstraction des versions du moteur.
 */
public interface IComputeCarreService {

	/**
	 * computeCarreByComputeJobV2Optim.
	 * @param job ComputeJob
	 * @return true if done
	 */
	Boolean computeCarreByComputeJob(ComputeJob job);

	/**
	 *  Used for mass update and full recompute ParkAreaEntrance.
	 *  @param inseeCode code
	 */
	void refreshParkEntrances(String inseeCode);

	/**
	 * Used for mass update and full recompute ParkAreaEntrance.
	 * @param cadastre Cadastre
	 */
	void refreshParkEntrances(Cadastre cadastre);

	/**
	 * Compute ParkEntrance from ParkArea and List<ParkEntrance>.
	 * @param park
	 * @return ParkAreaComputed
	 * @TODO to be reviewed
	 */
	ParkAreaComputed computeParkArea(ParkArea park);

	/**
	 * getSurface.
	 * @param geom  Geometry
	 * @return surface of Geometry
	 */
	Long getSurface(Geometry geom);

}