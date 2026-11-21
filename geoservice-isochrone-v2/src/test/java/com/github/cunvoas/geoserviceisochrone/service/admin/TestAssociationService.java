package com.github.cunvoas.geoserviceisochrone.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.repo.admin.AssociationRepository;

@ExtendWith(MockitoExtension.class)
class TestAssociationService {

	@Mock
	private AssociationRepository associationRepository;

	@InjectMocks
	private AssociationService tested;

	private static Association asso(Long id, String nom) {
		Association a = new Association();
		a.setId(id);
		a.setNom(nom);
		return a;
	}

	private static Contributeur contrib(ContributeurRole role, Association asso) {
		Contributeur c = new Contributeur();
		c.setId(1L);
		c.setRole(role);
		c.setAssociation(asso);
		return c;
	}

	@Test
	void findById_returns_empty_when_unknown() {
		when(associationRepository.findById(99L)).thenReturn(Optional.empty());

		assertNull(tested.findById(99L));
	}

	@Test
	void findById_returns_found_association() {
		Association a = asso(5L, "Asso 5");
		when(associationRepository.findById(5L)).thenReturn(Optional.of(a));

		assertSame(a, tested.findById(5L));
	}

	@Test
	void findByContextUser_gives_all_associations_to_admin() {
		List<Association> all = List.of(asso(1L, "A"), asso(2L, "B"));
		when(associationRepository.findAllOrderByNom()).thenReturn(all);

		List<Association> ret = tested.findByContextUser(contrib(ContributeurRole.ADMINISTRATOR, null));

		assertEquals(all, ret);
	}

	@Test
	void findByContextUser_gives_only_own_association_to_manager() {
		Association own = asso(7L, "Mienne");
		when(associationRepository.findById(7L)).thenReturn(Optional.of(own));

		List<Association> ret = tested.findByContextUser(contrib(ContributeurRole.ASSOCIATION_MANAGER, own));

		assertEquals(1, ret.size());
		assertSame(own, ret.get(0));
	}

	@Test
	void findByContextUser_returns_empty_when_manager_has_no_association() {
		when(associationRepository.findById((Long) null)).thenReturn(Optional.empty());

		assertTrue(tested.findByContextUser(contrib(ContributeurRole.ASSOCIATION_MANAGER, null)).isEmpty());
	}

	@Test
	void save_delegates_to_repository() {
		Association a = asso(null, "Nouvelle");
		Association saved = asso(3L, "Nouvelle");
		when(associationRepository.save(a)).thenReturn(saved);

		assertSame(saved, tested.save(a));
		verify(associationRepository).save(a);
	}
}
