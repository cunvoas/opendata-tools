package com.github.cunvoas.geoserviceisochrone.model.opendata;

import org.locationtech.jts.geom.Point;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.NumberFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Data
@Entity(name = "parc_jardin")
public class ParcEtJardin {

	@Id
	@Column(name = "identifiant")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_mel_park")
	private Long id;

	@Column(name = "nom_parc", length = 50)
	private String name;

	@Column(name = "quartier", length = 50)
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
	
	@Column(name = "adresse", length = 100)
	private String adresse;

	@NumberFormat(pattern = "#,##0")
	@Column(name = "surface")
	private Double surface;
	
	@Column(name = "coordonnee")
	private Point coordonnee;
	

	@ManyToOne
	@JoinColumn(name="id_city", nullable=true)
	private City commune;


/*
Identifiant;Nom d'usage;Quartier ou commune associée;Hiérarchie;Type;Sous-Type;Ouvert au public;Etat d'ouverture;Horaire d'ouverture estivale;Horaire d'ouverture hivernale;Aire de Jeux pour enfants;Nom (liste);Adresse ou Voie;Surface en m²;Description;Accès en métro à 500m;Autres accès;Année d'ouverture;X (L93);Y (L93);Longitude;Latitude;coord_geo
5.0;Parc des Dondaines;Fives;1.0;Espace vert public;Parc;Oui;Permanente;;;Non;Dondaines (Parc des);16 Rue Eugène Jacquet;45759.6606445312;;"Caulier, Ligne 1 ; Gare Lille Europe, Ligne 2";;;705643.2762;7059964.5414;3.079635;50.636618;50.636618, 3.079635
16.0;Cimetière d'Hellemmes;Hellemmes;1.0;Cimetière;Cimetière;Oui;Restreinte;Avril à Octobre : 9h00 à 17h45;Novembre à Mars : 9h00 à 16h45;Non;Hellemmes (Cimetière d');Rue Roger Salengro;47179.3505859375;;;;;708461.9466;7058864.541;3.119386;50.626714;50.626714, 3.119386


 */
	
	
}
