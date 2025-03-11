package com.github.cunvoas.geoserviceisochrone.model.opendata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Model Laposte.
 */
@Data
@Entity(name = "laposte")
@EqualsAndHashCode(of = {"idInsee"})
public class Laposte {
	
	@Id
	@Column(name = "code_commune_insee", length = 5)
	private String idInsee;

	@Column(name = "nom_commune", length = 50)
	private String name;

	@Column(name = "code_postal", length = 5)
	private String postalCode;

	@Column(name = "ligne_5", length = 50)
	private String lieuDit;

	@Column(name = "libelle_acheminement", length = 50)
	private String libelleAcheminement;

	@Column(name = "coordonnees_gps", length = 50)
	private String coordonneesGps;


}
