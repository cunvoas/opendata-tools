package com.github.cunvoas.geoserviceisochrone.model.opendata;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

/**
 * structure for initial data from prefecture.
 * Must be qualified to be plenty usable
 */
@Data
@Entity(name = "parc_prefecture")
@Table(name = "parc_prefecture",
indexes = {
   @Index(name = "idx_parc_prefecture_nom_pref", columnList="nom_pref", unique = false)
   }
)
public class ParcPrefecture {

	@Id
	@Column(name = "identifiant")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_park_pref")
	private Long id;
	
	// original name from prefecture
	@Column(name = "nom_pref", length = 100)
	private String namePrefecture;
	
	// original reversed from prefecture
	@Column(name = "area")
	private Polygon area;



	// updated name after qualification
	@Column(name = "nom_parc", length = 100)
	private String name;
	
	// computed from Polygon
	@Column(name="surface")
	private Long surface;
	

	// computed with Centroid
	@Column(name = "point")
	private Point point;

	// computed with distance computation
	@ManyToOne
	@JoinColumn(name="id_city", nullable=true)
	private City commune;

	// computed with distance computation
	@ManyToOne
	@JoinColumn(name="id_parc", nullable=true)
	private ParcEtJardin parcEtJardin;
	
	// set by user after check
	@Column(name="processed")
	private Boolean processed=Boolean.FALSE;
	
	
	
	// quick and dirty hotfix
	@Transient
	private Long idRegion;
	@Transient
	private Long idCommunauteDeCommunes;
	@Transient
	private Long idCommune;
	@Transient
	private Long idPark;
	
}
