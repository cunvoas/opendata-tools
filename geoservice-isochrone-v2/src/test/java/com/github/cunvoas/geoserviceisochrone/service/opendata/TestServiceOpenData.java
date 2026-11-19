package com.github.cunvoas.geoserviceisochrone.service.opendata;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;

class TestServiceOpenData {

	private final CommunauteCommuneRepository ccRepo = mock(CommunauteCommuneRepository.class);
	private final ServiceOpenData tested = new ServiceOpenData();

	TestServiceOpenData() {
		ReflectionTestUtils.setField(tested, "communauteCommuneRepository", ccRepo);
	}

	@Test
	void testEnvelopeBox_returns_null_when_communaute_is_unknown() {
		CommunauteCommune cc = new CommunauteCommune();
		cc.setId(1L);
		when(ccRepo.findById(1L)).thenReturn(Optional.empty());

		assertNull(tested.computeSquareOnMap(cc));
		verify(ccRepo).updateGeoShape(1L);
	}

	@Test
	void testEnvelopeBox_returns_low_shape_when_found() {
		CommunauteCommune input = new CommunauteCommune();
		input.setId(1L);
		CommunauteCommune stored = new CommunauteCommune();
		stored.setId(1L);
		org.locationtech.jts.geom.Polygon shape =
				new org.locationtech.jts.geom.GeometryFactory().createPolygon();
		stored.setGeoShapeLow(shape);
		when(ccRepo.findById(1L)).thenReturn(Optional.of(stored));

		org.locationtech.jts.geom.Geometry ret = tested.computeSquareOnMap(input);

		org.junit.jupiter.api.Assertions.assertSame(shape, ret);
	}
}
