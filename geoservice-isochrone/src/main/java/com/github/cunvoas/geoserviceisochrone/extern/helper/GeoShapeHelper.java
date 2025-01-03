package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.util.GeometricShapeFactory;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionGeo;

/**
 * 
 */
public class GeoShapeHelper {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	private static GeometricShapeFactory shapeFactory = new GeometricShapeFactory(factory);
	
	
	public static Point getPoint(com.github.cunvoas.geoserviceisochrone.model.Coordinate geoPoint) {
		Point point= null;
		Double lat = geoPoint.getLatitude();
		Double lng = geoPoint.getLongitude();
		
		point = factory.createPoint(new Coordinate(lng, lat));
		return point;
	}
	/**
	 * 
	 * @param geoPoint Lille 3,50.
	 * @return
	 */
	public static Point parsePointLatLon(String geoPoint) {
		Point point= null;
		
		String[] coords = geoPoint.split(",");
		Double lat = Double.valueOf(coords[0].trim());
		Double lng = Double.valueOf(coords[1].trim());
		
		point = factory.createPoint(new Coordinate(lng, lat));
		return point;
	}
	
	
	public static Point getPoint(Double lng, Double lat) {
		Point point= null;
		point = factory.createPoint(new Coordinate(lng, lat));
		return point;
	}
	
	/**
	 *  @param geoPoint Lille 50, 3.
	 * @return
	 */
	public static Point parsePointLatLng(String geoPoint) {
		Point point= null;
		
		String[] coords = geoPoint.split(",");
		Double lat = Double.valueOf(coords[0].trim());
		Double lng = Double.valueOf(coords[1].trim());
		point = factory.createPoint(new Coordinate(lng, lat));
		return point;
		
	}
	
	/**
	 *  @param geoPoint Lille 3,50
	 * @return
	 */
	public static Point parsePointLngLat(String geoPoint) {
		Point point= null;
		
		String[] coords = geoPoint.split(",");
		Double lng = Double.valueOf(coords[0].trim());
		Double lat = Double.valueOf(coords[1].trim());
		point = factory.createPoint(new Coordinate(lng, lat));
		return point;
		
	}
	
	
	/**
	 *  @param string "{"coordinates": [[[9.209601633281576, 41.36781596374774], [9.209578689845031, 41.36962406021186], [9.211959235983402, 41.36964287456159], [9.211982225364531, 41.367834769580924], [9.209601633281576, 41.36781596374774]]]
	 *                     , "type": "Polygon"}"
	 * @param geoPolygon
	 * @return
	 */
	public static Polygon parsePolygon(String geoPolygon) {
		Polygon polygon= null;
		
		JSONObject geoShape = new JSONObject(geoPolygon);
		
		String type = geoShape.getString("type");
		if ("Polygon".equals(type)) {
			
			List<Coordinate> coords = new ArrayList<>();
			JSONArray coordinates = geoShape.getJSONArray("coordinates");
			for (int i = 0; i < coordinates.length(); i++) {
				JSONArray sub = (JSONArray) coordinates.get(i);
				for (int j = 0; j < sub.length(); j++) {
					JSONArray coord = (JSONArray) sub.get(j);
					Coordinate dtoCoord = new Coordinate(coord.getDouble(0), coord.getDouble(1));
					coords.add(dtoCoord);
				}
			}
			Coordinate[] array = coords.toArray(Coordinate[]::new);
			polygon = (Polygon)factory.createPolygon(array).getEnvelope();
		}
		
		return polygon;
	}
	
	public static Polygon mergePolygonsWithoutHoles(Polygon poly1, Polygon poly2) {

		if (poly1==null && poly2!=null) {
			return poly2;
		} else if (poly1!=null && poly2==null) {
			return poly1;
		} else if (poly1==null &&  poly2==null){
			return null;
		}

		
		Polygon ret;
		try {
			Geometry geo = poly1.union(poly2);
			ret = null;
			if (geo instanceof Polygon) {
				ret = (Polygon) geo;
				// remove holes
				if (ret.getNumInteriorRing()>0) {
					LinearRing ext = ret.getExteriorRing();
					ret = factory.createPolygon(ext.getCoordinates());
				}
			} else if (geo instanceof MultiPolygon) {
				Coordinate[] coords = geo.getBoundary().getCoordinates();
				int size = coords.length;
				coords = Arrays.copyOf(coords, size + 1);
				coords[size] = coords[0];
				ret = factory.createPolygon(coords);
			}
		} catch (Exception mergeErr) {
			ret = mergePolygonsWithoutHolesConvex(poly1, poly2);
		}
		return ret;
		
	}
	
	/**
	 * Merge polygons and remove holes inside.
	 * @param poly1
	 * @param poly2
	 * @return Polygon without hole.
	 */
	protected static Polygon mergePolygonsWithoutHolesConvex(Polygon poly1, Polygon poly2) {
		
		if (poly1==null && poly2!=null) {
			return poly2;
		} else if (poly1!=null && poly2==null) {
			return poly1;
		} else if (poly1==null &&  poly2==null){
			return null;
		}

		// transform to a convec poly to prevent holes
		Geometry geom1 = validate(poly1.convexHull());
		Geometry geom2 = validate(poly2.convexHull());
		if (geom1 instanceof Polygon) {
			poly1 = (Polygon)geom1;
		}
		if (geom2 instanceof Polygon) {
			poly2 = (Polygon)geom2;
		}
		
		Geometry geo = poly1.union(poly2);
		Polygon ret = null;
		if (geo instanceof Polygon) {
			ret = (Polygon) geo;

			// remove holes
			if (ret.getNumInteriorRing()>0) {
				LinearRing ext = ret.getExteriorRing();
				ret = factory.createPolygon(ext.getCoordinates());
			}
		} else if (geo instanceof MultiPolygon) {
			Coordinate[] coords = geo.getBoundary().getCoordinates();
			int size = coords.length;
			coords = Arrays.copyOf(coords, size + 1);
			coords[size] = coords[0];
			
			ret = factory.createPolygon(coords);
			
		} else {
			throw new ExceptionGeo(ExceptionGeo.MERGE);
		}
		return ret;
	}
	
	
	
	//https://stackoverflow.com/questions/31473553/is-there-a-way-to-convert-a-self-intersecting-polygon-to-a-multipolygon-in-jts
	
	/**
	 * Get / create a valid version of the geometry given. If the geometry is a polygon or multi polygon, self intersections /
	 * inconsistencies are fixed. Otherwise the geometry is returned.
	 * 
	 * @param geom
	 * @return a geometry 
	 */
	@SuppressWarnings("unchecked")
	public static Geometry validate(Geometry geom){
	    if(geom instanceof Polygon){
	        if(geom.isValid()){
	            geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
	            return geom; // If the polygon is valid just return it
	        }
	        Polygonizer polygonizer = new Polygonizer();
	        addPolygon((Polygon)geom, polygonizer);
	        return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
	    }else if(geom instanceof MultiPolygon){
	        if(geom.isValid()){
	            geom.normalize(); // validate does not pick up rings in the wrong order - this will fix that
	            return geom; // If the multipolygon is valid just return it
	        }
	        Polygonizer polygonizer = new Polygonizer();
	        for(int n = geom.getNumGeometries(); n-- > 0;){
	            addPolygon((Polygon)geom.getGeometryN(n), polygonizer);
	        }
	        return toPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
	    }else{
	        return geom; // In my case, I only care about polygon / multipolygon geometries
	    }
	}

	/**
	 * Add all line strings from the polygon given to the polygonizer given
	 * 
	 * @param polygon polygon from which to extract line strings
	 * @param polygonizer polygonizer
	 */
	static void addPolygon(Polygon polygon, Polygonizer polygonizer){
	    addLineString(polygon.getExteriorRing(), polygonizer);
	    for(int n = polygon.getNumInteriorRing(); n-- > 0;){
	        addLineString(polygon.getInteriorRingN(n), polygonizer);
	    }
	}

	/**
	 * Add the linestring given to the polygonizer
	 * 
	 * @param linestring line string
	 * @param polygonizer polygonizer
	 */
	static void addLineString(LineString lineString, Polygonizer polygonizer){

	    if(lineString instanceof LinearRing){ // LinearRings are treated differently to line strings : we need a LineString NOT a LinearRing
	        lineString = lineString.getFactory().createLineString(lineString.getCoordinateSequence());
	    }

	    // unioning the linestring with the point makes any self intersections explicit.
	    Point point = lineString.getFactory().createPoint(lineString.getCoordinateN(0));
	    Geometry toAdd = lineString.union(point); 

	    //Add result to polygonizer
	    polygonizer.add(toAdd);
	}

	/**
	 * Get a geometry from a collection of polygons.
	 * 
	 * @param polygons collection
	 * @param factory factory to generate MultiPolygon if required
	 * @return null if there were no polygons, the polygon if there was only one, or a MultiPolygon containing all polygons otherwise
	 */
	static Geometry toPolygonGeometry(Collection<Polygon> polygons, GeometryFactory factory){
	    switch(polygons.size()){
	        case 0:
	            return null; // No valid polygons!
	        case 1:
	            return polygons.iterator().next(); // single polygon - no need to wrap
	        default:
	            //polygons may still overlap! Need to sym difference them
	            Iterator<Polygon> iter = polygons.iterator();
	            Geometry ret = iter.next();
	            while(iter.hasNext()){
	                ret = ret.symDifference(iter.next());
	            }
	            return ret;
	    }
	}
	
	

	
	/**
	 *  converti le geoshape en YX en XY.
	 *  X=longitude
	 *  Y=latitude
	 *  @param in
	 * @return
	 */
	public static String geoShape2SRID(String in) {
		if (in.contains("MULTIPOLYGON")) {
			return doMULTIPOLYGON(in);
		} else if (in.contains("POLYGON")) {
			return doPOLYGON(in);
		} else {
			return null;
		}
	}
	
	/**
	 * converti le geoshape en YX en XY.
	 * @param geoshape
	 * @return
	 */
	private static String doMULTIPOLYGON(String geoshape) {
		StringBuilder ret=new StringBuilder();
		ret.append("SRID=4326;MULTIPOLYGON (((");
		
		// début de geometrie
		String[] base = geoshape.split("MULTIPOLYGON \\(\\(\\(");
		// fin de geometrie
		String[] base2 = base[1].split("\\)\\)\\)");
		
		String polys = base2[0];
		
		// séparation des polygones
		String[] tpoints = polys.split("\\)\\), \\(\\(");
		for (int i = 0; i < tpoints.length; i++) {
			if (i>0) {
				// séparation entre polygons
				ret.append(")), ((");
			}
			ret.append(performPoints(tpoints[i]));
		}
		
		ret.append(")))");
		return ret.toString();
	
	}

	/**
	 *  converti le geoshape en YX en XY.
	 *  @param geoshape
	 * @return
	 */
	private static String doPOLYGON(String geoshape) {
		StringBuilder ret=new StringBuilder();
		ret.append("SRID=4326;POLYGON ((");
		String[] base = geoshape.split("POLYGON \\(\\(");
		String[] base2 = base[1].split("\\)\\)");
		
		System.out.println(base2.length);
		String data = base2[0];
		
		ret.append(performPoints(data));
		
		ret.append("))");
		return ret.toString();
	}
	
	private static StringBuilder performPoints(String data) {
		StringBuilder ret=new StringBuilder();
		String memo1st=null;
		// separation des points
		String[] work = data.split(", ");
		for (int i = 0; i < work.length; i++) {
			String[] point = work[i].split(" ");
			String pointXY = point[1]+" "+point[0];
			if (i>0) {
				ret.append(", ");
			} else {
				memo1st = pointXY;
			}
			ret.append(pointXY);
			
			if (i==work.length-1) {
				// controle polygon fermé
				if (pointXY!=memo1st)  {
					// si pas fermé, on ajoute le 1er pour le fermer
					ret.append(", ").append(memo1st);
				}
			}
		}
		return ret;
	}
}
