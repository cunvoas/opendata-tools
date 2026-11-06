package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.StatsSurfaceJson;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;

class StatsControllerTest {

	private StatsSurfaceService statsSurfaceService;
	private CityRepository cityRepository;
	private StatsController tested;

	@BeforeEach
	void setUp() {
		ApplicationBusinessProperties properties = mock(ApplicationBusinessProperties.class);
		when(properties.getDerniereAnnee()).thenReturn(2021);
		statsSurfaceService = mock(StatsSurfaceService.class);
		cityRepository = mock(CityRepository.class);
		tested = new StatsController(statsSurfaceService, cityRepository, properties);
	}

	@Test
	void suburbsAppelleLeCalculPeriurbainDeLAnnee() throws Exception {
		StatsSurfaceJson stats = new StatsSurfaceJson();
		when(statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllSuburbs(2019, 1L)).thenReturn(stats);

		ResponseEntity<StatsSurfaceJson> response = tested.com2co(1L, "suburbs", 2019);

		assertEquals(200, response.getStatusCode().value());
		assertSame(stats, response.getBody());
	}

	@Test
	void sansAnneeUtiliseLAnneeDeReference() throws Exception {
		tested.com2co(1L, "all", null);

		verify(statsSurfaceService).getStatsSurfaceByCom2CoAndAnneeAllV2(2021, 1L);
	}

	@Test
	void typeDeGrapheInconnuRefuse() {
		assertThrows(ResponseStatusException.class, () -> tested.com2co(1L, "inconnu", 2019));
	}

	@Test
	void communeInconnueRenvoie404() {
		when(cityRepository.findByInseeCode("00000")).thenReturn(null);

		assertEquals(404, tested.city("00000", 2019).getStatusCode().value());
	}
}
