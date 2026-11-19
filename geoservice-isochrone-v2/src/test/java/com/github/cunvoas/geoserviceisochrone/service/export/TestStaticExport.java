package com.github.cunvoas.geoserviceisochrone.service.export;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.cunvoas.geoserviceisochrone.support.PostgisContextTestBase;

class TestStaticExport extends PostgisContextTestBase {
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
	@Disabled
	void testExportIsochrone() {
		tested.exportIsochrone();;
	}

}
