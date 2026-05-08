package com.github.cunvoas.geoserviceisochrone.model.opendata;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Représente un carreau INSEE de 200m avec uniquement les informations de forme géométrique.
 * Permet d'associer un identifiant Inspire, la géométrie, le code INSEE et la présence de population.
 */
@Data
@Entity(name = "carre200onlyshape")
@Table(name = "carre200onlyshape",
indexes = {
   @Index(name = "idx_carre200onlyshape_idcarre", columnList="id_inspire", unique = false)
   }
)
@EqualsAndHashCode(of = {"idInspire"})
public class InseeCarre200mOnlyShape {
	
	@Id
	@Column(name="id_inspire", length=30)
	/**
	 * Identifiant Inspire du carreau de 200m.
	 */
	private String idInspire;
	
	@Column(name="id_carre_1km",length=31)
	/**
	 * Identifiant du carreau de 1km auquel appartient ce carreau de 200m.
	 */
	private String idCarre1km;
	
	@Column(name="geo_point_2d", columnDefinition = "geometry(Point,4326)")
	/**
	 * Coordonnée centrale du carreau (Point).
	 */
	private Point geoPoint2d;
	
	@Column(name="geo_shape", columnDefinition = "geometry(Polygon,4326)")
	/**
	 * Géométrie du carreau (Polygon).
	 */
	private Polygon geoShape;

	@Column(name="code_insee",length=20)
	/**
	 * Code INSEE de la commune associée.
	 */
	private String codeInsee;
	
	@Column(name="avec_pop")
	/**
	 * Indique si le carreau contient de la population (true/false).
	 */
	private Boolean withPop;
	
}