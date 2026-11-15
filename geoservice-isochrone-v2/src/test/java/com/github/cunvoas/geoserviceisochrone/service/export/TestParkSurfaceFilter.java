package com.github.cunvoas.geoserviceisochrone.service.export;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

/**
 * Vérifie le filtre de publication des parcs sans surface.
 */
class TestParkSurfaceFilter {

	private static final GeometryFactory FACTORY = new GeometryFactory();

	@Test
	void keepsOnlyPolygonGeometries() {
		Polygon square = (Polygon) FACTORY.toGeometry(new Envelope(0, 1, 0, 1));

		assertFalse(ServicePublicationExporter.hasSurface(null));
		assertFalse(ServicePublicationExporter.hasSurface(FACTORY.createPoint(new Coordinate(1, 2))));
		assertTrue(ServicePublicationExporter.hasSurface(square));
		assertTrue(ServicePublicationExporter.hasSurface(FACTORY.createMultiPolygon(new Polygon[] {square})));
	}
}
