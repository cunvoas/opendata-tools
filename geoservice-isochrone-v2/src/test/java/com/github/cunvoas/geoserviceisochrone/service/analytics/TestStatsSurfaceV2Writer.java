package com.github.cunvoas.geoserviceisochrone.service.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.Seuil;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.Stat;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.StatsSurfaceJson;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Contrat JSON v2 : identité officielle, bornes numériques, version, sans couleur ni libellé.
 */
class TestStatsSurfaceV2Writer {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final StatsSurfaceService tested = new StatsSurfaceService(objectMapper);

	@TempDir
	Path tempDir;

	@Test
	void testCommune_porteLEtatCivilEtLesBornesNumeriques() throws Exception {
		StatsSurfaceJson json = commune();

		tested.writeStatsV2(json, "commune", "59350", null, tempDir.toFile(), "stats_59350_2021");

		JsonNode v2 = objectMapper.readTree(tempDir.resolve("stats_59350_2021.v2.json").toFile());
		assertEquals("2.0", v2.get("version").asText());
		assertEquals("commune", v2.get("typeTerritoire").asText());
		assertEquals("59350", v2.get("codeInsee").asText());
		assertEquals(205789, v2.get("population").asInt());
		assertFalse(v2.has("siren"), "aucun Siren sur une commune");

		JsonNode premiere = v2.get("stats").get(0);
		assertEquals(0, premiere.get("borneMin").asInt());
		assertEquals(3, premiere.get("borneMax").asInt());
		assertEquals("m2/hab", premiere.get("unite").asText());
		assertFalse(premiere.has("surface"), "les libellés d'affichage quittent les données");
		assertFalse(premiere.has("barColor"));

		JsonNode derniere = v2.get("stats").get(4);
		assertEquals(12, derniere.get("borneMin").asInt());
		assertFalse(derniere.has("borneMax"), "tranche supérieure ouverte");

		JsonNode seuil = v2.get("seuils").get(0);
		assertEquals(0, seuil.get("borneMin").asInt());
		assertEquals(3, seuil.get("borneMax").asInt());
		assertTrue(seuil.get("pourcentage").isNumber());
	}

	@Test
	void testEpci_porteLeSirenEtLesNiveauxOms() throws Exception {
		StatsSurfaceJson json = epci();

		tested.writeStatsV2(json, "epci", null, "200093201", tempDir.toFile(), "stats_c2c_1_all_2021");

		JsonNode v2 = objectMapper.readTree(tempDir.resolve("stats_c2c_1_all_2021.v2.json").toFile());
		assertEquals("epci", v2.get("typeTerritoire").asText());
		assertEquals("200093201", v2.get("siren").asText());
		assertFalse(v2.has("codeInsee"), "aucun code INSEE sur un EPCI");
		assertEquals("INSUFFISANT", v2.get("stats").get(0).get("niveauOms").asText());
		assertEquals("MINIMUM", v2.get("stats").get(1).get("niveauOms").asText());
		assertEquals("PRECONISE", v2.get("stats").get(2).get("niveauOms").asText());
		assertFalse(v2.get("stats").get(0).has("borneMin"));
	}

	private StatsSurfaceJson commune() {
		StatsSurfaceJson json = new StatsSurfaceJson();
		json.setNom("LILLE");
		json.setAnnee("2021");
		int[] habitants = { 91445, 54092, 16630, 5408, 38214 };
		String[] seuils = {
				"0 <=  surface/habitant < 3 m²/hab.",
				"3 <=  surface/habitant < 7 m²/hab.",
				"7 <=  surface/habitant < 10 m²/hab.",
				"10 <=  surface/habitant < 12 m²/hab.",
				">= 12 m²/hab."
		};
		for (int i = 0; i < habitants.length; i++) {
			Stat stat = new Stat();
			stat.setHabitants(habitants[i]);
			json.getStats().add(stat);
			Seuil seuil = new Seuil();
			seuil.setSurface(seuils[i]);
			seuil.setHabitants(habitants[i]);
			json.getSeuils().add(seuil);
		}
		return json;
	}

	private StatsSurfaceJson epci() {
		StatsSurfaceJson json = new StatsSurfaceJson();
		json.setNom("Métropole Européenne de Lille - Dense et Périurbain");
		json.setAnnee("2021");
		String[] niveaux = { "Non respect des minima OMS", "Minimim conseillé par l'OMS", "Préconisé par l'OMS" };
		int[] habitants = { 995614, 42223, 209768 };
		for (int i = 0; i < habitants.length; i++) {
			Stat stat = new Stat();
			stat.setHabitants(habitants[i]);
			json.getStats().add(stat);
			Seuil seuil = new Seuil();
			seuil.setSurface(niveaux[i]);
			seuil.setHabitants(habitants[i]);
			json.getSeuils().add(seuil);
		}
		return json;
	}
}
