package com.github.cunvoas.geoserviceisochrone.model.admin;

import org.springframework.security.core.GrantedAuthority;

public enum ContributeurRole implements GrantedAuthority{

	ADMINISTRATOR("ADMINISTRATOR"), 
	ASSOCIATION_MANAGER("ASSO_MANAGER"),
	ASSOCIATION_CONSTRIBUTOR("ASSO_CONSTRIB"), 
	JOURNALIST("JOURNALIST"),
	SUPPORT("SUPPORT")
	;
	private final String valeur;

	private ContributeurRole(String valeur) {
		this.valeur = valeur;
	}

	public String getValeur() {
		return this.valeur;
	}
	public String toString() {
		return this.valeur;
	}
	@Override
	public String getAuthority() {
		return this.valeur;
	}

}
