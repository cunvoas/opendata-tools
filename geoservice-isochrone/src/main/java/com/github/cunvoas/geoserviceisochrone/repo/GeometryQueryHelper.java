package com.github.cunvoas.geoserviceisochrone.repo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.geolatte.geom.G2D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

public class GeometryQueryHelper {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
	public static String toText(Polygon p) {
		return "SRID=4326;"+p.toText();
	}
	public static String toText(Geometry p) {
		return "SRID=4326;"+p.toText();
	}
	
	public static Polygon cast(Object in) {
		return cast((org.geolatte.geom.Polygon) in);
	}
	
	public static Polygon cast(org.geolatte.geom.Polygon in) {
		Polygon poly = factory.createPolygon();
		List<Coordinate> coords = new ArrayList<>();
		
		
		for (Object pos : in.getPositions()) {
			G2D p =(G2D)pos;
			
			Coordinate dtoCoord = new Coordinate(p.getCoordinate(0), p.getCoordinate(1));
			coords.add(dtoCoord);
		}
		
		Coordinate[] array = coords.toArray(Coordinate[]::new);
		

		Geometry geo=null;
		try {
			geo = factory.createPolygon(array);
		} catch (Exception ignore) {
			geo = factory.createLineString(array);
		}
			
		if (geo instanceof Polygon) {
			poly=(Polygon)geo;
		} else {
			poly=generatePoly((LineString)geo, 200d);
		}
		return poly;
	}
	
	
	private static Polygon generatePoly(LineString line, double offset) {

	    Coordinate[] points = line.getCoordinates();

	    ArrayList<Coordinate> soln = new ArrayList<>();
	    //store initial points
	    soln.addAll(Arrays.asList(points));
	    // reverse the list
	    ArrayUtils.reverse(points);
	    // for each point move offset metres right 
	    for (Coordinate c:points) {
	      soln.add(new Coordinate(c.x+offset, c.y));
	    }
	    // close the polygon
	    soln.add(soln.get(0));
	    // create polygon
	    Polygon poly = factory.createPolygon(soln.toArray(new Coordinate[] {}));
	    return poly;
	  }
}
