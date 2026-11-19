package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeIrisJob;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComputeIrisServiceIrisTest {

	@Mock private ParkJardinRepository parkJardinRepository;
	@Mock private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Mock private IrisShapeRepository irisShapeRepository;
	@Mock private ServiceOpenData serviceOpenData;

	private ComputeIrisServiceIris tested;

	@BeforeEach
	void wire() {
		tested = new ComputeIrisServiceIris(parkJardinRepository, inseeCarre200mOnlyShapeRepository);
		ReflectionTestUtils.setField(tested, "irisShapeRepository", irisShapeRepository);
		ReflectionTestUtils.setField(tested, "serviceOpenData", serviceOpenData);
	}

	@Test
	void compute_job_returns_false_when_iris_is_unknown() {
		ComputeIrisJob job = new ComputeIrisJob();
		job.setIris("IRIS1");
		job.setAnnee(2024);
		when(irisShapeRepository.findById("IRIS1")).thenReturn(Optional.empty());

		assertEquals(Boolean.FALSE, tested.computeIrisByComputeJob(job));
		verify(serviceOpenData, never()).isDistanceDense(any(String.class));
	}

	@Test
	void compute_job_returns_false_on_error_and_keeps_going() {
		ComputeIrisJob job = new ComputeIrisJob();
		job.setIris("IRIS1");
		job.setAnnee(2024);
		IrisShape shape = new IrisShape();
		shape.setIris("IRIS1");
		shape.setCodeInsee("69123");
		shape.setSurface(100d);
		when(irisShapeRepository.findById("IRIS1")).thenReturn(Optional.of(shape));
		when(serviceOpenData.isDistanceDense("69123")).thenThrow(new IllegalStateException("boom"));

		assertEquals(Boolean.FALSE, tested.computeIrisByComputeJob(job));
	}

	@Test
	void get_surface_uses_iris_repository() {
		Geometry geom = new GeometryFactory().createPoint();
		when(irisShapeRepository.getSurface(geom)).thenReturn(1234L);

		assertEquals(1234L, tested.getSurface(geom));
	}

	@Test
	void missing_surface_is_computed_and_saved() {
		ComputeIrisJob job = new ComputeIrisJob();
		job.setIris("IRIS1");
		job.setAnnee(2024);
		IrisShape shape = new IrisShape();
		shape.setIris("IRIS1");
		shape.setCodeInsee("69123");
		shape.setContour(new GeometryFactory().createPolygon());
		when(irisShapeRepository.findById("IRIS1")).thenReturn(Optional.of(shape));
		when(irisShapeRepository.getSurface(shape.getContour())).thenReturn(5000L);
		when(serviceOpenData.isDistanceDense("69123")).thenThrow(new IllegalStateException("stop here"));

		tested.computeIrisByComputeJob(job);

		assertEquals(5000d, shape.getSurface());
		verify(irisShapeRepository).save(shape);
	}

	@Test
	void is_active_is_inherited_and_checks_park_dates() {
		com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea area =
				new com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea();
		area.setIdParcEtJardin(1L);
		com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin park =
				new com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin();
		park.setId(1L);
		when(parkJardinRepository.findById(1L)).thenReturn(Optional.of(park));

		Boolean active = ReflectionTestUtils.invokeMethod(tested, "isActive", area, 2024);

		assertEquals(Boolean.TRUE, active);
	}
}
