package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "project_simul")
public class ProjectSimulator {

	@Id
	private Long id;
	
	private Long idCommune;
	
	/**
	 * Année de la donnée.
	 */
	private Integer annee;
	private Boolean isDense;

	private BigDecimal population;
	private BigDecimal floorSurface;

	private BigDecimal densityPerAccommodation;  // 2.16 in 2022
	private BigDecimal avgAreaAccommodation;  // 68 in 2022
	
	private Point centerArea;
	private Geometry shapeArea;
	private BigDecimal surfaceArea;
	
	private BigDecimal surfacePark;
	private Geometry shapePark;

	private String name;
	

}
