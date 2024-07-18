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
	private String idInspire;
	
	@Column(name="id_carre_1km",length=31)
	private String idCarre1km;
	
	@Column(name="geo_point_2d", columnDefinition = "geometry(Point,4326)")
	private Point geoPoint2d;
	
	@Column(name="geo_shape", columnDefinition = "geometry(Polygon,4326)")
	private Polygon geoShape;

	@Column(name="code_insee",length=100)
	private String codeInsee;
	
	@Column(name="avec_pop")
	private Boolean withPop;
	
}
