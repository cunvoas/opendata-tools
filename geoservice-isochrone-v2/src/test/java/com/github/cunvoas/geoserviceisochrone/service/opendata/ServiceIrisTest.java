package com.github.cunvoas.geoserviceisochrone.service.opendata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisData;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisDataRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;

@ExtendWith(MockitoExtension.class)
class ServiceIrisTest {

	@Mock
	private IrisDataRepository irisDataRepository;
	@Mock
	private IrisShapeRepository irisShapeRepository;

	@InjectMocks
	private ServiceIris tested;

	private static IrisShape shape() {
		IrisShape s = new IrisShape();
		s.setContour(new GeometryFactory().createPolygon(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(0, 10), new Coordinate(10, 10),
				new Coordinate(10, 0), new Coordinate(0, 0) }));
		return s;
	}

	@Test
	void save_all_data_delegates_to_repository() {
		List<IrisData> datas = List.of(new IrisData());

		tested.saveAllData(datas);

		verify(irisDataRepository).saveAll(datas);
	}

	@Test
	void save_all_shape_delegates_to_repository() {
		List<IrisShape> shapes = List.of(shape());

		tested.saveAllShape(shapes);

		verify(irisShapeRepository).saveAll(shapes);
	}

	@Test
	void update_computes_a_stable_footprint() {
		IrisShape s = shape();
		when(irisShapeRepository.save(s)).thenReturn(s);

		IrisShape ret = tested.update(s);

		assertSame(s, ret);
		assertNotNull(ret.getFootprint());
		Integer first = ret.getFootprint();

		// même géométrie => même empreinte
		IrisShape other = shape();
		when(irisShapeRepository.save(other)).thenReturn(other);
		tested.update(other);

		assertEquals(first, other.getFootprint());
	}

	@Test
	void update_returns_shape_untouched_without_contour() {
		IrisShape s = new IrisShape();

		assertSame(s, tested.update(s));
		assertNull(s.getFootprint());
		verify(irisShapeRepository, never()).save(any());
	}

	@Test
	void compute_footprint_updates_only_shapes_without_footprint() {
		IrisShape s = shape();
		when(irisShapeRepository.findByFootprintIsNull()).thenReturn(List.of(s));
		when(irisShapeRepository.save(s)).thenReturn(s);

		tested.computeFootprint();

		assertNotNull(s.getFootprint());
		verify(irisShapeRepository).save(s);
	}
}
