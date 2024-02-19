package com.github.cunvoas.geoserviceisochrone.extern.helper;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

public class GeoShapeHelper {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
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
	
	/**
	 * Merge polygons and remove holes inside.
	 * @param poly1
	 * @param poly2
	 * @return Polygon without hole.
	 */
	public static Polygon mergePolygonsWithoutHoles(Polygon poly1, Polygon poly2) {
		
		if (poly1==null && poly2!=null) {
			return poly2;
		} else if (poly1!=null && poly2==null) {
			return poly1;
		} else if (poly1==null &&  poly2==null){
			return null;
		}
		
		Polygon ret = (Polygon)poly1.union(poly2);
		if (ret.getNumInteriorRing()>0) {
			LinearRing ext = ret.getExteriorRing();
			ret = factory.createPolygon(ext.getCoordinates());
		}
		return ret;
	}
}
