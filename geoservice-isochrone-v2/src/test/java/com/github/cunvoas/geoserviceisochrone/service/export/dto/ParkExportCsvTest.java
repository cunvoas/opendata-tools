package com.github.cunvoas.geoserviceisochrone.service.export.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.List;

import org.junit.jupiter.api.Test;

class ParkExportCsvTest {

	@Test
	void header_contains_all_columns_in_declaration_order() {
		List<String> header = ParkExportCsv.ParkExportCsvHeaders.header();

		assertEquals(14, header.size());
		assertEquals("idRegion", header.get(0));
		assertEquals("idParkArea", header.get(5));
		assertEquals("parkAreaComputedDate", header.get(13));
	}

	@Test
	void lookup_returns_the_matching_header() {
		assertSame(ParkExportCsv.ParkExportCsvHeaders.idCommune,
				ParkExportCsv.ParkExportCsvHeaders.get("idCommune"));
		assertNull(ParkExportCsv.ParkExportCsvHeaders.get("inconnu"));
	}

	@Test
	void each_header_exposes_its_column_name_used_as_lookup_key() {
		for (ParkExportCsv.ParkExportCsvHeaders h : ParkExportCsv.ParkExportCsvHeaders.values()) {
			assertSame(h, ParkExportCsv.ParkExportCsvHeaders.get(h.getColumn()));
		}
	}
}
