package com.github.cunvoas.geoserviceisochrone.service.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;

class TestNeighborsHelper {

	private final GeometryFactory factory = new GeometryFactory();

	private InseeCarre200mOnlyShape carre(String id, double lon, double lat) {
		InseeCarre200mOnlyShape c = new InseeCarre200mOnlyShape();
		c.setIdInspire(id);
		// carré ~200m
		double d = 0.001;
		Polygon p = factory.createPolygon(new Coordinate[] {
				new Coordinate(lon, lat), new Coordinate(lon + d, lat),
				new Coordinate(lon + d, lat + d), new Coordinate(lon, lat + d),
				new Coordinate(lon, lat) });
		c.setGeoShape(p);
		return c;
	}

	@Test
	void returns_neighbors_within_urban_distance() {
		InseeCarre200mOnlyShape centre = carre("C", 4.85, 45.75);
		InseeCarre200mOnlyShape close = carre("A", 4.8515, 45.75);   // ~120 m
		InseeCarre200mOnlyShape far = carre("B", 4.95, 45.75);       // ~7.8 km

		List<InseeCarre200mOnlyShape> ret = NeighborsHelper.findNeighbors(centre, List.of(centre, close, far), 300);

		assertTrue(ret.contains(centre));
		assertTrue(ret.contains(close));
		assertEquals(2, ret.size());
	}

	@Test
	void large_distance_includes_only_the_centre() {
		InseeCarre200mOnlyShape centre = carre("C", 4.85, 45.75);
		InseeCarre200mOnlyShape close = carre("A", 4.8600, 45.75); // ~780 m

		List<InseeCarre200mOnlyShape> ret = NeighborsHelper.findNeighbors(centre, List.of(centre, close), 10);

		assertEquals(1, ret.size());
	}

	@Test
	void empty_input_returns_empty_list() {
		InseeCarre200mOnlyShape centre = carre("C", 4.85, 45.75);

		assertTrue(NeighborsHelper.findNeighbors(centre, List.of(), 300).isEmpty());
	}
}
