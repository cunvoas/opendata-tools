package com.github.cunvoas.geoserviceisochrone.service.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.GeoJsonCadastreController;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.Stat;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.StatsSurfaceJson;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.TerritoiresIndex;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

import tools.jackson.databind.ObjectMapper;

/**
 * Index data/territoires.json : seules les communes et EPCI dont le fichier de statistiques
 * existe pour le dernier millésime sont publiés.
 */
class TestServicePublicationExporterTerritoires {

	@TempDir
	Path tempDir;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private ServicePublicationExporter tested;
	private ServiceReadReferences serviceReadReferences;
	private ApplicationBusinessProperties properties;
	private City lille;

	@BeforeEach
	void setUp() {
		serviceReadReferences = mock(ServiceReadReferences.class);
		properties = mock(ApplicationBusinessProperties.class);
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());
		when(properties.getInseeAnnees()).thenReturn(new Integer[] { 2015, 2021 });

		tested = new ServicePublicationExporter(objectMapper, properties, serviceReadReferences,
				mock(CadastreRepository.class), mock(GeoJsonCadastreController.class),
				mock(GeoMapServiceV2.class), mock(NdviController.class));

		lille = city(42L, "59350", "LILLE");
	}

	@Test
	void testEcritLesTerritoiresPublies() throws Exception {
		statsFileCommune("59350", 120);
		statsFileEpci(1L);

		when(serviceReadReferences.getCity()).thenReturn(List.of(
				lille,
				city(43L, "59512", "ROUBAIX"),
				city(44L, "59009", "VILLENEUVE D ASCQ")));
		when(serviceReadReferences.getCommunauteCommune()).thenReturn(List.of(
				com2co(1L, "Métropole Européenne de Lille", "200093201"),
				com2co(2L, "CU de Dunkerque", "245900428"),
				com2co(3L, "CA sans Siren", null)));

		File file = tested.writeTerritoires();

		TerritoiresIndex index = objectMapper.readValue(file, TerritoiresIndex.class);
		assertEquals(1, index.communes().size());
		assertEquals(new TerritoiresIndex.Commune("59350", "42", "Lille", 120), index.communes().get(0));
		assertEquals(1, index.epcis().size());
		assertEquals(new TerritoiresIndex.Epci("200093201", "Métropole Européenne de Lille", "1"),
				index.epcis().get(0));
		assertEquals(tempDir.resolve("data/territoires.json").toFile(), file);
	}

	@Test
	void testDernierMillesimeUtilise() throws Exception {
		// un fichier 2015 seul ne suffit pas : l'index est calculé sur le dernier millésime (2021)
		Path ancien = tempDir.resolve("data/stats/59/59350/stats_59350_2015.json");
		Files.createDirectories(ancien.getParent());
		Files.writeString(ancien, "{}");
		when(serviceReadReferences.getCity()).thenReturn(List.of(lille));
		when(serviceReadReferences.getCommunauteCommune()).thenReturn(List.of());

		File file = tested.writeTerritoires();
		TerritoiresIndex index = objectMapper.readValue(file, TerritoiresIndex.class);
		assertEquals(0, index.communes().size());
	}

	private void statsFileCommune(String insee, int population) throws Exception {
		StatsSurfaceJson stats = new StatsSurfaceJson();
		Stat tranche = new Stat();
		tranche.setHabitants(population);
		stats.getStats().add(tranche);
		Path path = tempDir.resolve("data/stats/" + insee.substring(0, 2) + "/" + insee
				+ "/stats_" + insee + "_2021.json");
		Files.createDirectories(path.getParent());
		objectMapper.writeValue(path.toFile(), stats);
	}

	private void statsFileEpci(long id) throws Exception {
		Path path = tempDir.resolve("data/stats/com2co/" + id + "/stats_c2c_" + id + "_all_2021.json");
		Files.createDirectories(path.getParent());
		Files.writeString(path, "{}");
	}

	private City city(long id, String insee, String nom) {
		City city = new City();
		city.setId(id);
		city.setInseeCode(insee);
		city.setName(nom);
		return city;
	}

	private CommunauteCommune com2co(long id, String nom, String siren) {
		CommunauteCommune com2co = new CommunauteCommune();
		com2co.setId(id);
		com2co.setName(nom);
		com2co.setSiren(siren);
		return com2co;
	}
}
