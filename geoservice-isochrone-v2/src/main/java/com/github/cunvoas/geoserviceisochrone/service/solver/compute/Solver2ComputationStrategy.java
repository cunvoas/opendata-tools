package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

import lombok.extern.slf4j.Slf4j;

/**
 * Global computation using Choco solver. Updates the provided map in place;
 * returns an empty list since propositions are applied directly on the map.
 */
@Slf4j
public class Solver2ComputationStrategy extends AbstractComputationtrategy {

	/**
	 * Calcule les propositions d'augmentation de parc pour tous les carrés d'une
	 * ville en une seule résolution utilisant Choco Solver.
	 * 
	 * <p>
	 * Cette méthode utilise un solveur de contraintes pour optimiser globalement
	 * l'allocation de parcs en tenant compte des interactions entre carrés voisins.
	 * </p>
	 * 
	 * <p>
	 * <strong>Modélisation du problème :</strong>
	 * </p>
	 * <ul>
	 * <li>Variables : une variable IntVar par carré représentant la surface de parc
	 * manquante de 0 à 40000 m²)</li>
	 * <li>Domaine : [0, 40000] m² (surface maximale d'un carré de 200m × 200m)</li>
	 * <li>Contraintes : chaque parc fera sera ≥ 650 m² et <= 40000m²</li>
	 * <li>la cible doit êtres légérement supérieure à 12m²/habitant</li>
	 * <li>l'ajout d'un parc réduit la surface manquante pour ledit parc mais aussi pour tous ses voisins.</li>
	 * <li>Objectif : minimiser la somme des écarts à la densité recommandée pour tous les carrés</li>
	 * </ul>
	 * 
	 * <p>
	 * <strong>Algorithme :</strong>
	 * </p>
	 * 
	 * <p>
	 * A définir.
	 * </p>

	 * 
	 * <p>
	 * <strong>Avantages du sorlver :</strong>
	 * </p>
	 * <ul>
	 * <li>Solution globalement optimale (pas de minimum local)</li>
	 * <li>Prise en compte simultanée de toutes les interactions entre carrés</li>
	 * <li>Équilibrage automatique de la distribution des parcs</li>
	 * </ul>
	 * 
	 * @param carreMap                 la carte des propositions de parc indexée par
	 *                                 idInspire. Modifiée par la méthode pour
	 *                                 appliquer les surfaces calculées.
	 * @param recoSquareMeterPerCapita densité recommandée en m²/habitant
	 *                                 (typiquement 12 m²/hab).
	 * @param urbanDistance            distance d'accessibilité en mètres pour
	 *                                 identifier les voisins.
	 * 
	 * @see #findNeighbors(String, Map, Integer)
	 * 
	 * @author github.com/cunvoas
	 */

	@Override
	public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap, Double minSquareMeterPerCapita,
			Double recoSquareMeterPerCapita, Integer urbanDistance) {

		List<ParkProposal> proposals = new ArrayList<>();
		if (carreMap.isEmpty()) {
			log.warn("Carte des carrés vide, aucune proposition à calculer");
			return proposals;
		}

		log.info("Démarrage du calcul global avec Choco Solver pour {} carrés", carreMap.size());

		// Pré-calcul des voisinages (ne change pas entre les itérations)
		List<String> carreIds = new ArrayList<>(carreMap.keySet());
		Map<String, List<ParkProposalWork>> voisinages = new HashMap<>();
		for (String idInspire : carreIds) {
			List<ParkProposalWork> voisins = findNeighbors(idInspire, carreMap, urbanDistance);
			voisinages.put(idInspire, voisins);
		}

		Model model = new Model("Park Area Optimization");
		Map<String, IntVar> additionVars = new HashMap<>();

		// bornes attendues : 0 ou au moins 650 m², jamais plus que la surface d'un carré
		int minParkSurface = (int) Math.max(650, MIN_PARK_SURFACE);
		for (String id : carreIds) {
			ParkProposalWork carre = carreMap.get(id);
			int population = carre.getAccessingPopulation() != null ? carre.getAccessingPopulation().intValue() : 0;
			boolean hasEnough = population == 0
					|| (carre.getSurfacePerCapita() != null
							&& carre.getSurfacePerCapita().doubleValue() >= recoSquareMeterPerCapita);

			if (hasEnough) {
				additionVars.put(id, model.intVar("add_" + id, 0));
				continue;
			}

			IntVar addVar = model.intVar("add_" + id, 0, (int) CARRE_SURFACE);
			model.or(
					model.arithm(addVar, "=", 0),
					model.and(
							model.arithm(addVar, ">=", minParkSurface),
							model.arithm(addVar, "<=", (int) CARRE_SURFACE)))
					.post();
			additionVars.put(id, addVar);
		}

		// Calcul des écarts et définition de l'objectif
		List<IntVar> deviationVars = new ArrayList<>();
		for (String idInspire : carreIds) {
			ParkProposalWork carre = carreMap.get(idInspire);
			int population = carre.getAccessingPopulation() != null ? carre.getAccessingPopulation().intValue() : 0;
			int surfaceExistante = carre.getAccessingSurface() != null ? carre.getAccessingSurface().intValue() : 0;

			if (population == 0) {
				deviationVars.add(model.intVar("abs_" + idInspire, 0));
				continue;
			}

			List<ParkProposalWork> voisins = voisinages.get(idInspire);
			List<IntVar> contributions = new ArrayList<>();
			contributions.add(additionVars.get(idInspire));
			for (ParkProposalWork voisin : voisins) {
				IntVar voisinVar = additionVars.get(voisin.getIdInspire());
				if (voisinVar != null) {
					contributions.add(voisinVar);
				}
			}

			int maxAdd = (int) (CARRE_SURFACE * Math.max(1, contributions.size()));
			IntVar sumAdd = model.intVar("sumAdd_" + idInspire, 0, maxAdd);
			model.sum(contributions.toArray(new IntVar[0]), "=", sumAdd).post();

			int totalMax = surfaceExistante + maxAdd;
			IntVar totalSurface = model.intVar("total_" + idInspire, surfaceExistante, totalMax);
			model.arithm(totalSurface, "=", sumAdd, "+", surfaceExistante).post();

			int targetSurface = (int) Math.ceil(population * recoSquareMeterPerCapita);
			if (minSquareMeterPerCapita != null && minSquareMeterPerCapita > 0) {
				int minTargetSurface = (int) Math.ceil(population * minSquareMeterPerCapita);
				model.arithm(totalSurface, ">=", minTargetSurface).post();
			}

			IntVar signedGap = model.intVar("gap_" + idInspire, -totalMax, totalMax + targetSurface);
			model.arithm(signedGap, "=", model.intVar(targetSurface), "-", totalSurface).post();

			IntVar absGap = model.intVar("abs_" + idInspire, 0, totalMax + targetSurface);
			model.absolute(absGap, signedGap).post();
			deviationVars.add(absGap);
		}

		IntVar objectif = model.intVar("objective", 0, IntVar.MAX_INT_BOUND);
		model.sum(deviationVars.toArray(new IntVar[0]), "=", objectif).post();
		model.setObjective(Model.MINIMIZE, objectif);

		log.info("Résolution du modèle global...");
		boolean solved = model.getSolver().solve();
		if (!solved) {
			log.warn("Aucune solution trouvée par le solver.");
			return proposals;
		}

		Map<String, Integer> additions = new HashMap<>();
		for (String id : carreIds) {
			additions.put(id, additionVars.get(id).getValue());
		}

		for (String idInspire : carreIds) {
			ParkProposalWork carre = carreMap.get(idInspire);
			int added = additions.get(idInspire);
			if (added > 0) {
				ParkProposal proposal = new ParkProposal();
				proposal.setAnnee(carre.getAnnee());
				proposal.setIdInspire(carre.getIdInspire());
				proposal.setIsDense(carre.getIsDense());
				proposal.setCentre(carre.getCentre());
				proposal.setParkSurface(BigDecimal.valueOf(added));
				proposals.add(proposal);
			}

			double totalAdded = added;
			for (ParkProposalWork voisin : voisinages.get(idInspire)) {
				totalAdded += additions.getOrDefault(voisin.getIdInspire(), 0);
			}

			double totalSurface = carre.getAccessingSurface() != null ? carre.getAccessingSurface().doubleValue() : 0d;
			totalSurface += totalAdded;

			double population = carre.getAccessingPopulation() != null ? carre.getAccessingPopulation().doubleValue() : 0d;
			if (population > 0) {
				double surfacePerCapita = totalSurface / population;
				carre.setNewSurfacePerCapita(BigDecimal.valueOf(surfacePerCapita));
				carre.setSurfacePerCapita(BigDecimal.valueOf(surfacePerCapita));
			} else {
				carre.setNewSurfacePerCapita(null);
			}

			BigDecimal missing = carre.getMissingSurface() != null ? carre.getMissingSurface() : BigDecimal.ZERO;
			BigDecimal updatedMissing = missing.subtract(BigDecimal.valueOf(totalAdded)).max(BigDecimal.ZERO);
			carre.setNewMissingSurface(updatedMissing);
			carre.setNewSurface(BigDecimal.valueOf(added));
		}

		log.info("Résolution terminée : {} propositions retenues.", proposals.size());

		return proposals;
	}
}
