package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "project_simul")
public class ProjectSimulator {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_project_simul")
	private Long id;
	
	private Long idCommune;
	private String insee;
	
	/**
	 * Année de la donnée.
	 */
	private Integer annee;
	private Boolean isDense;

	private BigDecimal population;
	private BigDecimal surfaceFloor;

	private BigDecimal densityPerAccommodation;  // 2.16 in 2022
	private BigDecimal avgAreaAccommodation;  // 68 in 2022
	
	private Point centerArea;
	private Geometry shapeArea;
	private BigDecimal surfaceArea;
	private Geometry influenceArea;

	private Point centerPark;
	private BigDecimal surfacePark;

	private String name;
	

	public BigDecimal getProjetPeople() {
		if (densityPerAccommodation!=null && surfaceArea!=null && avgAreaAccommodation!=null && !BigDecimal.ZERO.equals(avgAreaAccommodation)) {
			return densityPerAccommodation.multiply(surfaceArea.divide(avgAreaAccommodation));
		} else {
			return null;
		}
	}

}
