package com.github.cunvoas.geoserviceisochrone.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.extern.helper.EmailSender;
import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurRepository;

@ExtendWith(MockitoExtension.class)
class ContributeurServiceTest {

	@Mock
	private ContributeurRepository contributeurRepository;
	@Mock
	private PasswordService passwordService;
	@Mock
	private EmailSender emailSender;

	@InjectMocks
	private ContributeurService tested;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	private static Contributeur contrib(Long id, String login, String email) {
		Contributeur c = new Contributeur();
		c.setId(id);
		c.setLogin(login);
		c.setEmail(email);
		c.setNom("Dupont");
		c.setPrenom("Jean");
		Association a = new Association();
		a.setId(5L);
		c.setAssociation(a);
		return c;
	}

	private static void logIn(Contributeur c) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(c, null, java.util.List.of()));
	}

	@Test
	void get_returns_null_when_unknown() {
		when(contributeurRepository.findById(3L)).thenReturn(Optional.empty());

		assertNull(tested.get(3L));
	}

	@Test
	void get_returns_contributor_when_found() {
		Contributeur c = contrib(3L, "jdupont", "j@d.fr");
		when(contributeurRepository.findById(3L)).thenReturn(Optional.of(c));

		assertSame(c, tested.get(3L));
	}

	@Test
	void getByLogin_returns_optional_from_repository() {
		Contributeur c = contrib(3L, "jdupont", "j@d.fr");
		when(contributeurRepository.findByLogin("jdupont")).thenReturn(Optional.of(c));

		assertTrue(tested.getByLogin("jdupont").isPresent());
	}

	@Test
	void findByAssociation_returns_empty_list_when_association_is_null() {
		assertTrue(tested.findByAssociation(null).isEmpty());
	}

	@Test
	void findByAssociation_delegates_to_repository() {
		Association a = new Association();
		a.setId(5L);
		List<Contributeur> members = List.of(contrib(1L, "a", "a@a.fr"));
		when(contributeurRepository.findByAssociation(a)).thenReturn(members);

		assertEquals(members, tested.findByAssociation(a));
	}

	@Test
	void delete_delegates_to_repository() {
		tested.delete(9L);

		verify(contributeurRepository).deleteById(9L);
	}

	@Test
	void updateLoginDate_sets_now_and_saves() {
		Contributeur c = contrib(1L, "a", "a@a.fr");

		tested.updateLoginDate(c);

		assertNotNull(c.getLastLoginDate());
		verify(contributeurRepository).save(c);
	}

	@Test
	void save_new_account_uses_default_role_and_sends_welcome_mail() {
		Contributeur in = contrib(null, "new", "new@a.fr");
		in.setPassword("MotDePasse123!");
		logIn(contrib(1L, "admin", "admin@a.fr"));

		when(passwordService.isSafe("MotDePasse123!")).thenReturn(true);
		when(passwordService.securizePassword("MotDePasse123!")).thenReturn("HASHED");
		when(contributeurRepository.existsByEmail("new@a.fr")).thenReturn(false);
		when(contributeurRepository.existsByLogin("new")).thenReturn(false);
		when(contributeurRepository.save(any(Contributeur.class))).thenAnswer(inv -> inv.getArgument(0));

		Contributeur ret = tested.save(in, false);

		assertEquals(ContributeurRole.ASSOCIATION_CONSTRIBUTOR, ret.getRole());
		assertEquals("HASHED", ret.getPassword());
		verify(emailSender).sendWelcome(eq("new@a.fr"), anyString(), eq("new"));
	}

	@Test
	void save_new_account_rejects_existing_email() {
		Contributeur in = contrib(null, "new", "new@a.fr");
		when(contributeurRepository.existsByEmail("new@a.fr")).thenReturn(true);

		ExceptionAdmin ex = assertThrows(ExceptionAdmin.class, () -> tested.save(in, true));
		assertEquals(ExceptionAdmin.RG_EMAIL_EXISTS, ex.getMessage());
		verify(contributeurRepository, never()).save(any());
	}

	@Test
	void save_new_account_rejects_existing_login() {
		Contributeur in = contrib(null, "new", "new@a.fr");
		when(contributeurRepository.existsByEmail("new@a.fr")).thenReturn(false);
		when(contributeurRepository.existsByLogin("new")).thenReturn(true);

		ExceptionAdmin ex = assertThrows(ExceptionAdmin.class, () -> tested.save(in, false));
		assertEquals(ExceptionAdmin.RG_LOGIN_EXISTS, ex.getMessage());
	}

	@Test
	void save_rejects_unsafe_password() {
		Contributeur stored = contrib(2L, "exist", "exist@a.fr");
		Contributeur in = contrib(2L, "exist", "exist@a.fr");
		in.setPassword("123");
		when(contributeurRepository.findById(2L)).thenReturn(Optional.of(stored));
		when(passwordService.isSafe("123")).thenReturn(false);

		ExceptionAdmin ex = assertThrows(ExceptionAdmin.class, () -> tested.save(in, false));
		assertEquals(ExceptionAdmin.RG_PWD_NOT_SAFE, ex.getMessage());
		verify(contributeurRepository, never()).save(any());
	}

	@Test
	void save_existing_without_password_generates_one_and_mails_it_to_other_user() {
		Contributeur stored = contrib(2L, "exist", "exist@a.fr");
		Contributeur in = contrib(2L, "exist", "exist@a.fr");
		logIn(contrib(1L, "admin", "admin@a.fr"));

		when(contributeurRepository.findById(2L)).thenReturn(Optional.of(stored));
		when(passwordService.generatePassword(20)).thenReturn("Genere123!");
		when(passwordService.securizePassword("Genere123!")).thenReturn("HASHED");
		when(contributeurRepository.save(stored)).thenReturn(stored);

		Contributeur ret = tested.save(in, true);

		assertEquals("HASHED", ret.getPassword());
		verify(emailSender).sendPassword("exist@a.fr", stored.getFullName(), "Genere123!", "exist");
	}

	@Test
	void save_existing_without_password_mails_himself_without_login_variant() {
		Contributeur self = contrib(2L, "moi", "moi@a.fr");
		Contributeur in = contrib(2L, "moi", "moi@a.fr");
		logIn(self);

		when(contributeurRepository.findById(2L)).thenReturn(Optional.of(self));
		when(passwordService.generatePassword(20)).thenReturn("Genere123!");
		when(passwordService.securizePassword("Genere123!")).thenReturn("HASHED");
		when(contributeurRepository.save(self)).thenReturn(self);

		tested.save(in, true);

		verify(emailSender).sendPassword("moi@a.fr", self.getFullName(), "Genere123!");
		verify(emailSender, never()).sendPassword(anyString(), anyString(), anyString(), anyString());
	}

	@Test
	void save_existing_keeps_password_when_none_requested() {
		Contributeur stored = contrib(2L, "exist", "exist@a.fr");
		stored.setPassword("OLDHASH");
		Contributeur in = contrib(2L, "exist", "exist@a.fr");
		when(contributeurRepository.findById(2L)).thenReturn(Optional.of(stored));
		when(contributeurRepository.save(stored)).thenReturn(stored);

		Contributeur ret = tested.save(in, false);

		assertEquals("OLDHASH", ret.getPassword());
		verify(passwordService, never()).generatePassword(anyInt());
		verify(emailSender, never()).sendWelcome(anyString(), anyString(), anyString());
	}
}
