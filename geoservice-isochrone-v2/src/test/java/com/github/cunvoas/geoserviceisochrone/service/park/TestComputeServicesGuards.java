package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestComputeServicesGuards {

	@Mock private ApplicationBusinessProperties applicationBusinessProperties;
	@Mock private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Mock private ServiceOpenData serviceOpenData;

	@InjectMocks
	private ComputeServiceV2 computeServiceV2;

	private static ParkArea parkArea() {
		ParkArea pa = new ParkArea();
		pa.setId(3L);
		pa.setName("Parc");
		return pa;
	}

	@Test
	void compute_service_surface_delegates_to_repository() {
		Geometry geom = new GeometryFactory().createPoint();
		when(inseeCarre200mOnlyShapeRepository.getSurface(geom)).thenReturn(40000L);

		assertEquals(40000L, computeServiceV2.getSurface(geom));
	}

	@Test
	void compute_carre_by_job_returns_false_when_carre_is_unknown() {
		ComputeJob job = new ComputeJob();
		job.setIdInspire("I1");
		job.setAnnee(2024);
		when(inseeCarre200mOnlyShapeRepository.findById("I1")).thenReturn(Optional.empty());

		assertEquals(Boolean.FALSE, computeServiceV2.computeCarreByComputeJob(job));
		verify(serviceOpenData, never()).isDistanceDense(any(String.class));
	}

	@Test
	void compute_carre_by_job_returns_false_on_exception() {
		ComputeJob job = new ComputeJob();
		job.setIdInspire("I1");
		job.setAnnee(2024);
		InseeCarre200mOnlyShape carre = new InseeCarre200mOnlyShape();
		carre.setIdInspire("I1");
		carre.setCodeInsee("69123");
		when(inseeCarre200mOnlyShapeRepository.findById("I1")).thenReturn(Optional.of(carre));
		when(serviceOpenData.isDistanceDense("69123")).thenThrow(new IllegalStateException("boom"));

		assertEquals(Boolean.FALSE, computeServiceV2.computeCarreByComputeJob(job));
	}

	@Test
	void compute_park_area_is_skipped_without_polygon() {
		ComputeParkAreaV2 computeParkAreaV2 = new ComputeParkAreaV2();
		org.springframework.test.util.ReflectionTestUtils.setField(computeParkAreaV2,
				"inseeCarre200mOnlyShapeRepository", inseeCarre200mOnlyShapeRepository);
		org.springframework.test.util.ReflectionTestUtils.setField(computeParkAreaV2,
				"applicationBusinessProperties", applicationBusinessProperties);

		ParkAreaComputed ret = computeParkAreaV2.computeGenericParkAreaV2(parkArea());

		assertNull(ret);
	}
}
