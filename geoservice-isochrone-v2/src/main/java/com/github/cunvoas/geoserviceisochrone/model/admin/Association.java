package com.github.cunvoas.geoserviceisochrone.model.admin;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Représente une association.
 * Contient les informations principales d'une association (nom, email, logo, description, site web, etc.).
 */
@Data
@EqualsAndHashCode(of = {"id"})
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Entity(name = "adm_asso")
public class Association {

	/**
	 * Identifiant unique de l'association.
	 */
	@Id
	@ToString.Include
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_asso")
    @SequenceGenerator(
    		name="seq_asso",
    		allocationSize=1,
    		initialValue = 1
    	)
	private Long id;

	/**
	 * Nom de l'association.
	 */
	@ToString.Include
	private String nom;

	/**
	 * Adresse email de contact de l'association.
	 */
	private String email;

	/**
	 * URL du logo de l'association.
	 */
	private String logo;

	/**
	 * Description de l'association.
	 */
	private String description;

	/**
	 * URL du site web de l'association.
	 */
	@Column(name = "site_url")
	private String siteUrl;

	/**
	 * URL HelloAsso de l'association.
	 */
	@Column(name = "hello_asso_url")
	private String helloAssoUrl;
	
	
}