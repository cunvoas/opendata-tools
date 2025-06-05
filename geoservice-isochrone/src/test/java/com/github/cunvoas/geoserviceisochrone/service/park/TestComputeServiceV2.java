package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class TestComputeServiceV2 {

	
	ComputeCarreServiceV3 tested = new ComputeCarreServiceV3();
	

	@Test
	void testMath() {
		Long surfaceParkAccess = Long.valueOf("20001");
		
		Double inhabitant = Double.valueOf("400.6");
		BigDecimal allpop = new BigDecimal(Math.round(inhabitant));
		
		// fix eventual round error
		inhabitant = allpop.doubleValue();
		
		Long popIn = Math.round(inhabitant*surfaceParkAccess/40_000d);
		BigDecimal inClud = new BigDecimal(popIn);
		BigDecimal exClud = new BigDecimal(inhabitant-popIn);
		
		System.out.println(allpop);
		System.out.println(inClud);
		System.out.println(exClud);
	}
}
