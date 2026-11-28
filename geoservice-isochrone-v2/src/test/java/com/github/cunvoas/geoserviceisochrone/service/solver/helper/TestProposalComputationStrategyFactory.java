package com.github.cunvoas.geoserviceisochrone.service.solver.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.service.solver.compute.Genetic1Strategy;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.IterativeComputationDBSCANStrategy;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.IterativeComputationDeficit2Strategy;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.IterativeComputationPopulation1Strategy;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.LeastSquaresNeighbour2Strategy;

class TestProposalComputationStrategyFactory {

	@Test
	void available_types_are_stable() {
		List<ProposalComputationTypeAlgo> types = ProposalComputationStrategyFactory.getAvailableTypes();

		assertEquals(6, types.size());
		assertTrue(types.contains(ProposalComputationTypeAlgo.ITERATIVE_2));
		assertTrue(types.contains(ProposalComputationTypeAlgo.GENETIC_7));
	}

	@Test
	void each_type_creates_its_strategy() {
		assertTrue(ProposalComputationStrategyFactory.create(ProposalComputationTypeAlgo.ITERATIVE_2, 650d)
				instanceof IterativeComputationDeficit2Strategy);
		assertTrue(ProposalComputationStrategyFactory.create(ProposalComputationTypeAlgo.ITERATIVE_3, 650d)
				instanceof IterativeComputationPopulation1Strategy);
		assertTrue(ProposalComputationStrategyFactory.create(ProposalComputationTypeAlgo.ITERATIVE_DBSCAN, 650d)
				instanceof IterativeComputationDBSCANStrategy);
		assertTrue(ProposalComputationStrategyFactory.create(ProposalComputationTypeAlgo.CHI2_6, 650d)
				instanceof LeastSquaresNeighbour2Strategy);
		assertTrue(ProposalComputationStrategyFactory.create(ProposalComputationTypeAlgo.GENETIC_7, 650d)
				instanceof Genetic1Strategy);
		assertNotNull(ProposalComputationStrategyFactory.create(ProposalComputationTypeAlgo.PPC_3, 650d));
	}

	@Test
	void unknown_type_is_rejected() {
		assertThrows(NullPointerException.class,
				() -> ProposalComputationStrategyFactory.create(null, 650d));
	}
}
