package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class Carre200AndShapeView extends GeoJsonProperty {
	@EqualsAndHashCode.Include
	private String id;
	private String idInspire;
	private String commune="";

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
	
	private String fillColor="#060512";
	private String areaPerPeople="";
	private String area="";
	
}
