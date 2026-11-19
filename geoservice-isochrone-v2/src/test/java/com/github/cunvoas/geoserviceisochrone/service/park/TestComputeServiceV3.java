package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

@DisplayName("Tests des calculs de répartition de population (ComputeCarreServiceV3)")
class TestComputeServiceV3 {

	/**
	 * Le calcul complet d'un carreau (test_computeCarreByComputeJob) nécessite un
	 * jeu de données de production et la propriété application.feature-flipping.carre200m-impl=v3.
	 * Seuls les calculs unitaires sont couverts ici.
	 */
	@Test
	@Disabled("nécessite les données INSEE de production et carre200m-impl=v3")
	void test_computeCarreByComputeJob() {
	}

	@Test
	@DisplayName("calcul de la population par surface avec arrondi correcte")
	void testMath_PopulationDistribution() {
		long surfaceParkAccess = 20001L;
		double inhabitant = 400.6;
		BigDecimal allpop = new BigDecimal(Math.round(inhabitant));
		
		inhabitant = allpop.doubleValue();
		
		long popIn = Math.round(inhabitant * surfaceParkAccess / 40_000d);
		BigDecimal inClud = new BigDecimal(popIn);
		BigDecimal exClud = new BigDecimal(inhabitant - popIn);
		
		assertTrue(allpop.longValue() > 0L);
		assertTrue(inClud.longValue() >= 0L);
		assertTrue(exClud.doubleValue() >= 0.0);
		assertEquals(inhabitant, inClud.doubleValue() + exClud.doubleValue(), 1.0);
	}

	@Test
	@DisplayName("calcul avec surface zéro")
	void testMath_ZeroSurface() {
		long surfaceParkAccess = 0L;
		double inhabitant = 400.6;
		BigDecimal allpop = new BigDecimal(Math.round(inhabitant));
		
		inhabitant = allpop.doubleValue();
		
		long popIn = Math.round(inhabitant * surfaceParkAccess / 40_000d);
		
		assertEquals(0L, popIn);
	}

	@Test
	@DisplayName("calcul avec population zéro")
	void testMath_ZeroPopulation() {
		long surfaceParkAccess = 20001L;
		double inhabitant = 0d;
		BigDecimal allpop = new BigDecimal(Math.round(inhabitant));
		
		inhabitant = allpop.doubleValue();
		
		long popIn = Math.round(inhabitant * surfaceParkAccess / 40_000d);
		
		assertEquals(0L, popIn);
	}

	@Test
	@DisplayName("calcul avec grands nombres")
	void testMath_LargeNumbers() {
		long surfaceParkAccess = 1000000L;
		double inhabitant = 100000.5;
		BigDecimal allpop = new BigDecimal(Math.round(inhabitant));
		
		inhabitant = allpop.doubleValue();
		
		long popIn = Math.round(inhabitant * surfaceParkAccess / 40_000d);
		BigDecimal inClud = new BigDecimal(popIn);
		
		assertTrue(inClud.longValue() > 0L);
//		assertTrue(exClud.doubleValue() >= 0.0);
	}
}
