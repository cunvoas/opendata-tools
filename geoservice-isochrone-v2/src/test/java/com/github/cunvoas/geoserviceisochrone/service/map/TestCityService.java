package com.github.cunvoas.geoserviceisochrone.service.map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;

class TestCityService {

	private final CityService tested = new CityService();

	private byte[] gzippedCadastre() throws Exception {
		try (InputStream in = getClass().getResourceAsStream("/cadastre-59001-communes.json")) {
			assertNotNull(in, "cadastre-59001-communes.json doit être présent dans les ressources de test");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try (GZIPOutputStream gz = new GZIPOutputStream(bos)) {
				in.transferTo(gz);
			}
			return bos.toByteArray();
		}
	}

	@Test
	void testGetGeoJsonCadastre() throws Exception {
		String geo = tested.getGeoJsonCadastre(gzippedCadastre());

		assertNotNull(geo);
		assertTrue(geo.contains("FeatureCollection") || geo.contains("features"));
	}
}
