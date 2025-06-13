package com.github.cunvoas.geoserviceisochrone;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeIrisJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ComputeJobIrisRepository;
import com.github.cunvoas.geoserviceisochrone.service.admin.BatchJobService;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceIris;
import com.github.cunvoas.geoserviceisochrone.service.park.ComputeIrisServiceIris;


@SpringBootTest
@ActiveProfiles({"secret","rep"})
class TestIrisApplication {
	
	
	@Autowired 
	private ServiceIris serviceIris;

	@Autowired
	private BatchJobService batchJobService;
	@Autowired
	private ComputeJobIrisRepository computeJobIrisRepository;
	@Autowired
	private ComputeIrisServiceIris computeIrisServiceIris;

	

	@Test
	@Order(01)
	@Disabled
	void computeFootprint() {
		try {
			serviceIris.computeFootprint();

		} catch (Exception e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}
	

	@Test
	@Order(10)
	@Disabled
	void addIrisJob() {
		try {
			int nb = batchJobService.requestProcessCity("59350");
			assertNotEquals(0, nb, "no job add");

		} catch (Exception e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}
	

	@Test
	@Order(20)
//	@Disabled
	void processIrisJob() {
		try {
			
			List<ComputeIrisJob> jobs = computeJobIrisRepository.findByStatusOrderByDemandAsc(ComputeJobStatusEnum.TO_PROCESS, Pageable.ofSize(150));
			for (ComputeIrisJob  computeIrisJob: jobs) {
				boolean b=computeIrisServiceIris.computeIrisByComputeJob(computeIrisJob);
				
				if (b) {
					computeIrisJob.setStatus(ComputeJobStatusEnum.PROCESSED);
				} else {
					computeIrisJob.setStatus(ComputeJobStatusEnum.IN_ERROR);
				}
				
				computeIrisJob.setProcessed(new Date());
				computeJobIrisRepository.save(computeIrisJob);
			}
			

		} catch (Exception e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}
	
}