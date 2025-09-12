package com.github.cunvoas.geoserviceisochrone.model.opendata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Représente la grille communale de densité INSEE.
 * Permet de caractériser la densité de population d'une commune et la répartition de sa population par zones.
 * @see https://www.insee.fr/fr/information/6439600
 */
@Data
@Entity(name = "insee_densite_city")
@EqualsAndHashCode(of = {"codgeo"})
public class InseeDensiteCommune {
	
	public static final String header = "CODGEO,LIBGEO,DENS,LIBDENS,PMUN20,P1,P2,P3,P4,P5,P6,P7";

	@Id
	@Column(name = "codgeo", length = 5)
	/**
	 * Code géographique officiel de la commune (INSEE).
	 */
	private String codgeo;

	@Column(name = "libgeo", length = 50)
	/**
	 * Nom de la commune.
	 */
	private String name;
	

	@Column(name = "dens")
	/**
	 * Code de densité INSEE.
	 */
	private String codeDensite;
	@Column(name = "libdens", length = 50)
	/**
	 * Libellé de la densité INSEE.
	 */
	private String libDensite;

	@Column(name = "pmun20")
	/**
	 * Population municipale totale.
	 */
	private Integer population;

	@Column(name = "p1")
	/**
	 * Pourcentage de la population en zone 1.
	 */
	private Float pcentZon1;
	@Column(name = "p2")
	/**
	 * Pourcentage de la population en zone 2.
	 */
	private Float pcentZon2;
	@Column(name = "p3")
	/**
	 * Pourcentage de la population en zone 3.
	 */
	private Float pcentZon3;
	@Column(name = "p4")
	/**
	 * Pourcentage de la population en zone 4.
	 */
	private Float pcentZon4;
	@Column(name = "p5")
	/**
	 * Pourcentage de la population en zone 5.
	 */
	private Float pcentZon5;
	@Column(name = "p6")
	/**
	 * Pourcentage de la population en zone 6.
	 */
	private Float pcentZon6;
	@Column(name = "p7")
	/**
	 * Pourcentage de la population en zone 7.
	 */
	private Float pcentZon7;

}