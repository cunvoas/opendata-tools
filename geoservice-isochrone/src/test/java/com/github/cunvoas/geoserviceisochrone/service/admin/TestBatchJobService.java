package com.github.cunvoas.geoserviceisochrone.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TestBatchJobService {
	


	@Test
	void testChangeStatus() {
		BatchJobService tested = new BatchJobService();

		Boolean ret = tested.launchOrFinish(true);
		assertEquals(true, ret, "first launch");
		
		ret = tested.launchOrFinish(true);
		assertEquals(false, ret, "block new launch");
		

		ret = tested.launchOrFinish(false);
		assertEquals(false, ret, "stop launch");
		
	}

}
