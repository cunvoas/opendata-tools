package com.github.cunvoas.geoserviceisochrone.model.opendata;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity(name = "carre200shape")
@Table(name = "carre200shape",
indexes = {
   @Index(name = "idx_carre200shape_idcarre", columnList="id_carre_hab", unique = false)
   }
)
@Deprecated
public class InseeCarre200mShape {
	
	@Id
	@Column(name="id_inspire", length=30)
	private String idInspire;
	
	@Column(name="id_carre_hab",length=21)
	private String idCarreHab;
	
	@Column(name="id_rect_hab",length=25)
	private String idRectHab;

	// nb habitant au carre
	@Column(name="nb_hab_carre")
	private Double nbHabCarre;
	
	//nb carre habite dans rectangle
	@Column(name="nb_carre_hab")
	private Double nbCarreHabRect;

	@Column(name="geo_point_2d", columnDefinition = "geometry(Point,4326)")
	private Point geoPoint2d;
	
	@Column(name="geo_shape", columnDefinition = "geometry(Polygon,4326)")
	private Polygon geoShape;

	@Column(name="code",length=100)
	private String code;

	@Column(name="epci",length=100)
	private String epci;

	@Column(name="commune",length=100)
	private String commune;

	@Column(name="region",length=100)
	private String region;

	@Column(name="departement",length=100)
	private String departement;

	
	
}
