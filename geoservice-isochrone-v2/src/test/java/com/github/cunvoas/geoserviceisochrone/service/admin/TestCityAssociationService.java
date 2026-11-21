package com.github.cunvoas.geoserviceisochrone.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;

@ExtendWith(MockitoExtension.class)
class CityAssociationServiceTest {

	@Mock
	private CityRepository cityRepository;

	@Mock
	private CommunauteCommuneRepository communauteCommuneRepository;

	@InjectMocks
	private CityAssociationService tested;

	private static CommunauteCommune c2c(long id, String name) {
		CommunauteCommune c = new CommunauteCommune();
		c.setId(id);
		c.setName(name);
		return c;
	}

	private static City city(Long id, String insee, String name, CommunauteCommune c2c) {
		City c = new City();
		c.setId(id);
		c.setInseeCode(insee);
		c.setName(name);
		c.setCommunauteCommune(c2c);
		return c;
	}

	private static MockMultipartFile csv(String content) {
		return new MockMultipartFile("file", "villes.csv", "text/csv",
				content.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void associateCity_links_city_to_communaute() {
		City c = city(1L, "69123", "Lyon", null);
		CommunauteCommune c2c = c2c(10L, "Métropole");
		when(cityRepository.findById(1L)).thenReturn(Optional.of(c));
		when(communauteCommuneRepository.findById(10L)).thenReturn(Optional.of(c2c));
		when(cityRepository.save(c)).thenReturn(c);

		assertSame(c, tested.associateCity(1L, 10L));
		assertSame(c2c, c.getCommunauteCommune());
	}

	@Test
	void associateCity_returns_null_when_city_unknown() {
		when(cityRepository.findById(1L)).thenReturn(Optional.empty());

		assertNull(tested.associateCity(1L, 10L));
		verify(cityRepository, never()).save(any());
	}

	@Test
	void associateCity_returns_null_when_communaute_unknown() {
		City c = city(1L, "69123", "Lyon", null);
		when(cityRepository.findById(1L)).thenReturn(Optional.of(c));
		when(communauteCommuneRepository.findById(10L)).thenReturn(Optional.empty());

		assertNull(tested.associateCity(1L, 10L));
		verify(cityRepository, never()).save(any());
	}

	@Test
	void dissociateCity_clears_communaute() {
		CommunauteCommune c2c = c2c(10L, "Métropole");
		City c = city(1L, "69123", "Lyon", c2c);
		when(cityRepository.findById(1L)).thenReturn(Optional.of(c));
		when(cityRepository.save(c)).thenReturn(c);

		tested.dissociateCity(1L);

		assertNull(c.getCommunauteCommune());
	}

	@Test
	void associateCitiesFromCsv_counts_success_already_notFound_and_invalid() {
		CommunauteCommune target = c2c(10L, "Métropole");
		CommunauteCommune other = c2c(20L, "Autre");
		City free = city(1L, "69123", "Lyon", null);
		City alreadyTarget = city(2L, "69001", "Lyon 1", target);
		City alreadyOther = city(3L, "69002", "Lyon 2", other);

		when(communauteCommuneRepository.findById(10L)).thenReturn(Optional.of(target));
		when(cityRepository.findByInseeCode("69123")).thenReturn(free);
		when(cityRepository.findByInseeCode("69001")).thenReturn(alreadyTarget);
		when(cityRepository.findByInseeCode("69002")).thenReturn(alreadyOther);
		when(cityRepository.findByInseeCode("99999")).thenReturn(null);
		when(cityRepository.save(free)).thenReturn(free);

		String content = "code_insee\n69123\n69001\n69002\n99999\nabc\n\n";
		CityAssociationService.CsvUploadResult result = tested.associateCitiesFromCsv(csv(content), 10L);

		assertEquals(1, result.getSuccessList().size());
		assertEquals(1, result.getAlreadyAssociatedList().size());
		assertEquals(1, result.getNotFoundList().size());
		// ligne vide ignorée, en-tête ignoré, "abc" invalide et ville déjà rattachée ailleurs
		assertEquals(2, result.getErrorList().size());
		assertEquals(5, result.getTotalProcessed());
		assertTrue(result.hasErrors());
		assertSame(target, free.getCommunauteCommune());
	}

	@Test
	void associateCitiesFromCsv_fails_when_communaute_unknown() {
		when(communauteCommuneRepository.findById(10L)).thenReturn(Optional.empty());

		CityAssociationService.CsvUploadResult result = tested.associateCitiesFromCsv(csv("69123"), 10L);

		assertTrue(result.hasErrors());
		assertEquals(1, result.getTotalProcessed());
		verify(cityRepository, never()).findByInseeCode(any());
	}

	@Test
	void associateCitiesFromCsv_reports_io_error() {
		MockMultipartFile failing = new MockMultipartFile("file", "villes.csv", "text/csv", new byte[0]) {
			@Override
			public java.io.InputStream getInputStream() throws IOException {
				throw new IOException("boom");
			}
		};
		when(communauteCommuneRepository.findById(10L)).thenReturn(Optional.of(c2c(10L, "Métropole")));

		CityAssociationService.CsvUploadResult result = tested.associateCitiesFromCsv(failing, 10L);

		assertTrue(result.hasErrors());
		assertTrue(result.getErrorList().get(0).contains("boom"));
	}

	@Test
	void csv_upload_result_counters_are_consistent() {
		CityAssociationService.CsvUploadResult result = new CityAssociationService.CsvUploadResult();

		assertFalse(result.hasErrors());
		assertEquals(0, result.getTotalProcessed());

		result.addSuccess("a");
		result.addNotFound("b");
		result.addAlreadyAssociated("c");
		result.addError("d");

		assertEquals(4, result.getTotalProcessed());
		assertTrue(result.hasErrors());
	}
}
