package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.io.Serializable;

import org.locationtech.jts.geom.Point;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Identifiant pour le modèle ProjectSimulatorIsochrone.
 * <p>
 * Composé du ProjectSimulatorId et du point Isochrone.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"idProjectSimulator", "point"})
public class ProjectSimulatorIsochroneId implements Serializable {
	
	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = -6063538891990208907L;

	/**
	 *  Identifiant du projet simulateur.
	 */
    private Long idProjectSimulator;
    
	/**
	 * Point pour la recherche isochrone.
	 */
	private Point point;
	

}