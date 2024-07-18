package com.github.cunvoas.geoserviceisochrone;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.github.cunvoas.geoserviceisochrone.extern.csv.CsvCarre200ShapeParser;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mShape;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.map.CityService;
import com.github.cunvoas.geoserviceisochrone.service.park.ComputeService;
import com.github.cunvoas.geoserviceisochrone.service.park.ComputeServiceV2;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkService;


@SpringBootTest
@ActiveProfiles({"prod","dev"})
class TestGeoserviceIsochroneApplication {
	@Autowired
	private ComputeService computeService;
	@Autowired
	private ComputeServiceV2 computeServiceV2;

	@Autowired
	private CsvCarre200ShapeParser csvParser;

	@Autowired
	private InseeCarre200mShapeRepository repo;

	@Autowired
	private ParkService parkService;
	@Autowired
	private CityService cityService;

	@Autowired
	private InseeCarre200mRepository inseeCare200mRepository;
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
	void computeParkArea() {

		try {
			List<ParkArea> ll = parkAreaRepository.findAll();
			
			for (Iterator<ParkArea> iterator = ll.iterator(); iterator.hasNext();) {
				ParkArea parkArea = iterator.next();
				computeServiceV2.computeParkAreaV2(parkArea);
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
	
	/**
	 * calcule des carre vs aire des parcs
	 */
	@Test
//	@Disabled
	@Order(21)
	void computeCarreFix() {

		try {
			//lille
			computeServiceV2.computeCarreByInseeCode("59350");
			
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
	
	/**
	 * @deprecated
	 */
	@Test
	@Disabled
	@Order(11)
	void compute() {
		try {
			List<ParkArea> parks =  new ArrayList<>();
			parks.add(parkAreaRepository.findById(468L).get());
//			List<ParkArea> parks = parkAreaRepository.findAll();
//			List<ParkArea> parks = parkAreaRepository.findByBlock("Lomme");
			Collections.reverse(parks);

			for (ParkArea parkArea : parks) {

				if (parkArea.getPolygon() != null) {
					List<InseeCarre200m> carreShape = inseeCare200mRepository .getAllCarreInMap(GeometryQueryHelper.toText(parkArea.getPolygon()));

					for (InseeCarre200m carre : carreShape) {
						//FIXME
//						carreService.computeCarre(carre.getId());
						

					}
				}

				//FIXME
//				carreService.computeParkArea(parkArea);
			}

		} catch (Exception e) {
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

	@Test
	@Disabled
	@Order(2)
	void loadCarre200Shape() {
		//2017
		File test = new File(
				"/home/cus/Documents/Associations/DeulAir/200m-carreaux-metropole/200m_carreaux_metropole_shapefile_wgs84-TEST.csv");

		try {
			List<InseeCarre200mShape> rows = csvParser.parseCarree200Shape(test);

			int i = 0;
			List<InseeCarre200mShape> todos = new ArrayList<>();
			for (InseeCarre200mShape inseeCarre200mShape : rows) {
				i++;
				todos.add(inseeCarre200mShape);

				if (i % 50 == 0) {
					repo.saveAll(todos);
					todos.clear();
				}
			}

			if (todos.size() > 0) {
				repo.saveAll(todos);
			}

		} catch (IOException e) {
			fail(e.getMessage());
		}

	}
	
}