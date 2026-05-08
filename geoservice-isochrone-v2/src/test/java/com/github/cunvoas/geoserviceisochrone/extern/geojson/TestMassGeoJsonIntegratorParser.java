package com.github.cunvoas.geoserviceisochrone.extern.geojson;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"prod","dev"})
class TestMassGeoJsonIntegratorParser {

	@Autowired
	private MassGeoJsonIntegratorParser tested;
	
	@Test
	@Disabled
	void test() {
		
//		String geoj = "/work/PERSO/ASSO/data/grilleFull_gps/grille200m.geojsonl.json";
		String geoj = "/work/PERSO/ASSO/data/grilleFull_gps/test.json";
		try {
			tested.parseAndSave(geoj);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
