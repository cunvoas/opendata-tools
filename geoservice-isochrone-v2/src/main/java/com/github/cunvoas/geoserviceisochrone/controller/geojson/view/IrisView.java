package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for geojson IRIS.
 */
@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class IrisView extends GeoJsonProperty {
			
	@EqualsAndHashCode.Include
	private String id;
	private String idIris;
	private String commune;

	private String people="";
	
	private String surfaceTotalPark="";
	private String popParkIncluded="";
	private String popParkExcluded="";
	private String popSquareShare="";
	private String squareMtePerCapita="";
	
	private String surfaceTotalParkOms="";
	private String popParkIncludedOms="";
	private String popParkExcludedOms="";
	private String popSquareShareOms="";
	private String squareMtePerCapitaOms="";
	private Boolean isDense=null;
	
	private String commentParks="";
	
	private String areaPerPeople="";
	private String area="";
	
}
