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
 * Global computation using Choco solver.
 * Updates the provided map in place; returns an empty list
 * since propositions are applied directly on the map.
 */
@Slf4j
public class Solver1ComputationStrategy extends AbstractComputationtrategy {

   
	/**
	 * Calcule les propositions d'augmentation de parc pour tous les carrés d'une ville en une seule résolution
	 * utilisant Choco Solver.
	 * 
	 * <p>Cette méthode utilise un solveur de contraintes pour optimiser globalement l'allocation de parcs
	 * en tenant compte des interactions entre carrés voisins. Elle résout le problème en une seule passe
	 * contrairement à {@link #calculeEtapeProposition} qui procède itérativement.</p>
	 * 
	 * <p><strong>Modélisation du problème :</strong></p>
	 * <ul>
	 *   <li>Variables : une variable IntVar par carré représentant la surface de parc à ajouter (0 ou ≥ 1000 m²)</li>
	 *   <li>Domaine : [0, 40000] m² (surface maximale d'un carré de 200m × 200m)</li>
	 *   <li>Contraintes : chaque parc fait 0 m² OU ≥ 1000 m²</li>
	 *   <li>Objectif : minimiser la somme des écarts à la densité recommandée pour tous les carrés</li>
	 * </ul>
	 * 
	 * <p><strong>Algorithme :</strong></p>
	 * <pre>
	 * 1. Créer un modèle Choco Solver
	 * 2. Pour chaque carré :
	 *    - Créer une variable IntVar pour la surface à ajouter
	 *    - Contrainte : surface = 0 OU surface ≥ 1000
	 * 3. Pour chaque carré :
	 *    - Calculer la surface totale accessible (existante + somme des ajouts des voisins)
	 *    - Calculer la densité résultante
	 *    - Calculer l'écart à la densité recommandée
	 * 4. Définir l'objectif : minimiser la somme des écarts pondérés par la population
	 * 5. Résoudre et appliquer la solution trouvée
	 * </pre>
	 * 
	 * <p><strong>Avantages vs approche itérative :</strong></p>
	 * <ul>
	 *   <li>Solution globalement optimale (pas de minimum local)</li>
	 *   <li>Prise en compte simultanée de toutes les interactions entre carrés</li>
	 *   <li>Équilibrage automatique de la distribution des parcs</li>
	 * </ul>
	 * 
	 * @param carreMap la carte des propositions de parc indexée par idInspire.
	 *                 Modifiée par la méthode pour appliquer les surfaces calculées.
	 * @param recoSquareMeterPerCapita densité recommandée en m²/habitant (typiquement 12 m²/hab).
	 * @param urbanDistance distance d'accessibilité en mètres pour identifier les voisins.
	 * 
	 * @see #calculeEtapeProposition(Map, Double, Double, Integer)
	 * @see #findNeighbors(String, Map, Integer)
	 * 
	 * @author github.com/cunvoas
	 */
    
    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                                      Double minSquareMeterPerCapita,
                                      Double recoSquareMeterPerCapita,
                                      Integer urbanDistance) {
       
    		List<ParkProposal> proposals = new ArrayList<>();
    		if (carreMap.isEmpty()) {
    			log.warn("Carte des carrés vide, aucune proposition à calculer");
    			return null;
    		}

    		log.info("Démarrage du calcul itératif avec Choco Solver pour {} carrés", carreMap.size());

    		// Pré-calcul des voisinages (ne change pas entre les itérations)
    		List<String> carreIds = new ArrayList<>(carreMap.keySet());
    		Map<String, List<String>> voisinagesIds = new HashMap<>();
    		for (String idInspire : carreIds) {
    			List<ParkProposalWork> voisins = findNeighbors(idInspire, carreMap, urbanDistance);
    			List<String> voisinsIds = new ArrayList<>();
    			for (ParkProposalWork voisin : voisins) {
    				voisinsIds.add(voisin.getIdInspire());
    			}
    			voisinagesIds.put(idInspire, voisinsIds);
    		}

    		int maxIterations = carreIds.size();
    		for (int iter = 1; iter <= maxIterations; iter++) {
    			// Vérifier s'il reste des carrés en déficit notable
    			boolean resteDeficit = carreMap.values().stream().anyMatch(p -> {
    				double pop = p.getAccessingPopulation().doubleValue();
    				double densite = p.getSurfacePerCapita() != null ? p.getSurfacePerCapita().doubleValue() : 0d;
    				return pop > 0 && densite < recoSquareMeterPerCapita;
    			});

    			if (!resteDeficit) {
    				log.info("Itération {}: plus de déficit significatif, arrêt.", iter);
    				break;
    			}

    			Model model = new Model("Optimisation Parcs - itération " + iter);

    			// Variables : surface à ajouter pour chaque carré (0 ou >= 1000)
    			IntVar[] surfacesAAjouter = new IntVar[carreIds.size()];
    			for (int i = 0; i < carreIds.size(); i++) {
    				String id = carreIds.get(i);
    				ParkProposalWork carre = carreMap.get(id);
    				int population = carre.getAccessingPopulation().intValue();
    				int surfaceExistante = carre.getAccessingSurface().intValue();

    				// Besoin théorique pour atteindre la reco
    				int besoinTheorique = population > 0 ? (int)Math.max(0, Math.min((int)(population * recoSquareMeterPerCapita) - surfaceExistante, CARRE_SURFACE)) : 0;
    				int upperBound = Math.max(besoinTheorique, 0);

    				surfacesAAjouter[i] = model.intVar("add_" + id, 0, upperBound > 0 ? upperBound : (int)CARRE_SURFACE);
    				// 0 OU >= MIN_PARK_SURFACE lorsque une addition est décidée
    				model.or(
    					model.arithm(surfacesAAjouter[i], "=", 0),
    					model.arithm(surfacesAAjouter[i], ">=", (int)MIN_PARK_SURFACE)
    				).post();
    			}

    			// Calcul de l'écart absolu à la densité recommandée après ajout local + voisins
    			IntVar[] ecartsAbsolus = new IntVar[carreIds.size()];
    			for (int i = 0; i < carreIds.size(); i++) {
    				String idInspire = carreIds.get(i);
    				ParkProposalWork carre = carreMap.get(idInspire);
    				int population = carre.getAccessingPopulation().intValue();

    				if (population == 0) {
    					ecartsAbsolus[i] = model.intVar(0);
    					continue;
    				}

    				int surfaceExistante = carre.getAccessingSurface().intValue();
    				List<String> voisins = voisinagesIds.get(idInspire);

    				// Somme des ajouts voisins + local
    				List<IntVar> additions = new ArrayList<>();
    				additions.add(surfacesAAjouter[i]);
    				for (String vId : voisins) {
    					int vIndex = carreIds.indexOf(vId);
    					if (vIndex >= 0) {
    						additions.add(surfacesAAjouter[vIndex]);
    					}
    				}
    				IntVar[] additionsArr = additions.toArray(new IntVar[0]);
    				int maxAdd = (int)(CARRE_SURFACE * (additionsArr.length));
    				IntVar sommeAdd = model.intVar("sumAdd_" + idInspire, 0, maxAdd);
    				model.sum(additionsArr, "=", sommeAdd).post();

    				// Surface totale après ajouts
    				IntVar surfaceTotale = model.intVar("total_" + idInspire, surfaceExistante, surfaceExistante + maxAdd);
    				model.arithm(surfaceTotale, "=", sommeAdd, "+", surfaceExistante).post();

    				// Densité cible en surface brute
    				int surfaceCible = (int)(population * recoSquareMeterPerCapita);
    				IntVar ecart = model.intVar("ecart_" + idInspire, -(surfaceCible), surfaceCible + maxAdd);
    				model.arithm(ecart, "=", model.intVar(surfaceCible), "-", surfaceTotale).post();

    				ecartsAbsolus[i] = model.intVar("abs_" + idInspire, 0, surfaceCible + maxAdd);
    				model.absolute(ecartsAbsolus[i], ecart).post();
    			}

    			// Objectif : minimiser la somme des écarts absolus
    			IntVar objectif = model.intVar("obj_" + iter, 0, IntVar.MAX_INT_BOUND);
    			model.sum(ecartsAbsolus, "=", objectif).post();
    			model.setObjective(Model.MINIMIZE, objectif);

    			log.info("Itération {}: résolution du modèle...", iter);
    			boolean solved = model.getSolver().solve();
    			if (!solved) {
    				log.warn("Itération {}: aucune solution trouvée, arrêt.", iter);
    				break;
    			}

    			// Appliquer les ajouts non nuls
    			int ajouts = 0;
    			double surfaceAjoutee = 0d;
    			for (int i = 0; i < carreIds.size(); i++) {
    				int add = surfacesAAjouter[i].getValue();
    				if (add > 0) {
    					ajouts++;
    					surfaceAjoutee += add;
    					ParkProposalWork p = carreMap.get(carreIds.get(i));
    					p.setNewSurface(BigDecimal.valueOf(add));
    					p.setNewMissingSurface(p.getMissingSurface() != null ? p.getMissingSurface().subtract(BigDecimal.valueOf(add)).max(BigDecimal.ZERO) : BigDecimal.ZERO);
    				}
    			}

    			log.info("Itération {}: {} ajouts, {:.2f} m² ajoutés.", iter, ajouts, surfaceAjoutee);

    			if (ajouts == 0) {
    				log.info("Itération {}: aucun ajout proposé (seuil 1000 m²), arrêt.", iter);
    				break;
    			}

    			// Mettre à jour les densités locales après application
    			for (String idInspire : carreIds) {
    				ParkProposalWork carre = carreMap.get(idInspire);
    				double totalSurface = carre.getAccessingSurface().doubleValue();
    				if (carre.getNewSurface() != null) {
    					totalSurface += carre.getNewSurface().doubleValue();
    				}
    				for (String vId : voisinagesIds.get(idInspire)) {
    					ParkProposalWork voisin = carreMap.get(vId);
    					if (voisin != null && voisin.getNewSurface() != null) {
    						totalSurface += voisin.getNewSurface().doubleValue();
    					}
    				}
    				double pop = carre.getAccessingPopulation().doubleValue();
    				if (pop > 0) {
    					carre.setSurfacePerCapita(BigDecimal.valueOf(totalSurface / pop));
    				}
    			}
    		}

    		log.info("Calcul itératif terminé.");
    	
        return proposals;
    }
}
