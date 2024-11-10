package com.github.cunvoas.geoserviceisochrone.extern.helper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.charset.Charset;

import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.springframework.util.ResourceUtils;

class TestGeoJson2GeometryHelper {
	private GeoJson2GeometryHelper tested = new GeoJson2GeometryHelper();
	
	@Test
	void test() {
		try {
			File f = ResourceUtils.getFile("classpath:cadastre-59001-communes.json");
			String content = Files.contentOf(f, Charset.defaultCharset());
		
			Geometry g = tested.parse(content);
			assertNotNull(g);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	void testGeomanPoly() {
		try {
			File f = ResourceUtils.getFile("classpath:geoman-poly.json");
			String content = Files.contentOf(f, Charset.defaultCharset());
		
			Geometry g = tested.parseGeoman(content);
			assertNotNull(g);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	void testGeomanMPoly() {
		try {
			File f = ResourceUtils.getFile("classpath:geoman-mpoly.json");
			String content = Files.contentOf(f, Charset.defaultCharset());
		
			Geometry g = tested.parseGeoman(content);
			assertNotNull(g);
			
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}


}
