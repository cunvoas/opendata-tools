package com.github.cunvoas.geoserviceisochrone.service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceParcPrefecture;


@SpringBootTest
@ActiveProfiles({"prod","dev"})
class TestReversePrefectureReverse {

	
	@Autowired
	private ServiceParcPrefecture serviceParcPrefecture;
	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	

	/**
	 * Compute and affect parks from prefecture.
	 * @throws Exception
	 */
	@Test
//	@Disabled
	void testUpdate() throws Exception {
		serviceParcPrefecture.update();
	}
	
	@Test
	@Disabled
	void testImport() throws Exception {
		String file ="/work/PERSO/github/opendata-tools/prefecture/20812-garden-prepared.json";
		String text = new String(Files.readAllBytes(Paths.get(file)), StandardCharsets.UTF_8);
		

		JSONObject jsonObj = new JSONObject(text);
		int nbParc=0;
		JSONObject x = (JSONObject) jsonObj.get("x");
		JSONArray calls = (JSONArray) x.get("calls");
		for (int i = 0; i < calls.length(); i++) {
			
			System.out.println("calls "+i);
			JSONObject sub = (JSONObject) calls.get(i);
			displayObj(sub);
			
			if (i==0 && false) { // qu'est-ce? 17k items
				JSONArray arg = (JSONArray)sub.get("args");
				displayObj(arg);
				for (int j1 = 0; j1 < arg.length(); j1++) {
					

					Polygon polygon= null;
					List<Coordinate> coords = new ArrayList<>();
					
					JSONArray niv1 = (JSONArray)arg.get(j1);
					for (int j2 = 0; j2 < niv1.length(); j2++) {
						JSONArray niv2 = (JSONArray) niv1.get(j2);
						
						for (int j3 = 0; j3 < niv2.length(); j3++) {
							JSONArray niv3 = (JSONArray) niv2.get(j3);
							// shape of a park  [{lng:[]}, {lat :[]}]
							
							JSONObject niv4 = (JSONObject) niv3.get(0);
							JSONArray lng = (JSONArray)niv4.get("lng");
							JSONArray lat = (JSONArray)niv4.get("lat");
							
							nbParc++;
//							displayObj(lng);
//							displayObj(lat);
							
							// transform to GeoGson true impl
							
							for (int ii  = 0; ii <lng.length(); ii++) {
								Coordinate dtoCoord = new Coordinate(lng.getDouble(ii), lat.getDouble(ii));
								coords.add(dtoCoord);
							}
							coords.add(coords.get(0));
							
							Coordinate[] array = coords.toArray(Coordinate[]::new);
							polygon = (Polygon)factory.createPolygon(array);
							System.out.println(polygon);
							
							serviceParcPrefecture.prepareFromSite(null, polygon);
						}
					}
				}
			}
			

			if (i==1  ) { // parls areas 1802 items
				JSONArray arg = (JSONArray)sub.get("args");
				displayObj(arg);
				
				JSONArray shapes = (JSONArray)arg.get(0);
				displayObj(shapes);
				JSONArray names = (JSONArray)arg.get(6);
				displayObj(names);
				
				for (int j1 = 0; j1 < shapes.length(); j1++) {
					JSONArray niv1 = (JSONArray)shapes.get(j1);
//					displayObj(niv1);
					
					Polygon polygon= null;
					List<Coordinate> coords = new ArrayList<>();
					
					for (int j2 = 0; j2 < niv1.length(); j2++) {
						JSONArray niv2 = (JSONArray) niv1.get(j2);
//						displayObj(niv2);
						
							// shape of a park  [{lng:[]}, {lat :[]}]
//							displayObj(niv3);
							
							JSONObject niv4 = (JSONObject) niv2.get(0);
//							displayObj(niv4);
							String nom=null;
							if (names.length()>j1) {
								nom = (String)names.get(j1);
								System.out.println(nom);
							} else  {
								System.out.println("nom inconnu");
							}
							JSONArray lng = (JSONArray)niv4.get("lng");
							JSONArray lat = (JSONArray)niv4.get("lat");
							
							for (int ii  = 0; ii <lng.length(); ii++) {
								Coordinate dtoCoord = new Coordinate(lng.getDouble(ii), lat.getDouble(ii));
								coords.add(dtoCoord);
							}
							System.out.println(coords.get(0));
							System.out.println(coords.get(coords.size()-1));
							coords.add(coords.get(0));
							
							Coordinate[] array = coords.toArray(Coordinate[]::new);
							polygon = (Polygon)factory.createPolygon(array);
							System.out.println(polygon);
							
							serviceParcPrefecture.prepareFromSite(nom, polygon);
							
							
							nbParc++;
							
							
//							System.out.println(String.format("%s %s %s", j1, j2, j3));
//							displayObj(lng);
//							displayObj(lat);
													
							System.out.println("https://www.google.com/maps/@"+lat.get(0)+","+lng.get(0)+",14.13z?entry=ttu");
							
							//TODO transform to GeoGson true impl
						
						}
					
					}
				System.out.println("nbParc "+nbParc);
				}
				
			
		}
		
		
		System.out.println(nbParc);
	}
	
	private void displayObj(Object o) {
		if (o!=null) {
			int len = o.toString().length()-1;
			System.out.println(o.toString().subSequence(0, Math.min(len, 1000)));
		} else {
			System.out.println("displayObj null");
		}
	}

}
