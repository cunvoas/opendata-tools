package com.github.cunvoas.geoserviceisochrone.model.admin;

import org.springframework.security.core.GrantedAuthority;

/**
 * Enumération des rôles possibles pour un contributeur.
 * Permet de gérer les droits d'accès et les autorisations sur la plateforme.
 */
public enum ContributeurRole implements GrantedAuthority{

	/**
	 * Administrateur de la plateforme.
	 */
	ADMINISTRATOR("ADMINISTRATOR"), 
	/**
	 * Gestionnaire d'association.
	 */
	ASSOCIATION_MANAGER("ASSO_MANAGER"),
	/**
	 * Contributeur d'association.
	 */
	ASSOCIATION_CONSTRIBUTOR("ASSO_CONSTRIB"), 
	/**
	 * Journaliste.
	 */
	JOURNALIST("JOURNALIST"),
	/**
	 * Support technique.
	 */
	SUPPORT("SUPPORT")
	;
	
	/**
	 * Valeur du rôle.
	 */
	private final String valeur;

	/**
	 * Constructeur.
	 * @param valeur valeur du rôle
	 */
	private ContributeurRole(String valeur) {
		this.valeur = valeur;
	}

	/**
	 * Retourne la valeur du rôle.
	 * @return valeur du rôle
	 */
	public String getValeur() {
		return this.valeur;
	}
	
	/**
	 * Retourne la valeur du rôle sous forme de chaîne.
	 * @return valeur du rôle
	 */
	public String toString() {
		return this.valeur;
	}
	
	/**
	 * Retourne l'autorité associée au rôle (pour Spring Security).
	 * @return autorité du rôle
	 */
	@Override
	public String getAuthority() {
		return this.valeur;
	}

}