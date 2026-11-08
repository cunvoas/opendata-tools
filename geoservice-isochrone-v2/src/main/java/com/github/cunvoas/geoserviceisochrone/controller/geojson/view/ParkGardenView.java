package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import java.math.BigDecimal;
import java.util.Date;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for geojson Garden.
 */
@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class ParkGardenView extends GeoJsonProperty {
	
	@EqualsAndHashCode.Include
	private String id;
	private String name;
	private String city;
	private String source;
	private Double surface;
	private Boolean oms;
	private Boolean entry;
	private Boolean actif;
	private Date dateDebut;
	private Date dateFin;
	private Boolean ndviEnabled;
	private BigDecimal ndviMean;
	private BigDecimal ndviMin;
	private BigDecimal ndviMax;
	private BigDecimal ndviStddev;
	private BigDecimal ndreMean;
	private BigDecimal ndreMin;
	private BigDecimal ndreMax;
	private BigDecimal ndreStddev;
	private BigDecimal gndviMean;
	private BigDecimal gndviMin;
	private BigDecimal gndviMax;
	private BigDecimal gndviStddev;
	private Date ndviAcquisitionDate;
	
}
