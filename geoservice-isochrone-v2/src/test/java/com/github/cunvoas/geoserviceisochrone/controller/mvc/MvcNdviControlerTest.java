package com.github.cunvoas.geoserviceisochrone.controller.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController.NdviSummaries;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.MvcNdviControler.NdviZone;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

class MvcNdviControlerTest {

	private static final long REGION_ID = 9L;
	private static final long COM2CO_ID = 1L;
	private static final long CITY_ID = 2878L;

	private MvcNdviControler tested;
	private ExtendedModelMap model;
	private City city;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		ParkAreaComputedRepository parkAreaRepository = mock(ParkAreaComputedRepository.class);
		CommunauteCommuneRepository com2coRepository = mock(CommunauteCommuneRepository.class);
		NdviController ndviController = mock(NdviController.class);
		ApplicationBusinessProperties properties = mock(ApplicationBusinessProperties.class);
		ServiceReadReferences references = mock(ServiceReadReferences.class);

		Region region = new Region();
		region.setId(REGION_ID);
		region.setName("Hauts-de-France");
		CommunauteCommune com2co = new CommunauteCommune();
		com2co.setId(COM2CO_ID);
		com2co.setName("MEL");
		com2co.setRegion(region);
		city = new City();
		city.setId(CITY_ID);
		city.setName("Lille");
		city.setInseeCode("59350");
		city.setCommunauteCommune(com2co);
		city.setRegion(region);

		when(properties.getDerniereAnnee()).thenReturn(2021);
		when(properties.getSitePublique()).thenReturn("https://public/");
		when(references.getRegion()).thenReturn(List.of(region));
		when(references.getCommunauteByRegionId(REGION_ID)).thenReturn(List.of(com2co));
		when(references.getCityByCommunauteCommuneId(COM2CO_ID)).thenReturn(List.of(city));
		when(references.getCityByRegionId(REGION_ID)).thenReturn(List.of(city));
		when(references.getCityById(CITY_ID)).thenReturn(city);
		when(references.getCommunauteCommuneById(COM2CO_ID)).thenReturn(com2co);
		when(parkAreaRepository.findCom2coIdsWithComputedParks()).thenReturn(List.of(COM2CO_ID));
		when(com2coRepository.findAllById(List.of(COM2CO_ID))).thenReturn(List.of(com2co));
		when(ndviController.summaries(2021, COM2CO_ID))
				.thenReturn(new NdviSummaries(2021, 10, 4, "2025-06-10", List.of()));
		when(ndviController.summaries(2021, null, "59350"))
				.thenReturn(new NdviSummaries(2021, 3, 2, "2025-06-10", List.of()));

		tested = new MvcNdviControler(parkAreaRepository, com2coRepository, ndviController, properties, references);
		model = new ExtendedModelMap();
	}

	@Test
	void listsTheEpciOfTheSelectedRegion() {
		tested.list(null, null, null, model);
		assertEquals(1, zones().size());
		assertEquals("https://public/ndvi?region=9&com2co=1", zones().get(0).publicUrl());

		tested.list(REGION_ID, null, null, model);
		assertEquals(1, zones().size());
		assertEquals("MEL", zones().get(0).name());
	}

	@Test
	void showsTheCityWhenACityIsSelected() {
		tested.list(REGION_ID, COM2CO_ID, CITY_ID, model);

		NdviZone zone = zones().get(0);
		assertTrue(zone.city());
		assertEquals("Lille", zone.name());
		assertEquals(3, zone.total());
		assertEquals("https://public/ndvi?region=9&com2co=1&city=59350", zone.publicUrl());
	}

	@SuppressWarnings("unchecked")
	private List<NdviZone> zones() {
		return (List<NdviZone>) model.get("zones");
	}
}
