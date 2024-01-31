package com.github.cunvoas.geoserviceisochrone.extern.csv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mShape;


class TestCsvCarre200ShapeParser {

	CsvCarre200ShapeParser tested = new CsvCarre200ShapeParser();
	
	@Test
	@Disabled
	void test() {
		File test = new File("/home/cus/Documents/Associations/DeulAir/200m-carreaux-metropole/200m_carreaux_metropole_shapefile_wgs84.csv");
		try {
			List<InseeCarre200mShape>rows = tested.parseCarree200Shape(test);
			
			assertNotNull(rows, "rows");
			assertEquals(rows.size(), 2_278_213, "nb rows");
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}

}
