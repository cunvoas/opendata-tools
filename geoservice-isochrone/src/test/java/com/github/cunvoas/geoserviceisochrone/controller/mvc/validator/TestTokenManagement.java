package com.github.cunvoas.geoserviceisochrone.controller.mvc.validator;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestTokenManagement {
	
	TokenManagement tested= new TokenManagement();
	
	@BeforeEach
	void setUp() throws Exception {

	    Field secretFieldExpire = TokenManagement.class.getDeclaredField("tokenExpirationMillis");
	    secretFieldExpire.setAccessible(true);
	    secretFieldExpire.set(tested, 120000);
	    
	    Field secretFielSecret = TokenManagement.class.getDeclaredField("tokenSecret");
	    secretFielSecret.setAccessible(true);
	    secretFielSecret.set(tested, "qf0hbWXyK38g+/bhHDJrcQRIMkDFbaUKw1NBS9HwTjI=");
	}

	@Test
	@Order(1)
	void testGetKey() {
		try {
			String key = tested.getStringKey();
			System.out.println("Generated KEY: " + key);
			Assertions.assertNotNull(key);
		} catch (NoSuchAlgorithmException e) {
			fail(e);
		}
	}
	
	@Test
	@Order(10)
	void testGetValidToken() {
		String token = tested.getValidToken();
		System.out.println("Generated Token: " + token);
		Assertions.assertNotNull(token);
	}

	@Test
	@Order(11)
	void testIsTokenValid() {
		try {
			String token = tested.getValidToken();
			Thread.sleep(1000L);
			boolean isValid = tested.isTokenValid(token);
			Assertions.assertTrue(isValid);
		} catch (InterruptedException e) {
			fail(e);
		}
	}

}

