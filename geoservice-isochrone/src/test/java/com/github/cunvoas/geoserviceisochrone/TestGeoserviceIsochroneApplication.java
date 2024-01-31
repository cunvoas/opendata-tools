package com.github.cunvoas.geoserviceisochrone;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import com.github.cunvoas.geoserviceisochrone.service.map.InseeCarre200mService;
import com.github.cunvoas.geoserviceisochrone.service.park.ComputeService;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkService;

@SpringBootTest
@ActiveProfiles("secret")
class TestGeoserviceIsochroneApplication {
	@Autowired
	private ComputeService computeService;

	@Autowired
	private CsvCarre200ShapeParser csvParser;

	@Autowired
	private InseeCarre200mService carreService;
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
	@Order(0)
	void contextLoads() {
	}

	@Test
	@Order(10)
	@Disabled
	void loadParks() {
		File test = new File("/home/cus/Téléchargements/IsoChrone des Parcs - entrées (9).csv");
		try {
			parkService.importIsoChroneEntrance(test);

			parkService.mergeEntranceAreas();

		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

	@Test
	@Order(20)
	@Disabled
	void computeFix() {

		try {
			computeService.computeByInseeCode("59350");
		} catch (Exception e) {
			fail(e.getMessage());
		}
			
	}
	
	@Test
	@Order(11)
	@Disabled
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
						carreService.computeCarre(carre.getId());

					}
				}

				carreService.computePark(parkArea);
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