package com.github.cunvoas.geoserviceisochrone.extern.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.extern.mel.CsvRbxJardinFamilleParser;
import com.github.cunvoas.geoserviceisochrone.extern.mel.CsvRbxParkJardinParser;
import com.github.cunvoas.geoserviceisochrone.extern.mel.CsvTrgParkJardinParser;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapService;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkService;

import jakarta.transaction.Transactional;



@SpringBootTest
@ActiveProfiles({"prod","dev"})
class TestCsvParkEntranceUpdateParser {
	
	
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
//	@Disabled
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
	//@Disabled
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
