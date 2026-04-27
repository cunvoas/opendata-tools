package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for geojson Park.
 */
@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class ParkProposalView extends GeoJsonProperty {
	@EqualsAndHashCode.Include
	private String id;
	//private String desc;
	// valid with OMS definition
	private Boolean dense;
	private Integer surface;
	private Long radius;
	
	
}
