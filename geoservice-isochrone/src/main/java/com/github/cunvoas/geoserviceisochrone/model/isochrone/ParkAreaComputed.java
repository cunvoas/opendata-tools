package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Model ParkAreaComputed.
 */
@Data
@Entity(name = "park_area_computed")
@IdClass(ParkAreaComputedId.class)
@EqualsAndHashCode(of = {"annee", "id"})
public class ParkAreaComputed {

	// same as ParkArea
	@Id
	@Column(name = "id")
	private Long id;
	
	@Id
	@Column(name = "annee")
	private Integer annee;
	

	@Column(name = "oms")
	private Boolean oms;
	
	@Column(name = "surface", precision = 11, scale = 2)
	private BigDecimal surface;
	
	@Column(name = "population", precision = 7, scale = 2)
	private BigDecimal population;
	
	@Column(name = "surface_population", precision = 10, scale =2)
	private BigDecimal surfacePerInhabitant;
	
	@Column(name = "dense")
	private Boolean isDense = Boolean.TRUE;
	
	@DateTimeFormat
	@Column(name="updated")
	private Date updated;

}
