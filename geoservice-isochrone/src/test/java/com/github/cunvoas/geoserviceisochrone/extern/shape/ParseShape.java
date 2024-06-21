package com.github.cunvoas.geoserviceisochrone.extern.shape;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.util.factory.Hints;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;

import lombok.extern.java.Log;

@Log
public class ParseShape {

	private static org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory(new PrecisionModel(), 4326);
	
	
	public static Polygon convertLambertToWSG2(MultiPolygon mPoly) throws Exception {
		Hints hints = new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
		log.info("EPSG");
		
		
		CRSAuthorityFactory factory = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", hints);
		log.info("EPSG:4326");
		
		CoordinateReferenceSystem crsWGS84 = factory.createCoordinateReferenceSystem("EPSG:4326");
		// CoordinateReferenceSystem crsWGS84 = CRS.decode("EPSG:4326");

		log.info("EPSG:2154"); //GRS 1980
		CoordinateReferenceSystem crsLambert72 = CRS.decode("EPSG:2154");

//		System.out.println("WGS84: " + crsWGS84.toWKT());
//		System.out.println("Lambert72: " + crsLambert72.toWKT());

		// transforming from LAMBERT72 to WGS84
		MathTransform transformLambert72ToWGS84 = CRS.findMathTransform(crsLambert72, crsWGS84);

		
		// TODO here
		List<Coordinate> coords = new ArrayList<>();
		for (Coordinate srcCoord : mPoly.getCoordinates()) {
			Coordinate dstCoord = new Coordinate();
			JTS.transform(srcCoord, dstCoord, transformLambert72ToWGS84);
			
			

//			System.out.println("srcCoord : " + srcCoord);
//			System.out.println("dstCoord : " + dstCoord);
			
			
			coords.add(dstCoord);
		}
		
//		Coordinate c10 = new Coordinate(148378.77, 172011.96);
//		Coordinate c11 = new Coordinate();
//		JTS.transform(c10, c11, transformLambert72ToWGS84);
//		System.out.println("c10 : " + c10);
//		System.out.println("c11 : " + c11);
		
		
		
		Polygon polygon= null;
		Coordinate[] array = coords.toArray(Coordinate[]::new);
		polygon = (Polygon)geometryFactory.createPolygon(array);
		return polygon;
		
	}

//	public static Geometry convertLambertToWSG(BoundingBox bb) throws Exception {
//
//		CoordinateReferenceSystem srcLambert = CRS.decode("EPSG:31370");
//		CoordinateReferenceSystem trgWsg = CRS.decode("EPSG:4326");
//
//		MathTransform transform = CRS.findMathTransform(srcLambert, trgWsg, false);
//
//		org.locationtech.jts.geom.GeometryFactory geometryFactory = new org.locationtech.jts.geom.GeometryFactory(new PrecisionModel(), 4326);
//		Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
//		Point targetPoint = (Point) JTS.transform(point, transform);
//
//		DirectPosition dp = transform.transform(bb.getCoordinateReferenceSystem(), null);
//
//		return null;
//	}

	public static void main(String[] args) {
		File file = new File("/work/PERSO/ASSO/data/grille200m_shp/grille200m_metropole_shp/grille200m_metropole.shp");

		try {
			Map<String, String> connect = new HashMap<>();
			connect.put("url", file.toURI().toString());

			DataStore dataStore = DataStoreFinder.getDataStore(connect);

			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];

//			System.out.println("Reading content " + typeName);

			FeatureSource featureSource = dataStore.getFeatureSource(typeName);
			FeatureCollection collection = featureSource.getFeatures();
			FeatureIterator iterator = collection.features();
			int i = 0;
			
			try {
				while (iterator.hasNext()) {
					Feature feature = iterator.next();
					
					String id200m=null;
					String id1000m=null;
					MultiPolygon mpoly=null;
					SimpleFeatureImpl sfi = (SimpleFeatureImpl)feature;
					
					for (Object o : sfi.getAttributes()) {
						log.info(o.toString());
						
						if (o instanceof MultiPolygon) {
							mpoly = (MultiPolygon)o;
							
						} else if (o instanceof String) {
							String s = (String)o;
							if (s.contains("200m")) {
								id200m = s;
							} else  if (s.contains("1000m")) {
								id1000m = s;
							}
						}
					}
					
					

					GeometryAttribute sourceGeometry = feature.getDefaultGeometryProperty();
				    System.out.println(sourceGeometry);
					
//					System.out.println(feature.getDescriptor());
//					System.out.println(feature.getType());
//				    System.out.println(sourceGeometry.getBounds());
//		      System.out.println(feature.getName());
//		      System.out.println(feature.getDescriptor());
//		      System.out.println(feature.getProperties());
//		      
//					System.out.println(sourceGeometry.getUserData());
//					Polygon p = convertLambertToWSG2(sourceGeometry.getBounds());
					Polygon p = convertLambertToWSG2(mpoly);
					
					
					
					StringBuilder sb = new StringBuilder();
					sb.append(id200m).append(";").append(id1000m);
					sb.append(";").append(GeometryQueryHelper.toText(p));
					
					System.out.println(sb.toString());
					i++;
					if (i > 3) {
						break;
					}

				}
			} finally {
				iterator.close();
			}

		} catch (Throwable e) {
			System.err.println(e);
		}

	}

}
