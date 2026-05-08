package com.github.cunvoas.geoserviceisochrone.controller.rest.bo;

import lombok.Data;

@Data
public class ComputeJobResponse extends ComputeJobRequest {
	private Integer nbCarre;
	private Integer nbIris;
	
	public ComputeJobResponse(ComputeJobRequest reg) {
		this.setCom2coId(reg.getCom2coId());
		this.setCityId(reg.getCityId());
		this.setParkId(reg.getParkId());
	}
}
