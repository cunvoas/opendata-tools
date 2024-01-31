package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.util.Date;

import org.locationtech.jts.geom.Geometry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "cadastre")
public class Cadastre {
	@Id
	@Column(name="id_insee", length=5)
	private String idInsee;
	@Column(name="nom", length=50)
	private String nom;
	@Column(name="created")
	private Date created;
	@Column(name="updated")
	private Date updated;
	
	@Column(name="geo_shape", columnDefinition = "geometry(MultiPolygon,4326)")
    private Geometry geoShape;

}
