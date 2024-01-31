package com.github.cunvoas.geoserviceisochrone.dto;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.DtoIsoChroneParser;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoIsoChrone;

class TestDtoParser {
	
	String json1 = null;
	String json2 = null;
	String json3 = null;
	
	@BeforeEach
	void init() {
		try {
			InputStream in = this.getClass().getResourceAsStream("/isochrone-1.json");
			json1 = new String(in.readAllBytes());
			in.close();
			
			in = this.getClass().getResourceAsStream("/isochrone-2.json");
			json2 = new String(in.readAllBytes());
			in.close();
			
			in = this.getClass().getResourceAsStream("/isochrone-3.json");
			json3 = new String(in.readAllBytes());
			in.close();
			
		} catch (IOException e) {
			fail("IOException");
		}
	}

	@Test
	void test() {
		try {
			DtoIsoChroneParser tested = new DtoIsoChroneParser ();
			
			System.out.println(json1);
			DtoIsoChrone iso1 = tested.parseBasicIsoChrone(json1);
			assertNotNull(iso1, "json1");
			

			System.out.println(json2);
			DtoIsoChrone iso2 = tested.parseBasicIsoChrone(json2);
			assertNotNull(iso2, "json2");
			

			System.out.println(json3);
			DtoIsoChrone iso3 = tested.parseBasicIsoChrone(json3);
			assertNotNull(iso3, "json3");
			
			
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException");
		}
		
		
	}

}
