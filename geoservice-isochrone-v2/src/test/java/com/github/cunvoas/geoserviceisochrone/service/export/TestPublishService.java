package com.github.cunvoas.geoserviceisochrone.service.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;

class TestPublishService {

	private PublishService tested;

	private ServicePublicationExporter exporter;
	private StatsSurfaceService statsSurfaceService;
	private CommunauteCommuneRepository repository;
	private ApplicationBusinessProperties properties;

	@BeforeEach
	void setUp() {
		tested = new PublishService();
		exporter = mock(ServicePublicationExporter.class);
		statsSurfaceService = mock(StatsSurfaceService.class);
		repository = mock(CommunauteCommuneRepository.class);
		properties = mock(ApplicationBusinessProperties.class);

		ReflectionTestUtils.setField(tested, "servicePublicationExporter", exporter);
		ReflectionTestUtils.setField(tested, "statsSurfaceService", statsSurfaceService);
		ReflectionTestUtils.setField(tested, "communauteCommuneRepository", repository);
		ReflectionTestUtils.setField(tested, "applicationBusinessProperties", properties);

		CommunauteCommune c2c = new CommunauteCommune();
		c2c.setId(1L);
		c2c.setName("Métropole Européenne de Lille");
		c2c.setSiren("200093201");
		when(repository.findById(1L)).thenReturn(Optional.of(c2c));
	}

	@Test
	void testPublicationRefuseeSansSiren() {
		CommunauteCommune sansSiren = new CommunauteCommune();
		sansSiren.setId(1L);
		sansSiren.setName("CA sans Siren");
		when(repository.findById(1L)).thenReturn(Optional.of(sansSiren));

		tested.publishAsync(1L, 2025);

		PublicationStatus status = tested.getStatus(1L, 2025);
		assertEquals(PublicationStatus.Phase.PRODUCTION_FAILED, status.getPhase());
		assertTrue(status.getMessage().contains("CA sans Siren"));
		assertTrue(status.getMessage().contains("Siren"));
		verifyNoInteractions(exporter, statsSurfaceService);
	}

	@Test
	void testProductionFailureBlocksCommit() throws Exception {
		when(properties.getJsonFileFolder()).thenReturn("/tmp/data");
		doThrow(new IOException("boom")).when(exporter).writeGeoJsonCarreaux(any(), any());

		tested.publishAsync(1L, 2025);

		PublicationStatus status = tested.getStatus(1L, 2025);
		assertEquals(PublicationStatus.Phase.PRODUCTION_FAILED, status.getPhase());
		assertTrue(status.getMessage().contains("carres"));
		assertNull(status.getGitSha());
	}

	@Test
	void testSuccessfulProductionWaitsForFinalCommit(@TempDir Path tempDir) throws Exception {
		createExpectedFiles(tempDir);
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());

		tested.publishAsync(1L, 2025);

		PublicationStatus status = tested.getStatus(1L, 2025);
		assertEquals(PublicationStatus.Phase.PRODUCED, status.getPhase());
		assertNull(status.getGitSha());
		assertEquals(12, status.getFiles().size());
		assertEquals(12, status.getFilesProduced());
		assertEquals(9, status.getSteps().size());
		assertTrue(status.getSteps().stream().allMatch(s -> s.getState() == PublicationStatus.StepState.DONE));
		assertEquals("referentiel", status.getFiles().get(0).getType());
		assertEquals("territoires", status.getFiles().get(11).getType());
		assertEquals(tempDir + "/data/ndvi/1/ndvi_2025_1.json", status.getFiles().get(10).getPath());
		assertEquals(tempDir + "/data/territoires.json", status.getFiles().get(11).getPath());
	}

	@Test
	void testMissingFileBlocksPublication(@TempDir Path tempDir) throws Exception {
		createExpectedFiles(tempDir);
		Files.delete(tempDir.resolve("geojson/carres/1/carre_2025_1.json"));
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());

		tested.publishAsync(1L, 2025);

		PublicationStatus status = tested.getStatus(1L, 2025);
		assertEquals(PublicationStatus.Phase.PRODUCTION_FAILED, status.getPhase());
		assertEquals(11, status.getFilesProduced());
		assertTrue(status.hasProductionError());
	}

	@Test
	void testReferentialProductionDoesNotPush(@TempDir Path tempDir) throws Exception {
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());

		tested.publishReferentialAsync();

		verify(exporter).writeRegions();
		verify(exporter).writeTerritoires();
	}

	@Test
	void testPrepareCreatesStatusBeforeAsync(@TempDir Path tempDir) throws Exception {
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());

		PublicationStatus prepared = tested.prepare(1L, 2025);

		assertNotNull(prepared);
		assertEquals(PublicationStatus.Phase.PRODUCING, prepared.getPhase());
		assertEquals(9, prepared.getSteps().size());
		assertNotNull(tested.getStatus(1L, 2025));
		assertTrue(tested.isRunning(1L, 2025));
	}

	private void createExpectedFiles(Path tempDir) throws IOException {
		Files.createDirectories(tempDir.resolve("data/cities/com2co"));
		Files.createDirectories(tempDir.resolve("data/stats/com2co/1"));
		Files.createDirectories(tempDir.resolve("data/ndvi/1"));
		Files.createDirectories(tempDir.resolve("geojson/parkOutline/1"));
		Files.createDirectories(tempDir.resolve("geojson/isochrones/1"));
		Files.createDirectories(tempDir.resolve("geojson/carres/1"));
		Files.createDirectories(tempDir.resolve("geojson/iris/1"));
		Files.writeString(tempDir.resolve("data/regions.json"), "[]");
		Files.writeString(tempDir.resolve("data/cities/densite.json"), "{}");
		Files.writeString(tempDir.resolve("data/cities/com2co/cities_1.json"), "[]");
		Files.writeString(tempDir.resolve("data/territoires.json"), "{}");
		Files.writeString(tempDir.resolve("data/stats/com2co/1/stats_c2c_1_urbans_2025.json"), "{}");
		Files.writeString(tempDir.resolve("data/stats/com2co/1/stats_c2c_1_suburbs_2025.json"), "{}");
		Files.writeString(tempDir.resolve("data/stats/com2co/1/stats_c2c_1_all_2025.json"), "{}");
		Files.writeString(tempDir.resolve("data/ndvi/1/ndvi_2025_1.json"), "{}");
		Files.writeString(tempDir.resolve("geojson/parkOutline/1/parkOutline_2025_1.json"), "{}");
		Files.writeString(tempDir.resolve("geojson/isochrones/1/isochrone_2025_1.json"), "{}");
		Files.writeString(tempDir.resolve("geojson/carres/1/carre_2025_1.json"), "{}");
		Files.writeString(tempDir.resolve("geojson/iris/1/iris_2025_1.json"), "{}");
	}
}
