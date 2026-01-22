package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

class Solver3ComputationStrategyTest {

    private static final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    void computeWithEmptyMapReturnsEmpty() {
        Solver3ComputationStrategy sut = new Solver3ComputationStrategy();
        Map<String, ParkProposalWork> map = new HashMap<>();

        List<ParkProposal> res = sut.compute(map, 0.0, 12.0, 200);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void computeWithEmptyMapAndDifferentParametersReturnsEmpty() {
        Solver3ComputationStrategy sut = new Solver3ComputationStrategy();
        Map<String, ParkProposalWork> map = new HashMap<>();

        List<ParkProposal> res = sut.compute(map, 5.0, 15.0, 500);

        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void computeWithPopulationZeroSetsNewSurfacePerCapitaNull() {
        Solver3ComputationStrategy sut = new Solver3ComputationStrategy();
        Map<String, ParkProposalWork> map = new HashMap<>();

        ParkProposalWork p = new ParkProposalWork();
        p.setAnnee(2020);
        p.setIdInspire("C1");
        p.setAccessingPopulation(BigDecimal.ZERO);
        p.setAccessingSurface(BigDecimal.ZERO);
        p.setCentre(geometryFactory.createPoint(new Coordinate(0.0, 0.0)));

        map.put(p.getIdInspire(), p);

        List<ParkProposal> res = sut.compute(map, 10.0, 12.0, 200);

        assertNotNull(res);
        // Note: List may not be empty even when processing population zero squares
        // The solver can still create proposals for neighboring squares

        ParkProposalWork updated = map.get("C1");
        assertNull(updated.getNewSurfacePerCapita());
        assertNotNull(updated.getNewSurface());
        assertEquals(1, updated.getNewSurface().compareTo(BigDecimal.ZERO));
        assertNotNull(updated.getNewMissingSurface());
    }

}
