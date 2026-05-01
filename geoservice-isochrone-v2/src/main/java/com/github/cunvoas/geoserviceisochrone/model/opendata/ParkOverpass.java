package com.github.cunvoas.geoserviceisochrone.model.opendata;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

import java.util.Map;

@Data
@EqualsAndHashCode(of = {"id"})
@Entity(name = "park_overpass")
@Table(name = "park_overpass",
indexes = {
   @Index(name = "idx_park_overpass_name", columnList="name", unique = false)
   }
)
public class ParkOverpass {

	@Id
	@Column(name = "id")
	private Long id;
	private String type;
	
	private Point cornerSouthWest;
	private Point cornerNorthEast;
	private Geometry shape;
	
	@Column(length = 200)
	private String name;
	@Column(length = 200)
	private String source;
	
	// "access": "yes",
	private Boolean accesible;
	
	// "opening_hours": "24/7",
	@Column(length = 500)
	private String openingHours;
	
//	"operator": "Municipalité de ...",
	@Column(length = 500)
	private String operatorName;
	
	@Column(length = 10)
	private String zipCode;
	
	/**
     * Stocke les tags OSM dans une colonne jsonb PostgreSQL.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> tags;
	

}