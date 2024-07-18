package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"annee", "idInspire"})
public class InseeCarre200mComputedId implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -6063538891990208907L;

	/**
	 * Année de la donnée.
	 */
    private Integer annee;
	
	/**
	 *  Identifiant Inspire du carreau de 200 m.
	 */
    private String idInspire;

}
