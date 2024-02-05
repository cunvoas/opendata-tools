package com.github.cunvoas.geoserviceisochrone.jts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.union.CascadedPolygonUnion;

class TestJts {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
	@Test
	void testDistance() {
		Point point1 = factory.createPoint(new Coordinate(0, 0));
		Point point2 = factory.createPoint(new Coordinate(0, 1));
		double dist = point1.distance(point2);
		assertEquals( 1d, dist);
	}
	
	@Test
	void testMerge() {
		
		// rotation pattern is 00,0y,xy,x0,00
		List<Coordinate> shapeBig=new ArrayList<>();	
		shapeBig.add(new Coordinate(0, 0));
		shapeBig.add(new Coordinate(0, 2));
		shapeBig.add(new Coordinate(2, 2));	
		shapeBig.add(new Coordinate(2, 0));
		shapeBig.add(new Coordinate(0, 0));
		Polygon polygonBig = factory.createPolygon(shapeBig.toArray(Coordinate[]::new));
		
		List<Coordinate> shapeSmall=new ArrayList<>();	
		shapeSmall.add(new Coordinate(0, 0));
		shapeSmall.add(new Coordinate(1, 0));
		shapeSmall.add(new Coordinate(1, 1));
		shapeSmall.add(new Coordinate(0, 1));	
		shapeSmall.add(new Coordinate(0, 0));
		Polygon polygonSmall = factory.createPolygon(shapeSmall.toArray(Coordinate[]::new));
		
		
		Polygon merged = factory.createPolygon(polygonBig.getCoordinates());
		merged = (Polygon)merged.union(polygonSmall).getEnvelope();
		
		assertEquals( merged, polygonBig, "first method UNION");
		
		
		List<Polygon> polys = new ArrayList<>();
		polys.add(polygonBig);
		polys.add(polygonSmall);
		
		
		CascadedPolygonUnion op = new CascadedPolygonUnion(polys);
		merged = (Polygon)op.union().getEnvelope();
		  
		assertEquals( merged, polygonBig, "first method UNION all");
	}
}