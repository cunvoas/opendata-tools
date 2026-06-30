package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class ParkProposalWorkView extends GeoJsonProperty {
	@EqualsAndHashCode.Include
	private String idInspire;
	private String isDense;
	private String localPopulation;
	private String accessingPopulation;

	private String surface;
	private String surfacePerCapita;
	private String missingSurface;

	private String newSurface;
	private String newSurfacePerCapita;
	private String newMissingSurface;
}
