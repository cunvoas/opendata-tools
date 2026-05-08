package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entité représentant un parc ou espace vert importé depuis l'API Overpass (OpenStreetMap).
 * <p>
 * Contient les informations géographiques (forme, bounding box), descriptives (nom, source,
 * horaires, opérateur) et administratives (code postal, code INSEE) ainsi que les tags OSM bruts.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"id"})
@Entity(name = "park_overpass")
@Table(name = "park_overpass",
indexes = {
   @Index(name = "idx_park_overpass_name", columnList="name", unique = false)
   }
)
public class ParkOverpass {

	/** Identifiant OSM unique de l'élément (node, way ou relation). */
	@Id
	@Column(name = "id")
	private Long id;

	/** Type OSM de l'élément (node, way, relation). */
	private String type;

	/** Coin sud-ouest de la bounding box de l'élément. */
	private Point cornerSouthWest;

	/** Coin nord-est de la bounding box de l'élément. */
	private Point cornerNorthEast;

	/** Géométrie de l'espace vert (Polygon, MultiPolygon, etc.). */
	private Geometry shape;

	/** Nom de l'espace vert (tag OSM "name"). */
	@Column(length = 200)
	private String name;

	/** Source de la donnée OSM (tag OSM "source"). */
	@Column(length = 200)
	private String source;

	/** Indique si l'espace vert est accessible au public (tag OSM "access"). */
	// "access": "yes"
	private Boolean accesible;

	/** Horaires d'ouverture (tag OSM "opening_hours", ex : "24/7"). */
	@Column(length = 500)
	private String openingHours;

	/** Nom de l'exploitant ou gestionnaire (tag OSM "operator"). */
	@Column(length = 500)
	private String operatorName;

	/** Code postal associé à l'espace vert. */
	@Column(length = 10)
	private String zipCode;

	/**
     * Stocke les tags OSM dans une colonne jsonb PostgreSQL.
     * Permet d'accéder à tous les attributs supplémentaires de l'élément OSM.
     */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private Map<String, Object> tags;
    

	/** Code INSEE de la commune où se trouve l'espace vert. */
	@Column(length = 5)
	private String insee;

	/** Surface de l'espace vert. */
	@Column(length = 5)
	private Double surface;


    
}