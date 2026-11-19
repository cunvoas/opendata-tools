package com.github.cunvoas.geoserviceisochrone.service.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.export.dto.ParkExportLine;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceVerificationExporterTest {

	@Mock
	private ParkJardinRepository parkJardinRepository;
	@Mock
	private ParkAreaRepository parkAreaRepository;

	@InjectMocks
	private ServiceVerificationExporter tested;

	private static City city(Long id, String name) {
		Region region = new Region();
		region.setId(84L);
		CommunauteCommune c2c = new CommunauteCommune();
		c2c.setId(200L);
		City c = new City();
		c.setId(id);
		c.setName(name);
		c.setRegion(region);
		c.setCommunauteCommune(c2c);
		return c;
	}

	private static ParcEtJardin park(Long id, String name) {
		ParcEtJardin p = new ParcEtJardin();
		p.setId(id);
		p.setName(name);
		p.setSurface(1000d);
		p.setSurfaceContour(1100d);
		p.setOmsCustom(Boolean.TRUE);
		return p;
	}

	@Test
	void export_city_maps_each_park() {
		City c = city(1L, "Lyon");
		when(parkJardinRepository.findByCityId(1L)).thenReturn(List.of(park(10L, "Parc A")));

		List<ParkExportLine> lines = tested.export4verif(c);

		assertEquals(1, lines.size());
		ParkExportLine line = lines.get(0);
		assertEquals(84L, line.getIdRegion());
		assertEquals(200L, line.getIdCom2Co());
		assertEquals("Lyon", line.getCommune());
		assertEquals("Parc A", line.getParkName());
		assertEquals(1000d, line.getSurfaceOpendata());
		assertEquals(1100d, line.getSurfaceContour());
		assertNull(line.getIdParkArea());
	}

	@Test
	void export_city_enriches_with_computed_park_area() {
		City c = city(1L, "Lyon");
		ParcEtJardin p = park(10L, "Parc A");
		ParkArea pa = new ParkArea();
		pa.setId(77L);
		pa.setToCompute(Boolean.FALSE);
		Date updated = new Date();
		pa.setUpdated(updated);
		pa.setEntrances(List.of(new com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance(),
				new com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance()));
		when(parkJardinRepository.findByCityId(1L)).thenReturn(List.of(p));
		when(parkAreaRepository.findByIdParcEtJardin(10L)).thenReturn(pa);

		ParkExportLine line = tested.export4verif(c).get(0);

		assertEquals(77L, line.getIdParkArea());
		assertEquals(2, line.getNbParkEntrance());
		assertEquals(updated, line.getParkAreaComputedDate());
	}

	@Test
	void export_city_does_not_expose_date_when_area_still_to_compute() {
		City c = city(1L, "Lyon");
		ParcEtJardin p = park(10L, "Parc A");
		ParkArea pa = new ParkArea();
		pa.setId(77L);
		pa.setToCompute(Boolean.TRUE);
		pa.setUpdated(new Date());
		when(parkJardinRepository.findByCityId(1L)).thenReturn(List.of(p));
		when(parkAreaRepository.findByIdParcEtJardin(10L)).thenReturn(pa);

		assertNull(tested.export4verif(c).get(0).getParkAreaComputedDate());
	}

	@Test
	void export_communaute_aggregates_all_cities() {
		City c1 = city(1L, "Lyon");
		City c2 = city(2L, "Villeurbanne");
		CommunauteCommune c2c = new CommunauteCommune();
		c2c.setId(200L);
		c2c.setCities(List.of(c1, c2));
		when(parkJardinRepository.findByCityId(1L)).thenReturn(List.of(park(10L, "A")));
		when(parkJardinRepository.findByCityId(2L)).thenReturn(List.of(park(11L, "B"), park(12L, "C")));

		List<ParkExportLine> lines = tested.export4verif(c2c);

		assertEquals(3, lines.size());
	}

	@Test
	void export_city_without_park_returns_empty_list() {
		when(parkJardinRepository.findByCityId(1L)).thenReturn(List.of());

		assertTrue(tested.export4verif(city(1L, "Lyon")).isEmpty());
	}
}
