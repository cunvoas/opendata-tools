package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Model Cadastre.
 */
@Data
@EqualsAndHashCode(of = {"idInsee", "idInseeProche"})
public class CadastreProcheId implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -6063538891990200907L;
	
	private String idInsee;
	
	private String idInseeProche;
}
