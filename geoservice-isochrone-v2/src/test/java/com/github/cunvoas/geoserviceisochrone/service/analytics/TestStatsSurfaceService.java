package com.github.cunvoas.geoserviceisochrone.service.analytics;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.StatsDescripteur;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.StatsSurfaceJson;
import com.github.cunvoas.geoserviceisochrone.model.analytics.StatsSeuilOmsEnum;
import com.github.cunvoas.geoserviceisochrone.model.analytics.StatsSurface;
import com.github.cunvoas.geoserviceisochrone.repo.analytics.StatsSurfaceRepository;

import tools.jackson.databind.ObjectMapper;

class TestStatsSurfaceService {
	ObjectMapper objectMapper=new ObjectMapper();
	StatsSurfaceService tested = new StatsSurfaceService(objectMapper);
	
	
	static List<StatsSurface> stats;
	static String[] sSeuils = {StatsSeuilOmsEnum.INSUFFISANT.name(), StatsSeuilOmsEnum.MINIMUM.name(), StatsSeuilOmsEnum.PRECONISE.name()};
	static int[] iSeuils = {0, 3, 7, 10, 12};
	
//	@Test
	@Order(5)
	void testPopulateSurface5() {
		setupSurface5();
		StatsSurfaceJson json = new StatsSurfaceJson();
		tested.populateSurface(json, stats, iSeuils);
		
		assertEquals(json.getStats().size(), 5);
		
		assertEquals(json.getStats().get(0).getHabitants(), 150, "Population INSUFFISANT incorrecte");
		assertEquals(json.getStats().get(1).getHabitants(),  2, "Population MINIMUM incorrecte");
		assertEquals(json.getStats().get(2).getHabitants(),  3, "Population PRECONISE incorrecte");
		assertEquals(json.getStats().get(3).getHabitants(),  4, "Population PRECONISE incorrecte");
		assertEquals(json.getStats().get(4).getHabitants(),  5, "Population PRECONISE incorrecte");
		
	}

	static void setupSurface5() {
		stats=new ArrayList<>();

		StatsSurface stat0= new StatsSurface();
		stat0.setAnnee(2010);
		stat0.setSurfaceMin(0);
		stat0.setSurfaceMax(3);
		stat0.setPopulationInclue(BigDecimal.valueOf(1));
		stat0.setPopulationExclue(BigDecimal.valueOf(10));
		stat0.setSeuil(StatsSeuilOmsEnum.INSUFFISANT);
		stats.add(stat0);

		StatsSurface stat1= new StatsSurface();
		stat1.setAnnee(2010);
		stat1.setSurfaceMin(3);
		stat1.setSurfaceMax(7);
		stat1.setPopulationInclue(BigDecimal.valueOf(2));
		stat1.setPopulationExclue(BigDecimal.valueOf(20));
		stat1.setSeuil(StatsSeuilOmsEnum.INSUFFISANT);
		stats.add(stat1);

		StatsSurface stat2= new StatsSurface();
		stat2.setAnnee(2010);
		stat2.setSurfaceMin(7);
		stat2.setSurfaceMax(10);
		stat2.setPopulationInclue(BigDecimal.valueOf(3));
		stat2.setPopulationExclue(BigDecimal.valueOf(30));
		stat2.setSeuil(StatsSeuilOmsEnum.INSUFFISANT);
		stats.add(stat2);
		


		StatsSurface stat3= new StatsSurface();
		stat3.setAnnee(2010);
		stat3.setSurfaceMin(10);
		stat3.setSurfaceMax(12);
		stat3.setPopulationInclue(BigDecimal.valueOf(4));
		stat3.setPopulationExclue(BigDecimal.valueOf(40));
		stat3.setSeuil(StatsSeuilOmsEnum.MINIMUM);
		stats.add(stat3);


		StatsSurface stat4= new StatsSurface();
		stat4.setAnnee(2010);
		stat4.setSurfaceMin(12);
		stat4.setSurfaceMax(10000);
		stat4.setPopulationInclue(BigDecimal.valueOf(5));
		stat4.setPopulationExclue(BigDecimal.valueOf(50));
		stat4.setSeuil(StatsSeuilOmsEnum.PRECONISE);
		stats.add(stat4);
		
	}
	
	
	
	@Test
	@Order(5)
	void testPopulateSurface3() {
		// base EMMERIN 59193
		setupSurface3();
		StatsSurfaceJson json = new StatsSurfaceJson();
		tested.populateSurface(json, stats, iSeuils);
		
		assertEquals(json.getStats().size(), 5);
		
		assertEquals(json.getStats().get(0).getHabitants(),  123+66, "Population 0-3 INSUFFISANT incorrecte");
		assertEquals(json.getStats().get(1).getHabitants(),  0, "Population 3-7 INSUFFISANT incorrecte");
		assertEquals(json.getStats().get(2).getHabitants(),  0, "Population 7-10 INSUFFISANT incorrecte");
		assertEquals(json.getStats().get(3).getHabitants(),  20, "Population 10-12 MINIMUM incorrecte");
		assertEquals(json.getStats().get(4).getHabitants(),  3745, "Population 12+ PRECONISE incorrecte");
		
	}
/*
 * 
 *        min   max  seuil inc    exc  nbInspire
	2019	0	    3	0	0	  123	4
	2019	10	   12	1	20	   0	1
	2019	12	10000	2	3745   66	23
*/


	static void setupSurface3() {
		stats=new ArrayList<>();

		StatsSurface stat0= new StatsSurface();
		stat0.setAnnee(2010);
		stat0.setSurfaceMin(0);
		stat0.setSurfaceMax(3);
		stat0.setPopulationInclue(BigDecimal.valueOf(0));
		stat0.setPopulationExclue(BigDecimal.valueOf(123));
		stat0.setSeuil(StatsSeuilOmsEnum.INSUFFISANT);
		stats.add(stat0);

		StatsSurface stat1= new StatsSurface();
		stat1.setAnnee(2010);
		stat1.setSurfaceMin(10);
		stat1.setSurfaceMax(12);
		stat1.setPopulationInclue(BigDecimal.valueOf(20));
		stat1.setPopulationExclue(BigDecimal.valueOf(0));
		stat1.setSeuil(StatsSeuilOmsEnum.MINIMUM);
		stats.add(stat1);

		StatsSurface stat2= new StatsSurface();
		stat2.setAnnee(2010);
		stat2.setSurfaceMin(12);
		stat2.setSurfaceMax(10000);
		stat2.setPopulationInclue(BigDecimal.valueOf(3745));
		stat2.setPopulationExclue(BigDecimal.valueOf(66));
		stat2.setSeuil(StatsSeuilOmsEnum.PRECONISE);
		stats.add(stat2);
		
		
	}
	
	


	@Test
	@Order(10)
	void testPopulateAll() {
		stats.clear();
		setupAll();
		
		
		StatsSurfaceJson json = new StatsSurfaceJson();
		tested.populateAll(json, stats, sSeuils);
		
		assertEquals(json.getStats().size(), 3);
		
		assertEquals(json.getStats().get(0).getHabitants(), 34, "Population INSUFFISANT incorrecte");
		assertEquals(json.getStats().get(1).getHabitants(),  2, "Population MINIMUM incorrecte");
		assertEquals(json.getStats().get(2).getHabitants(),  3, "Population PRECONISE incorrecte");
		
	}

	static void setupAll() {
		stats=new ArrayList<>();

		StatsSurface stat0= new StatsSurface();
		stat0.setAnnee(2010);
		stat0.setPopulationInclue(BigDecimal.valueOf(1));
		stat0.setPopulationExclue(BigDecimal.valueOf(10));
		stat0.setSeuil(StatsSeuilOmsEnum.INSUFFISANT);
		stats.add(stat0);

		StatsSurface stat1= new StatsSurface();
		stat1.setAnnee(2010);
		stat1.setPopulationInclue(BigDecimal.valueOf(2));
		stat1.setPopulationExclue(BigDecimal.valueOf(11));
		stat1.setSeuil(StatsSeuilOmsEnum.MINIMUM);
		stats.add(stat1);

		StatsSurface stat2= new StatsSurface();
		stat2.setAnnee(2010);
		stat2.setPopulationInclue(BigDecimal.valueOf(3));
		stat2.setPopulationExclue(BigDecimal.valueOf(12));
		stat2.setSeuil(StatsSeuilOmsEnum.PRECONISE);
		stats.add(stat2);
		
	}

	private static StatsSurface row(int min, int max, int inc, int exc, StatsSeuilOmsEnum seuil) {
		StatsSurface r = new StatsSurface();
		r.setAnnee(2019);
		r.setSurfaceMin(min);
		r.setSurfaceMax(max);
		r.setPopulationInclue(BigDecimal.valueOf(inc));
		r.setPopulationExclue(BigDecimal.valueOf(exc));
		r.setSeuil(seuil);
		return r;
	}

	/** Lille 2019 : habitants publiés par tranche, sans habitant hors isochrone supplémentaire. */
	private static List<StatsSurface> lille2019() {
		return new ArrayList<>(List.of(
				row(0, 3, 93150, 0, StatsSeuilOmsEnum.INSUFFISANT),
				row(3, 7, 53543, 0, StatsSeuilOmsEnum.INSUFFISANT),
				row(7, 10, 13751, 0, StatsSeuilOmsEnum.INSUFFISANT),
				row(10, 12, 5319, 0, StatsSeuilOmsEnum.MINIMUM),
				row(12, 10000, 45100, 0, StatsSeuilOmsEnum.PRECONISE)));
	}

	@Test
	void testPopulateSurface_orderOfRowsDoesNotMatter() {
		List<StatsSurface> sorted = lille2019();
		List<StatsSurface> reversed = new ArrayList<>(sorted);
		java.util.Collections.reverse(reversed);

		StatsSurfaceJson expected = new StatsSurfaceJson();
		tested.populateSurface(expected, sorted, iSeuils);
		StatsSurfaceJson actual = new StatsSurfaceJson();
		tested.populateSurface(actual, reversed, iSeuils);

		for (int i = 0; i < 5; i++) {
			assertEquals(expected.getStats().get(i).getHabitants(), actual.getStats().get(i).getHabitants(), "tranche " + i);
		}
		assertEquals(93150, actual.getStats().get(0).getHabitants());
		assertEquals(45100, actual.getStats().get(4).getHabitants());
	}

	@Test
	void testPopulateSurface_labelsExpressHalfOpenIntervals() {
		StatsSurfaceJson json = new StatsSurfaceJson();
		tested.populateSurface(json, lille2019(), iSeuils);

		assertEquals("0 à <3", json.getStats().get(0).getSurface());
		assertEquals("10 à <12", json.getStats().get(3).getSurface());
		assertEquals("12 et +", json.getStats().get(4).getSurface());
		assertEquals("0 <=  surface/habitant < 3 m²/hab.", json.getSeuils().get(0).getSurface());
		assertEquals(" >= 12 m²/hab.", json.getSeuils().get(4).getSurface());
	}

	@Test
	void testPopulateSurface_ratioIsRoundedAndSumsToHundred() {
		StatsSurfaceJson json = new StatsSurfaceJson();
		tested.populateSurface(json, lille2019(), iSeuils);

		assertEquals("44.2", json.getSeuils().get(0).getRatio());
		assertEquals("25.4", json.getSeuils().get(1).getRatio());
		assertEquals("6.5", json.getSeuils().get(2).getRatio());
		assertEquals("2.5", json.getSeuils().get(3).getRatio());
		assertEquals("21.4", json.getSeuils().get(4).getRatio());
	}

	@Test
	void testPopulateSurface_populationWithoutParkJoinsFirstBucket() {
		List<StatsSurface> rows = List.of(
				row(0, 3, 100, 10, StatsSeuilOmsEnum.INSUFFISANT),
				row(12, 10000, 300, 40, StatsSeuilOmsEnum.PRECONISE));

		StatsSurfaceJson json = new StatsSurfaceJson();
		tested.populateSurface(json, rows, iSeuils);

		assertEquals(150, json.getStats().get(0).getHabitants());
		assertEquals(300, json.getStats().get(4).getHabitants());
		assertEquals("33.3", json.getSeuils().get(0).getRatio());
	}

	@Test
	void testPopulateSurface_emptyRowsDoNotFail() {
		StatsSurfaceJson json = new StatsSurfaceJson();
		tested.populateSurface(json, new ArrayList<>(), iSeuils);

		assertEquals(5, json.getStats().size());
		assertEquals("0.0", json.getSeuils().get(0).getRatio());
	}

	@Test
	void testPopulateSurface_unexpectedBoundIsIgnored() {
		List<StatsSurface> rows = lille2019();
		rows.add(row(99, 100, 5000, 5000, StatsSeuilOmsEnum.PRECONISE));

		StatsSurfaceJson json = new StatsSurfaceJson();
		tested.populateSurface(json, rows, iSeuils);

		assertEquals(93150, json.getStats().get(0).getHabitants());
		assertEquals("44.2", json.getSeuils().get(0).getRatio());
	}

	@Test
	void testPourcent() {
		assertEquals("44.4", StatsSurfaceService.pourcent(91445, 205789));
		assertEquals("0.0", StatsSurfaceService.pourcent(5, 0));
	}

	private static List<Object[]> ligne(Object population, Object ponderes, Object surface) {
		List<Object[]> lignes = new ArrayList<>();
		lignes.add(new Object[] { population, ponderes, surface });
		return lignes;
	}

	@Test
	void testDescripteur_moyennePondereeEtRatioGlobal() {
		// commune A du jeu de contrôle SQL : 95 habitants, somme(pop x m²/hab) = 1039,96, parcs OMS = 5000,4 m²
		StatsDescripteur d = StatsSurfaceService.descripteur(ligne(new BigDecimal("95.000"), new BigDecimal("1039.96"), new BigDecimal("5000.4")));

		assertEquals(95L, d.getHabitants());
		assertEquals(10.95, d.getM2ParHabitantMoyen(), 0.0001, "somme(pop carreau x m²/hab) / somme(pop carreau)");
		assertEquals(5000L, d.getSurfaceParcsOmsM2());
		assertEquals(52.64, d.getM2ParHabitantGlobal(), 0.0001, "somme(surface parcs OMS) / population totale");
	}

	@Test
	void testDescripteur_territoireEntier() {
		StatsDescripteur d = StatsSurfaceService.descripteur(ligne(new BigDecimal("235"), new BigDecimal("2699.96"), new BigDecimal("7000.6")));

		assertEquals(11.49, d.getM2ParHabitantMoyen(), 0.0001);
		assertEquals(29.79, d.getM2ParHabitantGlobal(), 0.0001);
		assertEquals(7001L, d.getSurfaceParcsOmsM2());
	}

	@Test
	void testDescripteur_leRatioGlobalDiffereDeLaMoyennePonderee() {
		// un grand parc dans un quartier peu peuplé fait monter le ratio global, pas la moyenne vécue
		StatsDescripteur d = StatsSurfaceService.descripteur(ligne(new BigDecimal("1000"), new BigDecimal("2000"), new BigDecimal("30000")));

		assertEquals(2.0, d.getM2ParHabitantMoyen(), 0.0001);
		assertEquals(30.0, d.getM2ParHabitantGlobal(), 0.0001);
	}

	@Test
	void testDescripteur_arrondiSeulementALaFin() {
		// 1/3 : la division porte sur les sommes exactes, arrondie à deux décimales
		StatsDescripteur d = StatsSurfaceService.descripteur(ligne(new BigDecimal("3"), new BigDecimal("1"), new BigDecimal("2")));

		assertEquals(0.33, d.getM2ParHabitantMoyen(), 0.0001);
		assertEquals(0.67, d.getM2ParHabitantGlobal(), 0.0001);
	}

	@Test
	void testDescripteur_accepteLesAutresTypesNumeriques() {
		StatsDescripteur d = StatsSurfaceService.descripteur(ligne(100.0d, 500L, 2500));

		assertEquals(5.0, d.getM2ParHabitantMoyen(), 0.0001);
		assertEquals(25.0, d.getM2ParHabitantGlobal(), 0.0001);
	}

	@Test
	void testDescripteur_nulSansPopulation() {
		assertNull(StatsSurfaceService.descripteur(null));
		assertNull(StatsSurfaceService.descripteur(Collections.emptyList()));
		assertNull(StatsSurfaceService.descripteur(ligne(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));
		assertNull(StatsSurfaceService.descripteur(ligne(null, new BigDecimal("10"), new BigDecimal("10"))));
	}

	@Test
	void testDescripteur_territoireSansParc() {
		StatsDescripteur d = StatsSurfaceService.descripteur(ligne(new BigDecimal("500"), null, null));

		assertEquals(0.0, d.getM2ParHabitantMoyen(), 0.0001);
		assertEquals(0.0, d.getM2ParHabitantGlobal(), 0.0001);
		assertEquals(0L, d.getSurfaceParcsOmsM2());
	}

	@Test
	void testDescripteur_publieDansLeJsonSansToucherAuxTranches() {
		StatsSurfaceJson json = new StatsSurfaceJson();
		json.setNom("LILLE");
		json.setDescripteur(StatsSurfaceService.descripteur(ligne(new BigDecimal("95"), new BigDecimal("1039.96"), new BigDecimal("5000.4"))));
		tested.populateSurface(json, lille2019(), iSeuils);

		String texte = objectMapper.writeValueAsString(json);
		assertTrue(texte.contains("\"descripteur\":{\"habitants\":95,\"m2ParHabitantMoyen\":10.95,\"surfaceParcsOmsM2\":5000,\"m2ParHabitantGlobal\":52.64}"), texte);
		assertEquals(5, json.getStats().size());
		assertTrue(texte.indexOf("\"descripteur\"") < texte.indexOf("\"stats\""), "le descripteur précède les tranches");
	}

	@Test
	void testDescripteur_absentDuJsonQuandIlNEstPasCalcule() {
		StatsSurfaceJson json = new StatsSurfaceJson();
		json.setNom("ANCIEN");

		assertFalse(objectMapper.writeValueAsString(json).contains("descripteur"));
		assertNull(json.getDescripteur());
	}

	@Test
	void testRequetes_ratioExactDepuisLesDonneesBrutes() {
		String exact = "cc.surface_access_park_oms / NULLIF(cc.pop_park_share_oms, 0)";
		for (String requete : new String[] { StatsSurfaceRepository.QRY_CITY, StatsSurfaceRepository.QRY_SUBURBS,
				StatsSurfaceRepository.QRY_COM2CO_DENSE, StatsSurfaceRepository.QRY_COM2CO_SUBURBS,
				StatsSurfaceRepository.QRY_DESC_CITY, StatsSurfaceRepository.QRY_DESC_COM2CO_DENSE,
				StatsSurfaceRepository.QRY_DESC_COM2CO_SUBURBS, StatsSurfaceRepository.QRY_DESC_COM2CO_ALL }) {
			assertTrue(requete.contains(exact), "ratio recalculé depuis la surface et la population brutes");
			assertFalse(requete.contains("ROUND(cc.surface_park_pcapita"), "plus de ratio arrondi à deux décimales");
		}
	}

	@Test
	void testRequetes_descripteurCompteChaqueParcUneSeuleFois() {
		for (String requete : new String[] { StatsSurfaceRepository.QRY_DESC_CITY, StatsSurfaceRepository.QRY_DESC_COM2CO_DENSE,
				StatsSurfaceRepository.QRY_DESC_COM2CO_SUBURBS, StatsSurfaceRepository.QRY_DESC_COM2CO_ALL }) {
			assertTrue(requete.contains("SUM(pj.surface)"), "surface brute du parc, une fois par parc");
			assertTrue(requete.contains("pac.oms IS TRUE"), "parcs OMS seulement");
			assertTrue(requete.contains("pj.date_fin"), "parcs actifs dans l'année");
			assertFalse(requete.contains("SUM(cc.surface_access_park_oms)"), "la surface par carreau compterait un parc plusieurs fois");
		}
		assertTrue(StatsSurfaceRepository.QRY_DESC_COM2CO_DENSE.contains("is_dense IS TRUE"));
		assertTrue(StatsSurfaceRepository.QRY_DESC_COM2CO_SUBURBS.contains("is_dense IS FALSE"));
		assertFalse(StatsSurfaceRepository.QRY_DESC_COM2CO_ALL.contains("is_dense IS"));
	}

	@Test
	void testBornesV2_litLesLibellesHistoriquesSansExpressionReguliereCoteFront() {
		assertArrayEquals(new Integer[] { 0, 3 }, StatsSurfaceService.bornes("0 <=  surface/habitant < 3 m²/hab."));
		assertArrayEquals(new Integer[] { 3, 7 }, StatsSurfaceService.bornes("> 3<=7"));
		assertArrayEquals(new Integer[] { 12, null }, StatsSurfaceService.bornes(">= 12 m²/hab."));
		assertArrayEquals(new Integer[] { null, 3 }, StatsSurfaceService.bornes("< 3 m²/hab."));
		assertNull(StatsSurfaceService.bornes("Non respect des minima OMS"));
	}
}
