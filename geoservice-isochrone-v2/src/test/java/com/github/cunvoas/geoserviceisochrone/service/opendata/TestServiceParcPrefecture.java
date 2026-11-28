package com.github.cunvoas.geoserviceisochrone.service.opendata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusPrefEnum;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParcPrefectureRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.map.CityService;
import com.github.cunvoas.geoserviceisochrone.service.park.IComputeCarreService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestServiceParcPrefecture {

	@Mock private ParcPrefectureRepository parcPrefectureRepository;
	@Mock private ParkJardinRepository parkJardinRepository;
	@Mock private IComputeCarreService computeService;
	@Mock private CityService cityService;

	@InjectMocks
	private ServiceParcPrefecture tested;

	private static Polygon polygon() {
		GeometryFactory f = new GeometryFactory();
		return f.createPolygon(new Coordinate[] {
				new Coordinate(0, 0), new Coordinate(0, 1), new Coordinate(1, 1),
				new Coordinate(1, 0), new Coordinate(0, 0) });
	}

	private static ParcPrefecture pp() {
		ParcPrefecture pp = new ParcPrefecture();
		pp.setId(1L);
		pp.setArea(polygon());
		return pp;
	}

	private static ParcEtJardin park(String name) {
		ParcEtJardin p = new ParcEtJardin();
		p.setId(10L);
		p.setName(name);
		p.setCoordonnee(new GeometryFactory().createPoint(new Coordinate(0.5, 0.5)));
		return p;
	}

	@Test
	void getById_returns_null_when_unknown() {
		when(parcPrefectureRepository.findById(4L)).thenReturn(Optional.empty());

		assertNull(tested.getById(4L));
	}

	@Test
	void computeAndUpdate_computes_surface_and_marks_no_match() {
		ParcPrefecture p = pp();
		when(computeService.getSurface(p.getArea())).thenReturn(1000L);
		when(parkJardinRepository.findByArea(anyString())).thenReturn(List.of());
		when(parcPrefectureRepository.save(p)).thenReturn(p);

		ParcPrefecture ret = tested.computeAndUpdate(p);

		assertEquals(1000L, ret.getSurface());
		assertEquals(ParcStatusPrefEnum.NO_MATCH, ret.getStatus());
		verify(parcPrefectureRepository).save(p);
	}

	@Test
	void computeAndUpdate_links_unique_matching_park() {
		ParcPrefecture p = pp();
		p.setSurface(500L);
		ParcEtJardin park = park("Parc du Rhône");
		when(parkJardinRepository.findByArea(anyString())).thenReturn(List.of(park));
		when(parcPrefectureRepository.save(p)).thenReturn(p);

		ParcPrefecture ret = tested.computeAndUpdate(p);

		assertSame(park, ret.getParcEtJardin());
		assertEquals("Parc du Rhône", ret.getName());
		assertEquals(ParcStatusPrefEnum.VALID, ret.getStatus());
		verify(computeService, never()).getSurface(any());
	}

	@Test
	void computeAndUpdate_keeps_to_qualify_when_several_parks_ambiguous() {
		ParcPrefecture p = pp();
		p.setSurface(500L);
		ParcEtJardin p1 = park("Parc 1");
		ParcEtJardin p2 = park("Parc 2");
		when(parkJardinRepository.findByArea(anyString())).thenReturn(List.of(p1, p2));
		when(parcPrefectureRepository.findByArea(anyString())).thenReturn(List.of(pp(), pp()));
		when(parcPrefectureRepository.save(p)).thenReturn(p);

		ParcPrefecture ret = tested.computeAndUpdate(p);

		assertEquals(ParcStatusPrefEnum.TO_QUALIFY, ret.getStatus());
		assertSame(p1, ret.getParcEtJardin());
	}

	@Test
	void computeAndUpdate_does_not_save_when_already_linked() {
		ParcPrefecture p = pp();
		p.setSurface(500L);
		p.setParcEtJardin(park("Déjà lié"));

		ParcPrefecture ret = tested.computeAndUpdate(p);

		assertSame(p, ret);
		verify(parcPrefectureRepository, never()).save(any());
	}

	@Test
	void update_processes_all_prefectures() {
		ParcPrefecture p1 = pp();
		ParcPrefecture p2 = pp();
		p1.setSurface(1L);
		p2.setSurface(2L);
		p1.setParcEtJardin(park("A"));
		p2.setParcEtJardin(park("B"));
		when(parcPrefectureRepository.findAll()).thenReturn(List.of(p1, p2));

		tested.update();

		verify(parcPrefectureRepository, never()).save(any());
	}

	@Test
	void prepareFromSite_reuses_city_matching_name() {
		Polygon area = polygon();
		com.github.cunvoas.geoserviceisochrone.model.opendata.City city = new com.github.cunvoas.geoserviceisochrone.model.opendata.City();
		city.setName("LYON");
		when(computeService.getSurface(area)).thenReturn(2000L);
		when(cityService.findAround(any(), org.mockito.ArgumentMatchers.eq(0.07d))).thenReturn(List.of(city));
		when(parcPrefectureRepository.save(any(ParcPrefecture.class))).thenAnswer(inv -> inv.getArgument(0));
		when(parkJardinRepository.findByArea(anyString())).thenReturn(List.of());

		ParcPrefecture ret = tested.prepareFromSite("Lyon: Parc de la Tête d'Or", area);

		assertEquals("Lyon: Parc de la Tête d'Or", ret.getNamePrefecture());
		assertEquals(2000L, ret.getSurface());
		assertEquals(city, ret.getCommune());
		verify(parcPrefectureRepository, org.mockito.Mockito.atLeastOnce()).save(any());
	}

	@Test
	void prepareFromSite_falls_back_to_first_nearby_city() {
		Polygon area = polygon();
		com.github.cunvoas.geoserviceisochrone.model.opendata.City city = new com.github.cunvoas.geoserviceisochrone.model.opendata.City();
		city.setName("VILLEURBANNE");
		when(computeService.getSurface(area)).thenReturn(10L);
		when(cityService.findAround(any(), org.mockito.ArgumentMatchers.eq(0.07d))).thenReturn(List.of(city));
		when(parcPrefectureRepository.save(any(ParcPrefecture.class))).thenAnswer(inv -> inv.getArgument(0));
		when(parkJardinRepository.findByArea(anyString())).thenReturn(List.of());

		ParcPrefecture ret = tested.prepareFromSite("Autre site", area);

		assertSame(city, ret.getCommune());
	}
}
