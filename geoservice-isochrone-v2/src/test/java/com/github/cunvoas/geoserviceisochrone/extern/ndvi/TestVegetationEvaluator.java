package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.extern.ndvi.VegetationEvaluator.VegetationEvaluation;

class TestVegetationEvaluator {

	private static NdviStatistics.IndexStatistics index(Double mean, Double min, Double max, Double std) {
		return new NdviStatistics.IndexStatistics(dec(mean), dec(min), dec(max), dec(std));
	}

	private static BigDecimal dec(Double value) {
		return value == null ? null : BigDecimal.valueOf(value);
	}

	private static NdviStatistics stats(
			NdviStatistics.IndexStatistics savi,
			NdviStatistics.IndexStatistics ndvi,
			NdviStatistics.IndexStatistics ndre,
			NdviStatistics.IndexStatistics gndvi) {
		return stats(savi, ndvi, ndre, gndvi, 100);
	}

	private static NdviStatistics stats(
			NdviStatistics.IndexStatistics savi,
			NdviStatistics.IndexStatistics ndvi,
			NdviStatistics.IndexStatistics ndre,
			NdviStatistics.IndexStatistics gndvi,
			Integer sampleCount) {
		return new NdviStatistics(ndvi, ndre, gndvi, savi, new Date(3000L), new Date(2000L), sampleCount);
	}

	@Test
	void donnees_insuffisantes_sans_savi_ni_ndvi() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(null, null,
				index(0.4, 0.2, 0.6, 0.02), index(0.5, 0.2, 0.7, 0.02)));

		assertEquals("Données insuffisantes (SAVI et NDVI manquants)", result.recouvrement());
		assertNull(result.scoreSante());
		assertEquals("Nulle", result.confiance());
		assertEquals("Évaluation impossible", result.diagnosticPhysiologique());
		assertNull(result.anomalieLocaleDetectee());
	}

	@Test
	void stats_null_renvoie_une_evaluation_indisponible() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(null);

		assertEquals("Nulle", result.confiance());
		assertNull(result.scoreSante());
	}

	@Test
	void sol_nu_avec_confiance_elevee() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.10, 0.0, 0.2, 0.03),
				index(0.40, 0.2, 0.6, 0.05),
				index(0.30, 0.1, 0.5, 0.04),
				index(0.45, 0.2, 0.7, 0.05)));

		assertEquals("Minéral, bâti, ombre dense ou sol nu", result.recouvrement());
		assertEquals(0.0, result.scoreSante());
		assertEquals("Élevée", result.confiance());
		assertEquals("Absence de couvert végétal actif", result.diagnosticPhysiologique());
		assertEquals("Non applicable", result.homogeneite());
	}

	@Test
	void couvert_faible_pondere_savi_et_ndvi() {
		// vigueur = 0,65*0,30 + 0,35*0,50 = 0,37 → 37 - 0,02*20 = 36,6
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.30, 0.1, 0.5, 0.02),
				index(0.50, 0.2, 0.8, 0.02),
				index(0.30, 0.1, 0.5, 0.01),
				index(0.45, 0.2, 0.7, 0.01)));

		assertEquals("Couvert faible / Pelouse ouverte ou arbustif jeune", result.recouvrement());
		assertEquals(36.6, result.scoreSante());
		assertEquals("Élevée", result.confiance());
		assertEquals("Homogène", result.homogeneite());
		// amplitude NDVI 0,8 - 0,2 = 0,6 > 0,5
		assertTrue(result.anomalieLocaleDetectee());
	}

	@Test
	void couvert_dense_pondere_ndre_gndvi_ndvi() {
		// vigueur = 0,5*0,36 + 0,3*0,55 + 0,2*0,70 = 0,485 → 48,5 - 0,10*20 = 46,5
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.50, 0.2, 0.7, 0.10),
				index(0.70, 0.2, 0.8, 0.06),
				index(0.36, 0.2, 0.5, 0.06),
				index(0.55, 0.3, 0.7, 0.05)));

		assertEquals("Couvert dense / Strate arborée ou parc mature", result.recouvrement());
		assertEquals(46.5, result.scoreSante());
		assertEquals("Modérément hétérogène", result.homogeneite());
	}

	@Test
	void savi_absent_utilise_le_ndvi_avec_seuils_rehausses() {
		// NDVI 0,30 ≥ seuil minéral NDVI (0,22) et < seuil dense (0,55) → couvert faible
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				null,
				index(0.30, 0.2, 0.4, 0.02),
				index(0.20, 0.1, 0.3, 0.01),
				index(0.25, 0.1, 0.4, 0.01)));

		assertEquals("Couvert faible / Pelouse ouverte ou arbustif jeune", result.recouvrement());
		// vigueur = NDVI seul = 0,30 → 30 - 0,02*20 = 29,6
		assertEquals(29.6, result.scoreSante());
		assertEquals("Moyenne", result.confiance());
	}

	@Test
	void savi_absent_sous_le_seuil_ndvi_donne_du_mineral() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				null, index(0.20, 0.1, 0.3, 0.02), null, null));

		assertEquals("Minéral, bâti, ombre dense ou sol nu", result.recouvrement());
		assertEquals(0.0, result.scoreSante());
		assertEquals("Faible", result.confiance());
	}

	@Test
	void ndre_absent_transfere_son_poids_au_gndvi() {
		// vigueur = (0,3*0,50 + 0,2*0,60) / 0,5 = 0,54 → 54 - 0,02*20 = 53,6
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.45, 0.2, 0.7, 0.02),
				index(0.60, 0.3, 0.8, 0.02),
				null,
				index(0.50, 0.3, 0.7, 0.02)));

		assertEquals("Couvert dense / Strate arborée ou parc mature", result.recouvrement());
		assertEquals(53.6, result.scoreSante());
		assertEquals("Moyenne", result.confiance());
	}

	@Test
	void ndre_et_gndvi_absents_limitent_le_diagnostic() {
		// dense, NDVI seul → vigueur = 0,70, aucun écart-type → pas de pénalité
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.45, 0.2, 0.7, null),
				index(0.70, null, null, null),
				null,
				null));

		assertEquals("Couvert dense / Strate arborée ou parc mature", result.recouvrement());
		assertEquals(70.0, result.scoreSante());
		assertEquals("Diagnostic limité (NDRE et GNDVI manquants pour détecter un stress précoce)",
				result.diagnosticPhysiologique());
		assertEquals("Moyenne", result.confiance());
	}

	@Test
	void savi_seul_porte_toute_la_vigueur_en_couvert_dense() {
		// SAVI ≥ 0,42 sans NDVI/NDRE/GNDVI → poids total nul, repli sur SAVI
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.60, 0.5, 0.7, 0.02), null, null, null));

		assertEquals("Couvert dense / Strate arborée ou parc mature", result.recouvrement());
		assertEquals(59.6, result.scoreSante());
		assertEquals("Faible", result.confiance());
	}

	@Test
	void sans_ecart_type_homogeneite_non_calculable_et_aucune_penalite() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.30, 0.1, 0.5, null),
				index(0.50, 0.2, 0.8, null),
				index(0.30, 0.1, 0.5, null),
				index(0.45, 0.2, 0.7, null)));

		assertEquals("Non calculable (écart-type manquant)", result.homogeneite());
		// vigueur 0,37 → 37 sans pénalité
		assertEquals(37.0, result.scoreSante());
	}

	@Test
	void ecart_type_ndvi_utilise_quand_savi_sans_ecart_type() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.30, 0.1, 0.5, null),
				index(0.50, 0.2, 0.8, 0.17),
				index(0.30, 0.1, 0.5, null),
				index(0.45, 0.2, 0.7, null)));

		assertEquals("Très hétérogène", result.homogeneite());
		// 37 - 0,17*20 = 33,6
		assertEquals(33.6, result.scoreSante());
	}

	@Test
	void anomalie_calculee_sur_savi_quand_ndvi_sans_bornes() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.30, 0.05, 0.90, 0.02),
				index(0.50, null, null, 0.02),
				index(0.30, 0.1, 0.5, 0.01),
				index(0.45, 0.2, 0.7, 0.01)));

		assertTrue(result.anomalieLocaleDetectee());
	}

	@Test
	void anomalie_indisponible_sans_min_ni_max() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.30, null, null, 0.02),
				index(0.50, null, null, 0.02),
				index(0.30, null, null, 0.01),
				index(0.45, null, null, 0.01)));

		assertNull(result.anomalieLocaleDetectee());
	}

	@Test
	void alerte_stress_par_ndre_bas() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.50, 0.2, 0.7, 0.02),
				index(0.70, 0.4, 0.8, 0.02),
				index(0.30, 0.1, 0.5, 0.02),
				index(0.60, 0.4, 0.8, 0.02)));

		assertEquals("Alerte : Déficit chlorophyllien canopée (NDRE bas)", result.diagnosticPhysiologique());
	}

	@Test
	void alerte_stress_par_gndvi_bas() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.50, 0.2, 0.7, 0.02),
				index(0.70, 0.4, 0.8, 0.02),
				index(0.45, 0.2, 0.6, 0.02),
				index(0.40, 0.2, 0.6, 0.02)));

		assertEquals("Alerte : Stress hydrique ou azoté (GNDVI bas)", result.diagnosticPhysiologique());
	}

	@Test
	void score_plafonne_a_100_et_plancher_a_0() {
		VegetationEvaluation haut = VegetationEvaluator.evaluate(stats(
				index(0.60, 0.5, 0.7, 0.0),
				index(1.10, 1.0, 1.2, 0.0),
				index(1.30, 1.2, 1.4, 0.0),
				index(1.20, 1.1, 1.3, 0.0)));
		assertEquals(100.0, haut.scoreSante());

		// vigueur 0,65*0,20 + 0,35*0,10 = 0,165 → 16,5 - 0,9*20 = -1,5 → 0
		VegetationEvaluation bas = VegetationEvaluator.evaluate(stats(
				index(0.20, 0.0, 0.4, 0.9),
				index(0.10, 0.0, 0.2, 0.9),
				index(0.10, 0.0, 0.2, 0.9),
				index(0.10, 0.0, 0.2, 0.9)));
		assertEquals(0.0, bas.scoreSante());
		assertEquals("Très hétérogène", bas.homogeneite());
	}

	@Test
	void un_seul_pixel_ne_produit_pas_de_diagnostic_d_heterogeneite() {
		// std = 0 renvoyé par l'API sur un pixel unique : ce n'est pas de l'homogénéité
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.30, 0.30, 0.30, 0.0),
				index(0.50, 0.50, 0.50, 0.0),
				index(0.30, 0.30, 0.30, 0.0),
				index(0.45, 0.45, 0.45, 0.0),
				1));

		assertEquals("Non calculable (pixels insuffisants)", result.homogeneite());
		// aucune pénalité : vigueur = 0,65*0,30 + 0,35*0,50 = 0,37 → 37,0
		assertEquals(37.0, result.scoreSante());
		assertNull(result.anomalieLocaleDetectee());
	}

	@Test
	void deux_pixels_suffisent_a_mesurer_la_dispersion() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.30, 0.20, 0.40, 0.10),
				index(0.50, 0.30, 0.90, 0.10),
				index(0.30, 0.20, 0.40, 0.02),
				index(0.45, 0.30, 0.60, 0.02),
				2));

		assertEquals("Modérément hétérogène", result.homogeneite());
		assertTrue(result.anomalieLocaleDetectee());
	}

	@Test
	void nombre_de_pixels_inconnu_conserve_l_analyse() {
		VegetationEvaluation result = VegetationEvaluator.evaluate(stats(
				index(0.30, 0.1, 0.5, 0.02),
				index(0.50, 0.2, 0.8, 0.02),
				index(0.30, 0.1, 0.5, 0.01),
				index(0.45, 0.2, 0.7, 0.01),
				null));

		assertEquals("Homogène", result.homogeneite());
		assertTrue(result.anomalieLocaleDetectee());
	}
}
