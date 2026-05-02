package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Entité représentant un projet de simulation de nouveau quartier ou aménagement urbain.
 * <p>
 * Permet d'estimer les besoins en espaces verts d'un projet immobilier en fonction de la
 * surface, la densité et la superficie moyenne des logements. Contient les zones d'influence
 * et le parc proposé associé.
 * </p>
 */
@Data
@Entity(name = "project_simul")
public class ProjectSimulator {

	/** Identifiant unique du projet (clé primaire). */
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_project_simul")
	private Long id;
	
	/** Identifiant de la commune associée au projet. */
	private Long idCommune;

	/** Code INSEE de la commune. */
	private String insee;
	
	/** Année de référence de la donnée démographique. */
	private Integer annee;

	/** Indique si la zone du projet est en zone dense. */
	private Boolean isDense;

	/** Population estimée de la zone (calculée ou saisie). */
	private BigDecimal population;

	/** Surface au sol du projet (m²). */
	private BigDecimal surfaceFloor;

	/** Densité par logement (nombre moyen d'habitants par logement, ex : 2.16 en 2022). */
	private BigDecimal densityPerAccommodation;

	/** Surface moyenne d'un logement (m², ex : 68 m² en 2022). */
	private BigDecimal avgAreaAccommodation;

	/** Point central de la zone du projet. */
	private Point centerArea;

	/** Géométrie de la zone du projet (Polygon ou MultiPolygon). */
	private Geometry shapeArea;

	/** Surface totale de la zone du projet (m²). */
	private BigDecimal surfaceArea;

	/** Zone d'influence du projet (isochrone ou buffer). */
	private Geometry influenceArea;

	/** Point central du parc proposé. */
	private Point centerPark;

	/** Surface du parc proposé (m²). */
	private BigDecimal surfacePark;

	/** Nom du projet de simulation. */
	private String name;
	

	/**
	 * Calcule le nombre d'habitants estimé du projet en fonction de la densité,
	 * de la surface et de la surface moyenne des logements.
	 * @return nombre d'habitants estimé, ou {@code null} si les données sont insuffisantes
	 */
	public BigDecimal getProjetPeople() {
		if (densityPerAccommodation!=null && surfaceArea!=null && avgAreaAccommodation!=null && !BigDecimal.ZERO.equals(avgAreaAccommodation)) {
			return densityPerAccommodation.multiply(surfaceArea.divide(avgAreaAccommodation));
		} else {
			return null;
		}
	}

}