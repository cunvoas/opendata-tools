package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.math.BigDecimal;

/**
 * Évaluation fine de la végétation d'une zone à partir des indices Sentinel-2
 * calculés par {@link ClientCopernicusNdvi} : SAVI, NDVI, NDRE et GNDVI.
 *
 * <p>L'évaluation tolère les données partielles (bande Red-Edge non extraite,
 * polygone d'un seul pixel Sentinel-2 sans écart-type ni bornes) : les indices
 * disponibles sont repondérés, les analyses impossibles sont neutralisées et un
 * niveau de confiance est retourné.</p>
 *
 * <p>Étapes :</p>
 * <ol>
 *   <li>recouvrement du sol — SAVI prioritaire, NDVI en repli avec un seuil relevé ;</li>
 *   <li>vigueur — pondération dynamique des indices présents ;</li>
 *   <li>diagnostic physiologique — NDVI élevé avec NDRE/GNDVI bas ;</li>
 *   <li>hétérogénéité — seulement si un écart-type est fourni ;</li>
 *   <li>anomalie locale — seulement si min et max sont fournis ;</li>
 *   <li>score sur 100, pénalisé par l'irrégularité quand elle est mesurable.</li>
 * </ol>
 */
public final class VegetationEvaluator {

	/** Seuil sol nu / bâti quand le SAVI est disponible. */
	private static final double SAVI_SEUIL_MINERAL = 0.18;
	/** Seuil sol nu / bâti quand seul le NDVI est disponible (compense l'effet du sol). */
	private static final double NDVI_SEUIL_MINERAL = 0.22;
	/** Seuil couvert dense quand le SAVI est disponible. */
	private static final double SAVI_SEUIL_DENSE = 0.42;
	/** Seuil couvert dense quand seul le NDVI est disponible. */
	private static final double NDVI_SEUIL_DENSE = 0.55;

	/** NDVI de canopée forte, susceptible de masquer un stress. */
	private static final double NDVI_FORTE_BIOMASSE = 0.60;
	private static final double NDRE_STRESS = 0.32;
	private static final double GNDVI_STRESS = 0.48;

	private static final double STD_TRES_HETEROGENE = 0.16;
	private static final double STD_HETEROGENE = 0.09;

	/** Amplitude (max - min) déclenchant l'alerte locale. */
	private static final double AMPLITUDE_ALERTE = 0.50;
	/** Poids de la pénalité d'écart-type dans le score. */
	private static final double PENALITE_STD = 20.0;

	private VegetationEvaluator() {
	}

	/**
	 * Résultat de l'évaluation d'une zone.
	 *
	 * @param recouvrement             état du couvert (minéral, clairsemé, dense)
	 * @param scoreSante               score sur 100 arrondi au dixième, {@code null} si inévaluable
	 * @param confiance                niveau de confiance lié aux indices disponibles
	 * @param diagnosticPhysiologique  diagnostic de stress, éventuellement limité
	 * @param homogeneite              hétérogénéité, {@code "Non calculable"} sans écart-type
	 * @param anomalieLocaleDetectee   {@code null} si min/max indisponibles
	 */
	public record VegetationEvaluation(
			String recouvrement,
			Double scoreSante,
			String confiance,
			String diagnosticPhysiologique,
			String homogeneite,
			Boolean anomalieLocaleDetectee) {
	}

	/**
	 * Évalue la végétation avec dégradation gracieuse.
	 *
	 * @param stats statistiques Copernicus (champs et indices potentiellement absents)
	 * @return l'évaluation, jamais {@code null}
	 */
	public static VegetationEvaluation evaluate(NdviStatistics stats) {
		NdviStatistics.IndexStatistics savi = stats == null ? null : stats.savi();
		NdviStatistics.IndexStatistics ndvi = stats == null ? null : stats.ndvi();
		NdviStatistics.IndexStatistics ndre = stats == null ? null : stats.ndre();
		NdviStatistics.IndexStatistics gndvi = stats == null ? null : stats.gndvi();

		boolean hasSavi = hasMean(savi);
		boolean hasNdvi = hasMean(ndvi);
		boolean hasNdre = hasMean(ndre);
		boolean hasGndvi = hasMean(gndvi);

		if (!hasSavi && !hasNdvi) {
			return new VegetationEvaluation(
					"Données insuffisantes (SAVI et NDVI manquants)",
					null,
					"Nulle",
					"Évaluation impossible",
					"Non applicable",
					null);
		}

		int nbIndices = (hasSavi ? 1 : 0) + (hasNdvi ? 1 : 0) + (hasNdre ? 1 : 0) + (hasGndvi ? 1 : 0);
		String confiance = nbIndices == 4 ? "Élevée" : nbIndices >= 2 ? "Moyenne" : "Faible";

		// 1. Indice de référence pour le recouvrement : SAVI prioritaire, NDVI en repli
		double refCouvert = hasSavi ? mean(savi) : mean(ndvi);
		double seuilMineral = hasSavi ? SAVI_SEUIL_MINERAL : NDVI_SEUIL_MINERAL;
		double seuilDense = hasSavi ? SAVI_SEUIL_DENSE : NDVI_SEUIL_DENSE;

		if (refCouvert < seuilMineral) {
			return new VegetationEvaluation(
					"Minéral, bâti, ombre dense ou sol nu",
					0.0,
					confiance,
					"Absence de couvert végétal actif",
					"Non applicable",
					null);
		}

		// 2. Vigueur : pondération dynamique des indices réellement présents
		String recouvrement;
		double sommePonderee = 0.0;
		double poidsTotal = 0.0;

		if (refCouvert < seuilDense) {
			recouvrement = "Couvert faible / Pelouse ouverte ou arbustif jeune";
			if (hasSavi) {
				sommePonderee += mean(savi) * 0.65;
				poidsTotal += 0.65;
			}
			if (hasNdvi) {
				sommePonderee += mean(ndvi) * 0.35;
				poidsTotal += 0.35;
			}
		} else {
			recouvrement = "Couvert dense / Strate arborée ou parc mature";
			if (hasNdre) {
				sommePonderee += mean(ndre) * 0.50;
				poidsTotal += 0.50;
			}
			if (hasGndvi) {
				sommePonderee += mean(gndvi) * 0.30;
				poidsTotal += 0.30;
			}
			if (hasNdvi) {
				sommePonderee += mean(ndvi) * 0.20;
				poidsTotal += 0.20;
			}
			// SAVI seul (ni NDRE, ni GNDVI, ni NDVI) : il porte toute la vigueur
			if (poidsTotal == 0.0 && hasSavi) {
				sommePonderee = mean(savi);
				poidsTotal = 1.0;
			}
		}

		double indiceVigueur = poidsTotal == 0.0 ? refCouvert : sommePonderee / poidsTotal;

		// 3. Diagnostic physiologique
		String diagnostic = "Normal";
		if (hasNdvi && mean(ndvi) > NDVI_FORTE_BIOMASSE) {
			if (hasNdre && mean(ndre) < NDRE_STRESS) {
				diagnostic = "Alerte : Déficit chlorophyllien canopée (NDRE bas)";
			} else if (hasGndvi && mean(gndvi) < GNDVI_STRESS) {
				diagnostic = "Alerte : Stress hydrique ou azoté (GNDVI bas)";
			} else if (!hasNdre && !hasGndvi) {
				diagnostic = "Diagnostic limité (NDRE et GNDVI manquants pour détecter un stress précoce)";
			}
		}

		// 4. Hétérogénéité : seulement si la dispersion est mesurable
		// (un polygone d'un seul pixel renvoie std = 0, ce qui ne veut pas dire « homogène »)
		boolean dispersionMesurable = dispersionMesurable(stats);
		Double refStd = dispersionMesurable ? firstStd(savi, ndvi) : null;
		String homogeneite = dispersionMesurable
				? "Non calculable (écart-type manquant)"
				: "Non calculable (pixels insuffisants)";
		double penalite = 0.0;
		if (refStd != null) {
			if (refStd > STD_TRES_HETEROGENE) {
				homogeneite = "Très hétérogène";
			} else if (refStd > STD_HETEROGENE) {
				homogeneite = "Modérément hétérogène";
			} else {
				homogeneite = "Homogène";
			}
			penalite = refStd * PENALITE_STD;
		}

		// 5. Anomalie locale : seulement si min et max sont fournis sur plusieurs pixels
		Boolean anomalie = dispersionMesurable ? amplitude(savi, ndvi) : null;

		// 6. Score synthétique
		double score = clamp((indiceVigueur * 100.0) - penalite, 0.0, 100.0);

		return new VegetationEvaluation(recouvrement, arrondir(score), confiance, diagnostic, homogeneite, anomalie);
	}

	private static boolean dispersionMesurable(NdviStatistics stats) {
		// sampleCount inconnu : on ne bloque pas l'analyse
		return stats == null || stats.sampleCount() == null || stats.sampleCount() >= 2;
	}

	private static boolean hasMean(NdviStatistics.IndexStatistics index) {
		return index != null && index.mean() != null;
	}

	private static double mean(NdviStatistics.IndexStatistics index) {
		return index.mean().doubleValue();
	}

	private static Double firstStd(NdviStatistics.IndexStatistics savi, NdviStatistics.IndexStatistics ndvi) {
		if (savi != null && savi.stdDev() != null) {
			return savi.stdDev().doubleValue();
		}
		if (ndvi != null && ndvi.stdDev() != null) {
			return ndvi.stdDev().doubleValue();
		}
		return null;
	}

	private static Boolean amplitude(NdviStatistics.IndexStatistics savi, NdviStatistics.IndexStatistics ndvi) {
		if (completeRange(ndvi)) {
			return ndvi.max().subtract(ndvi.min()).doubleValue() > AMPLITUDE_ALERTE;
		}
		if (completeRange(savi)) {
			return savi.max().subtract(savi.min()).doubleValue() > AMPLITUDE_ALERTE;
		}
		return null;
	}

	private static boolean completeRange(NdviStatistics.IndexStatistics index) {
		return index != null && index.min() != null && index.max() != null;
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	private static double arrondir(double value) {
		return Math.round(value * 10.0) / 10.0;
	}
}
