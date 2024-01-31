package com.github.cunvoas.geoserviceisochrone.service.map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import com.github.cunvoas.geoserviceisochrone.service.map.CityService;

class TestCityService {

	
	CityService tested = new CityService();
	

	@Test
	@Disabled
	void testGetGzipCadastre() {
		byte[] geo = tested.getGzipCadastre("59001");
		
		assertNotNull(geo);
	} 
	@Test
	void testGetGeoJsonCadastre() {

		try {
			File f = ResourceUtils.getFile("classpath:cadastre-59001-communes.json.gz");
			byte[] gzf = java.nio.file.Files.readAllBytes(Paths.get(f.getAbsolutePath()));
			
			String geo = tested.getGeoJsonCadastre(gzf);
			
			assertNotNull(geo);
		} catch (FileNotFoundException e) {
			fail("FileNotFoundException");
		} catch (IOException e) {
			fail("IOException");
		}
	}

}
