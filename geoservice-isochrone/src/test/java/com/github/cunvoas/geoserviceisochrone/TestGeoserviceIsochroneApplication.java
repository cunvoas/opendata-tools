package com.github.cunvoas.geoserviceisochrone;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.StatsSurfaceJson;
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
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;
import com.github.cunvoas.geoserviceisochrone.service.compute.BatchJobService;
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

	@Autowired
	private StatsSurfaceService statsSurfaceService;

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
		
		irisGeoJsonIntegratorParser.parseAndSave(2020, "/work/PERSO/ASSO/data/fond_iris.json");
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@Test
//	@Disabled
	@Order(22)
	void batchStatsSurfaceJson() {
		StatsSurfaceJson stats= statsSurfaceService.getStatsSurfaceByInseeAndAnnee("59350", 2019);
		
		Assertions.assertNotNull(stats);
		
		try {
			String s =  statsSurfaceService.getStringStatsSurfaceByInseeAndAnnee("59350", 2019);
			System.out.println(s);
			
			
			Assertions.assertNotNull(s);
		} catch (JsonProcessingException e) {
			Assertions.fail(e.getMessage());
		}
	}
	
	
	
	
	
	
	
	
	
	/**
	 * calcule des carre vs aire des parcs
	 */
	@Test
	@Disabled
	@Order(22)
	void batchCarreRequestProcessCity() {
// TODO en attendant une IHM pour lancer le recalcul. 
/*
ALLENNES LES MARAIS
ANNOEULLIN
ANSTAING
ARMENTIERES
AUBERS
BAISIEUX
BAUVIN
BEAUCAMPS LIGNY
BOIS GRENIER
BONDUES
BOUSBECQUE
BOUVINES
CAPINGHEM
CARNIN
CHERENG
COMINES
CROIX  ***
DEULEMONT
DON
EMMERIN 
ENGLOS
ENNETIERES EN WEPPES
ERQUINGHEM LE SEC 
ERQUINGHEM LYS  
ESCOBECQUES
FACHES THUMESNIL
FOREST SUR MARQUE
FOURNES EN WEPPES
FRELINGHIEN
FRETIN 
FROMELLES
GRUSON   
HALLENNES LEZ HAUBOURDIN
HALLUIN   
HANTAY	
HAUBOURDIN
HEM
HERLIES
HOUPLIN ANCOISNE
HOUPLINES
ILLIES
LA BASSEE
LA MADELEINE 
LAMBERSART **
LANNOY
LE MAISNIL
LEERS
LESQUIN   
LEZENNES   *
LILLE   *
LINSELLES
LOMPRET
LOOS
LYS LEZ LANNOY **
MARCQ EN BAROEUL
MARQUETTE LEZ LILLE
MARQUILLIES
MONS EN BAROEUL
MOUVAUX
NEUVILLE EN FERRAIN
NOYELLES LES SECLIN
PERENCHIES
PERONNE EN MELANTOIS
PREMESQUES
PROVIN
QUESNOY SUR DEULE
RADINGHEM EN WEPPES
RONCHIN
RONCQ
ROUBAIX **
SAILLY LEZ LANNOY
SAINGHIN EN MELANTOIS
SAINGHIN EN WEPPES
SALOME
SANTES
SECLIN 
SEQUEDIN
ST ANDRE LEZ LILLE  **
TEMPLEMARS
TOUFFLERS
TOURCOING  **
TRESSIN
VENDEVILLE
VERLINGHEM
VILLENEUVE D ASCQ
WAMBRECHIES
WARNETON
WASQUEHAL
WATTIGNIES
WATTRELOS
WAVRIN <<<<<  (Bois de lézy ?)
WERVICQ SUD
WICRES
WILLEMS

 */
		
		//batchJobService.requestProcessCity("27022");
		
//		batchJobService.requestProcessCity("59056");
//		batchJobService.requestProcessCity("59088");
//		batchJobService.requestProcessCity("59090");
//		batchJobService.requestProcessCity("59098");
//		batchJobService.requestProcessCity("59128");
//		batchJobService.requestProcessCity("59202");
//		batchJobService.requestProcessCity("59317");
//		batchJobService.requestProcessCity("59252");
//		batchJobService.requestProcessCity("59133");
//		batchJobService.requestProcessCity("59146");
//		batchJobService.requestProcessCity("59106");
//		batchJobService.requestProcessCity("59152");
//		batchJobService.requestProcessCity("59527");
//		batchJobService.requestProcessCity("59009");
//		batchJobService.requestProcessCity("59350");
//		batchJobService.requestProcessCity("59163");//CROIX
//		batchJobService.requestProcessCity("59599");// tourk
//		batchJobService.requestProcessCity("59512");//RBX
//		batchJobService.requestProcessCity("59173");
//		batchJobService.requestProcessCity("59670");
//		batchJobService.requestProcessCity("59193");
//		batchJobService.requestProcessCity("59195");
//		batchJobService.requestProcessCity("59196");
//		batchJobService.requestProcessCity("59201");
//		batchJobService.requestProcessCity("59202");
//		batchJobService.requestProcessCity("59208");
//		batchJobService.requestProcessCity("59220");
//		batchJobService.requestProcessCity("59247");
//		batchJobService.requestProcessCity("59250");
//		batchJobService.requestProcessCity("59252");
//		batchJobService.requestProcessCity("59256");
//		batchJobService.requestProcessCity("59257");
//		batchJobService.requestProcessCity("59275");
//		batchJobService.requestProcessCity("59278");
//		batchJobService.requestProcessCity("59279");
//		batchJobService.requestProcessCity("59281");
//		batchJobService.requestProcessCity("59286");
//		batchJobService.requestProcessCity("59299");
//		batchJobService.requestProcessCity("59303");
//		batchJobService.requestProcessCity("59316");
//		batchJobService.requestProcessCity("59317");
//		batchJobService.requestProcessCity("59320");
//		batchJobService.requestProcessCity("59051");
//		batchJobService.requestProcessCity("59143");
//		batchJobService.requestProcessCity("59368");
//		batchJobService.requestProcessCity("59328");
//		batchJobService.requestProcessCity("59332");
//		batchJobService.requestProcessCity("59367");
//		batchJobService.requestProcessCity("59371");
//		batchJobService.requestProcessCity("59339");
//		batchJobService.requestProcessCity("59343");
//		batchJobService.requestProcessCity("59352");
//		batchJobService.requestProcessCity("59356");
//		batchJobService.requestProcessCity("59360");
//		batchJobService.requestProcessCity("59378");
//		batchJobService.requestProcessCity("59386");
//		batchJobService.requestProcessCity("59388");
//		batchJobService.requestProcessCity("59410");
//		batchJobService.requestProcessCity("59421");
//		batchJobService.requestProcessCity("59426");
//		batchJobService.requestProcessCity("59437");
//		batchJobService.requestProcessCity("59457");
//		batchJobService.requestProcessCity("59458");
//		batchJobService.requestProcessCity("59470");
//		batchJobService.requestProcessCity("59477");
//		batchJobService.requestProcessCity("59482");
//		batchJobService.requestProcessCity("59487");
//		batchJobService.requestProcessCity("59507");
//		batchJobService.requestProcessCity("59508");
//		batchJobService.requestProcessCity("59512");
//		batchJobService.requestProcessCity("59522");
//		batchJobService.requestProcessCity("59523");
//		batchJobService.requestProcessCity("59524");
//		batchJobService.requestProcessCity("59550");
//		batchJobService.requestProcessCity("59553");
//		batchJobService.requestProcessCity("59560");
//		batchJobService.requestProcessCity("59566");
//		batchJobService.requestProcessCity("59527");
//		batchJobService.requestProcessCity("59585");
//		batchJobService.requestProcessCity("59598");
//		batchJobService.requestProcessCity("59599");
//		batchJobService.requestProcessCity("59602");
//		batchJobService.requestProcessCity("59609");
//		batchJobService.requestProcessCity("59611");
//		batchJobService.requestProcessCity("59009");
//		batchJobService.requestProcessCity("59636");
//		batchJobService.requestProcessCity("59643");
//		batchJobService.requestProcessCity("59646");
//		batchJobService.requestProcessCity("59648");
//		batchJobService.requestProcessCity("59650");
//		batchJobService.requestProcessCity("59653");
//		batchJobService.requestProcessCity("59656");
//		batchJobService.requestProcessCity("59658");
//		batchJobService.requestProcessCity("59660");
		
		
		
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