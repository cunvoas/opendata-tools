package com.github.cunvoas.geoserviceisochrone.model.proposal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoJson2GeometryHelper;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.util.GeoPolygonRenderer;
import com.github.cunvoas.geoserviceisochrone.util.GeometryPointReducer;

public class TestProjectSimulator {
	
	private GeoJson2GeometryHelper helper=new GeoJson2GeometryHelper();
	String demo2 = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[1,5],[2,4],[4,4],[5,1],[5,-1],[6,-2],[5,-4],[4,-4],[1,-1],[-1,-1],[-2,-4],[-5,-5],[-6,0],[-3,2],[-1,2],[0,4],[1,5]]]},\"properties\":{}}";
	 
	Geometry sourcePoly2 = null;
	Geometry sourcePoly3 = null;

	
	@BeforeEach
	public void init() {
		
		
		try {
		   sourcePoly2 = helper.parse(demo2);
		} catch (JsonProcessingException e) {
			fail(e.getMessage());
		}
		
		try {
			//chargé depuis "isochrone-1.json"
			String sPoly3 = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/test/resources/exempleGeoJson.json")), java.nio.charset.StandardCharsets.UTF_8);
			sourcePoly3 = helper.parse(sPoly3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	@Test
	public void test() throws Exception {
		GeometryPointReducer reducer=new GeometryPointReducer();
				


		Geometry r2=reducer.reduceByConvexHull(sourcePoly3);
		assertTrue(sourcePoly3.getNumPoints()>=r2.getNumPoints());
		
		Geometry r3=reducer.reduceConvexHullToMax10Min6(sourcePoly3);
		assertTrue(sourcePoly3.getNumPoints()>=r3.getNumPoints());
		assertTrue(r3.getNumPoints()>=6);
		assertTrue(r3.getNumPoints()<=10);
		
		writeGeom(sourcePoly3, r3, "Result");
		
		assertEquals(84,sourcePoly3.getNumPoints() );
		assertEquals(10,r3.getNumPoints() );
		
	}
	
	private void writeGeom(Geometry sourcePoly2, String n) throws Exception  {
		// Génération SVG
		String svg = GeoPolygonRenderer.toSVG(sourcePoly2, 400, 400);
		System.out.println(svg);
		File svgFile = new File(System.getProperty("user.dir")+"/target", "polygon-test"+n+".svg");
		try (java.io.FileWriter fw = new java.io.FileWriter(svgFile)) {
			fw.write(svg);
		}
		System.out.println("SVG saved to: " + svgFile.getAbsolutePath());
	}
	private void writeGeom(Geometry geom1, Geometry geom2, String n) throws Exception  {
		// Génération SVG avec deux géométries
		String svg = GeoPolygonRenderer.toSVG(geom1, geom2, 400, 400);
		System.out.println(svg);
		File svgFile = new File(System.getProperty("user.dir")+"/target", "polygon-test"+n+".svg");
		try (java.io.FileWriter fw = new java.io.FileWriter(svgFile)) {
			fw.write(svg);
		}
		System.out.println("SVG saved to: " + svgFile.getAbsolutePath());
	}

}