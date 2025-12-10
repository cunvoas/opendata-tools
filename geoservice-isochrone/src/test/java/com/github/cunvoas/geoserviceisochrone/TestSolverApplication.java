package com.github.cunvoas.geoserviceisochrone;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.service.solver.ServicePropositionParc;

import lombok.extern.slf4j.Slf4j;


@SpringBootTest
@Slf4j
@ActiveProfiles({"secret","pi_nuc"}) //pi_nuc rep
class TestSolverApplication {

	@Autowired
	private ServicePropositionParc tested;
	
		
	
	@Test
//	@Disabled
	@Order(10)
	void calculeProposition() {
		
		
		try {
			Map<String, ParkProposalWork> map =  tested.calculeProposition("59350", 2019);
			
			Assertions.assertNotNull(map);
			
			if (map!=null && !map.isEmpty()) {
				tested.saveProposals(map);
			}
			
			
		} catch (Exception e) {
			log.error("Erreur lors du test de calcul de proposition de parc ",e);
			Assertions.fail(e.getMessage());
		}
	}
	
	
	
}