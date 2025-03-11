package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.util.Date;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

/**
 * Model ParkArea.
 */
@Data
@ToString(onlyExplicitlyIncluded = true)
@Entity(name = "park_area")
@Table(indexes = {
		  @Index(name = "idx_parkarea_point", columnList = "point"),
		  @Index(name = "idx_parkarea_polygon", columnList = "polygon")
		})
public class ParkArea {

	@Id
	@ToString.Include
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_park")
    @SequenceGenerator(
    		name="seq_park",
    		allocationSize=1,
    		initialValue = 1
    	)
	private Long id;

	@Column(name = "id_parc_jardin")
	private Long idParcEtJardin;

//        @ManyToOne
//        @JoinColumn(name="city_id", nullable=false)
//        private City city;

	@OneToMany(mappedBy = "parkArea", fetch = FetchType.EAGER)
//	@OneToMany(mappedBy = "parkArea")
	private List<ParkEntrance> entrances;

	@ToString.Include
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
	
    @ToString.Include
	@DateTimeFormat
	@Column(name="updated")
	private Date updated;
	
	@ManyToOne
	@JoinColumn( name="type_id", nullable = true)
	private ParkType type;

	@Column(name = "oms_custom")
	private Boolean omsCustom;

	@Column(name = "to_compute")
	private Boolean toCompute;
	
}
