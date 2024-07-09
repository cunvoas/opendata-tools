package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import com.github.cunvoas.geoserviceisochrone.service.park.ComputeService;

class TestComputeServiceV2 {

	
	ComputeServiceV2 tested = new ComputeServiceV2();
	

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
