package com.github.cunvoas.geoserviceisochrone.service.park;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeIrisJob;

public interface IComputeIrisService {

	/**
	 * computeCarreByComputeJob.
	 * @param job ComputeJob
	 * @return true if done
	 */
	Boolean computeIrisByComputeJob(ComputeIrisJob job);

}