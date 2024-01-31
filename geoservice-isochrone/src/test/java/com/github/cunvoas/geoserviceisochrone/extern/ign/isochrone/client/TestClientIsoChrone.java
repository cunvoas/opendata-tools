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
	private IsoChroneClientService testedApi = new ClientIsoChroneApi();

	@Test
	@Disabled
	void testGetIsoChrone() {
		
		Coordinate coord = new Coordinate(3.106738328933716,50.624763376155535);
		String resp = tested.getIsoChrone(coord, "200");
		assertNotNull(resp, "not null");
		
		System.out.println(resp);
	}
	

	@Test
	void testGetApiIsoChrone() {

		Coordinate coord = new Coordinate(3.106738328933716,50.624763376155535);
		String resp = testedApi.getIsoChrone(coord, "200");
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
