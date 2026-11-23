package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

@ExtendWith(MockitoExtension.class)
class TestAbstractComputeService {

	@Mock
	private ParkJardinRepository parkJardinRepository;
	@Mock
	private InseeCarre200mOnlyShapeRepository surfaceRepository;

	/** Implémentation concrète minimale pour tester la classe abstraite. */
	private static class Testable extends AbstractComputeService {
		Testable(ParkJardinRepository parkJardinRepository,
				InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository) {
			super(parkJardinRepository, inseeCarre200mOnlyShapeRepository);
		}

		boolean active(ParkArea pa, Integer annee) {
			return isActive(pa, annee);
		}
	}

	private Testable tested() {
		return new Testable(parkJardinRepository, surfaceRepository);
	}

	private static ParkArea area(long parkId) {
		ParkArea pa = new ParkArea();
		pa.setIdParcEtJardin(parkId);
		return pa;
	}

	private static ParcEtJardin park(Date debut, Date fin) {
		ParcEtJardin p = new ParcEtJardin();
		p.setId(1L);
		p.setDateDebut(debut);
		p.setDateFin(fin);
		return p;
	}

	private static Date year(int year) {
		Calendar cal = Calendar.getInstance();
		cal.clear();
		cal.set(Calendar.YEAR, year);
		return cal.getTime();
	}

	@Test
	void inactive_when_park_is_unknown() {
		when(parkJardinRepository.findById(1L)).thenReturn(Optional.empty());

		assertFalse(tested().active(area(1L), 2024));
	}

	@Test
	void active_by_default_without_dates() {
		when(parkJardinRepository.findById(1L)).thenReturn(Optional.of(park(null, null)));

		assertTrue(tested().active(area(1L), 2024));
	}

	@Test
	void active_only_between_dates() {
		when(parkJardinRepository.findById(1L)).thenReturn(Optional.of(park(year(2010), year(2020))));
		Testable t = tested();

		assertFalse(t.active(area(1L), 2009));
		assertTrue(t.active(area(1L), 2010));
		assertTrue(t.active(area(1L), 2020));
		assertFalse(t.active(area(1L), 2021));
	}

	@Test
	void active_from_start_date_only() {
		when(parkJardinRepository.findById(1L)).thenReturn(Optional.of(park(year(2015), null)));
		Testable t = tested();

		assertFalse(t.active(area(1L), 2014));
		assertTrue(t.active(area(1L), 2100));
	}

	@Test
	void active_until_end_date_only() {
		when(parkJardinRepository.findById(1L)).thenReturn(Optional.of(park(null, year(2015))));
		Testable t = tested();

		assertTrue(t.active(area(1L), 1900));
		assertFalse(t.active(area(1L), 2016));
	}

	@Test
	void get_surface_delegates_to_repository() {
		Geometry geom = new GeometryFactory().createPoint();
		when(surfaceRepository.getSurface(geom)).thenReturn(40000L);

		assertEquals(40000L, tested().getSurface(geom));
	}

	@Test
	void null_geometry_gives_null_surface() {
		when(surfaceRepository.getSurface((Geometry) null)).thenReturn(null);

		assertNull(tested().getSurface(null));
		verify(surfaceRepository).getSurface((Geometry) null);
	}
}
