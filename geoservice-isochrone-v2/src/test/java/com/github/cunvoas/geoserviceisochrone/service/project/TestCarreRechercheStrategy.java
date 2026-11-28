package com.github.cunvoas.geoserviceisochrone.service.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.MapperIsoChrone;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.DtoIsoChroneParser;
import com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.IsoChroneClientService;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorIsochone;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorlIsochroneRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestCarreRechercheStrategy {

	private final GeometryFactory factory = new GeometryFactory();

	@Mock private NeighborsCarreRechercheStrategy neighborsStrategy;
	@Mock private GeometryReducerIsochroneCarreRechercheStrategy geometryStrategy;
	@Mock private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Mock private ProjectSimulatorlIsochroneRepository projectSimulatorlIsochroneRepository;
	@Mock private IsoChroneClientService clientIsoChrone;
	@Mock private DtoIsoChroneParser dtoIsoChroneParser;
	@Mock private MapperIsoChrone mapperIsoChrone;

	private NeighborsCarreRechercheStrategy neighborsTested;
	private GeometryReducerIsochroneCarreRechercheStrategy reducerTested;

	@org.junit.jupiter.api.BeforeEach
	void wireStrategies() {
		neighborsTested = new NeighborsCarreRechercheStrategy();
		org.springframework.test.util.ReflectionTestUtils.setField(neighborsTested,
				"inseeCarre200mOnlyShapeRepository", inseeCarre200mOnlyShapeRepository);

		reducerTested = new GeometryReducerIsochroneCarreRechercheStrategy();
		org.springframework.test.util.ReflectionTestUtils.setField(reducerTested,
				"geometryPointReducer", new GeometryPointReducer());
		org.springframework.test.util.ReflectionTestUtils.setField(reducerTested,
				"inseeCarre200mOnlyShapeRepository", inseeCarre200mOnlyShapeRepository);
		org.springframework.test.util.ReflectionTestUtils.setField(reducerTested,
				"projectSimulatorlIsochroneRepository", projectSimulatorlIsochroneRepository);
		org.springframework.test.util.ReflectionTestUtils.setField(reducerTested, "clientIsoChrone", clientIsoChrone);
		org.springframework.test.util.ReflectionTestUtils.setField(reducerTested, "dtoIsoChroneParser", dtoIsoChroneParser);
		org.springframework.test.util.ReflectionTestUtils.setField(reducerTested, "mapperIsoChrone", mapperIsoChrone);
	}

	private static InseeCarre200mOnlyShape carre(String id, double lon, double lat) {
		InseeCarre200mOnlyShape c = new InseeCarre200mOnlyShape();
		c.setIdInspire(id);
		double d = 0.001;
		GeometryFactory f = new GeometryFactory();
		c.setGeoShape(f.createPolygon(new Coordinate[] {
				new Coordinate(lon, lat), new Coordinate(lon + d, lat),
				new Coordinate(lon + d, lat + d), new Coordinate(lon, lat + d),
				new Coordinate(lon, lat) }));
		return c;
	}

	@Test
	void factory_returns_the_configured_strategy() {
		CarreRechercheStrategyFactory f = new CarreRechercheStrategyFactory();
		org.springframework.test.util.ReflectionTestUtils.setField(f, "neighborsCarreRechercheStrategy", neighborsStrategy);
		org.springframework.test.util.ReflectionTestUtils.setField(f, "geometryReducerIsochroneCarreRechercheStrategy", geometryStrategy);

		assertSame(neighborsStrategy, f.getStrategy(ModeRechercheCarre.NEIGHBORS));
		assertSame(geometryStrategy, f.getStrategy(ModeRechercheCarre.GEOMETRY_REDUCER_ISOCHRONE));
	}

	@Test
	void factory_rejects_unknown_mode() {
		CarreRechercheStrategyFactory f = new CarreRechercheStrategyFactory();
		org.springframework.test.util.ReflectionTestUtils.setField(f, "neighborsCarreRechercheStrategy", neighborsStrategy);
		org.springframework.test.util.ReflectionTestUtils.setField(f, "geometryReducerIsochroneCarreRechercheStrategy", geometryStrategy);

		assertThrows(RuntimeException.class, () -> f.getStrategy(null));
	}

	@Test
	void neighbors_strategy_aggregates_neighbors_of_project_carres() {
		ProjectSimulator project = new ProjectSimulator();
		project.setInsee("69123");
		project.setShapeArea(factory.createPolygon(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(0, 1), new Coordinate(1, 1),
				new Coordinate(1, 0), new Coordinate(0, 0) }));

		InseeCarre200mOnlyShape centre = carre("C", 4.85, 45.75);
		InseeCarre200mOnlyShape neighbour = carre("N", 4.8505, 45.75);
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode("69123", true))
				.thenReturn(List.of(centre, neighbour));
		when(inseeCarre200mOnlyShapeRepository.findCarreInMapArea(project.getShapeArea()))
				.thenReturn(List.of(centre));

		Set<InseeCarre200mOnlyShape> ret = neighborsTested.findCarres(project, 300);

		assertTrue(ret.contains(centre));
		assertTrue(ret.contains(neighbour));
	}

	@Test
	void neighbors_strategy_returns_empty_without_project_carre() {
		ProjectSimulator project = new ProjectSimulator();
		project.setInsee("69123");
		project.setShapeArea(factory.createPolygon());
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(anyString(), anyBoolean())).thenReturn(List.of());
		when(inseeCarre200mOnlyShapeRepository.findCarreInMapArea(any(Polygon.class))).thenReturn(List.of());

		assertTrue(neighborsTested.findCarres(project, 300).isEmpty());
	}

	@Test
	void reducer_strategy_queries_carres_inside_merged_shape() {
		ProjectSimulator project = new ProjectSimulator();
		project.setShapeArea(factory.createPolygon(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(0, 1), new Coordinate(1, 1),
				new Coordinate(1, 0), new Coordinate(0, 0) }));
		when(projectSimulatorlIsochroneRepository.findByIdProjectSimulator(any())).thenReturn(List.of());
		when(projectSimulatorlIsochroneRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
		when(clientIsoChrone.getIsoChrone(any(), anyString())).thenReturn("{}");
		when(dtoIsoChroneParser.parseBasicIsoChrone("{}"))
				.thenReturn(new com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoIsoChrone());
		when(mapperIsoChrone.map(any(com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto.DtoIsoChrone.class)))
				.thenReturn(factory.createPolygon(new Coordinate[] {
						new Coordinate(0, 0), new Coordinate(0, 1), new Coordinate(1, 1),
						new Coordinate(1, 0), new Coordinate(0, 0) }));
		InseeCarre200mOnlyShape impacted = carre("C", 4.85, 45.75);
		when(inseeCarre200mOnlyShapeRepository.findCarreInMapArea(any(org.locationtech.jts.geom.Geometry.class), anyBoolean()))
				.thenReturn(List.of(impacted));

		Set<InseeCarre200mOnlyShape> ret = reducerTested.findCarres(project, 300);

		assertEquals(Set.of(impacted), ret);
	}
}
