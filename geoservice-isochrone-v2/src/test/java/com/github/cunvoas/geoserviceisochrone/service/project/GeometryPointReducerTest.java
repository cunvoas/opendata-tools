package com.github.cunvoas.geoserviceisochrone.service.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

class GeometryPointReducerTest {

	private final GeometryFactory factory = new GeometryFactory();
	private final GeometryPointReducer tested = new GeometryPointReducer();

	private Polygon square() {
		return factory.createPolygon(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(0, 10), new Coordinate(10, 10),
				new Coordinate(10, 0), new Coordinate(0, 0) });
	}

	@Test
	void null_and_empty_geometries_are_returned_as_is() {
		assertEquals(null, tested.reduceByConvexHull(null));
		assertEquals(null, tested.reduceConvexHullToMax10Min6(null));
		assertEquals(0, tested.getPoints(null).size());
	}

	@Test
	void convex_hull_of_square_keeps_five_coordinates() {
		Geometry hull = tested.reduceByConvexHull(square());

		assertEquals(5, hull.getCoordinates().length);
	}

	@Test
	void small_hull_is_not_resampled() {
		Geometry hull = tested.reduceConvexHullToMax10Min6(square());

		assertSame(Polygon.class, hull.getClass());
		assertEquals(5, hull.getCoordinates().length);
	}

	@Test
	void large_hull_is_reduced_to_ten_points() {
		Coordinate[] coords = new Coordinate[21];
		for (int i = 0; i < 20; i++) {
			double angle = 2 * Math.PI * i / 20;
			coords[i] = new Coordinate(Math.cos(angle), Math.sin(angle));
		}
		coords[20] = coords[0];
		Polygon circle = factory.createPolygon(coords);

		Geometry reduced = tested.reduceConvexHullToMax10Min6(circle);

		assertEquals(10, reduced.getCoordinates().length);
	}

	@Test
	void get_points_removes_duplicates() {
		List<org.locationtech.jts.geom.Point> points = tested.getPoints(square());

		assertEquals(4, points.size());
	}
}
