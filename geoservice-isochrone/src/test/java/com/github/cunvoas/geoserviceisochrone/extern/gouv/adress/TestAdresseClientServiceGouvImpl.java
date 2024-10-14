package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestAdresseClientServiceGouvImpl {

	private AdresseClientServiceGouvImpl tested;
	
	@Test
	void test() {
		tested = new AdresseClientServiceGouvImpl();
		
		try {
			String res = tested.search("59350", "rue jean jaures");
			Assertions.assertNotNull(res);
			Assertions.assertNotEquals("", res);
			
			System.out.println(res);
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
	}

}
