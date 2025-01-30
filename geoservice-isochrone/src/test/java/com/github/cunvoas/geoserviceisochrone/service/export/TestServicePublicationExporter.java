package com.github.cunvoas.geoserviceisochrone.service.export;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;

@SpringBootTest
@ActiveProfiles({"prod","dev"})
class TestServicePublicationExporter {

	@Autowired
	private ServicePublicationExporter tested;
	
	@Test
	void testGetCom2CoShape() {
		CommunauteCommune c2c = new CommunauteCommune();
		c2c.setId(1L);
		Polygon p= tested.getCom2CoSquareShape(c2c);
		Assert.notNull(p, "Polygon not null");
		
		
	}

}
