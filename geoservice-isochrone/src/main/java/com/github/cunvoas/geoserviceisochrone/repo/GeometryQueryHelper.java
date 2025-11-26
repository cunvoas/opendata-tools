package com.github.cunvoas.geoserviceisochrone.repo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.geolatte.geom.C2D;
import org.geolatte.geom.G2D;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;


/**
 * Spring JPA repository.
 * Helper to convert :
 *   - Geometry in text.
 *   - geolatte.* in locationtech.*
 *  
 */
@Component
public class GeometryQueryHelper {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
	/**
	 * toText.
	 * @param p polygon
	 * @return text of polygon
	 */
	public static String toText(Polygon p) {
		return "SRID=4326;"+p.toText();
	}
	/**
	 * toText.
	 * @param p Geometry
	 * @return text of polygon
	 */
	public static String toText(Geometry p) {
		return "SRID=4326;"+p.toText();
	}
	/**
	 * toTextWoSrid.
	 * @param p Geometry
	 * @return text of polygon without SRID
	 */
	public static String toTextWoSrid(Geometry p) {
		return p.toText();
	}
	
	/**
	 * cast geolatte into JTS.
	 * @param in geolatte Geometry
	 * @return jts Geometry
	 */
	public static Geometry cast(org.geolatte.geom.Geometry<?> in) {
		Geometry ret =null;
		if (in instanceof org.geolatte.geom.Polygon) {
			Polygon poly = cast((org.geolatte.geom.Polygon<?>) in);
			ret = poly;
		} else if (in instanceof org.geolatte.geom.MultiPolygon) {
			MultiPolygon mpoly =  cast((org.geolatte.geom.MultiPolygon<?>)in);
			ret = mpoly;
		}
		
		return ret;
	}

	/**
	 * cast geolatte into JTS.
	 * @param in geolatte MultiPolygon
	 * @return jts MultiPolygon
	 */
	public static MultiPolygon cast(org.geolatte.geom.MultiPolygon<?> in) {
		
		int nbPolys = in.getNumGeometries();
		//List<Coordinate> coords = new ArrayList<>();
		Polygon[] retPolys= new Polygon[nbPolys];
		
		for (int i = 0; i < nbPolys; i++) {
			org.geolatte.geom.Polygon<?> poly = in.getGeometryN(i);
			retPolys[i]= cast(poly);
		}
		
		MultiPolygon mpoly = factory.createMultiPolygon(retPolys);
		return mpoly;
	}

	/**
	 * cast geolatte into JTS.
	 * @param in geolatte Polygon
	 * @return jts Polygon
	 */
	public static Polygon cast(org.geolatte.geom.Polygon<?> in) {
		Polygon poly = factory.createPolygon();
		List<Coordinate> coords = new ArrayList<>();
		
		
		for (Object pos : in.getPositions()) {
			
			if (pos instanceof C2D) {
				C2D p =(C2D)pos;
				Coordinate dtoCoord = new Coordinate(p.getCoordinate(0), p.getCoordinate(1));
				coords.add(dtoCoord);
			} else if (pos instanceof G2D) {
				G2D p =(G2D)pos;
				Coordinate dtoCoord = new Coordinate(p.getCoordinate(0), p.getCoordinate(1));
				coords.add(dtoCoord);
			}
		}
		
		//Coordinate[] array = coords.toArray(Coordinate[]::new);
		Coordinate[] array = coords.toArray(new Coordinate[0]);
		

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
	

	/**
	 * generatePoly.
	 * @param line of polygon
	 * @param offset in meter at right of each point
	 * @return Polygon
	 */
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
	
	

    /**
     * getPolygonFromBounds.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
     * @return
     */
    public Polygon getPolygonFromBounds(Double swLat, Double swLng, Double neLat, Double neLng) {
    	Polygon polygon=null;
    	
    	Double x1= swLng;
    	Double x2= neLng;
    	Double y1= swLat;
    	Double y2= neLat;
    	
    	Coordinate southWest = new Coordinate(x1,y1);
    	Coordinate northEast = new Coordinate(x2,y2);
    	
    	if (checkDistance(southWest, northEast)) {
	    	List<Coordinate> coords = new ArrayList<>();
	    	coords.add( new Coordinate(x1,y1) );
	    	coords.add( new Coordinate(x1,y2) );
	    	coords.add( new Coordinate(x2,y2) );
	    	coords.add( new Coordinate(x2,y1) );
	    	coords.add( new Coordinate(x1,y1) );
	    	
//	    	Coordinate[] array = coords.toArray(Coordinate[]::new);
	    	Coordinate[] array = coords.toArray(new Coordinate[0]);

	    	polygon= factory.createPolygon(array);
    	}
    	
    	return polygon;
    }
    

    /**
     * check distance between corners to limit big request.
     * @param southWest
     * @param northEast
     * @return
     */
    protected boolean checkDistance(Coordinate southWest, Coordinate northEast) {
    	Double d =  DistanceHelper.crowFlyDistance(
	    				southWest.getY(), southWest.getX(),
	    				northEast.getY(), northEast.getX()
	    			);
    	return d<50;
    	
    }
    
}
