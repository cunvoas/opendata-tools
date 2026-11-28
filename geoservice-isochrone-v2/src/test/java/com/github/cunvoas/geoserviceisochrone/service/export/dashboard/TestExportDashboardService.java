package com.github.cunvoas.geoserviceisochrone.service.export.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GitHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;

class TestExportDashboardService {

	private ExportDashboardService tested;
	private GitHelper gitHelper;
	private ApplicationBusinessProperties properties;
	private CommunauteCommuneRepository repository;

	@BeforeEach
	void setUp() {
		tested = new ExportDashboardService();
		gitHelper = mock(GitHelper.class);
		properties = mock(ApplicationBusinessProperties.class);
		repository = mock(CommunauteCommuneRepository.class);
		ReflectionTestUtils.setField(tested, "gitHelper", gitHelper);
		ReflectionTestUtils.setField(tested, "applicationBusinessProperties", properties);
		ReflectionTestUtils.setField(tested, "communauteCommuneRepository", repository);
		when(properties.getInseeAnnees()).thenReturn(new Integer[] { 2025 });
	}

	@Test
	void scanBuildsEpciMatrixAndSnapshot(@TempDir Path tempDir) throws Exception {
		createFiles(tempDir, "59350");
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());
		when(gitHelper.getLocalPath()).thenReturn(tempDir.toString());
		when(gitHelper.state(tempDir.toString())).thenReturn(new GitHelper.RepoState("abc123", "main", true));
		when(gitHelper.changedPaths(tempDir.toString())).thenReturn(List.of(
				"data/regions.json",
				"geojson/parkOutline/1/parkOutline_2025_1.json"));

		ExportDashboard dashboard = tested.scan();

		assertTrue(dashboard.isRepositoryFound());
		assertEquals("abc123", dashboard.getHead());
		assertEquals(2, dashboard.getChangedFiles().size());
		assertNotNull(dashboard.getSnapshotHash());
		assertTrue(dashboard.getBlockers().isEmpty());
		assertEquals(1, dashboard.getEpcis().size());
		assertEquals("OK", dashboard.getEpcis().get(0).getStatus());
		assertEquals(1, dashboard.getEpcis().get(0).getCitiesWithStats());
		assertEquals(1, dashboard.getEpcis().get(0).getCitiesWithNdvi());
	}

	@Test
	void scanBlocksCommitWhenTerritoiresMissing(@TempDir Path tempDir) throws Exception {
		createFiles(tempDir, "59350");
		Files.delete(tempDir.resolve("data/territoires.json"));
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());
		when(gitHelper.getLocalPath()).thenReturn(tempDir.toString());
		when(gitHelper.state(tempDir.toString())).thenReturn(new GitHelper.RepoState("abc123", "main", true));
		when(gitHelper.changedPaths(tempDir.toString())).thenReturn(List.of("data/stats/59/59350/stats_59350_2025.json"));

		ExportDashboard dashboard = tested.scan();

		assertFalse(dashboard.getBlockers().isEmpty());
		assertTrue(dashboard.getBlockers().get(0).contains("territoires.json"));
	}

	@Test
	void commitRejectsStaleSnapshot(@TempDir Path tempDir) throws Exception {
		createFiles(tempDir, "59350");
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());
		when(gitHelper.getLocalPath()).thenReturn(tempDir.toString());
		when(gitHelper.state(tempDir.toString())).thenReturn(new GitHelper.RepoState("abc123", "main", true));
		when(gitHelper.changedPaths(tempDir.toString())).thenReturn(List.of("data/regions.json"));

		assertThrows(IllegalStateException.class, () -> tested.commit("stale", null));
	}

	@Test
	void commitStagesExactChangedFiles(@TempDir Path tempDir) throws Exception {
		createFiles(tempDir, "59350");
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());
		when(gitHelper.getLocalPath()).thenReturn(tempDir.toString());
		when(gitHelper.state(tempDir.toString())).thenReturn(new GitHelper.RepoState("abc123", "main", true));
		when(gitHelper.changedPaths(tempDir.toString())).thenReturn(List.of("data/regions.json"));
		when(gitHelper.commitFiles(eq(tempDir.toString()), anyList(), any()))
				.thenReturn(new GitHelper.CommitResult("sha1", true, "OK"));

		ExportDashboard dashboard = tested.scan();
		GitHelper.CommitResult result = tested.commit(dashboard.getSnapshotHash(), "test");

		assertEquals("sha1", result.sha());
		org.mockito.Mockito.verify(gitHelper).commitFiles(eq(tempDir.toString()), eq(List.of("data/regions.json")), eq("test"));
	}

	private void createFiles(Path root, String insee) throws Exception {
		Files.createDirectories(root.resolve("data/cities/com2co"));
		Files.createDirectories(root.resolve("data/stats/com2co/1"));
		Files.createDirectories(root.resolve("data/stats/59/" + insee));
		Files.createDirectories(root.resolve("data/ndvi/1/" + insee));
		Files.createDirectories(root.resolve("geojson/parkOutline/1"));
		Files.createDirectories(root.resolve("geojson/isochrones/1"));
		Files.createDirectories(root.resolve("geojson/carres/1"));
		Files.createDirectories(root.resolve("geojson/iris/1"));
		Files.writeString(root.resolve("data/regions.json"), "[]");
		Files.writeString(root.resolve("data/cities/densite.json"), "{}");
		Files.writeString(root.resolve("data/cities/com2co/cities_1.json"), "[]");
		Files.writeString(root.resolve("data/territoires.json"), "{}");
		Files.writeString(root.resolve("data/stats/com2co/1/stats_c2c_1_urbans_2025.json"), "{}");
		Files.writeString(root.resolve("data/stats/com2co/1/stats_c2c_1_suburbs_2025.json"), "{}");
		Files.writeString(root.resolve("data/stats/com2co/1/stats_c2c_1_all_2025.json"), "{}");
		Files.writeString(root.resolve("data/stats/59/" + insee + "/stats_" + insee + "_2025.json"), "{}");
		Files.writeString(root.resolve("data/ndvi/1/ndvi_2025_1.json"), "{}");
		Files.writeString(root.resolve("data/ndvi/1/" + insee + "/ndvi_2025_" + insee + ".json"), "{}");
		Files.writeString(root.resolve("geojson/parkOutline/1/parkOutline_2025_1.json"), "{}");
		Files.writeString(root.resolve("geojson/isochrones/1/isochrone_2025_1.json"), "{}");
		Files.writeString(root.resolve("geojson/carres/1/carre_2025_1.json"), "{}");
		Files.writeString(root.resolve("geojson/iris/1/iris_2025_1.json"), "{}");

		City city = new City();
		city.setId(2878L);
		city.setInseeCode(insee);
		city.setName("Lille");
		CommunauteCommune c2c = new CommunauteCommune();
		c2c.setId(1L);
		c2c.setName("MEL");
		c2c.setSiren("200093201");
		c2c.setCities(List.of(city));
		when(repository.findAll()).thenReturn(List.of(c2c));
		when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(c2c)));
	}
}
