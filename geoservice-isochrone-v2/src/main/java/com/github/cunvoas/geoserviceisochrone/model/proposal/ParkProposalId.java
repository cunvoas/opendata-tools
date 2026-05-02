package com.github.cunvoas.geoserviceisochrone.model.proposal;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Identifiant composite pour le modèle {@link ParkProposal}.
 * <p>
 * Composé de l'année, de l'identifiant Inspire du carreau de 200m et de l'identifiant de la proposition.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"annee", "idInspire", "idMeta"})
public class ParkProposalId implements Serializable {

	/** Numéro de version de sérialisation. */
	private static final long serialVersionUID = -6063538891990208907L;

	/** Année de la donnée. */
    private Integer annee;
	
	/** Identifiant Inspire du carreau de 200 m. */
    private String idInspire;

	/** Identifiant de la proposition (ParkProposalMeta). */
	private Long idMeta;
}