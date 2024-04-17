package com.github.cunvoas.geoserviceisochrone.service.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

class TestGeoMapService {
	
	GeoMapService tested=new GeoMapService();


	@Test
	@Disabled
	void testColor() {
		for (int i = 0; i < 20; i++) {
			System.out.println(tested.getColor(i));
		}
	}
	

	@Test
	void testColorGet() {
		System.out.println("testColorGet");
		for (Double i = 0.0; i < 10; i+=0.1) {
//			
//			Double iSph = 224-i*10;
//			Long sph = Math.round(iSph);
//			System.out.println( Integer.toString(Ints.checkedCast(sph), 16));
			
			System.out.println( tested.getColorGrey(i));
			
		}
	}
	
	@Test
	void testGetPolygonFromBounds() {
		

		Double x1=  3.125267028808594d;
		Double x2=  2.9675102233886723d;
		Double y1= 50.65675247614678d;
		Double y2= 50.61320139365915d;
		

		String sBound="{ \"_southWest\": { \"lat\": 50.60677419392376, \"lng\": 3.0161762237548833 }, \"_northEast\": { \"lat\": 50.6503312283444, \"lng\": 3.173933029174805 } }}";

		try {
			assertInstanceOf(Polygon.class, tested.getPolygonFromBounds(sBound));
		} catch (Exception e) {
			fail(e.getMessage());
		}	
	}
	


	@Test
	void testCheckDistance() {
		

		Double x1=  3.125267028808594d;
		Double x2=  2.9675102233886723d;
		Double y1= 50.65675247614678d;
		Double y2= 50.61320139365915d;
		
		Coordinate southWest = new Coordinate(x1,y1);
    	Coordinate northEast = new Coordinate(x2,y2);
    	
    	assertTrue (tested.checkDistance(southWest, northEast));
	}
	

	@Test
	void testSurface() {
		Polygon carre = makeZone(0d,10d, 0d,10d);
		System.out.println( carre.getArea() );
		assertEquals(100d, carre.getArea(), "aire carre");

		Polygon jardin1 = makeZone(-10d,10d, -10d,5d);
		System.out.println( jardin1.getArea() );
		assertEquals(300d, jardin1.getArea(), "aire jardin1");
		
		Polygon jardin2 = makeZone(0d,5d, -5d,10d);
		System.out.println( jardin2.getArea() );
		assertEquals(75d, jardin2.getArea(), "aire jardin2");
		
		
		
		Polygon allJardin = (Polygon)jardin1.union(jardin2);
	//	allJardin.getExteriorRing()
		System.out.println( allJardin.getArea() );
		assertEquals(325d, allJardin.getArea(), "aire allJardin");
		
		
		Polygon intersec = (Polygon)carre.intersection(allJardin);
		System.out.println( intersec.getArea() );
		assertEquals(75d, intersec.getArea(), "aire intersec");
    	
	}
	

	@Test
	void testHole() {
		Polygon zone1 = makeZone(0d,2d, 0d,10d);
		System.out.println( zone1.getArea() );
		assertEquals(20d, zone1.getArea(), "aire zone1");

		Polygon zone2 = makeZone(8d,10d, 0d,10d);
		System.out.println( zone2.getArea() );
		assertEquals(20d, zone2.getArea(), "aire zone2");

		Polygon zone3 = makeZone(2d,10d, 0d,2d);
		System.out.println( zone3.getArea() );
		assertEquals(16d, zone3.getArea(), "aire zone3");

		Polygon zone4 = makeZone(0d,10d, 8d,10d);
		System.out.println( zone4.getArea() );
		assertEquals(20d, zone4.getArea(), "aire zone4");

		
		Polygon allZone = (Polygon)zone1.union(zone2).union(zone3).union(zone4);
		System.out.println( allZone.getArea() );
		assertEquals(64d, allZone.getArea(), "aire allZone");
		
		assertEquals(100d, allZone.getEnvelope().getArea(), "aire allZone.getBoundary");

		
	}

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
	static Polygon makeZone(Double x1, Double x2, Double y1, Double y2) {    	
    	List<Coordinate> coords = new ArrayList<>();
    	coords.add(new Coordinate(x1,y1) );
    	coords.add(new Coordinate(x1,y2) );
    	coords.add(new Coordinate(x2,y2) );
    	coords.add(new Coordinate(x2,y1) );
    	coords.add(new Coordinate(x1,y1) );
    	
    	Coordinate[] array = coords.toArray(Coordinate[]::new);
		return (Polygon)factory.createPolygon(array).getEnvelope();
    }


}
