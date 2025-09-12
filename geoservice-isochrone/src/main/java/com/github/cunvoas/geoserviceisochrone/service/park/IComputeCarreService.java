package com.github.cunvoas.geoserviceisochrone.service.park;

import org.locationtech.jts.geom.Geometry;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;

/**
 * Interface d'abstraction pour les services de calcul sur les carrés de 200m.
 * <p>
 * Définit les méthodes principales pour le calcul, la mise à jour et la gestion des entrées de parcs,
 * ainsi que le calcul de surface géométrique.
 */
public interface IComputeCarreService {

	/**
	 * Lance le calcul sur un carré 200m à partir d'un job de calcul.
	 * @param job Job de calcul à traiter
	 * @return TRUE si le calcul a été effectué, FALSE sinon
	 */
	Boolean computeCarreByComputeJob(ComputeJob job);

	/**
	 * Met à jour en masse et recalcule les entrées de parcs pour un code INSEE donné.
	 * @param inseeCode Code INSEE de la commune
	 */
	void refreshParkEntrances(String inseeCode);

	/**
	 * Met à jour en masse et recalcule les entrées de parcs pour un cadastre donné.
	 * @param cadastre Entité cadastre
	 */
	void refreshParkEntrances(Cadastre cadastre);

	/**
	 * Calcule les entrées de parc à partir d'une entité ParkArea.
	 * @param park Entité ParkArea
	 * @return Résultat du calcul sous forme de ParkAreaComputed
	 */
	ParkAreaComputed computeParkArea(ParkArea park);

	/**
	 * Calcule la surface d'une géométrie.
	 * @param geom Géométrie à mesurer
	 * @return Surface de la géométrie
	 */
	Long getSurface(Geometry geom);

}