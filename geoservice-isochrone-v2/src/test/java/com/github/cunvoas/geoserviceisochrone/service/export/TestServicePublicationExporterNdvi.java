package com.github.cunvoas.geoserviceisochrone.service.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.GeoJsonCadastreController;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController.NdviSummaries;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController.NdviSummary;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

import tools.jackson.databind.ObjectMapper;

/**
 * Répertoires et villes écrits par l'export NDVI (même principe que les dossiers des carrés de
 * {@link TestPublishService}) : un dossier par EPCI, un sous-dossier par ville ayant des mesures.
 */
class TestServicePublicationExporterNdvi {

	private static final long LILLE = 2878L;
	private static final long ROUBAIX = 27046L;

	@TempDir
	Path tempDir;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private NdviController ndviController;
	private ServicePublicationExporter tested;
	private CommunauteCommune com2co;

	@BeforeEach
	void setUp() {
		ApplicationBusinessProperties properties = mock(ApplicationBusinessProperties.class);
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());
		ndviController = mock(NdviController.class);
		tested = new ServicePublicationExporter(objectMapper, properties, mock(ServiceReadReferences.class),
				mock(CadastreRepository.class), mock(GeoJsonCadastreController.class), mock(GeoMapServiceV2.class),
				ndviController);

		com2co = new CommunauteCommune();
		com2co.setId(1L);
		com2co.setCities(List.of(city(LILLE, "LILLE", "59350"), city(ROUBAIX, "ROUBAIX", "59512")));
	}

	@Test
	void writesTheEpciFileAndTheFileOfEachCityWithMeasures() throws Exception {
		stubLilleWithMeasuresRoubaixWithout();

		List<File> written = tested.writeNdviJson(com2co, 2021);

		assertEquals(List.of(
				tempDir.resolve("data/ndvi/1/ndvi_2021_1.json").toFile(),
				tempDir.resolve("data/ndvi/1/2878/ndvi_2021_2878.json").toFile()), written);
		assertTrue(Files.isRegularFile(tempDir.resolve("data/ndvi/1/ndvi_2021_1.json")));
		assertTrue(Files.isRegularFile(tempDir.resolve("data/ndvi/1/2878/ndvi_2021_2878.json")));
	}

	@Test
	void createsNoDirectoryForACityWithoutMeasures() throws Exception {
		stubLilleWithMeasuresRoubaixWithout();

		tested.writeNdviJson(com2co, 2021);

		assertFalse(Files.exists(tempDir.resolve("data/ndvi/1/27046")));
	}

	@Test
	void cityFileHoldsOnlyTheParksOfThatCity() throws Exception {
		stubLilleWithMeasuresRoubaixWithout();

		tested.writeNdviJson(com2co, 2021);

		NdviSummaries lille = objectMapper.readValue(
				tempDir.resolve("data/ndvi/1/2878/ndvi_2021_2878.json").toFile(), NdviSummaries.class);
		assertEquals(230, lille.total());
		assertEquals(List.of("Citadelle"), lille.parks().stream().map(NdviSummary::name).toList());
	}

	@Test
	void writesTheEpciFileEvenWhenNoCityHasMeasures() throws Exception {
		when(ndviController.summaries(2021, 1L)).thenReturn(summaries(5, 0));
		when(ndviController.summaries(2021, null, LILLE)).thenReturn(summaries(3, 0));
		when(ndviController.summaries(2021, null, ROUBAIX)).thenReturn(summaries(2, 0));

		List<File> written = tested.writeNdviJson(com2co, 2021);

		assertEquals(1, written.size());
		try (Stream<Path> children = Files.list(tempDir.resolve("data/ndvi/1"))) {
			assertEquals(List.of("ndvi_2021_1.json"), children.map(p -> p.getFileName().toString()).toList());
		}
	}

	private void stubLilleWithMeasuresRoubaixWithout() {
		when(ndviController.summaries(2021, 1L)).thenReturn(summaries(788, 230, "Citadelle", "Parc A"));
		when(ndviController.summaries(2021, null, LILLE)).thenReturn(summaries(230, 230, "Citadelle"));
		when(ndviController.summaries(2021, null, ROUBAIX)).thenReturn(summaries(5, 0));
	}

	private City city(long id, String name, String insee) {
		City city = new City();
		city.setId(id);
		city.setName(name);
		city.setInseeCode(insee);
		return city;
	}

	private NdviSummaries summaries(long total, long withNdvi, String... parkNames) {
		List<NdviSummary> parks = Arrays.stream(parkNames)
				.map(name -> new NdviSummary("1", name, new BigDecimal("1200"), new BigDecimal("0.5"),
						new BigDecimal("0.1"), new BigDecimal("0.9"), new BigDecimal("0.2"), "2025-06-16", 2021))
				.toList();
		return new NdviSummaries(2021, total, withNdvi, withNdvi == 0 ? null : "2025-06-16", parks);
	}
}
