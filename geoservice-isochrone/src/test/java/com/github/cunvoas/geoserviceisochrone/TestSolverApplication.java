package com.github.cunvoas.geoserviceisochrone;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.solver.ServicePropositionParc;

import lombok.extern.slf4j.Slf4j;


@SpringBootTest
@Slf4j
@ActiveProfiles({"secret","rep"}) //pi_nuc rep
class TestSolverApplication {


	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private ServicePropositionParc tested;
	
		
	
	
	@Test
	@Disabled
	@Order(11)
	void calculePropositionInsee() {

		try {
			Map<String, ParkProposalWork> map =  tested.calculeProposition("59350", 2019);
			
			Assertions.assertNotNull(map);
			if (map!=null && !map.isEmpty()) {
				//tested.saveProposals(map);
				
				for (ParkProposalWork ppw : map.values()) {
					log.info("Proposal for square {} : add park area = {} m²",ppw.getIdInspire(),ppw.getNewMissingSurface());
				}
			}
		} catch (Exception ignore) {
			log.error(ignore.getMessage());
		}
					
			
	}
	
	
	@Test
	@Disabled
	@Order(10)
	void calculePropositionC2C() {
		
		
		try {
			
			Optional<CommunauteCommune> opt=communauteCommuneRepository.findById(1L);
			if (opt.isPresent()) {
				CommunauteCommune c2c = opt.get();
				for (City city : c2c.getCities()) {
					
					try {
						Map<String, ParkProposalWork> map =  tested.calculeProposition(city.getInseeCode(), 2019);
						
						Assertions.assertNotNull(map);
						if (map!=null && !map.isEmpty()) {
							tested.saveProposals(map);
						}
					} catch (Exception ignore) {
						log.error(ignore.getMessage());
					}
					
				}
				
			}
			
			
			
			
			
		} catch (Exception e) {
			log.error("Erreur lors du test de calcul de proposition de parc ",e);
			Assertions.fail(e.getMessage());
		}
	}
	
	
	
}