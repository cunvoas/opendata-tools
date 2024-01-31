package com.github.cunvoas.geoserviceisochrone.model.opendata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * Grille communale de densité.
 * @see https://www.insee.fr/fr/information/6439600
 */
@Data
@Entity(name = "insee_densite_city")
public class InseeDensiteCommune {
	
	public static final String header = "CODGEO,LIBGEO,DENS,LIBDENS,PMUN20,P1,P2,P3,P4,P5,P6,P7";

	
	@Id
	@Column(name = "codgeo", length = 5)
	private String codgeo;

	@Column(name = "libgeo", length = 50)
	private String name;
	

	@Column(name = "dens")
	private String codeDensite;
	@Column(name = "libdens", length = 50)
	private String libDensite;

	@Column(name = "pmun20")
	private Integer population;

	@Column(name = "p1")
	private Float pcentZon1;
	@Column(name = "p2")
	private Float pcentZon2;
	@Column(name = "p3")
	private Float pcentZon3;
	@Column(name = "p4")
	private Float pcentZon4;
	@Column(name = "p5")
	private Float pcentZon5;
	@Column(name = "p6")
	private Float pcentZon6;
	@Column(name = "p7")
	private Float pcentZon7;

}
