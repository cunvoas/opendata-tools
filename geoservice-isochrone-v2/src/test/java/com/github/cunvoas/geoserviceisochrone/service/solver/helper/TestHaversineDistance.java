package com.github.cunvoas.geoserviceisochrone.service.solver.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TestHaversineDistance {

	private final HaversineDistance tested = new HaversineDistance();

	@Test
	void same_point_is_zero() {
		assertEquals(0d, tested.compute(new double[] { 4.85, 45.75 }, new double[] { 4.85, 45.75 }), 0.0001);
	}

	@Test
	void lyon_to_paris_is_about_390_km() {
		// [lon, lat]
		double d = tested.compute(new double[] { 4.85, 45.75 }, new double[] { 2.35, 48.85 });

		assertTrue(d > 380_000 && d < 400_000, "distance attendue ~390 km, obtenu " + d);
	}

	@Test
	void distance_is_symmetric() {
		double ab = tested.compute(new double[] { 4.85, 45.75 }, new double[] { 4.86, 45.76 });
		double ba = tested.compute(new double[] { 4.86, 45.76 }, new double[] { 4.85, 45.75 });

		assertEquals(ab, ba, 0.0001);
	}
}
