package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "park_area_computed")
public class ParkAreaComputed {

	// same as ParkArea
	@Id
	private long id;

	@Column(name = "oms")
	private Boolean oms;
	
	@Column(name = "surface", precision = 11, scale = 2)
	private BigDecimal surface;
	
	@Column(name = "population", precision = 7, scale = 2)
	private BigDecimal population;
	
	@Column(name = "surface_population", precision = 10, scale =2)
	private BigDecimal surfacePerInhabitant;

	@Column(name="updated")
	private Date updated;

}
