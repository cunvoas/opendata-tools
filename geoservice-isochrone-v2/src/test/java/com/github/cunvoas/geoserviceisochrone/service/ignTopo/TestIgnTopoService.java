package com.github.cunvoas.geoserviceisochrone.service.ignTopo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.cunvoas.geoserviceisochrone.model.ignTopo.IgnTopoVegetal;
import com.github.cunvoas.geoserviceisochrone.repo.ignTopo.IgnTopoVegetalRepository;

@ExtendWith(MockitoExtension.class)
class TestIgnTopoService {

	@Mock
	private IgnTopoVegetalRepository ignTopoVegetalRepository;

	@InjectMocks
	private IgnTopoService tested;

	private static Polygon polygon() {
		GeometryFactory f = new GeometryFactory();
		return f.createPolygon(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(0, 1), new Coordinate(1, 1),
				new Coordinate(1, 0), new Coordinate(0, 0) });
	}

	@Test
	void getVegetal_delegates_to_repository() {
		List<IgnTopoVegetal> list = List.of(new IgnTopoVegetal());
		when(ignTopoVegetalRepository.findByInseeId("69123")).thenReturn(list);

		assertEquals(list, tested.getVegetal("69123"));
	}

	@Test
	void findAllCarreByArea_queries_with_polygon_text() {
		List<IgnTopoVegetal> list = List.of(new IgnTopoVegetal());
		when(ignTopoVegetalRepository.findTopoVegetalByMapArea(anyString())).thenReturn(list);

		assertEquals(list, tested.findAllCarreByArea(polygon()));
		verify(ignTopoVegetalRepository).findTopoVegetalByMapArea(anyString());
	}

	@Test
	void findAllCarreByInseeAndArea_forwards_insee() {
		when(ignTopoVegetalRepository.findTopoVegetalByVilleProcheMapArea(org.mockito.ArgumentMatchers.eq("69123"),
				anyString())).thenReturn(List.of());

		assertEquals(List.of(), tested.findAllCarreByInseeAndArea("69123", polygon()));
	}

	@Test
	void findAllCarreByCom2coIdAndArea_forwards_id() {
		when(ignTopoVegetalRepository.findTopoVegetalByCom2CoAndMapArea(org.mockito.ArgumentMatchers.eq(200L),
				anyString())).thenReturn(List.of());

		assertEquals(List.of(), tested.findAllCarreByCom2coIdAndArea(200L, polygon()));
	}
}
