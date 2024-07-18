package com.github.cunvoas.geoserviceisochrone.service.export;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"prod","dev"})
class TestStaticExport {
	@Autowired
	private StaticExport tested;

	@Test
	@Disabled
	void testExportCadastre() {
		tested.exportCadastre();
	}

	@Test
	@Disabled
	void testExportCarre() {
		tested.exportCarre();
	}

	@Test
//	@Disabled
	void testExportIsochrone() {
		tested.exportIsochrone();;
	}

}
