package com.github.cunvoas.geoserviceisochrone.controller.rest.bo;

import lombok.Data;

@Data
public class ComputeJobRequest {
	
	private String token;
	private String com2coId;
	private String cityId;
	private String parkId;

}
