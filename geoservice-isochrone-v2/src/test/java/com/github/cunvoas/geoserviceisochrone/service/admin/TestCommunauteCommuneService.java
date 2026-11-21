package com.github.cunvoas.geoserviceisochrone.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;

@ExtendWith(MockitoExtension.class)
class TestCommunauteCommuneService {

	@Mock
	private CommunauteCommuneRepository repository;

	@InjectMocks
	private CommunauteCommuneService tested;

	private static CommunauteCommune c2c(Long id, String name, String siren) {
		CommunauteCommune c = new CommunauteCommune();
		c.setId(id);
		c.setName(name);
		c.setSiren(siren);
		return c;
	}

	private static Contributeur contrib(ContributeurRole role) {
		Contributeur c = new Contributeur();
		c.setId(1L);
		c.setRole(role);
		return c;
	}

	@Test
	void findById_returns_null_when_unknown() {
		when(repository.findById(4L)).thenReturn(Optional.empty());

		assertNull(tested.findById(4L));
	}

	@Test
	void findByContextUserAndRegion_filters_on_region() {
		List<CommunauteCommune> region = List.of(c2c(1L, "A", null));
		when(repository.findByRegionId(11L)).thenReturn(region);

		assertEquals(region, tested.findByContextUserAndRegion(contrib(ContributeurRole.ADMINISTRATOR), 11L));
	}

	@Test
	void findByContextUserAndRegion_returns_all_without_region() {
		List<CommunauteCommune> all = List.of(c2c(1L, "A", null), c2c(2L, "B", null));
		when(repository.findAll()).thenReturn(all);

		assertEquals(all, tested.findByContextUserAndRegion(contrib(ContributeurRole.ASSOCIATION_CONSTRIBUTOR), null));
	}

	@Test
	void save_creates_new_entity_and_trims_siren() {
		CommunauteCommune in = c2c(null, "Nouvelle", "  123456789  ");
		when(repository.save(in)).thenReturn(in);

		assertSame(in, tested.save(in));
		assertEquals("123456789", in.getSiren());
		verify(repository, never()).findById(any());
	}

	@Test
	void save_replaces_blank_siren_with_null() {
		CommunauteCommune in = c2c(null, "Nouvelle", "   ");
		when(repository.save(in)).thenReturn(in);

		tested.save(in);

		assertNull(in.getSiren());
	}

	@Test
	void save_updates_existing_entity_fields() {
		CommunauteCommune existing = c2c(8L, "Ancien nom", "111111111");
		CommunauteCommune in = c2c(8L, "Nouveau nom", "222222222");
		when(repository.findById(8L)).thenReturn(Optional.of(existing));
		when(repository.save(existing)).thenReturn(existing);

		CommunauteCommune ret = tested.save(in);

		assertSame(existing, ret);
		assertEquals("Nouveau nom", existing.getName());
		assertEquals("222222222", existing.getSiren());
	}

	@Test
	void save_creates_entity_when_id_is_unknown_instead_of_saving_null() {
		CommunauteCommune in = c2c(99L, "Orpheline", "333333333");
		when(repository.findById(99L)).thenReturn(Optional.empty());
		when(repository.save(in)).thenReturn(in);

		CommunauteCommune ret = tested.save(in);

		assertSame(in, ret);
		verify(repository).save(in);
		verify(repository, never()).save(org.mockito.ArgumentMatchers.isNull());
	}
}
