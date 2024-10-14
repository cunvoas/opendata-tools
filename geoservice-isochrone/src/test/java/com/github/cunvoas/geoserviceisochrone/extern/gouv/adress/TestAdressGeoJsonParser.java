package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Set;

import org.assertj.core.util.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto.AdressBo;

class TestAdressGeoJsonParser {
	AdressGeoJsonParser tested = new AdressGeoJsonParser();

	@Test
	void test() {

		
		try {
			File f = ResourceUtils.getFile("classpath:adresseResponse.json");
			String content = Files.contentOf(f, Charset.defaultCharset());
			
			Set<AdressBo>set = tested.parse(content);
			
			Assertions.assertFalse(set==null);
			Assertions.assertEquals(2, set.size());
			
		} catch (JsonProcessingException e) {
			fail(e.getMessage());
		} catch (FileNotFoundException e) {
			fail(e.getMessage());
		}
		
	}

}
