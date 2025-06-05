package com.github.cunvoas.geoserviceisochrone.controller.rest.bo;

import lombok.Data;

@Data
public class ComputeJobRequest {
	
	private String token;
	private Long com2coId;
	private Long cityId;
	private Long parkId;

}
