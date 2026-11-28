package com.github.cunvoas.geoserviceisochrone.service.admin;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;

class TestAccessScopeService {

	private final AccessScopeService tested = new AccessScopeService();

	@AfterEach
	void clear() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void adminSeesAllEpci() {
		Contributeur admin = contrib(ContributeurRole.ADMINISTRATOR, null);
		assertTrue(tested.isAdmin(admin));
		assertTrue(tested.canAccessEpci(admin, 1L));
		assertNull(tested.scopedEpciId(admin));
	}

	@Test
	void managerIsLimitedToItsEpci() {
		Contributeur manager = contrib(ContributeurRole.ASSOCIATION_MANAGER, 42L);
		assertTrue(tested.isManager(manager));
		assertTrue(tested.canAccessEpci(manager, 42L));
		assertFalse(tested.canAccessEpci(manager, 1L));
	}

	@Test
	void managerWithoutEpciCannotAccess() {
		Contributeur manager = contrib(ContributeurRole.ASSOCIATION_MANAGER, null);
		assertFalse(tested.canAccessEpci(manager, 42L));
		assertNull(tested.scopedEpciId(manager));
	}

	@Test
	void currentReadsSecurityContext() {
		Contributeur manager = contrib(ContributeurRole.ASSOCIATION_MANAGER, 42L);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(manager, null, manager.getAuthorities()));
		assertTrue(tested.current() == manager);
		assertTrue(tested.canAccessEpci(tested.current(), 42L));
	}

	private Contributeur contrib(ContributeurRole role, Long epciId) {
		Contributeur c = new Contributeur();
		c.setId(1L);
		c.setRole(role);
		c.setIdCommunauteCommune(epciId);
		return c;
	}
}
