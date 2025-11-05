package com.github.cunvoas.geoserviceisochrone.service.park;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ComputeJobRepository;


@SpringBootTest
@ActiveProfiles({"secret","pi_nuc"})
class TestComputeCarreServiceV3 {
	@Autowired
	private ComputeCarreServiceV3 computeCarreServiceV3;
	
	@Autowired
	private ComputeJobRepository computeJobRepository;

	
	@Test
	void testComputeCarreShape() {
		
		
		List<ComputeJob> jobs = computeJobRepository.findByStatusOrderByDemandDesc(ComputeJobStatusEnum.IN_ERROR, PageRequest.of(0, 120));
		ComputeJob job = new ComputeJob();
		job = jobs.get(0);

		Boolean b  = computeCarreServiceV3.computeCarreByComputeJob(job);
		Assert.notNull(b, "service runs");
		
//		for (ComputeJob job : jobs) {
//			Boolean b  = computeCarreServiceV3.computeCarreByComputeJob(job);
//			Assert.notNull(b, "service runs");
//		}
		
		
	
	}

//	@Test
//	void testIsActive() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testComputePopAndDensityDetail() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testComputePopAndDensity() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testComputeCarreByComputeJob() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testRefreshParkEntrancesString() {
//		fail("Not yet implemented");
//		
//	}
//
//	@Test
//	void testRefreshParkEntrancesCadastre() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testComputeParkAreaParkAreaInteger() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testComputeParkAreaParkArea() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	void testGetSurface() {
//		fail("Not yet implemented");
//	}

}
