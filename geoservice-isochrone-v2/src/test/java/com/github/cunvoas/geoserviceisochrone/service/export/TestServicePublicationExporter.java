package com.github.cunvoas.geoserviceisochrone.service.export;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.cunvoas.geoserviceisochrone.support.PostgisContextTestBase;
import org.springframework.util.Assert;

import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;

class TestServicePublicationExporter extends PostgisContextTestBase {

	@Autowired
	private ServicePublicationExporter tested;
	
	@Test
	@org.junit.jupiter.api.Disabled("nécessite le jeu de données de production (communaute de communes id=1)")
	void testGetCom2CoShape() {
		CommunauteCommune c2c = new CommunauteCommune();
		c2c.setId(1L);
		Polygon p= tested.getCom2CoSquareShape(c2c);
		Assert.notNull(p, "Polygon not null");
		
		
	}

}
