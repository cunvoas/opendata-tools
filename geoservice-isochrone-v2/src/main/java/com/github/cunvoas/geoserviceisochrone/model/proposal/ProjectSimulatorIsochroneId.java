package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.io.Serializable;

import org.locationtech.jts.geom.Point;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Identifiant composite pour le modèle {@link ProjectSimulatorIsochone}.
 * <p>
 * Composé de l'identifiant du simulateur de projet et du point de recherche isochrone.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"idProjectSimulator", "point"})
public class ProjectSimulatorIsochroneId implements Serializable {

	/** Numéro de version de sérialisation. */
	private static final long serialVersionUID = -6063538891990208907L;

	/** Identifiant du projet simulateur. */
    private Long idProjectSimulator;
    
	/** Point pour la recherche isochrone. */
	private Point point;
	
}