package com.github.cunvoas.geoserviceisochrone.model.analytics;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Identifiant composite pour le modèle {@link StatsSurface}.
 * <p>
 * Composé de l'année de la donnée, de la borne inférieure et supérieure de la tranche de surface.
 * </p>
 */
@Data
@EqualsAndHashCode(of = {"annee", "surfaceMin", "surfaceMax"})
public class StatsSurfaceId {

	/** Année de la donnée. */
	private Integer annee;
	/** Borne inférieure de la tranche de surface (m²). */
	private Integer surfaceMin;
	/** Borne supérieure de la tranche de surface (m²). */
	private Integer surfaceMax;
	
}