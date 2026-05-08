package com.github.cunvoas.geoserviceisochrone.service.opendata;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.RegionRepository;

@SpringBootTest
@ActiveProfiles({"prod","dev"})
class TestServiceOpenData {
	

	@Autowired
	private CommunauteCommuneRepository ccRepo;
	
	@Autowired
	private ServiceOpenData tested;

	@Autowired
	private RegionRepository  regionRepository;

	@Test
	@Disabled
	void testRegion() {
		
		String sRegions = "Auvergne-Rhône-Alpes, Bourgogne-Franche-Comté, Bretagne, Centre-Val de Loire, Corse, Grand-Est, Guadeloupe, Guyane, Hauts-de-France, Île-de-France, La Réunion, Martinique, Mayotte, Normandie, Nouvelle-Aquitaine, Occitanie, Pays de la Loire, Provence-Alpes-Côte d'Azur";
		String[] tRegions = sRegions.split(", ");
		
		for (int i = 0; i < tRegions.length; i++) {
			String sRegion = tRegions[i];
			Region region = new Region() ;
			region.setName(sRegion);
			tested.save(region);
		}
	}
	

	@Test
	@Disabled
	void testComm2Co() {
		
		Region region = regionRepository.findById(9L).get();
		CommunauteCommune com2co = new CommunauteCommune();
		com2co.setName("Métropôle Européenne de Lille");
		com2co.setRegion(region);
		tested.save(com2co);
	}
	

	@Test
	void testEnvelopeBox() {
		List<CommunauteCommune> ccs=ccRepo.findAll();
		for (CommunauteCommune cc : ccs) {
			tested.computeSquareOnMap(cc);
			
		}
	}

}
