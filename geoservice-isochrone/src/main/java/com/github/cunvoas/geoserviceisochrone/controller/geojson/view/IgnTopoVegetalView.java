package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for geojson Garden.
 */
@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class IgnTopoVegetalView extends GeoJsonProperty {
	
	@EqualsAndHashCode.Include
	private String id;
	private Double surface;
	private String nature;
	
}
