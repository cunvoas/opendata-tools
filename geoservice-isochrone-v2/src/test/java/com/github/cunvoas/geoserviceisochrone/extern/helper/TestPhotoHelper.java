package com.github.cunvoas.geoserviceisochrone.extern.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.drew.imaging.ImageProcessingException;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;

class TestPhotoHelper {

	private PhotoHelper tested = new PhotoHelper();
	private String img = "/work/PERSO/github/opendata-tools/geoservice-isochrone/src/test/resources/img/IMG_20231001_120504785.jpg";
	
	@Test
	void  testResize() {
		try {
			tested.resizeImage(new File(img), "le parc");
					
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}
	
	
	@Test
	void testExif() {
		try {
			Coordinate coord = tested.getCoordinateFromExif(new File(img));
			assertNotNull(coord);


		} catch (ImageProcessingException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	void testCoord() {
		String lat =" N 50° 38' 14,27\"";
		String lon =" E 3° 3' 52,79\"";
		
		
		
		Double val = tested.getDecimalGps(lat);
		System.out.println(val);
		assertEquals(50.637297222222216d, val);

		val = tested.getDecimalGps(lon);
		System.out.println(val);
		assertEquals(3.064663888888889d, val);
		
		
	}

}
