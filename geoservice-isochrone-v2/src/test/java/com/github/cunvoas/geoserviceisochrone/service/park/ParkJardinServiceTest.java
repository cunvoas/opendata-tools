package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionGeo;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ParkJardinServiceTest {

	@Mock private ParkJardinRepository parkJardinRepository;
	@Mock private ParkAreaRepository parkAreaRepository;
	@Mock private ParkTypeService parkTypeService;
	@Mock private CadastreRepository cadastreRepository;
	@Mock private CityRepository cityRepository;
	@Mock private InseeCarre200mOnlyShapeRepository surfaceRepo;

	@InjectMocks
	private ParkJardinService tested;

	private static City city(Long id) {
		City c = new City();
		c.setId(id);
		c.setInseeCode("69123");
		return c;
	}

	private static ParkType type(Long id, boolean strict, boolean oms) {
		ParkType t = new ParkType();
		t.setId(id);
		t.setStrict(strict);
		t.setOms(oms);
		return t;
	}

	private ParcEtJardin existingPark(City commune) {
		ParcEtJardin p = new ParcEtJardin();
		p.setId(10L);
		p.setCommune(commune);
		p.setTypeId(1L);
		p.setContour(new org.locationtech.jts.geom.GeometryFactory().createPolygon());
		return p;
	}

	@Test
	void save_rejects_city_change_on_existing_park() {
		ParcEtJardin p = existingPark(city(1L));
		when(parkJardinRepository.getReferenceById(10L)).thenReturn(existingPark(city(2L)));

		ExceptionGeo ex = assertThrows(ExceptionGeo.class, () -> tested.save(p, false));
		assertTrue(ex.getMessage().contains("PARK_CHANGE_CITY"));
		verify(parkJardinRepository, never()).saveAndFlush(any());
	}

	@Test
	void save_marks_park_area_to_compute_when_type_changes() {
		City c = city(1L);
		ParcEtJardin p = existingPark(c);
		ParkArea pa = new ParkArea();
		pa.setType(type(2L, true, false));
		ParkType newType = type(1L, true, true);

		when(parkJardinRepository.getReferenceById(10L)).thenReturn(existingPark(c));
		when(parkAreaRepository.findByIdParcEtJardin(10L)).thenReturn(pa);
		when(parkTypeService.get(1L)).thenReturn(newType);
		when(parkJardinRepository.saveAndFlush(p)).thenReturn(p);

		tested.save(p, false);

		assertEquals(Boolean.TRUE, pa.getToCompute());
		assertNotNull(pa.getUpdated());
		assertSame(newType, pa.getType());
		assertEquals(Boolean.TRUE, p.getOmsCustom());
		verify(parkAreaRepository).save(pa);
	}

	@Test
	void save_does_not_touch_park_area_when_nothing_changed() {
		City c = city(1L);
		ParcEtJardin p = existingPark(c);
		p.setOmsCustom(Boolean.TRUE);
		ParkArea pa = new ParkArea();
		pa.setType(type(1L, false, true));
		pa.setOmsCustom(Boolean.TRUE);

		when(parkJardinRepository.getReferenceById(10L)).thenReturn(existingPark(c));
		when(parkAreaRepository.findByIdParcEtJardin(10L)).thenReturn(pa);
		when(parkJardinRepository.saveAndFlush(p)).thenReturn(p);

		tested.save(p, false);

		verify(parkAreaRepository, never()).save(any());
	}

	@Test
	void save_recomputes_contour_surface_when_asked() {
		City c = city(1L);
		ParcEtJardin p = existingPark(c);
		when(parkJardinRepository.getReferenceById(10L)).thenReturn(existingPark(c));
		when(parkAreaRepository.findByIdParcEtJardin(10L)).thenReturn(null);
		when(surfaceRepo.getSurface(p.getContour())).thenReturn(1500L);
		when(parkJardinRepository.saveAndFlush(p)).thenReturn(p);

		tested.save(p, true);

		assertEquals(1500d, p.getSurfaceContour());
	}

	@Test
	void save_does_not_recompute_surface_when_not_asked() {
		City c = city(1L);
		ParcEtJardin p = existingPark(c);
		when(parkJardinRepository.getReferenceById(10L)).thenReturn(existingPark(c));
		when(parkAreaRepository.findByIdParcEtJardin(10L)).thenReturn(null);
		when(surfaceRepo.getSurface(p.getContour())).thenReturn(1500L);
		when(parkJardinRepository.saveAndFlush(p)).thenReturn(p);

		tested.save(p, false);

		assertNull(p.getSurfaceContour());
	}

	@Test
	void save_derives_coordinate_from_contour_centroid() {
		City c = city(1L);
		ParcEtJardin p = existingPark(c);
		when(parkJardinRepository.getReferenceById(10L)).thenReturn(existingPark(c));
		when(parkJardinRepository.saveAndFlush(p)).thenReturn(p);

		tested.save(p, false);

		assertNotNull(p.getCoordonnee());
	}
}
