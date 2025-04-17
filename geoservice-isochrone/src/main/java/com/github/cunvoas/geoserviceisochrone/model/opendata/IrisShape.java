package com.github.cunvoas.geoserviceisochrone.model.opendata;

	
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.format.annotation.NumberFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Fond IRIS.
 * @see https://geoservices.ign.fr/contoursiris
 * @see https://geoservices.ign.fr/irisge
 * @see https://www.geoportail.gouv.fr/carte
 */
@Data
@Entity(name = "iris_shape")
@EqualsAndHashCode(of = {"fid"})
public class IrisShape {

	/** fid.*/
	@Column(name = "fid")
	private Integer fid;

	/** cleabs.*/
	@Column(name = "cleabs", length = 24)
	private String cleabs;

	/** code_insee.*/
	@Column(name = "code_insee", length = 5)
	private String codeInsee;

	/** nom_commune.*/
	@Column(name = "nom_commune", length = 50)
	private String commune;

	/** iris.*/
	@Column(name = "iris", length = 4)
	private String iris4;

	/** code_iris.*/
	@Id
	@Column(name = "code_iris", length = 9)
	private String iris;

	/** nom_iris.*/
	@Column(name = "nom_iris", length = 100)
	private String nomIris;

	/** type_iris.*/
	@Column(name = "type_iris", length = 1)
	private String typeIris;

	/** centorid de l'iris (calculé).*/
	@Column(name = "coordonnee")
	private Point coordonnee;

	/** forme de l'iris.*/
	@Column(name="contour")
	private Geometry contour;

	/** surface de l'iris.*/
	//@NumberFormat(pattern = "#,##0.0")
	@Column(name="surface")
	private Double surface;
	
	
}
