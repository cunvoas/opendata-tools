package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.util.Date;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity(name = "park_area")
@Table(indexes = {
		  @Index(name = "idx_parkarea_point", columnList = "point"),
		  @Index(name = "idx_parkarea_polygon", columnList = "polygon")
		})
public class ParkArea {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_park")
	private Long id;

	@Column(name = "id_parc_jardin")
	private Long idParcEtJardin;

//        @ManyToOne
//        @JoinColumn(name="city_id", nullable=false)
//        private City city;

	@OneToMany(mappedBy = "parkArea")
	private List<ParkEntrance> entrances;

	@Column(name = "name")
	private String name;
	
	@Column(name = "block")
	private String block;

	@Column(name = "description")
	private String description;

	@Column(columnDefinition = "geometry(Point,4326)")
	private Point point;

	/**
	 * isochrone polygon
	 */
	@Column(columnDefinition = "geometry(Polygon,4326)")
	private Polygon polygon;
	
	@DateTimeFormat
	@Column(name="updated")
	private Date updated;
	
	@ManyToOne
	@JoinColumn( name="type_id", nullable = true)
	private ParkType type;
}
