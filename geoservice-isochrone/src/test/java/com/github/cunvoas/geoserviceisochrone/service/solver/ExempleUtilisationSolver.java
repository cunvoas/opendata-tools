package com.github.cunvoas.geoserviceisochrone.service.solver;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorWork;

/**
 * Exemple d'utilisation de la méthode calculePropositionSolver
 * avec un cas concret de quartier urbain.
 * 
 * @author github.com/cunvoas
 */
public class ExempleUtilisationSolver {

	private static final GeometryFactory geometryFactory = new GeometryFactory();
	
	public static void main(String[] args) {
		// Simulation d'un quartier de 5 carrés avec différents niveaux de déficit
		Map<String, ProjectSimulatorWork> quartier = creerQuartierExemple();
		
		System.out.println("=== ÉTAT INITIAL DU QUARTIER ===\n");
		afficherEtatQuartier(quartier);
		
		// Note: Dans un contexte réel, ce service serait injecté par Spring
		// et utiliserait la méthode calculeProposition(insee, annee)
		System.out.println("\n=== EXEMPLE D'UTILISATION ===\n");
		System.out.println("Cette classe de test simule l'utilisation du service ServicePropositionParc");
		System.out.println("avec un quartier fictif de 5 carrés.\n");
		
		System.out.println("=== RÉSUMÉ DU QUARTIER ===\n");
		afficherPropositions(quartier);
	}
	
	/**
	 * Crée un quartier fictif de 5 carrés avec différents profils.
	 */
	private static Map<String, ProjectSimulatorWork> creerQuartierExemple() {
		Map<String, ProjectSimulatorWork> quartier = new HashMap<>();
		
		// Carré 1 : Centre-ville dense, très peu de parc
		quartier.put("CARRE_A", creerCarre(
			"CARRE_A",
			3.0500, 50.6300,      // Lille centre (exemple)
			2000,                 // 2000 habitants
			4000.0,               // 4000 m² de parc existant
			2.0                   // 2 m²/hab (déficit critique)
		));
		
		// Carré 2 : Résidentiel, déficit moyen
		quartier.put("CARRE_B", creerCarre(
			"CARRE_B",
			3.0518, 50.6318,      // ~200m du carré A
			1500,
			10500.0,
			7.0                   // 7 m²/hab (déficit moyen)
		));
		
		// Carré 3 : Zone mixte, légèrement sous la norme
		quartier.put("CARRE_C", creerCarre(
			"CARRE_C",
			3.0536, 50.6336,      // ~400m du carré A
			1200,
			12000.0,
			10.0                  // 10 m²/hab (léger déficit)
		));
		
		// Carré 4 : Quartier avec parc, norme atteinte
		quartier.put("CARRE_D", creerCarre(
			"CARRE_D",
			3.0518, 50.6282,      // ~200m du carré A (sud)
			1000,
			13000.0,
			13.0                  // 13 m²/hab (norme atteinte)
		));
		
		// Carré 5 : Zone périphérique, isolée
		quartier.put("CARRE_E", creerCarre(
			"CARRE_E",
			3.0700, 50.6400,      // ~2km du carré A (hors portée)
			800,
			5600.0,
			7.0                   // 7 m²/hab (déficit moyen, isolé)
		));
		
		return quartier;
	}
	
	/**
	 * Crée un carré avec les paramètres donnés.
	 */
	private static ProjectSimulatorWork creerCarre(
			String id, 
			double lon, 
			double lat,
			int population,
			double surfaceExistante,
			double densiteActuelle) {
		
		ProjectSimulatorWork carre = new ProjectSimulatorWork();
		carre.setAnnee(2023);
		carre.setIdInspire(id);
		carre.setIsDense(true);
		
		Point centre = geometryFactory.createPoint(new Coordinate(lon, lat));
		carre.setCentre(centre);
		
		carre.setLocalPopulation(BigDecimal.valueOf(population));
		carre.setAccessingPopulation(BigDecimal.valueOf(population));
		carre.setAccessingSurface(BigDecimal.valueOf(surfaceExistante));
		carre.setSurfacePerCapita(BigDecimal.valueOf(densiteActuelle));
		
		// Calculer le déficit
		double densiteRecommandee = 12.0;
		double deficit = Math.max((densiteRecommandee - densiteActuelle) * population, 0);
		carre.setMissingSurface(BigDecimal.valueOf(deficit));
		
		carre.setNewSurface(BigDecimal.ZERO);
		
		return carre;
	}
	
	/**
	 * Affiche l'état actuel de tous les carrés du quartier.
	 */
	private static void afficherEtatQuartier(Map<String, ProjectSimulatorWork> quartier) {
		System.out.printf("%-10s | %8s | %12s | %12s | %10s%n",
			"Carré", "Pop.", "Surf. exist.", "Densité", "Déficit");
		System.out.println("-".repeat(70));
		
		for (Map.Entry<String, ProjectSimulatorWork> entry : quartier.entrySet()) {
			ProjectSimulatorWork carre = entry.getValue();
			System.out.printf("%-10s | %8d | %10.0f m² | %8.1f m²/h | %8.0f m²%n",
				entry.getKey(),
				carre.getAccessingPopulation().intValue(),
				carre.getAccessingSurface().doubleValue(),
				carre.getSurfacePerCapita().doubleValue(),
				carre.getMissingSurface().doubleValue()
			);
		}
	}
	
	/**
	 * Affiche les propositions d'ajout de parc.
	 */
	private static void afficherPropositions(Map<String, ProjectSimulatorWork> quartier) {
		double surfaceTotaleAjoutee = 0;
		int nbParcs = 0;
		
		for (Map.Entry<String, ProjectSimulatorWork> entry : quartier.entrySet()) {
			ProjectSimulatorWork carre = entry.getValue();
			
			if (carre.getNewSurface() != null && carre.getNewSurface().doubleValue() > 0) {
				double surface = carre.getNewSurface().doubleValue();
				System.out.printf("✓ %s : Ajouter un parc de %.0f m²%n",
					entry.getKey(), surface);
				surfaceTotaleAjoutee += surface;
				nbParcs++;
			}
		}
		
		if (nbParcs == 0) {
			System.out.println("Aucun parc à ajouter (tous les carrés respectent la norme)");
		} else {
			System.out.printf("%nTotal : %d parc(s) pour %.0f m²%n", nbParcs, surfaceTotaleAjoutee);
			
			// Calculer l'impact
			double populationTotale = quartier.values().stream()
				.mapToDouble(c -> c.getAccessingPopulation().doubleValue())
				.sum();
			double densite = surfaceTotaleAjoutee / populationTotale;
			System.out.printf("Impact : +%.2f m²/habitant en moyenne%n", densite);
		}
	}
}
