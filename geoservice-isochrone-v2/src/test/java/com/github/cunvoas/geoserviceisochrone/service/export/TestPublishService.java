package com.github.cunvoas.geoserviceisochrone.service.export;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GitHelper;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;

class TestPublishService {

	private PublishService tested;

	private ServicePublicationExporter exporter;
	private StatsSurfaceService statsSurfaceService;
	private CommunauteCommuneRepository repository;
	private GitHelper gitHelper;
	private ApplicationBusinessProperties properties;

	@BeforeEach
	void setUp() {
		tested = new PublishService();
		exporter = mock(ServicePublicationExporter.class);
		statsSurfaceService = mock(StatsSurfaceService.class);
		repository = mock(CommunauteCommuneRepository.class);
		gitHelper = mock(GitHelper.class);
		properties = mock(ApplicationBusinessProperties.class);

		ReflectionTestUtils.setField(tested, "servicePublicationExporter", exporter);
		ReflectionTestUtils.setField(tested, "statsSurfaceService", statsSurfaceService);
		ReflectionTestUtils.setField(tested, "communauteCommuneRepository", repository);
		ReflectionTestUtils.setField(tested, "gitHelper", gitHelper);
		ReflectionTestUtils.setField(tested, "applicationBusinessProperties", properties);

		CommunauteCommune c2c = new CommunauteCommune();
		c2c.setId(1L);
		when(repository.findById(1L)).thenReturn(Optional.of(c2c));
		when(gitHelper.getLocalPath()).thenReturn("/tmp/repo");
	}

	@Test
	void testProductionFailureBlocksGitPublication() throws Exception {
		when(properties.getJsonFileFolder()).thenReturn("/tmp/data");
		doThrow(new IOException("boom")).when(exporter).writeGeoJsonCarreaux(any(), any());

		tested.publishAsync(1L, 2025);

		PublicationStatus status = tested.getStatus(1L, 2025);
		assertEquals(PublicationStatus.Phase.PRODUCTION_FAILED, status.getPhase());
		assertTrue(status.getMessage().contains("carres"));
		assertNull(status.getGitSha());
		verifyNoInteractions(gitHelper);
	}

	@Test
	void testPushSkippedWhenGitNotConfigured() throws Exception {
		when(properties.getJsonFileFolder()).thenReturn("/tmp/data");
		when(gitHelper.isConfigured()).thenReturn(false);

		tested.publishAsync(1L, 2025);

		PublicationStatus status = tested.getStatus(1L, 2025);
		assertEquals(PublicationStatus.Phase.PUSH_NOT_CONFIGURED, status.getPhase());
		verify(gitHelper, never()).addCommitPush(any(), any(String[].class));
	}

	@Test
	void testPublishedAfterSuccessfulProduction(@TempDir Path tempDir) throws Exception {
		Files.createDirectories(tempDir.resolve("geojson/carres/1"));
		Files.createDirectories(tempDir.resolve("data/stats/com2co/1"));
		when(properties.getJsonFileFolder()).thenReturn(tempDir.toString());
		when(gitHelper.isConfigured()).thenReturn(true);
		when(gitHelper.getLocalPath()).thenReturn(tempDir.toString());
		when(gitHelper.addCommitPush(eq(tempDir.toString()), any(String[].class))).thenReturn("abc123");

		tested.publishAsync(1L, 2025);

		PublicationStatus status = tested.getStatus(1L, 2025);
		assertEquals(PublicationStatus.Phase.PUBLISHED, status.getPhase());
		assertEquals("abc123", status.getGitSha());
		assertEquals(10, status.getFiles().size());
		assertEquals("referentiels", status.getFiles().get(0).getType());

		org.mockito.ArgumentCaptor<String[]> folders = org.mockito.ArgumentCaptor.forClass(String[].class);
		verify(gitHelper).addCommitPush(eq(tempDir.toString()), folders.capture());
		assertArrayEquals(new String[] { "geojson/carres/1", "data/stats/com2co/1" }, folders.getValue());
	}
}
