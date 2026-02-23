package com.github.cunvoas.geoserviceisochrone.model.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;

public class TestProjectSimulator {
	
	ProjectSimulator tested =  null;
	String demo1 = "{\"coordinates\": [[[1,5],[2,4],[4,4],[5,1],[5,-1],[6,-2],[5,-4],[4,-4],[1,-1],[-1,-1],[-2,-4],[-5,-5],[-6,0],[-3,2],[-1,2],[0,4],[1,5]]], \"type\": \"Polygon\"}";
	
	@BeforeEach
	public void init() {
		tested =  new ProjectSimulator();
		
		
		
		Polygon poly = GeoShapeHelper.parsePolygon(demo1);
		tested.setShapeArea(poly);
		Point pt = poly.getCentroid();
		tested.setCenterArea(pt);
		
		
		
	}

	@Test
	public void test() {
		assertEquals(17, tested.getShapeArea().getNumPoints() );
		
		
	}

}
