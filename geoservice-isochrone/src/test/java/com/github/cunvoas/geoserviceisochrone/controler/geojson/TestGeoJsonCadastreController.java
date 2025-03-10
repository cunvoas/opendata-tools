package com.github.cunvoas.geoserviceisochrone.controler.geojson;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.GeoJsonCadastreController;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;

@SpringBootTest
@ActiveProfiles({"prod","dev"})
class TestGeoJsonCadastreController {

	@Autowired
	private GeoJsonCadastreController tested;

	private ObjectMapper mapper = new ObjectMapper();
	
	
	@Test
	@Disabled
	void testGetCadastreByCom2Com() {
		
		for (Long i = 1L; i < 18L; i++) {
			GeoJsonRoot r = tested.getCadastreByCom2Com(i);
			
			try {
				mapper.writeValue(new File("./cadastre_c2c_"+i+".json"), r);
			} catch (StreamWriteException e) {
				fail(e);
			} catch (DatabindException e) {
				fail(e);
			} catch (IOException e) {
				fail(e);
			}
		}
		
		
	}

	
	
	
	
	
}
