package com.github.cunvoas.geoserviceisochrone.extern.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionParseUrl;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

class TestUrlPointParser {

	
	private UrlPointParser tested = new UrlPointParser();
	

	@Test
	void testParse() {
		
		Coordinate coord = tested.parse("https://www.google.com/maps/@50.1234567,3.1234567,18z");
		Assert.notNull(coord, "Gmap coord null");
		assertEquals(3.1234567d, coord.getX());
		assertEquals(50.1234567d, coord.getY());
		
		coord = tested.parseGeoportail("https://www.geoportail.gouv.fr/carte?c=2.473994493484497,48.85187488786221&z=17&l0=ORTHOIMAGERY.ORTHOPHOTOS::GEOPORTAIL:OGC:WMTS(1)&l1=GEOGRAPHICALNAMES.NAMES::GEOPORTAIL:OGC:WMTS(1)&l2=UTILITYANDGOVERNMENTALSERVICES.IGN.POI.ENSEIGNEMENTPRIMAIRE::GEOPORTAIL:OGC:WMS(1)&l3=UTILITYANDGOVERNMENTALSERVICES.IGN.POI.ENSEIGNEMENTMATERNELLES::GEOPORTAIL:OGC:WMS(1)&permalink=yes");
		Assert.notNull(coord, "Geoportail coord null");
		assertEquals(2.473994493484497d, coord.getX());
		assertEquals(48.85187488786221d, coord.getY());
		
		try {
			coord = tested.parseGeoportail("https://www.randomSite/carte?c=2.473994493484497,48.85187488786221");
		} catch (Exception ex) {
			assertInstanceOf(ExceptionParseUrl.class, ex, "random site");
		}
	}
	
	@Test
	void testParseGoogle() {
		
		
		
		Coordinate coord = tested.parseGoogle("https://www.google.com/maps/@50.1234567,3.1234567,18z");
		Assert.notNull(coord, "Gmap coord null");
		assertEquals(3.1234567d, coord.getX());
		assertEquals(50.1234567d, coord.getY());
		
		coord  =tested.parseGoogle("https://www.google.com/maps/place/6+Parc+Bocquet,+59260+Lille/@50.6258461,3.1089747,19.78z/data=!4m6!3m5!1s0x47c2d60fc38d7df5:0x39360a1913245aba!8m2!3d50.626053!4d3.1092666!16s%2Fg%2F11c1ksqcx3");
		Assert.notNull(coord, "Gmap coord null");
	}
	
	
	@Test
	void testParseGeoportail() {
		
		Coordinate coord = tested.parseGeoportail("https://www.geoportail.gouv.fr/carte?c=2.473994493484497,48.85187488786221&z=17&l0=ORTHOIMAGERY.ORTHOPHOTOS::GEOPORTAIL:OGC:WMTS(1)&l1=GEOGRAPHICALNAMES.NAMES::GEOPORTAIL:OGC:WMTS(1)&l2=UTILITYANDGOVERNMENTALSERVICES.IGN.POI.ENSEIGNEMENTPRIMAIRE::GEOPORTAIL:OGC:WMS(1)&l3=UTILITYANDGOVERNMENTALSERVICES.IGN.POI.ENSEIGNEMENTMATERNELLES::GEOPORTAIL:OGC:WMS(1)&permalink=yes");
		Assert.notNull(coord, "Geoportail coord null");
		assertEquals(2.473994493484497d, coord.getX());
		assertEquals(48.85187488786221d, coord.getY());
	}

}
