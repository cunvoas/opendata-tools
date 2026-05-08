package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Identifiant composite pour la relation de proximité cadastrale.
 * Utilisé pour représenter la clé primaire composée de deux codes INSEE.
 */
@Data
@EqualsAndHashCode(of = {"idInsee", "idInseeProche"})
public class CadastreProcheId implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -6063538891990200907L;
	
	/**
	 * Code INSEE de la commune principale.
	 */
	private String idInsee;
	/**
	 * Code INSEE de la commune proche (voisine).
	 */
	private String idInseeProche;
}