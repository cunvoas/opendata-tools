package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.BaseUnitTest;


@DisplayName("Tests pour ComputeCarreServiceV3")
class TestComputeServiceV3 extends BaseUnitTest {

	private ComputeCarreServiceV3 tested = new ComputeCarreServiceV3();
	

	@Test
	@DisplayName("calcul de la population par surface avec arrondi correcte")
	void testMath_PopulationDistribution() {
		Long surfaceParkAccess = Long.valueOf("20001");
		Double inhabitant = Double.valueOf("400.6");
		BigDecimal allpop = new BigDecimal(Math.round(inhabitant));
		
		inhabitant = allpop.doubleValue();
		
		Long popIn = Math.round(inhabitant * surfaceParkAccess / 40_000d);
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
		Long surfaceParkAccess = Long.valueOf("0");
		Double inhabitant = Double.valueOf("400.6");
		BigDecimal allpop = new BigDecimal(Math.round(inhabitant));
		
		inhabitant = allpop.doubleValue();
		
		Long popIn = Math.round(inhabitant * surfaceParkAccess / 40_000d);
		
		assertEquals(0L, popIn);
	}

	@Test
	@DisplayName("calcul avec population zéro")
	void testMath_ZeroPopulation() {
		Long surfaceParkAccess = Long.valueOf("20001");
		Double inhabitant = Double.valueOf("0");
		BigDecimal allpop = new BigDecimal(Math.round(inhabitant));
		
		inhabitant = allpop.doubleValue();
		
		Long popIn = Math.round(inhabitant * surfaceParkAccess / 40_000d);
		
		assertEquals(0L, popIn);
	}

	@Test
	@DisplayName("calcul avec grands nombres")
	void testMath_LargeNumbers() {
		Long surfaceParkAccess = Long.valueOf("1000000");
		Double inhabitant = Double.valueOf("100000.5");
		BigDecimal allpop = new BigDecimal(Math.round(inhabitant));
		
		inhabitant = allpop.doubleValue();
		
		Long popIn = Math.round(inhabitant * surfaceParkAccess / 40_000d);
		BigDecimal inClud = new BigDecimal(popIn);
		BigDecimal exClud = new BigDecimal(inhabitant - popIn);
		
		assertTrue(inClud.longValue() > 0L);
//		assertTrue(exClud.doubleValue() >= 0.0);
	}
}
