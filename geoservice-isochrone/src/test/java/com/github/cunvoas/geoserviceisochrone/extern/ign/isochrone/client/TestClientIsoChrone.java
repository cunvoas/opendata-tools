package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoIsoChrone;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

class TestClientIsoChrone {
	
	private IsoChroneClientService tested = new ClientIsoChroneUi1();
	private IsoChroneClientService testedApiV1 = new ClientIsoChroneApiV1();
	private IsoChroneClientService testedApiV2 = new ClientIsoChroneApiV2();

	@Test
	@Disabled
	void testGetIsoChrone() {
		
		Coordinate coord = new Coordinate(3.106738328933716,50.624763376155535);
		String resp = tested.getIsoChrone(coord, "200");
		assertNotNull(resp, "not null");
		
		System.out.println(resp);
	}
	

	@Test
	void testGetIsoChronePh() {
		
		Coordinate coord = new Coordinate(3.0144703388214116,50.63679884038829);
		String resp = testedApiV2.getIsoChrone(coord, "300");
		assertNotNull(resp, "not null");
		
		System.out.println(resp);
	}
	

	@Test
	@Disabled
	void testGetApiIsoChrone() {

		Coordinate coord = new Coordinate(3.106738328933716,50.624763376155535);
		String resp = testedApiV1.getIsoChrone(coord, "200");
		assertNotNull(resp, "not null");
		System.out.println(resp);
		

		DtoIsoChroneParser parser = new DtoIsoChroneParser();
		DtoIsoChrone dto;
		try {
			dto = parser.parseBasicIsoChrone(resp);
			assertNotNull(dto, "not null");
		} catch (JsonProcessingException e) {
			fail(e);
		}	

		
		
	}

}
