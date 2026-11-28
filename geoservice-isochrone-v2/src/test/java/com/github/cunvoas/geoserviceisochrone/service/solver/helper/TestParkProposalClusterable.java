package com.github.cunvoas.geoserviceisochrone.service.solver.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

class TestParkProposalClusterable {

	@Test
	void exposes_centre_as_lon_lat_array() {
		ParkProposalWork work = new ParkProposalWork();
		work.setIdInspire("I1");
		work.setCentre(new GeometryFactory().createPoint(new Coordinate(4.85, 45.75)));
		ParkProposalClusterable tested = new ParkProposalClusterable(work);

		double[] point = tested.getPoint();

		assertEquals(4.85, point[0], 0.000001);
		assertEquals(45.75, point[1], 0.000001);
		assertSame(work, tested.getWork());
	}
}
