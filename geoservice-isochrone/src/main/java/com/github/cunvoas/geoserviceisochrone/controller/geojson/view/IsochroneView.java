package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class IsochroneView extends GeoJsonProperty {
	
	@EqualsAndHashCode.Include
	private String id;
	private String name;
	private String fillColor="#ede7e6";
	
}
