package com.github.cunvoas.geoserviceisochrone.extern.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.GeoJsonObject;
import org.geojson.LngLatAlt;
import org.geojson.MultiPolygon;
import org.geojson.Polygon;
import org.locationtech.jts.geom.Geometry;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GeomanGeojsonParser {
	
	private ObjectMapper mapper =  new ObjectMapper();
	
	public Geometry parse(String geoman) {
		Geometry geom=null;
		//FeatureCollection featureCollection =  mapper.readValue(geoman, FeatureCollection.class);
		
		return geom;
	}

	public static void main(String[] args) {
		
		File test = new File("/work/PERSO/ASSO/parcs_jardins.geojson");
		
		List<POCpojo> pojos=new ArrayList<>();
		
		try ( FileInputStream fis = new FileInputStream(test) ){
			
			FeatureCollection featureCollection =  new ObjectMapper().readValue(fis, FeatureCollection.class);
			for (Feature feature : featureCollection.getFeatures()) {
				GeoJsonObject geom = feature.getGeometry();
				
				POCpojo data = new POCpojo();
				pojos.add(data);
				data.setId(feature.getProperty("id").toString().trim().replaceAll(".0",""));
				data.setNom(feature.getProperty("nom").toString().trim());
				
				if (geom instanceof org.geojson.Polygon) {
					Polygon poly = (Polygon)geom;
					List<List<LngLatAlt>> l2s = poly.getCoordinates();
					StringBuffer sbPoly = new StringBuffer("SRID=4326;POLYGON (");
					
					// liste de poly
					int i2=0;
					for (List<LngLatAlt> l1s : l2s) {
						if (i2>0) {
							sbPoly.append(", ");
						}
						sbPoly.append(processPolygon(l1s));
						i2++;
					}
					sbPoly.append(")");
					data.setGeom(sbPoly.toString());
					
			    } else if (geom instanceof org.geojson.MultiPolygon) {
					
					MultiPolygon mpoly = (MultiPolygon)geom;
					List<List<List<LngLatAlt>>> l3s = mpoly.getCoordinates();
					StringBuffer sbMulti = new StringBuffer("SRID=4326;MULTIPOLYGON (");
					int i3=0;
					for (List<List<LngLatAlt>> l2s : l3s) {
						if (i3>0) {
							sbMulti.append(", ");
						}
						// liste de poly
						int i2=0;
						for (List<LngLatAlt> l1s : l2s) {
							if (i2>0) {
								sbMulti.append(", ");
							}
							sbMulti.append("(");
							
							/*
							// liste de points > 1 poly
							int nbPts=0;
							StringBuffer sbPoints = new StringBuffer("(");
							for (LngLatAlt pts : l1s) {
								if (nbPts>0) {
									sbPoints.append(",");
								}
								sbPoints.append(pts.getLongitude());
								sbPoints.append(" ");
								sbPoints.append(pts.getLatitude());
								nbPts++;
							}
							sbPoints.append(")");
							//fin points
							 * */
							
							sbMulti.append(processPolygon(l1s));
							sbMulti.append(")");
							i2++;
						}
						i3++;
					}
					sbMulti.append(")");
					System.out.println(sbMulti.toString()+"\n\n");
					
					
					data.setGeom(sbMulti.toString());
					//break;
				}
				
				System.out.println(data.toSqlUpdateByNom());
			}
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		

		File fOut = new File("/home/cus/exchange/out_geojson.sql");
		try {
			Writer writer = new BufferedWriter(new FileWriter(fOut));
			
			for (POCpojo poCpojo : pojos) {
				writer.write(poCpojo.toSqlUpdateByNom());
				writer.write("\n");
				writer.flush();
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	static StringBuffer processPolygon(List<LngLatAlt> l1s) {
		// liste de points > 1 poly
		int nbPts=0;
		StringBuffer sbPoints = new StringBuffer("(");
		for (LngLatAlt pts : l1s) {
			if (nbPts>0) {
				sbPoints.append(",");
			}
			sbPoints.append(pts.getLongitude());
			sbPoints.append(" ");
			sbPoints.append(pts.getLatitude());
			nbPts++;
		}
		sbPoints.append(")");
		//fin points
		
		return sbPoints;
	}

}
