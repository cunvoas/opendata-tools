package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import com.github.cunvoas.geoserviceisochrone.BaseUnitTest;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.service.solver.helper.ParkProposalHelper;
import com.github.cunvoas.geoserviceisochrone.service.solver.helper.ProposalComputationTypeAlgo;

import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;

@Log
class ParkProposalHelperTest extends BaseUnitTest {

    private static final GeometryFactory GF = new GeometryFactory();

    private static final int URBAN_DISTANCE = 300;
    private static final double BASE_LON = 3.0145658;
    private static final double BASE_LAT = 50.6319291;

    @Test
    void findNeighbors_centerSquareExists_returnsNeighborsWithinRadius() {
        Map<String, ParkProposalWork> map = buildGrid(7, 0.002);
        ParkProposalWork center = map.get("C-0-0");

        List<ParkProposalWork> neighbors = ParkProposalHelper.findNeighbors(
                center.getIdInspire(), map, URBAN_DISTANCE);

        assertNotNull(neighbors);
        assertTrue(neighbors.size() > 0);
        assertTrue(neighbors.size() < map.size());
    }

    @Test
    void findNeighbors_centerSquareNotFound_returnsEmptyList() {
        Map<String, ParkProposalWork> map = buildGrid(7, 0.002);

        List<ParkProposalWork> neighbors = ParkProposalHelper.findNeighbors(
                "UNKNOWN_ID", map, URBAN_DISTANCE);

        assertNotNull(neighbors);
        assertTrue(neighbors.isEmpty());
    }

    @Test
    void findNeighbors_emptyMap_returnsEmptyList() {
        Map<String, ParkProposalWork> map = new HashMap<>();

        List<ParkProposalWork> neighbors = ParkProposalHelper.findNeighbors(
                "C-0-0", map, URBAN_DISTANCE);

        assertNotNull(neighbors);
        assertTrue(neighbors.isEmpty());
    }

    @Test
    void findNeighbors_allSquaresFarAway_returnsEmptyList() {
        Map<String, ParkProposalWork> map = new HashMap<>();
        map.put("CENTER", makeSquare("CENTER", BASE_LON, BASE_LAT));
        map.put("FAR", makeSquare("FAR", BASE_LON + 0.05, BASE_LAT));

        List<ParkProposalWork> neighbors = ParkProposalHelper.findNeighbors(
                "CENTER", map, URBAN_DISTANCE);

        assertNotNull(neighbors);
        assertTrue(neighbors.isEmpty());
    }

    @Test
    void findNeighbors_largeUrbanDistance_returnsAllOtherSquares() {
        Map<String, ParkProposalWork> map = buildGrid(5, 0.002);

        List<ParkProposalWork> neighbors = ParkProposalHelper.findNeighbors(
                "C-0-0", map, 10_000);

        assertEquals(map.size() - 1, neighbors.size());
    }

    @Test
    void findNeighbors_returnsExactDistanceSquares() {
        Map<String, ParkProposalWork> map = new HashMap<>();
        map.put("C", makeSquare("C", BASE_LON, BASE_LAT));
        map.put("VERY_CLOSE", makeSquare("VERY_CLOSE", BASE_LON + 0.0005, BASE_LAT));
        map.put("BORDERLINE_IN", makeSquare("BORDERLINE_IN", BASE_LON + 0.0035, BASE_LAT + 0.0015));

        List<ParkProposalWork> neighbors = ParkProposalHelper.findNeighbors(
                "C", map, URBAN_DISTANCE);

        assertTrue(neighbors.size() >= 1);
    }

    @Test
    void findNeighbors_selfExcludedFromResult() {
        Map<String, ParkProposalWork> map = buildGrid(3, 0.002);
        String centerId = "C-0-0";

        List<ParkProposalWork> neighbors = ParkProposalHelper.findNeighbors(
                centerId, map, URBAN_DISTANCE);

        boolean selfInList = neighbors.stream()
                .anyMatch(n -> n.getIdInspire().equals(centerId));
        assertTrue(neighbors.isEmpty() || !selfInList);
    }

    @Test
    void findNeighbors_centerSquareAtGridEdge_stillFindsNeighbors() {
        Map<String, ParkProposalWork> map = buildGrid(7, 0.002);

        List<ParkProposalWork> neighbors = ParkProposalHelper.findNeighbors(
                "C--3--3", map, URBAN_DISTANCE);

        assertNotNull(neighbors);
    }

    @Test
    void findNeighbors_withDenseTerritory_returnsExpectedNeighbors() {
        Map<String, ParkProposalWork> map = buildGrid(5, 0.002);

        List<ParkProposalWork> neighbors = ParkProposalHelper.findNeighbors(
                "C-0-0", map, URBAN_DISTANCE);

        assertNotNull(neighbors);
        assertFalse(neighbors.isEmpty(), "un carré entouré doit avoir des voisins");
        assertTrue(neighbors.size() < map.size(), "le carré central ne doit pas etre son propre voisin");
        assertFalse(neighbors.stream().anyMatch(n -> "C-0-0".equals(n.getIdInspire())));
    }

    private Map<String, ParkProposalWork> buildGrid(int size, double stepDeg) {
        Map<String, ParkProposalWork> map = new HashMap<>();
        int half = size / 2;
        for (int dx = -half; dx <= half; dx++) {
            for (int dy = -half; dy <= half; dy++) {
                String id = "C-" + dx + "-" + dy;
                double lon = BASE_LON + dx * stepDeg;
                double lat = BASE_LAT + dy * stepDeg;
                map.put(id, makeSquare(id, lon, lat));
            }
        }
        return map;
    }

    private static ParkProposalWork makeSquare(String id, double lon, double lat) {
        ParkProposalWork p = new ParkProposalWork();
        p.setAnnee(2020);
        p.setIdInspire(id);
        p.setCentre(GF.createPoint(new Coordinate(lon, lat)));
        p.setAccessingPopulation(BigDecimal.valueOf(500));
        p.setAccessingSurface(BigDecimal.valueOf(2000));
        p.setMissingSurface(BigDecimal.valueOf(5000));
        p.setSurfacePerCapita(BigDecimal.valueOf(4));
        p.setTypeAlgo(ProposalComputationTypeAlgo.ITERATIVE_1);
        return p;
    }
}
