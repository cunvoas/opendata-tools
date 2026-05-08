package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Identifiant composite pour la classe Filosofil200m (année et identifiant Inspire du carreau).
 */

@Data
@EqualsAndHashCode(of = {"annee", "idInspire"})
public class Filosofil200mId implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -6063538891990208907L;

	/**
	 * Année de la donnée.
	 */
    private Integer annee;
	/**
	 * Identifiant Inspire du carreau de 200 m.
	 */
    private String idInspire;

}