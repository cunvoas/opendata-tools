package com.github.cunvoas.geoserviceisochrone.model.isochrone;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Id for Model ParkAreaComputed.
 */
@Data
@EqualsAndHashCode(of = {"annee", "id"})
public class ParkAreaComputedId implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	
	private static final long serialVersionUID = 7354082390895135525L;

	/**
	 *  id du park area.
	 */
    private Long id;
    
	/**
	 * Année de la donnée.
	 */
    private Integer annee;

}
