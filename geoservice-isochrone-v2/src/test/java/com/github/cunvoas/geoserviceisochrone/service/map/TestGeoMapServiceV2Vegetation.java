package com.github.cunvoas.geoserviceisochrone.service.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ParkGardenView;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.ParkView;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviResult;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviResultObjectType;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.repo.NdviResultRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestGeoMapServiceV2Vegetation {

	@Mock
	private NdviResultRepository ndviResultRepository;

	private GeoMapServiceV2 tested;

	@BeforeEach
	void setUp() {
		tested = new GeoMapServiceV2();
		ReflectionTestUtils.setField(tested, "ndviResultRepository", ndviResultRepository);
	}

	private static NdviResult result() {
		NdviResult r = new NdviResult();
		r.setObjectType(NdviResultObjectType.PARK_AREA_COMPUTED);
		r.setParkAreaComputedId(7L);
		r.setSampleCount(120);
		r.setPeriodStart(new Date(1000L));
		r.setPeriodEnd(new Date(3000L));
		r.setNdviMean(new BigDecimal("0.50"));
		r.setNdviMin(new BigDecimal("0.20"));
		r.setNdviMax(new BigDecimal("0.80"));
		r.setNdviStddev(new BigDecimal("0.02"));
		r.setNdreMean(new BigDecimal("0.30"));
		r.setNdreMin(new BigDecimal("0.10"));
		r.setNdreMax(new BigDecimal("0.50"));
		r.setNdreStddev(new BigDecimal("0.01"));
		r.setGndviMean(new BigDecimal("0.45"));
		r.setGndviMin(new BigDecimal("0.20"));
		r.setGndviMax(new BigDecimal("0.70"));
		r.setGndviStddev(new BigDecimal("0.01"));
		r.setSaviMean(new BigDecimal("0.30"));
		r.setSaviMin(new BigDecimal("0.10"));
		r.setSaviMax(new BigDecimal("0.50"));
		r.setSaviStddev(new BigDecimal("0.02"));
		return r;
	}

	private static ParkArea parkArea() {
		ParkArea pa = new ParkArea();
		pa.setId(7L);
		return pa;
	}

	@Test
	void la_vue_de_parc_expose_les_quatre_indices_et_l_evaluation() {
		when(ndviResultRepository.findByObjectTypeAndParkAreaComputedIdOrderByPeriodEndAsc(
				NdviResultObjectType.PARK_AREA_COMPUTED, 7L)).thenReturn(List.of(result()));
		ParkGardenView pv = new ParkGardenView();

		ReflectionTestUtils.invokeMethod(tested, "applyVegetation", pv, parkArea());

		assertEquals(new BigDecimal("0.50"), pv.getNdviMean());
		assertEquals(new BigDecimal("0.30"), pv.getSaviMean());
		assertEquals(new BigDecimal("0.02"), pv.getSaviStddev());
		assertEquals(new BigDecimal("0.45"), pv.getGndviMean());
		assertEquals(new BigDecimal("0.30"), pv.getNdreMean());
		assertNotNull(pv.getNdviAcquisitionDate());
		assertEquals("Couvert faible / Pelouse ouverte ou arbustif jeune", pv.getVegetationRecouvrement());
		assertEquals(36.6, pv.getVegetationScore());
		assertEquals("Élevée", pv.getVegetationConfiance());
		assertEquals("Normal", pv.getVegetationDiagnostic());
		assertEquals("Homogène", pv.getVegetationHomogeneite());
	}

	@Test
	void la_vue_parc_utilise_la_derniere_mesure() {
		NdviResult ancien = result();
		ancien.setSaviMean(new BigDecimal("0.10"));
		NdviResult recent = result();
		recent.setSaviMean(new BigDecimal("0.60"));
		when(ndviResultRepository.findByObjectTypeAndParkAreaComputedIdOrderByPeriodEndAsc(
				NdviResultObjectType.PARK_AREA_COMPUTED, 7L)).thenReturn(List.of(ancien, recent));
		ParkView pv = new ParkView();

		ReflectionTestUtils.invokeMethod(tested, "applyVegetation", pv, 7L);

		assertEquals(new BigDecimal("0.60"), pv.getSaviMean());
		assertEquals("Couvert dense / Strate arborée ou parc mature", pv.getVegetationRecouvrement());
	}

	@Test
	void sans_mesure_la_vue_reste_vide() {
		when(ndviResultRepository.findByObjectTypeAndParkAreaComputedIdOrderByPeriodEndAsc(
				NdviResultObjectType.PARK_AREA_COMPUTED, 7L)).thenReturn(List.of());
		ParkGardenView pv = new ParkGardenView();

		ReflectionTestUtils.invokeMethod(tested, "applyVegetation", pv, parkArea());

		assertNull(pv.getSaviMean());
		assertNull(pv.getVegetationScore());
	}

	@Test
	void zone_nulle_ne_declenche_aucune_requete() {
		ParkGardenView pv = new ParkGardenView();

		ReflectionTestUtils.invokeMethod(tested, "applyVegetation", pv, (ParkArea) null);

		assertNull(pv.getVegetationScore());
	}
}
