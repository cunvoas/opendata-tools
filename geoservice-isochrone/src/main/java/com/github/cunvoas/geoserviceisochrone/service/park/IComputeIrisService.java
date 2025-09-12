package com.github.cunvoas.geoserviceisochrone.service.park;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeIrisJob;

/**
 * Interface d'abstraction pour les services de calcul sur les IRIS.
 * <p>
 * Définit la méthode principale pour lancer un calcul IRIS à partir d'un job de calcul.
 */
public interface IComputeIrisService {

	/**
	 * Lance le calcul IRIS à partir d'un job de calcul IRIS.
	 * @param job Job de calcul IRIS à traiter
	 * @return TRUE si le calcul a été effectué, FALSE sinon
	 */
	Boolean computeIrisByComputeJob(ComputeIrisJob job);

}