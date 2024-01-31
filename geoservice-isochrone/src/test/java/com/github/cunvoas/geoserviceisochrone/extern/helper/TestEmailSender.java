package com.github.cunvoas.geoserviceisochrone.extern.helper;

import static org.junit.jupiter.api.Assertions.fail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class TestEmailSender {
	
	private EmailSender tested = new EmailSender();
	
	
	@Test
	@Disabled
	void test() {
		
		try {
			tested.sendWelcome("mon@emaol.com", "prenom nom", "logion");
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}

}
