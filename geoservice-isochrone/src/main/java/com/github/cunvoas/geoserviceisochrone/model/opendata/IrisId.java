package com.github.cunvoas.geoserviceisochrone.model.opendata;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Id for Model Iris**.
 */
@Data
@EqualsAndHashCode(of = {"annee", "iris"})
public class IrisId implements Serializable {
	
	/** serialVersionUID. */
	private static final long serialVersionUID = -1616751410241287142L;

	/** Année de la donnée. */
    private Integer annee;
	
	/** Identifiant iris. */
    private String iris;

}
