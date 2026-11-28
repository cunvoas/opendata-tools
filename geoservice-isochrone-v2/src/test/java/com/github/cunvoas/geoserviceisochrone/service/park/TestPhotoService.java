package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.github.cunvoas.geoserviceisochrone.extern.helper.PhotoHelper;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.tools.ParkPhoto;
import com.github.cunvoas.geoserviceisochrone.repo.ParkPhotoRepository;
import com.github.cunvoas.geoserviceisochrone.service.park.dto.PhotoDto;

@ExtendWith(MockitoExtension.class)
class TestPhotoService {

	@Mock
	private ParkPhotoRepository parkPhotoRepository;

	@Mock
	private PhotoHelper photoHelper;

	@InjectMocks
	private PhotoService tested;

	@TempDir
	File tempDir;

	private PhotoDto dto(String filename, byte[] content) {
		ParcEtJardin park = new ParcEtJardin();
		park.setId(42L);

		PhotoDto dto = new PhotoDto();
		dto.setParcEtJardin(park);
		dto.setPhoto(new MockMultipartFile("photo", filename, "image/jpeg", content));
		dto.setStoreRoot(tempDir.getAbsolutePath() + "/resized/");
		dto.setStoreRootOrigin(tempDir.getAbsolutePath() + "/origin/");
		dto.setStoreFolder("69123");
		dto.setCommuneId(1L);
		dto.setInseeCode("69123");
		return dto;
	}

	@Test
	void empty_photo_is_ignored() {
		PhotoDto dto = dto("photo.jpg", new byte[0]);

		assertNull(tested.savePhoto(dto));
		verify(parkPhotoRepository, never()).save(any());
	}

	@Test
	void path_traversal_file_name_is_rejected() {
		PhotoDto dto = dto("../evil.jpg", "data".getBytes(StandardCharsets.UTF_8));

		assertNull(tested.savePhoto(dto));
		verify(parkPhotoRepository, never()).save(any());
	}

	@Test
	void valid_photo_is_stored_hashed_and_resized() throws Exception {
		PhotoDto dto = dto("photo.jpg", "contenu".getBytes(StandardCharsets.UTF_8));
		Coordinate coord = new Coordinate(45.75, 4.85);
		when(photoHelper.getCoordinateFromExif(any(File.class))).thenReturn(coord);
		when(parkPhotoRepository.save(any(ParkPhoto.class))).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<ParkPhoto> captor = ArgumentCaptor.forClass(ParkPhoto.class);
		tested.savePhoto(dto);

		verify(parkPhotoRepository).save(captor.capture());
		ParkPhoto saved = captor.getValue();
		assertEquals(42L, saved.getParcId());
		assertEquals("69123", saved.getInseeCode());
		assertEquals("photo.jpg", saved.getOriginalFileName());
		assertNotNull(saved.getOriginalFileHash());
		assertEquals(64, saved.getOriginalFileHash().length());
		assertNotNull(saved.getLocation());
		verify(photoHelper).resizeImage(any(File.class), any(File.class));
	}
}
