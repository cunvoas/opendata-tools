package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Identifiant pour le modèle InseeCarre200mComputed.
 * <p>
 * Composé de l'année de la donnée et de l'identifiant Inspire du carreau de 200 m.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"annee", "idInspire", "idProjectSimulator"})
public class ProjectSimulatorWorkId implements Serializable {
	
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
	
	/**
	 *  Identifiant du projet simulateur.
	 */
    private Long idProjectSimulator;

}