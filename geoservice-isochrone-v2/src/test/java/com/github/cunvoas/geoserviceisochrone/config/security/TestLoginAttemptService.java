package com.github.cunvoas.geoserviceisochrone.config.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class TestLoginAttemptService {

	@Mock
	private HttpServletRequest request;

	private LoginAttemptService tested;

	@BeforeEach
	void setUp() throws Exception {
		tested = new LoginAttemptService();
		Field requestField = LoginAttemptService.class.getDeclaredField("request");
		requestField.setAccessible(true);
		requestField.set(tested, request);
	}

	@Test
	void forgedForwardedForDoesNotBypassBlock() {
		when(request.getRemoteAddr()).thenReturn("10.0.0.5");
		for (int i = 0; i < LoginAttemptService.MAX_ATTEMPT; i++) {
			tested.loginFailed("10.0.0.5");
		}
		assertTrue(tested.isBlocked());
	}

	@Test
	void otherClientIsNotBlocked() {
		when(request.getRemoteAddr()).thenReturn("10.0.0.6");
		for (int i = 0; i < LoginAttemptService.MAX_ATTEMPT; i++) {
			tested.loginFailed("10.0.0.5");
		}
		assertFalse(tested.isBlocked());
	}
}
