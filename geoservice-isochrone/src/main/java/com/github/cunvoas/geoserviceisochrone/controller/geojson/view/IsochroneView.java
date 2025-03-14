package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for geojson Isochrone.
 */
@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class IsochroneView extends GeoJsonProperty {
	
	@EqualsAndHashCode.Include
	private String id;
	private String name;
	private String fillColor="#ede7e6";
	private Boolean valid=true;
	
}
