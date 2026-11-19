package com.github.cunvoas.geoserviceisochrone.extern.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

class TestPhotoHelper {

	private final PhotoHelper tested = new PhotoHelper();

	@TempDir
	Path tempDir;

	private File photo() throws URISyntaxException {
		return new File(getClass().getResource("/img/IMG_20231001_120504785.jpg").toURI());
	}

	@Test
	void testResize() throws Exception {
		File out = new File(tempDir.toFile(), "le_parc.jpg");

		tested.resizeImage(photo(), out);

		assertTrue(out.exists(), "l'image redimensionnée doit être écrite");
		assertTrue(out.length() > 0, "l'image redimensionnée ne doit pas être vide");
	}

	@Test
	void testExif() throws Exception {
		Coordinate coord = tested.getCoordinateFromExif(photo());

		assertNotNull(coord, "l'image de test doit contenir des coordonnées GPS");
		assertNotNull(coord.getLatitude());
		assertNotNull(coord.getLongitude());
	}

	@Test
	void testCoord() {
		String lat = " N 50° 38' 14,27\"";
		String lon = " E 3° 3' 52,79\"";

		assertEquals(50.637297222222216d, tested.getDecimalGps(lat));
		assertEquals(3.064663888888889d, tested.getDecimalGps(lon));
	}
}
