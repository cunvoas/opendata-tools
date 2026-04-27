package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.util.Date;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Représente une entité cadastrale correspondant à une commune.
 * Contient les informations principales du cadastre, y compris la géométrie.
 */
@Data
@Entity(name = "cadastre")
@EqualsAndHashCode(of = {"idInsee"})
public class Cadastre {
	@Id
	@Column(name="id_insee", length=5)
	/**
	 * Code INSEE de la commune.
	 */
	private String idInsee;
	@Column(name="nom", length=50)
	/**
	 * Nom de la commune.
	 */
	private String nom;
	@Column(name="created")
	/**
	 * Date de création de l'entrée.
	 */
	private Date created;
	@Column(name="updated")
	/**
	 * Date de mise à jour de l'entrée.
	 */
	private Date updated;
	
	@Column(name="geo_shape", columnDefinition = "geometry(MultiPolygon,4326)")
	/**
	 * Géométrie du cadastre (MultiPolygon).
	 */
    private Geometry geoShape;

	/**
	 * Calcule et retourne le centre géométrique de la commune.
	 *
	 * @return le point central (Point) ou null si la géométrie n'est pas définie
	 */
	public Point getCenter() {
		if (geoShape!=null) {
			return geoShape.getInteriorPoint();
		} else {
			return null;
		}
	}
}