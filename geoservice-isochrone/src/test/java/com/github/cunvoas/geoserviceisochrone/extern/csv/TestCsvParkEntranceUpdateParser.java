package com.github.cunvoas.geoserviceisochrone.extern.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.geojson.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.extern.mel.CsvRbxJardinFamilleParser;
import com.github.cunvoas.geoserviceisochrone.extern.mel.CsvRbxParkJardinParser;
import com.github.cunvoas.geoserviceisochrone.extern.mel.CsvTrgParkJardinParser;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkService;



@SpringBootTest
@ActiveProfiles({"prod","dev"})
class TestCsvParkEntranceUpdateParser {
	

	private CsvParkGeomUpdateParser testedGeomUpdate = new CsvParkGeomUpdateParser();
	private CsvParkEntranceParser tested = new CsvParkEntranceParser();
	private CsvParkUpdateParser testedUpdate = new CsvParkUpdateParser();
	private CsvRbxParkJardinParser testedParcRbx = new CsvRbxParkJardinParser();
	private CsvRbxJardinFamilleParser testedJardinRbx = new CsvRbxJardinFamilleParser();
	private CsvTrgParkJardinParser testedParcTrg = new CsvTrgParkJardinParser();
	
	@Autowired
	private CityRepository cityRepository;
	@Autowired
	private ParkJardinRepository parcEtJardinRepository;

	@Autowired
	private ParkService parkService;
	
	@Test
	void testParseGeom() {
//		File test = new File("/work/PERSO/ASSO/parcs_jardins_lille_hellemmes_lomme_emprise_deduit.csv");
		File test = new File("/work/PERSO/ASSO/parcs_jardins_lille_hellemmes_lomme_emprise_test.csv");
		
		try {
			FileInputStream fis = new FileInputStream(test);
			FeatureCollection featureCollection = 
					new ObjectMapper().readValue(fis, FeatureCollection.class);
			FeatureIterator fi = featureCollection.features();
			while(fi.hasNext()) {
				//Feature feat = fi.next();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			List<CsvParkGeomUpdate> rows = testedGeomUpdate.parseParkGeom(test);
			
			File fOut = new File("/home/cus/exchange/out_test.sql");
			
			int matched=0;
			
			for (CsvParkGeomUpdate csvParkGeomUpdate : rows) {
				String txtGeom = GeoShapeHelper.geoShape2SRID(csvParkGeomUpdate.getGeom());
				csvParkGeomUpdate.setGeom(txtGeom);
				
				/*
				org.geolatte.geom.Geometry<?> geolatteGeom = parcEtJardinRepository.getGeometryFromText(txtGeom);
				Geometry geom = GeometryQueryHelper.cast(geolatteGeom);
				
				List<ParcEtJardin> parks= parcEtJardinRepository.findByAreaAndCityId(2878L, geom);
				if (parks!=null && !parks.isEmpty()) {
					if (parks.size()==1) {
						matched++;
						ParcEtJardin pj = parks.get(0);
						System.out.println("MATCH "+pj.getName());
						
						geom.getCentroid().disjoint(pj.getCoordonnee());
						if (pj.getContour()!=null) {
							pj.setContour(geom);
							//parcEtJardinRepository.save(pj);
							System.out.println("SAVED "+pj.getName());
						}
					} else {
						System.out.println("MANY "+ parks.size() );
					}
				}
				*/
				
			}
			
			testedGeomUpdate.write(fOut, rows);
			
			System.out.println("NB MATCHED "+matched);
			
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	@Disabled
	void testParse() {
		File test = new File("/home/cus/Documents/Associations/Aut-MEL/OpenData/lomme.csv");
		try {
			List<CsvParkUpdate>rows = testedUpdate.parseParkEntrance(test);
			assertNotNull(rows, "rows");
			assertEquals(125, rows.size(),"nb rows");

			
			MassUpdatePreProcess prepross = new MassUpdatePreProcess();
			List<CsvMassUpdatePivot> preps = prepross.preProcess(rows);
			assertNotNull(preps, "prep");
			assertEquals(36, preps.size(),"nb parks");
			
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Disabled
	void testUpdateParcs() {
		File test = new File("/home/cus/Documents/Associations/Aut-MEL/OpenData/lomme_tst.csv");
		try {
			List<CsvParkUpdate>rows = testedUpdate.parseParkEntrance(test);
			
			MassUpdatePreProcess prepross = new MassUpdatePreProcess();
			List<CsvMassUpdatePivot> preps = prepross.preProcess(rows);
			
			parkService.importIsoChroneEntrance(preps);
			
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}

	@Test
	@Disabled
	void testRbxParcs() {
		File test = new File("/home/cus/Documents/Associations/Aut-MEL/OpenData/lomme.csv");
		try {
			List<ParcEtJardin> rows = testedParcRbx.parseCsv(test);
			

			// insee 59512
			City c = cityRepository.findByInseeCode("59512");
			for (ParcEtJardin parcEtJardin : rows) {
				parcEtJardin.setCommune(c);
			}
			parcEtJardinRepository.saveAll(rows);
			
			assertNotNull(rows, "rows");
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}

	
	@Test
	@Disabled
	void testTrq() {
		File test = new File("/home/cus/Documents/Associations/Aut-MEL/carte/tourcoing-parcs-et-jardins.csv");
		try {
			
			List<ParcEtJardin> rows = testedParcTrg.parseCsv(test);
			
			// insee 59599
			City c = cityRepository.findByInseeCode("59599");
			for (ParcEtJardin parcEtJardin : rows) {
				parcEtJardin.setCommune(c);
			}
			parcEtJardinRepository.saveAll(rows);
			
			assertNotNull(rows, "rows");
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
	}

	

}
