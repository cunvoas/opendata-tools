package com.github.cunvoas.geoserviceisochrone;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.extern.csv.CsvCarre200ShapeParser;
import com.github.cunvoas.geoserviceisochrone.extern.csv.CsvIrisDataParser;
import com.github.cunvoas.geoserviceisochrone.extern.geojson.IrisGeoJsonIntegratorParser;
import com.github.cunvoas.geoserviceisochrone.extern.mel.CsvLyonParkJardinParser;
import com.github.cunvoas.geoserviceisochrone.extern.mel.CsvNantesParkJardinParser;
import com.github.cunvoas.geoserviceisochrone.extern.mel.JsonToulouseParkJardinParser;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisData;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.admin.BatchJobService;
import com.github.cunvoas.geoserviceisochrone.service.map.CityService;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceIris;
import com.github.cunvoas.geoserviceisochrone.service.park.ComputeCarreServiceV3;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkService;


@SpringBootTest
@ActiveProfiles({"secret","pi"})
class TestGeoserviceIsochroneApplication {

	@Autowired
	private IrisGeoJsonIntegratorParser irisGeoJsonIntegratorParser;

	@Autowired
	private CsvIrisDataParser csvIrisDataParser;

	@Autowired
	private ServiceIris serviceIris;
	
	@Autowired
	private ComputeCarreServiceV3 computeService;

	@Autowired
	private CsvCarre200ShapeParser csvParser;

	@Autowired
	private InseeCarre200mOnlyShapeRepository repo;

	@Autowired
	private ParkService parkService;
	
	@Autowired
	private BatchJobService batchJobService;
	
	@Autowired
	private CityService cityService;

	@Autowired
	private ParkAreaRepository parkAreaRepository;

	@Test
	@Disabled
	@Order(0)
	void contextLoads() {
	}

	@Test
	@Disabled
	@Order(10)
	void loadParks() {
		File test = new File("/home/cus/Téléchargements/IsoChrone des Parcs - entrées (9).csv");
		try {
			parkService.importIsoChroneEntrance(test);

			parkService.mergeNullEntranceAreas();

		} catch (IOException e) {
			System.err.println(e);
			fail(e.getMessage());
		}
	}

	/**
	 * calul des aires et densité pour chaque parc (carte isochrone)
	 */
	@Test
	@Disabled
	@Order(10)
	@Deprecated
	void computeParkArea() {

		try {
			List<ParkArea> ll = parkAreaRepository.findAll();
			
			for (Iterator<ParkArea> iterator = ll.iterator(); iterator.hasNext();) {
				ParkArea parkArea = iterator.next();
				computeService.computeParkArea(parkArea);
			}	
			
			
		
//			//lille
//			Optional<ParkArea> parkArea = parkAreaRepository.findById(parkArea);
//			if (parkArea.isPresent()) {
//				computeService.computeParkAreaV2(parkArea.get());
		
			
		} catch (Exception e) {
			System.err.println(e);
			fail(e.getMessage());
		}
			
	}

	@Test
	@Disabled
	@Order(24)
	void importIrisData() {
		
		try {
			List<IrisData> data = csvIrisDataParser.parseIrisData(2020, new File("/work/PERSO/ASSO/data/iris_base-ic-evol-struct-pop-2020.CSV"));
			System.out.println(data!=null?data.size():0);
			
			serviceIris.saveAllData(data);
			
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
	}

	@Test
	@Disabled
	@Order(23)
	void importIris() {
		
		irisGeoJsonIntegratorParser.parseAndSave("/work/PERSO/ASSO/data/fond_iris.json");
	}
	
	
	/**
	 * calcule des carre vs aire des parcs
	 */
	@Test
	@Disabled
	@Order(23)
	void batchCarreFixOne() {
		batchJobService.processCarres(2019, "CRS3035RES200mN2933000E3687600");
	}
	
	
	
	
	/**
	 * calcule des carre vs aire des parcs
	 */
	@Test
	@Disabled
	@Order(22)
	void batchCarreRequestProcessCity() {
// TODO en attendant une IHM pour lancer le recalcul. 
		
		batchJobService.requestProcessCity("59346");
//		batchJobService.requestProcessCity("59009");
		//batchJobService.requestProcessCity("27022");
//		batchJobService.requestProcessCity("59350");

	}
	/**
	 * calcule des carre vs aire des parcs
	 */
	@Test
	@Disabled
	@Order(21)
	void computeCarreFix() {

		try {
			//lille
//			computeServiceV2.computeCarreByInseeCode("59350");
			//lezennes					
//			computeServiceV2.computeCarreByInseeCode("59346");
			
/*
 * 
INSERT INTO public.compute_job( annee, id_inspire, demand, processed, status)
select 2019, id_inspire, now(), null, 0
FROM public.carre200onlyshape
WHERE code_insee='59346'

 */
			// v'ascq
			//computeServiceV2.computeCarreByInseeCode("59009");
//			computeServiceV2.computeCarreByInseeCode("59328");
//			computeServiceV2.computeCarreByInseeCode("59128");
			
//			computeServiceV2.computeCarreByCarre200m("CRS3035RES200mN3079800E3834000");
//			computeServiceV2.computeCarreByCarre200m("CRS3035RES200mN3081600E3829800");
			
			

//			computeService.computeCarreByInseeCode("59328");
//			//Capinghem
//			computeService.computeCarreByInseeCode("59128");
			
			System.out.println("bla");
		} catch (Exception e) {
			System.err.println(e);
			fail(e.getMessage());
		}
			
	}

	@Test
	@Disabled
	@Order(20)
	void computeParkAreAndEntranceFix() {

		try {
			computeService.refreshParkEntrances("59350");
			computeService.refreshParkEntrances("59328");
			computeService.refreshParkEntrances("59128");
			
			System.out.println("bla");
		} catch (Exception e) {
			System.err.println(e);
			fail(e.getMessage());
		}
			
	}
	

	@Test
	@Disabled
	@Order(1)
	void loadMelParks() {
		try {
			File test = new File("/home/cus/Téléchargements/parcs-jardins-lille-hellemmes-lomme-emprise.csv");

			parkService.importOpenDataMelParcJardin(test);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	@Autowired
	private CsvNantesParkJardinParser parserNantes;
	
	@Test
	@Disabled
	void loadNantesParks() {
		try {
			File f = new File("/work/PERSO/ASSO/data/autresVilles/integ_nantes.csv");
			List<ParcEtJardin> parks = parserNantes.parseCsv(f);
			parkService.importParcJardin(parks);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Autowired
	private CsvLyonParkJardinParser parserLyon;
	
	@Test
	@Disabled
	void loadLyonParks() {
		try {
			File f = new File("/work/PERSO/ASSO/data/autresVilles/integ_lyons.csv");
			List<ParcEtJardin> parks = parserLyon.parseCsv(f);
			parkService.importParcJardin(parks);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Autowired
	private JsonToulouseParkJardinParser parserToulouse;
	
	@Test
	@Disabled
	void loadToulouseParks() {
		try {
			File f = new File("/work/PERSO/ASSO/data/autresVilles/toulouse.json.txt");
			List<ParcEtJardin> parks = parserToulouse.parseJson(f);
			parkService.importParcJardin(parks);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
}