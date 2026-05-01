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
 * Classe utilitaire pour la manipulation de géométries (JTS).
 * Permet de créer et parser des points, polygones, etc. à partir de différentes représentations.
 */
public class GeoShapeHelper {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	private static GeometricShapeFactory shapeFactory = new GeometricShapeFactory(factory);
	
	
	/**
	 * Retourne un point JTS à partir d'un objet Coordinate personnalisé.
	 * @param geoPoint objet Coordinate (latitude/longitude)
	 * @return Point JTS correspondant
	 */
	public static Point getPoint(com.github.cunvoas.geoserviceisochrone.model.Coordinate geoPoint) {
		Point point= null;
		Double lat = geoPoint.getLatitude();
		Double lng = geoPoint.getLongitude();
		
		point = factory.createPoint(new Coordinate(lng, lat));
		return point;
	}
	/**
	 * Parse une chaîne "lat,lon" et retourne un point JTS.
	 * @param geoPoint chaîne de coordonnées (ex: "50,3")
	 * @return Point JTS correspondant
	 */
	public static Point parsePointLatLon(String geoPoint) {
		Point point= null;
		
		String[] coords = geoPoint.split(",");
		Double lat = Double.valueOf(coords[0].trim());
		Double lng = Double.valueOf(coords[1].trim());
		
		point = factory.createPoint(new Coordinate(lng, lat));
		return point;
	}
	
	
	/**
	 * Retourne un point JTS à partir de coordonnées longitude et latitude.
	 * @param lng longitude
	 * @param lat latitude
	 * @return Point JTS correspondant
	 */
	public static Point getPoint(Double lng, Double lat) {
		Point point= null;
		point = factory.createPoint(new Coordinate(lng, lat));
		return point;
	}
	
	/**
	 * Parse une chaîne "lat,lng" et retourne un point JTS.
	 * @param geoPoint chaîne de coordonnées (ex: "50,3")
	 * @return Point JTS correspondant
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
	 * Parse une chaîne "lng,lat" et retourne un point JTS.
	 * @param geoPoint chaîne de coordonnées (ex: "3,50")
	 * @return Point JTS correspondant
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
//			Coordinate[] array = coords.toArray(Coordinate[]::new);
			Coordinate[] array = coords.toArray(new Coordinate[0]);
			
			polygon = (Polygon)factory.createPolygon(array).getEnvelope();
		}
		
		return polygon;
	}
	/**
	 * Construit un Polygon JTS à partir d'une liste de Point (ordre donné).
	 * Utilise un stream pour extraire les coordonnées.
	 * Ferme le polygone si besoin.
	 */
	public static Polygon getPolygon(List<Point> jtsPoints) {
		if (jtsPoints == null || jtsPoints.size() < 3) return null;
		Coordinate[] coords = jtsPoints.stream()
			.map(Point::getCoordinate)
			.toArray(Coordinate[]::new);
		
		// Ferme le polygone si nécessaire
		if (!coords[0].equals2D(coords[coords.length - 1])) {
			coords = Arrays.copyOf(coords, coords.length + 1);
			coords[coords.length - 1] = coords[0];
		}
		return factory.createPolygon(coords);
	}
	
	
	/**
	 * Construit un MultiPolygon à partir de listes de Polygon (outers et inners).
	 * Chaque inner est associé à l'outer qui le contient (par centroïde).
	 * @param outers Liste de polygones extérieurs
	 * @param inners Liste de polygones intérieurs (trous)
	 * @return MultiPolygon JTS
	 */
	public static MultiPolygon getMultiPolygon(List<Polygon> outers, List<Polygon> inners) {
		if (outers == null || outers.isEmpty()) {
			return null;
		}
		List<Polygon> polygons = new ArrayList<>();
		List<LinearRing> innerRings = new ArrayList<>();
		if (inners != null) {
			for (Polygon inner : inners) {
				if (inner != null && inner.getNumPoints() >= 4) {
					innerRings.add(factory.createLinearRing(inner.getExteriorRing().getCoordinates()));
				}
			}
		}
		for (Polygon outer : outers) {
			if (outer == null || outer.getNumPoints() < 4) continue;
			LinearRing outerRing = factory.createLinearRing(outer.getExteriorRing().getCoordinates());
			List<LinearRing> holesForThisOuter = new ArrayList<>();
			for (LinearRing innerRing : innerRings) {
				Polygon innerPoly = factory.createPolygon(innerRing);
				if (outer.contains(innerPoly.getCentroid())) {
					holesForThisOuter.add(innerRing);
				}
			}
			Polygon poly = factory.createPolygon(outerRing, holesForThisOuter.toArray(new LinearRing[0]));
			Geometry validatedPoly = validate(poly);
			if (validatedPoly instanceof Polygon) {
				polygons.add((Polygon) validatedPoly);
			} else if (validatedPoly instanceof MultiPolygon) {
				// validate() split the polygon into multiple parts; add each one
				MultiPolygon mp2 = (MultiPolygon) validatedPoly;
				for (int i = 0; i < mp2.getNumGeometries(); i++) {
					polygons.add((Polygon) mp2.getGeometryN(i));
				}
			}
		}
		if (polygons.isEmpty()) return null;
		MultiPolygon mp = factory.createMultiPolygon(polygons.toArray(new Polygon[0]));
		Geometry validated = validate(mp);
		if (validated instanceof MultiPolygon) {
			return (MultiPolygon) validated;
		} else if (validated instanceof Polygon) {
			// Wrap single polygon in a MultiPolygon
			return factory.createMultiPolygon(new Polygon[]{(Polygon) validated});
		} else {
			// Unexpected geometry type
			return null;
		}
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
	            // buffer(0) is used to fix TopologyException caused by nearly-touching or
	            // slightly invalid geometries before performing overlay operations
	            Iterator<Polygon> iter = polygons.iterator();
	            Geometry ret = iter.next().buffer(0);
	            while(iter.hasNext()){
	                Geometry next = iter.next().buffer(0);
	                try {
	                    ret = ret.symDifference(next);
	                } catch (org.locationtech.jts.geom.TopologyException e) {
	                    // fallback: union instead of symDifference to avoid side location conflicts
	                    ret = ret.union(next);
	                }
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
	
	/**
	 * performPoints.
	 * @param data points
	 * @return StringBuilder
	 */
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
			
			// controle polygon fermé
			// si pas fermé, on ajoute le 1er pour le fermer
			if (i==work.length-1 && !pointXY.equals(memo1st)) {
				ret.append(", ").append(memo1st);
			}
		}
		return ret;
	}
}
