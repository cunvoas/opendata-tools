package com.github.cunvoas.geoserviceisochrone.service.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

class GeometryComparatorTest {

	private final GeometryFactory factory = new GeometryFactory();
	private final GeometryComparator tested = new GeometryComparator();

	private Geometry point(double x, double y) {
		return factory.createPoint(new Coordinate(x, y));
	}

	@Test
	void compares_on_x_first() {
		assertTrue(tested.compare(point(1, 5), point(2, 0)) < 0);
		assertTrue(tested.compare(point(3, 0), point(2, 9)) > 0);
	}

	@Test
	void compares_on_y_when_x_equal() {
		assertTrue(tested.compare(point(1, 5), point(1, 9)) < 0);
		assertEquals(0, tested.compare(point(1, 5), point(1, 5)));
	}

	@Test
	void null_geometry_is_sorted_first() {
		assertTrue(tested.compare(null, point(0, 0)) < 0);
		assertTrue(tested.compare(point(0, 0), null) < 0);
		assertTrue(tested.compare(null, null) <= 0);
	}
}
