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
 * Entité représentant une association partenaire ou actrice du projet.
 * <p>
 * Contient les informations principales d'une association :
 * <ul>
 *   <li>Nom, email, logo, description</li>
 *   <li>Site web, page HelloAsso</li>
 * </ul>
 * Utilisée pour l'affichage, la gestion des droits et la valorisation des partenaires.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"id"})
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Entity(name = "adm_asso")
public class Association {

	/**
	 * Identifiant unique de l'association (clé primaire).
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
	 * Nom officiel de l'association.
	 */
	@ToString.Include
	@Column(nullable = false, length = 200)
	private String nom;

	/**
	 * Adresse email de contact de l'association.
	 */
	@Column(length = 200)
	private String email;

	/**
	 * URL du logo de l'association (image publique).
	 */
	@Column(length = 500)
	private String logo;

	/**
	 * Description courte de l'association (présentation, objet, etc.).
	 */
	@Column(length = 1000)
	private String description;

	/**
	 * URL du site web officiel de l'association.
	 */
	@Column(name = "site_url", length = 500)
	private String siteUrl;

	/**
	 * URL de la page HelloAsso de l'association (financement participatif).
	 */
	@Column(name = "hello_asso_url", length = 500)
	private String helloAssoUrl;
	
}