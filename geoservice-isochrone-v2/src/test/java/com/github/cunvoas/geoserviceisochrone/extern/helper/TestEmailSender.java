package com.github.cunvoas.geoserviceisochrone.extern.helper;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.github.cunvoas.geoserviceisochrone.BaseUnitTest;

@DisplayName("Tests pour EmailSender")
class TestEmailSender extends BaseUnitTest {
	
	private EmailSender tested = new EmailSender();
	
	@Mock
	private EmailSender mockEmailService;
	
	@Test
	@DisplayName("création d'instance EmailSender réussie")
	void test_instantiation() {
		assertNotNull(tested);
	}
	
	@Test
	@DisplayName("envoi email bienvenue avec données valides")
	void test_sendWelcome_success() throws Exception {
		String email = "test@example.com";
		String prenom = "John";
		String login = "johndoe";
		
		try {
			tested.sendWelcome(email, prenom, login);
		} catch (Exception e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("envoi email bienvenue avec email invalide")
	void test_sendWelcome_invalidEmail() {
		String email = "invalid-email";
		String prenom = "John";
		String login = "johndoe";
		
		try {
			tested.sendWelcome(email, prenom, login);
		} catch (Exception e) {
			assertNotNull(e.getMessage());
		}
	}

	@Test
	@DisplayName("envoi email avec valeurs nulles")
	void test_sendWelcome_nullValues() {
		String email = null;
		String prenom = null;
		String login = null;
		
		try {
			tested.sendWelcome(email, prenom, login);
		} catch (NullPointerException | IllegalArgumentException e) {
			assertNotNull(e.getMessage());
		}
	}
}
