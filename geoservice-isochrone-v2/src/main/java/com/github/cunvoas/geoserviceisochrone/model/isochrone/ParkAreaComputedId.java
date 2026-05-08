package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Identifiant composite pour le modèle ParkAreaComputed.
 * Permet d'identifier de façon unique une zone de parc calculée pour une année donnée.
 */
@Data
@EqualsAndHashCode(of = {"annee", "id"})
public class ParkAreaComputedId implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 7354082390895135525L;

	/** Identifiant unique de la zone de parc. */
    private Long id;
    
	/** Année de la donnée. */
    private Integer annee;

}