package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.util.Date;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.format.annotation.NumberFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Modèle représentant un parc ou jardin issu de l'opendata.
 * <p>
 * Cette entité contient les informations détaillées sur un parc ou jardin :
 * <ul>
 *   <li>Identifiant, nom, quartier, hiérarchie, type et sous-type</li>
 *   <li>État d'ouverture, aire de jeux, adresse</li>
 *   <li>Surface, coordonnées, contour géométrique</li>
 *   <li>Dates d'ouverture/fermeture, type, source, statut</li>
 *   <li>Référence à la commune (City)</li>
 * </ul>
 * Fournit également des méthodes utilitaires pour obtenir la latitude/longitude.
 */
@Data
@Entity(name = "parc_jardin")
@EqualsAndHashCode(of = {"id"})
@ToString(onlyExplicitlyIncluded = true)
public class ParcEtJardin {

	@Id
	@ToString.Include
	@Column(name = "identifiant")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_mel_park")
    @SequenceGenerator(
    		name="seq_mel_park",
    		allocationSize=1,
    		initialValue = 1
    	)
	private Long id;

	@ToString.Include
	@Column(name = "nom_parc", length = 200)
	private String name;

	@Column(name = "quartier", length = 100)
	private String quartier;

	@Column(name = "hierarchie", length = 50)
	private String hierarchie;

	@Column(name = "type", length = 50)
	private String type;

	@Column(name = "sous_type", length = 50)
	private String sousType;
	@Column(name = "etat_ouverture", length = 50)
	private String etatOuverture;

	@Column(name = "aire_jeux", length = 50)
	private String aireJeux;
	
	@Column(name = "adresse", length = 200)
	private String adresse;

	@NumberFormat(pattern = "#,##0.0")
	@Column(name = "surface")
	private Double surface;

	@NumberFormat(pattern = "#,##0.0")
	@Column(name = "surface_contour")
	private Double surfaceContour;
	
	@Column(name = "coordonnee")
	private Point coordonnee;

	@Column(name="contour")
	private Geometry contour;
	
	// date 
	@Column(name="date_debut")
	private Date dateDebut;
	
	@Column(name="date_fin")
	private Date dateFin;
	
	@Column(name="date_suppr")
	private Date dateSuppression;
	
	@Column( name="type_id", nullable = true )
	private Long typeId;

	@Column(name = "oms_custom")
	private Boolean omsCustom;
	
	public String getLat() {
		if (coordonnee!=null) {
			return  String.valueOf(coordonnee.getCoordinate().y);
		}
		return "";
	}
	public String getLng() {
		if (coordonnee!=null) {
			return  String.valueOf(coordonnee.getCoordinate().x);
		}
		return "";
	}
	public String getLatLng() {
		if (coordonnee!=null) {
			return  String.valueOf(coordonnee.getCoordinate().y)+","+String.valueOf(coordonnee.getCoordinate().x);
		}
		return "";
	}
	
	@ManyToOne
	@JoinColumn(name="id_city", nullable=true)
	private City commune;
	
	@Column(name="source", length=15)
	private ParcSourceEnum source = ParcSourceEnum.OPENDATA;
	
	@Column(name="status", length=15)
	private ParcStatusEnum status = ParcStatusEnum.TO_QUALIFY;
	

}