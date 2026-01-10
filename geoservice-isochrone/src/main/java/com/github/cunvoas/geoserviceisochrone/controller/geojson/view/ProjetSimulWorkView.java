package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for geojson Garden.
 */
@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class ProjetSimulWorkView extends GeoJsonProperty {
	
	@EqualsAndHashCode.Include
	private String idInspire;

	private String accessingPopulation;
	private String localPopulation;

	private String accessingSurface;
	private String missingSurface;
	private String surfacePerCapita;
	
	private String newSurface;
	private String newMissingSurface;
	private String newSurfacePerCapita;
	
}
