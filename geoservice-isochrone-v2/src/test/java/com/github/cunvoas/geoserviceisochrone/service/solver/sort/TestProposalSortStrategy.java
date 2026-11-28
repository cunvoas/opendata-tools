package com.github.cunvoas.geoserviceisochrone.service.solver.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

class TestProposalSortStrategy {

	private static ParkProposalWork work(String id, Double accessingSurface, Double missingSurface,
			Double surfacePerCapita, Double population) {
		ParkProposalWork w = new ParkProposalWork();
		w.setIdInspire(id);
		w.setAccessingSurface(accessingSurface == null ? null : BigDecimal.valueOf(accessingSurface));
		w.setNewMissingSurface(missingSurface == null ? null : BigDecimal.valueOf(missingSurface));
		w.setNewSurfacePerCapita(surfacePerCapita == null ? null : BigDecimal.valueOf(surfacePerCapita));
		w.setAccessingPopulation(population == null ? null : BigDecimal.valueOf(population));
		return w;
	}

	private static Map<String, ParkProposalWork> sample() {
		Map<String, ParkProposalWork> map = new LinkedHashMap<>();
		map.put("a", work("a", 30d, 5d, 3d, 10d));
		map.put("b", work("b", 10d, 50d, 1d, 100d));
		map.put("c", work("c", 20d, 10d, 2d, 1d));
		return map;
	}

	private static String ids(List<ParkProposalWork> sorted) {
		StringBuilder sb = new StringBuilder();
		for (ParkProposalWork w : sorted) {
			sb.append(w.getIdInspire());
		}
		return sb.toString();
	}

	@Test
	void accessing_surface_strategy_sorts_ascending() {
		List<ParkProposalWork> sorted = new AccessingSurfaceSortStrategy().sort(sample());

		// b=10, c=20, a=30
		assertEquals("bca", ids(sorted));
	}

	@Test
	void deficit_strategy_sorts_descending() {
		List<ParkProposalWork> sorted = new DeficitProposalSortStrategy().sort(sample());

		assertEquals("bca", ids(sorted));
	}

	@Test
	void persona_strategy_sorts_surface_per_capita_ascending() {
		List<ParkProposalWork> sorted = new PersonaProposalSortStrategy().sort(sample());

		assertEquals("bca", ids(sorted));
	}

	@Test
	void missing_population_strategy_prioritises_human_impact() {
		List<ParkProposalWork> sorted = new MissingPopulationProposalSortStrategy().sort(sample());

		// b: 50*100 = 5000 ; a: 5*10 = 50 ; c: 10*1 = 10
		assertEquals("bac", ids(sorted));
	}

	@Test
	void null_values_are_treated_as_zero() {
		Map<String, ParkProposalWork> map = new LinkedHashMap<>();
		map.put("n", work("n", null, null, null, null));
		map.put("v", work("v", 5d, 5d, 5d, 5d));

		assertEquals("nv", ids(new AccessingSurfaceSortStrategy().sort(map)));
		assertEquals("vn", ids(new DeficitProposalSortStrategy().sort(map)));
		assertEquals("nv", ids(new PersonaProposalSortStrategy().sort(map)));
		assertEquals("vn", ids(new MissingPopulationProposalSortStrategy().sort(map)));
	}

	@Test
	void empty_map_gives_empty_list() {
		assertTrue(new DeficitProposalSortStrategy().sort(Map.of()).isEmpty());
	}

	@Test
	void factory_creates_each_strategy() {
		assertNotNull(ProposalSortStrategyFactory.create(ProposalSortStrategyFactory.Type.DEFICIT));
		assertTrue(ProposalSortStrategyFactory.create(ProposalSortStrategyFactory.Type.PERSONA)
				instanceof PersonaProposalSortStrategy);
		assertTrue(ProposalSortStrategyFactory.create(ProposalSortStrategyFactory.Type.ACCESSING_SURFACE)
				instanceof AccessingSurfaceSortStrategy);
		assertTrue(ProposalSortStrategyFactory.create(ProposalSortStrategyFactory.Type.MISSING_POPULATION)
				instanceof MissingPopulationProposalSortStrategy);
	}
}
