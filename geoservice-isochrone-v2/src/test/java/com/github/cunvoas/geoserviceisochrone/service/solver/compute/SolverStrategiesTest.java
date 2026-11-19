package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

class SolverStrategiesTest {

	private static ParkProposalWork work(String id, double missing, double population, double perCapita) {
		ParkProposalWork w = new ParkProposalWork();
		w.setIdInspire(id);
		w.setNewMissingSurface(BigDecimal.valueOf(missing));
		w.setAccessingPopulation(BigDecimal.valueOf(population));
		w.setNewSurfacePerCapita(BigDecimal.valueOf(perCapita));
		w.setAccessingSurface(BigDecimal.valueOf(missing));
		w.setCentre(new org.locationtech.jts.geom.GeometryFactory().createPoint());
		return w;
	}

	private static Map<String, ParkProposalWork> sample() {
		Map<String, ParkProposalWork> map = new LinkedHashMap<>();
		map.put("a", work("a", 10, 5, 2));
		map.put("b", work("b", 30, 100, 1));
		return map;
	}

	private static class ConcreteStrategy extends AbstractComputationtrategy {
		@Override
		public List<ParkProposal> compute(Map<String, ParkProposalWork> squaresOnTerritoryMap,
				Double minSquareMeterPerCapita, Double recoSquareMeterPerCapita, Integer urbanDistance) {
			return List.of();
		}
	}

	private final ConcreteStrategy tested = new ConcreteStrategy();

	@Test
	void sort_by_deficit_puts_biggest_first() {
		List<ParkProposalWork> sorted = tested.sortProposalsByDeficit(sample());

		assertEquals("b", sorted.get(0).getIdInspire());
	}

	@Test
	void sort_by_persona_puts_lowest_per_capita_first() {
		List<ParkProposalWork> sorted = tested.sortProposalsByPersona(sample());

		assertEquals("b", sorted.get(0).getIdInspire());
	}

	@Test
	void sort_by_missing_population_puts_biggest_impact_first() {
		List<ParkProposalWork> sorted = tested.sortProposalsByMissingPopulation(sample());

		assertEquals("b", sorted.get(0).getIdInspire());
	}

	@Test
	void find_neighbors_delegates_to_helper() {
		assertTrue(tested.findNeighbors("absent", sample(), 300).isEmpty());
	}

	@Test
	void expected_constants_are_defined() {
		assertEquals(1_000d, AbstractComputationtrategy.AT_LEAST_PARK_SURFACE);
		assertEquals(650d, AbstractComputationtrategy.MIN_PARK_SURFACE);
		assertEquals(200d, AbstractComputationtrategy.CARRE_SIZE);
		assertEquals(40_000d, AbstractComputationtrategy.CARRE_SURFACE);
		assertEquals(120, AbstractComputationtrategy.SQUARE_DISTANCE);
	}

	@Test
	void strategies_can_be_instantiated_with_min_surface() {
		assertNotNull(new IterativeComputationDeficit2Strategy(650d));
		assertNotNull(new IterativeComputationPopulation1Strategy(650d));
		assertNotNull(new IterativeComputationDBSCANStrategy(650d));
		assertNotNull(new Genetic1Strategy(650d));
	}

	@Test
	void iterative_deficit_returns_empty_on_empty_map() {
		IterativeComputationDeficit2Strategy strategy = new IterativeComputationDeficit2Strategy(650d);

		assertTrue(strategy.compute(Map.of(), 12d, 12d, 300).isEmpty());
	}

	@Test
	void iterative_population_returns_empty_on_empty_map() {
		IterativeComputationPopulation1Strategy strategy = new IterativeComputationPopulation1Strategy(650d);

		assertTrue(strategy.compute(Map.of(), 12d, 12d, 300).isEmpty());
	}

	@Test
	void dbscan_returns_empty_on_empty_map() {
		IterativeComputationDBSCANStrategy strategy = new IterativeComputationDBSCANStrategy(650d);

		assertTrue(strategy.compute(Map.of(), 12d, 12d, 300).isEmpty());
	}

	@Test
	void least_squares_returns_empty_on_empty_map() {
		LeastSquaresNeighbour2Strategy strategy = new LeastSquaresNeighbour2Strategy();

		assertTrue(strategy.compute(Map.of(), 12d, 12d, 300).isEmpty());
	}
}
