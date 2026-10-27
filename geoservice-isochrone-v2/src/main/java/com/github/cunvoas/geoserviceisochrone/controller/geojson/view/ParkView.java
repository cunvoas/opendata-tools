package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import java.math.BigDecimal;
import java.util.Date;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for geojson Park.
 */
@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class ParkView extends GeoJsonProperty {
	@EqualsAndHashCode.Include
	private String id;
	private String quartier;
	private String name;
	//private String desc;
	// valid with OMS definition
	private Boolean oms;
	private Boolean dense;
	//private String fillColor="#ede7e6";
	private String areaPerPeople;
	private String area;
	private String people;
	private Boolean ndviEnabled;
	private BigDecimal ndviMean;
	private BigDecimal ndviMin;
	private BigDecimal ndviMax;
	private BigDecimal ndviStddev;
	private Date ndviAcquisitionDate;
	
}
