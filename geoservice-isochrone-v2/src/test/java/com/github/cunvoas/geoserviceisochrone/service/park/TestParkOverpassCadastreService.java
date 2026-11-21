package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.github.cunvoas.geoserviceisochrone.BaseUnitTest;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParkOverpass;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkOverpassRepository;

@DisplayName("ParkOverpassCadastreService")
class ParkOverpassCadastreServiceTest extends BaseUnitTest {

	private static final String INSEE_A = "59100";
	private static final String INSEE_B = "59350";
	private static final double LON_A = 2.0;
	private static final double LON_B = 2.1;
	private static final double LON_SPAN_START = 1.99;
	private static final double LON_SPAN_END = 2.15;
	private static final double LAT = 48.0;
	private static final double LAT_TOP = 48.1;

	private static final GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);

	@Mock
	private CityRepository cityRepository;
	@Mock
	private CadastreRepository cadastreRepository;
	@Mock
	private ParkOverpassRepository parkOverpassRepository;
	@Mock
	private ParkJardinRepository parkJardinRepository;
	@Mock
	private ParkAreaComputedRepository parkAreaComputedRepository;
	@Mock
	private InseeCarre200mOnlyShapeRepository surfaceRepo;

	@InjectMocks
	private ParkOverpassCadastreService service;

	private City cityA;
	private City cityB;
	private Cadastre cadastreA;
	private Cadastre cadastreB;
	private ParkOverpass parkSpan;
	private ParkOverpass parkInsideA;
	private ParkOverpass cornerPark;
	private String textA;
	private String textB;

	@BeforeEach
	void setUp() {
		cityA = new City();
		cityA.setId(1L);
		cityA.setInseeCode(INSEE_A);
		cityA.setName("VilleA");

		cityB = new City();
		cityB.setId(2L);
		cityB.setInseeCode(INSEE_B);
		cityB.setName("VilleB");

		cadastreA = new Cadastre();
		cadastreA.setIdInsee(INSEE_A);
		cadastreA.setGeoShape(multiPolygon(LON_A, LON_B, LAT, LAT_TOP));

		cadastreB = new Cadastre();
		cadastreB.setIdInsee(INSEE_B);
		cadastreB.setGeoShape(multiPolygon(LON_B, LON_B + 0.1, LAT, LAT_TOP));

		textA = "SRID=4326;" + cadastreA.getGeoShape().toText();
		textB = "SRID=4326;" + cadastreB.getGeoShape().toText();

		parkSpan = new ParkOverpass();
		parkSpan.setId(100L);
		parkSpan.setShape(polygon(LON_SPAN_START, LON_SPAN_END, LAT, LAT_TOP));

		parkInsideA = new ParkOverpass();
		parkInsideA.setId(101L);
		parkInsideA.setShape(polygon(LON_A + 0.01, LON_A + 0.05, LAT, LAT_TOP));

		cornerPark = new ParkOverpass();
		cornerPark.setId(102L);
		cornerPark.setShape(null);
		cornerPark.setCornerSouthWest(factory.createPoint(new Coordinate(LON_A + 0.05, LAT + 0.05)));

		when(cityRepository.findAll()).thenReturn(List.of(cityA, cityB));
		when(cadastreRepository.findById(INSEE_A)).thenReturn(Optional.of(cadastreA));
		when(cadastreRepository.findById(INSEE_B)).thenReturn(Optional.of(cadastreB));
		when(surfaceRepo.getSurface(any(Geometry.class))).thenReturn(10_000L);
	}

	@Test
	@DisplayName("process doit construire un code INSEE composite trié pour un parc multi-communes")
	void process_builds_composite_insee_for_spanning_park() {
		when(parkOverpassRepository.findByAreaGeo(eq(textA), anyDouble())).thenReturn(List.of(parkSpan, parkInsideA));
		when(parkOverpassRepository.findByAreaGeo(eq(textB), anyDouble())).thenReturn(List.of(parkSpan));
		when(parkOverpassRepository.findByCornerSouthWestInArea(anyString())).thenReturn(List.of());
		when(parkOverpassRepository.findById(parkSpan.getId())).thenReturn(Optional.of(parkSpan));
		when(parkOverpassRepository.findById(parkInsideA.getId())).thenReturn(Optional.of(parkInsideA));
		when(parkAreaComputedRepository.isComputedForCity(cityA.getId())).thenReturn(true);
		when(parkAreaComputedRepository.isComputedForCity(cityB.getId())).thenReturn(true);

		service.process();

		assertEquals(INSEE_A + "," + INSEE_B, parkSpan.getInsee(), "parc chevauchant 2 communes");
		assertEquals(INSEE_A, parkInsideA.getInsee(), "parc dans 1 seule commune");
	}

	@Test
	@DisplayName("process doit fusionner le composite avec les codes existants sans doublon")
	void process_merges_with_existing_composite() {
		parkSpan.setInsee(INSEE_B);
		parkInsideA.setInsee(INSEE_A);
		when(parkOverpassRepository.findByAreaGeo(eq(textA), anyDouble())).thenReturn(List.of(parkSpan, parkInsideA));
		when(parkOverpassRepository.findByAreaGeo(eq(textB), anyDouble())).thenReturn(List.of(parkSpan));
		when(parkOverpassRepository.findByCornerSouthWestInArea(anyString())).thenReturn(List.of());
		when(parkOverpassRepository.findById(parkSpan.getId())).thenReturn(Optional.of(parkSpan));
		when(parkOverpassRepository.findById(parkInsideA.getId())).thenReturn(Optional.of(parkInsideA));
		when(parkAreaComputedRepository.isComputedForCity(any(Long.class))).thenReturn(true);

		service.process();

		assertEquals(INSEE_A + "," + INSEE_B, parkSpan.getInsee(), "union triée, pas de doublon");
		assertEquals(INSEE_A, parkInsideA.getInsee(), "inchangé si déjà correct");
	}

	@Test
	@DisplayName("process doit rattacher un parc sans forme via son coin sud-ouest")
	void process_matches_corner_park_without_shape() {
		when(parkOverpassRepository.findByAreaGeo(anyString(), anyDouble())).thenReturn(List.of());
		when(parkOverpassRepository.findByCornerSouthWestInArea(anyString())).thenReturn(List.of(cornerPark));
		when(parkOverpassRepository.findById(cornerPark.getId())).thenReturn(Optional.of(cornerPark));
		when(parkAreaComputedRepository.isComputedForCity(any(Long.class))).thenReturn(true);

		service.process();

		assertEquals(INSEE_A, cornerPark.getInsee(), "coin SW dans le cadastre de A");
	}

	@Test
	@DisplayName("process ne doit pas rattacher un parc sans forme hors cadastre")
	void process_ignores_corner_park_outside_cadastre() {
		cornerPark.setCornerSouthWest(factory.createPoint(new Coordinate(LON_B + 0.5, LAT + 0.5)));
		when(parkOverpassRepository.findByAreaGeo(anyString(), anyDouble())).thenReturn(List.of());
		when(parkOverpassRepository.findByCornerSouthWestInArea(anyString())).thenReturn(List.of(cornerPark));
		when(parkOverpassRepository.findById(cornerPark.getId())).thenReturn(Optional.of(cornerPark));
		when(parkAreaComputedRepository.isComputedForCity(any(Long.class))).thenReturn(true);

		service.process();

		assertNull(cornerPark.getInsee(), "coin SW hors de tout cadastre -> pas de rattachement");
	}

	@Test
	@DisplayName("process ne doit rien faire pour une ville sans cadastre")
	void process_skips_city_without_cadastre() {
		when(cadastreRepository.findById(INSEE_A)).thenReturn(Optional.empty());
		when(parkOverpassRepository.findByAreaGeo(anyString(), anyDouble())).thenReturn(List.of());
		when(parkOverpassRepository.findByCornerSouthWestInArea(anyString())).thenReturn(List.of());
		when(parkAreaComputedRepository.isComputedForCity(any(Long.class))).thenReturn(true);

		service.process();

		verify(parkAreaComputedRepository, times(1)).isComputedForCity(any(Long.class));
	}

	@Test
	@DisplayName("process doit enrichir un parc point-seul d'une commune non calculée")
	void process_enriches_point_only_park_when_not_computed() {
		when(parkOverpassRepository.findByAreaGeo(anyString(), anyDouble())).thenReturn(List.of());
		when(parkOverpassRepository.findByCornerSouthWestInArea(anyString())).thenReturn(List.of());
		when(parkAreaComputedRepository.isComputedForCity(cityA.getId())).thenReturn(false);
		when(parkAreaComputedRepository.isComputedForCity(cityB.getId())).thenReturn(true);

		ParcEtJardin pointPark = new ParcEtJardin();
		pointPark.setId(1_000L);
		pointPark.setCoordonnee(factory.createPoint(new Coordinate(LON_A + 0.05, LAT + 0.05)));

		when(parkJardinRepository.findByCityIdNoContour(cityA.getId())).thenReturn(List.of(pointPark));
		when(parkOverpassRepository.findByInseeContains(INSEE_A)).thenReturn(List.of(parkSpan));

		service.process();

		assertNotNull(pointPark.getContour(), "la forme Overpass doit être copiée");
		assertEquals(pointPark.getContour().getCentroid(), pointPark.getCoordonnee(), "centroïde recalculé");
		assertEquals(ParcSourceEnum.OPENDATA, pointPark.getSource(), "source existante conservée (non écrasée)");
		assertEquals(ParcStatusEnum.TO_QUALIFY, pointPark.getStatus());
		assertEquals(10_000.0, pointPark.getSurfaceContour());
		verify(parkJardinRepository, times(1)).saveAndFlush(pointPark);
	}

	@Test
	@DisplayName("process ne doit pas enrichir les parcs d'une commune déjà calculée")
	void process_skips_enrichment_when_city_computed() {
		when(parkOverpassRepository.findByAreaGeo(anyString(), anyDouble())).thenReturn(List.of());
		when(parkOverpassRepository.findByCornerSouthWestInArea(anyString())).thenReturn(List.of());
		when(parkAreaComputedRepository.isComputedForCity(any(Long.class))).thenReturn(true);

		service.process();

		verify(parkJardinRepository, never()).findByCityIdNoContour(any(Long.class));
		verify(parkOverpassRepository, never()).findByInseeContains(anyString());
		verify(parkJardinRepository, never()).saveAndFlush(any(ParcEtJardin.class));
	}

	@Test
	@DisplayName("process ne doit pas enrichir un point-seul sans parc Overpass correspondant")
	void process_leaves_point_only_park_when_no_overpass_match() {
		when(parkOverpassRepository.findByAreaGeo(anyString(), anyDouble())).thenReturn(List.of());
		when(parkOverpassRepository.findByCornerSouthWestInArea(anyString())).thenReturn(List.of());
		when(parkAreaComputedRepository.isComputedForCity(any(Long.class))).thenReturn(false);

		ParcEtJardin pointPark = new ParcEtJardin();
		pointPark.setId(1_000L);
		pointPark.setCoordonnee(factory.createPoint(new Coordinate(LON_B + 0.05, LAT + 0.05)));

		when(parkJardinRepository.findByCityIdNoContour(any(Long.class))).thenReturn(List.of(pointPark));
		when(parkOverpassRepository.findByInseeContains(anyString())).thenReturn(List.of());

		service.process();

		assertNull(pointPark.getContour(), "aucune forme copiée");
		verify(parkJardinRepository, never()).saveAndFlush(any(ParcEtJardin.class));
	}

	@Test
	@DisplayName("process ne doit rien copier si le parc Overpass source n'a pas de forme")
	void process_leaves_point_only_park_when_overpass_has_no_shape() {
		ParkOverpass shapelessPark = new ParkOverpass();
		shapelessPark.setId(200L);
		shapelessPark.setShape(null);
		shapelessPark.setCornerSouthWest(factory.createPoint(new Coordinate(LON_A - 0.01, LAT - 0.01)));
		shapelessPark.setCornerNorthEast(factory.createPoint(new Coordinate(LON_SPAN_END, LAT_TOP + 0.01)));

		ParcEtJardin pointPark = new ParcEtJardin();
		pointPark.setId(2_000L);
		pointPark.setCoordonnee(factory.createPoint(new Coordinate(LON_A + 0.05, LAT + 0.05)));

		when(parkOverpassRepository.findByAreaGeo(anyString(), anyDouble())).thenReturn(List.of());
		when(parkOverpassRepository.findByCornerSouthWestInArea(anyString())).thenReturn(List.of());
		when(parkAreaComputedRepository.isComputedForCity(any(Long.class))).thenReturn(false);
		when(parkJardinRepository.findByCityIdNoContour(any(Long.class))).thenReturn(List.of(pointPark));
		when(parkOverpassRepository.findByInseeContains(anyString())).thenReturn(List.of(shapelessPark));

		service.process();

		assertNull(pointPark.getContour(), "aucune forme source à copier");
		verify(parkJardinRepository, never()).saveAndFlush(any(ParcEtJardin.class));
	}

	@Test
	@DisplayName("process doit persister le composite via saveAndFlush")
	void process_persists_composite_changes() {
		when(parkOverpassRepository.findByAreaGeo(eq(textA), anyDouble())).thenReturn(List.of(parkSpan));
		when(parkOverpassRepository.findByAreaGeo(eq(textB), anyDouble())).thenReturn(List.of(parkSpan));
		when(parkOverpassRepository.findByCornerSouthWestInArea(anyString())).thenReturn(List.of());
		when(parkOverpassRepository.findById(parkSpan.getId())).thenReturn(Optional.of(parkSpan));
		when(parkAreaComputedRepository.isComputedForCity(any(Long.class))).thenReturn(true);

		service.process();

		ArgumentCaptor<ParkOverpass> captor = ArgumentCaptor.forClass(ParkOverpass.class);
		verify(parkOverpassRepository, times(1)).saveAndFlush(captor.capture());
		assertEquals(INSEE_A + "," + INSEE_B, captor.getValue().getInsee());
	}

	private Geometry multiPolygon(double minLon, double maxLon, double minLat, double maxLat) {
		return factory.createMultiPolygon(new org.locationtech.jts.geom.Polygon[]{polygon(minLon, maxLon, minLat, maxLat)});
	}

	private org.locationtech.jts.geom.Polygon polygon(double minLon, double maxLon, double minLat, double maxLat) {
		Coordinate[] coords = new Coordinate[]{
				new Coordinate(minLon, minLat),
				new Coordinate(maxLon, minLat),
				new Coordinate(maxLon, maxLat),
				new Coordinate(minLon, maxLat),
				new Coordinate(minLon, minLat)
		};
		return factory.createPolygon(factory.createLinearRing(coords), null);
	}
}