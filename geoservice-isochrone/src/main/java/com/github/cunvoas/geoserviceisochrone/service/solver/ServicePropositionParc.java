package com.github.cunvoas.geoserviceisochrone.service.solver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalWorkRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import lombok.extern.slf4j.Slf4j;

/**
 * Service pour calculer les propositions d'augmentation de parc par carré 
 * en utilisant Choco Solver.
 * 
 * Contraintes :
 * - Un parc doit faire minimum 1000 m²
 * - Densité recommandée : 12 m²/habitant
 */
@Service
@Slf4j
public class ServicePropositionParc {
	

	private static final double MIN_PARK_SURFACE = 1000.0; // m²
	private static final double CARRE_SIZE = 200.0; // mètres (200m x 200m)	
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	@Autowired
	private ServiceOpenData serviceOpenData;

	@Autowired
	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Autowired
	private InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;
	@Autowired
	private Filosofil200mRepository filosofil200mRepository;

	@Autowired
	private ParkProposalWorkRepository parkProposalWorkRepository;
	
	
	public void saveProposals(Map<String, ParkProposalWork> proposals) {
		if (proposals!=null && !proposals.isEmpty()) {
			List<ParkProposalWork> list = new ArrayList<>(proposals.values());
			//proposals.values().stream().toList()
			parkProposalWorkRepository.saveAll(list);
		}
	}
	
	/**
	 * Fait une propostion avec une approche par carré.
	 * @param insee
	 * @param annee
	 */
	public Map<String, ParkProposalWork> calculeProposition(String insee, Integer annee) {
		
		Boolean dense = serviceOpenData.isDistanceDense(insee);
		// Distance OMS selon densité
		Integer urbanDistance = Integer.valueOf(dense ? 
				applicationBusinessProperties.getOmsUrbanDistance() :
					applicationBusinessProperties.getOmsSubUrbanDistance());
		// Surface recommandée par habitant
		Double recoSquareMeterPerCapita = dense ?
				applicationBusinessProperties.getRecoUrbSquareMeterPerCapita() :
					applicationBusinessProperties.getRecoSubUrbSquareMeterPerCapita();

		// Surface mini par habitant
		Double minSquareMeterPerCapita = dense ?
				applicationBusinessProperties.getMinUrbSquareMeterPerCapita() :
					applicationBusinessProperties.getMinSubUrbSquareMeterPerCapita();
		
		// Récupérer les carrés de la commune
		List<InseeCarre200mOnlyShape> carreShapes = inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(insee, true);
		
		
		//préparation des données pour le calcul
		Map<String, ParkProposalWork> carreMap = new HashMap<>();
		for (InseeCarre200mOnlyShape shape : carreShapes) {
			Optional<InseeCarre200mComputedV2> oCarreCputd = inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(annee, shape.getIdInspire());
			if (oCarreCputd.isPresent()) {
				InseeCarre200mComputedV2 carreCputd = oCarreCputd.get();
				Filosofil200m filo = filosofil200mRepository.findByAnneeAndIdInspire(annee, shape.getIdInspire());
				
				ParkProposalWork parkProposal = new ParkProposalWork();
				parkProposal.setAnnee(annee);
				parkProposal.setIdInspire(shape.getIdInspire());
				parkProposal.setCentre(shape.getGeoPoint2d());
				parkProposal.setIsDense(dense);
				parkProposal.setSurfacePerCapita(carreCputd.getSurfaceParkPerCapitaOms());
				
				// ( Seuil OMS – MAX (0, surface disponible  - seuil OMS) ) * Nb Habitant qui ont accès
				Double densiteMissing = Math.max(recoSquareMeterPerCapita - carreCputd.getSurfaceParkPerCapitaOms().doubleValue(), 0);
				parkProposal.setMissingSurface(BigDecimal.valueOf(densiteMissing*carreCputd.getPopAll().doubleValue())); 
				parkProposal.setAccessingPopulation(carreCputd.getPopAll());
				parkProposal.setAccessingSurface(carreCputd.getSurfaceTotalParkOms());
				

				try {
					parkProposal.setLocalPopulation(filo!=null?filo.getNbIndividus():BigDecimal.ZERO);
				} catch (Exception e) {
					log.warn("CRASH: Filosofil {}",  shape.getIdInspire());
				}
				parkProposal.setNewSurface(parkProposal.getAccessingSurface()); 
				parkProposal.setNewSurfacePerCapita(parkProposal.getSurfacePerCapita()); 
				parkProposal.setNewMissingSurface(parkProposal.getMissingSurface()); 
				carreMap.put(shape.getIdInspire(), parkProposal);
			} else {
				log.info("Pas de données Filosofil pour le carré {} en {}", shape.getIdInspire(), annee);
			}
		}
		
		if (carreMap.isEmpty()) {
			log.info("Aucun carré avec données pour la commune {} en {}", insee, annee);
		} else  {
			log.info("Calcul des propositions pour {} carrés dans la commune {} en {} (dense={}): reco={} m²/hab, min={} m²/hab, distance={} m", 
					carreMap.size(), insee, annee, dense, recoSquareMeterPerCapita, minSquareMeterPerCapita, urbanDistance);
		}
		
		for (int i=0; i<carreMap.size(); i++) {
			this.calculeEtapeProposition(carreMap, minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
		}
		
//		this.calculePropositionSolver(carreMap, recoSquareMeterPerCapita, urbanDistance);
		
		// utilisation du solver pour trouver des solutions car chaque carré interragit avec ses voisins pour les distances d'accibilité.
		// chaque proposition est >= 1000m² ou 0m²
		// tous les carrés voisins sont pris en compte pour le calcul de la densité
		// la densité cible est de 12m²/habitant
		// pour les carrés avec les moins de parc, réaliser une proposition d'augmentation
		// l'appliquer dans les calculs jurs'à ce que tous les carrés sent traités directement ou non.
		
		return carreMap;
	}
	

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
	public void calculePropositionSolver(Map<String, ParkProposalWork> carreMap, Double recoSquareMeterPerCapita, Integer urbanDistance) {
		if (carreMap.isEmpty()) {
			log.warn("Carte des carrés vide, aucune proposition à calculer");
			return;
		}
		
		log.info("Démarrage du calcul avec Choco Solver pour {} carrés", carreMap.size());
		
		// Créer le modèle Choco Solver
		Model model = new Model("Optimisation Globale Parcs");
		
		// Indexation des carrés pour accès rapide
		List<String> carreIds = new ArrayList<>(carreMap.keySet());
		Map<String, Integer> carreIndex = new HashMap<>();
		for (int i = 0; i < carreIds.size(); i++) {
			carreIndex.put(carreIds.get(i), i);
		}
		
		// Variables : surface à ajouter pour chaque carré
		IntVar[] surfacesAAjouter = new IntVar[carreIds.size()];
		for (int i = 0; i < carreIds.size(); i++) {
			surfacesAAjouter[i] = model.intVar("surface_" + carreIds.get(i), 0, 40000);
			
			// Contrainte : 0 OU >= 1000 m²
			model.or(
				model.arithm(surfacesAAjouter[i], "=", 0),
				model.arithm(surfacesAAjouter[i], ">=", (int)MIN_PARK_SURFACE)
			).post();
		}
		
		// Pré-calcul des voisinages pour optimisation
		Map<String, List<String>> voisinagesIds = new HashMap<>();
		for (String idInspire : carreIds) {
			List<ParkProposalWork> voisins = findNeighbors(idInspire, carreMap, urbanDistance);
			List<String> voisinsIds = new ArrayList<>();
			for (ParkProposalWork voisin : voisins) {
				voisinsIds.add(voisin.getIdInspire());
			}
			voisinagesIds.put(idInspire, voisinsIds);
		}
		
		// Variables pour les écarts à la densité recommandée
		IntVar[] ecarts = new IntVar[carreIds.size()];
		IntVar[] ecartsAbsolus = new IntVar[carreIds.size()];
		
		for (int i = 0; i < carreIds.size(); i++) {
			String idInspire = carreIds.get(i);
			ParkProposalWork carre = carreMap.get(idInspire);
			
			int population = carre.getAccessingPopulation().intValue();
			if (population == 0) {
				// Pas de population, pas d'écart
				ecarts[i] = model.intVar(0);
				ecartsAbsolus[i] = model.intVar(0);
				continue;
			}
			
			int surfaceExistante = carre.getAccessingSurface().intValue();
			int surfaceCible = (int)(population * recoSquareMeterPerCapita);
			
			// Calculer la surface totale après ajouts (carré + voisins)
			// surfaceTotale = surfaceExistante + surfaceAAjouter[i] + somme(surfacesAAjouter[voisins])
			
			List<String> voisinsIds = voisinagesIds.get(idInspire);
			IntVar[] surfacesVoisins = new IntVar[voisinsIds.size() + 1];
			surfacesVoisins[0] = surfacesAAjouter[i]; // Surface du carré lui-même
			
			for (int j = 0; j < voisinsIds.size(); j++) {
				String voisinId = voisinsIds.get(j);
				Integer voisinIdx = carreIndex.get(voisinId);
				if (voisinIdx != null) {
					surfacesVoisins[j + 1] = surfacesAAjouter[voisinIdx];
				} else {
					// Voisin hors map (ne devrait pas arriver)
					surfacesVoisins[j + 1] = model.intVar(0);
				}
			}
			
			// Somme des surfaces ajoutées (carré + voisins)
			IntVar sommeSurfacesAjoutees = model.intVar("somme_" + idInspire, 0, 40000 * (voisinsIds.size() + 1));
			model.sum(surfacesVoisins, "=", sommeSurfacesAjoutees).post();
			
			// Surface totale accessible après ajouts
			IntVar surfaceTotale = model.intVar("total_" + idInspire, 
					surfaceExistante, surfaceExistante + 40000 * (voisinsIds.size() + 1));
			model.arithm(surfaceTotale, "=", sommeSurfacesAjoutees, "+", surfaceExistante).post();
			
			// Variable pour la surface cible
			IntVar surfaceCibleVar = model.intVar("cible_" + idInspire, surfaceCible);
			
			// Écart = surfaceCible - surfaceTotale
			ecarts[i] = model.intVar("ecart_" + idInspire, 
					surfaceExistante - surfaceCible, 
					surfaceExistante + 40000 * (voisinsIds.size() + 1) - surfaceCible);
			model.arithm(ecarts[i], "=", surfaceCibleVar, "-", surfaceTotale).post();
			
			// Écart absolu pour la fonction objectif
			ecartsAbsolus[i] = model.intVar("ecart_abs_" + idInspire, 0, 
					Math.max(Math.abs(surfaceExistante - surfaceCible), 
							Math.abs(surfaceExistante + 40000 * (voisinsIds.size() + 1) - surfaceCible)));
			model.absolute(ecartsAbsolus[i], ecarts[i]).post();
		}
		
		// Objectif : minimiser la somme des écarts absolus
		// On peut pondérer par la population pour privilégier les zones peuplées
		IntVar objectif = model.intVar("objectif", 0, IntVar.MAX_INT_BOUND);
		model.sum(ecartsAbsolus, "=", objectif).post();
		
		model.setObjective(Model.MINIMIZE, objectif);
		
		// Résolution
		log.info("Lancement de la résolution...");
		if (model.getSolver().solve()) {
			log.info("Solution trouvée ! Application des propositions...");
			
			// Appliquer les solutions
			int nbParcsAjoutes = 0;
			double surfaceTotaleAjoutee = 0;
			
			for (int i = 0; i < carreIds.size(); i++) {
				int surfaceProposee = surfacesAAjouter[i].getValue();
				
				if (surfaceProposee > 0) {
					String idInspire = carreIds.get(i);
					ParkProposalWork carre = carreMap.get(idInspire);
					
					carre.setNewSurface(BigDecimal.valueOf(surfaceProposee));
					nbParcsAjoutes++;
					surfaceTotaleAjoutee += surfaceProposee;
					
					log.info("Carré {} : ajout de {} m² de parc", idInspire, surfaceProposee);
				}
			}
			
			// Recalculer les surfaces par habitant pour tous les carrés
			for (String idInspire : carreIds) {
				ParkProposalWork carre = carreMap.get(idInspire);
				double surfaceAccessible = carre.getAccessingSurface().doubleValue();
				
				// Ajouter les surfaces des voisins
				List<String> voisinsIds = voisinagesIds.get(idInspire);
				for (String voisinId : voisinsIds) {
					ParkProposalWork voisin = carreMap.get(voisinId);
					if (voisin != null && voisin.getNewSurface() != null) {
						surfaceAccessible += voisin.getNewSurface().doubleValue();
					}
				}
				
				// Ajouter la surface locale
				if (carre.getNewSurface() != null) {
					surfaceAccessible += carre.getNewSurface().doubleValue();
				}
				
				// Recalculer la densité
				double population = carre.getAccessingPopulation().doubleValue();
				if (population > 0) {
					double nouvelleDensite = surfaceAccessible / population;
					carre.setSurfacePerCapita(BigDecimal.valueOf(nouvelleDensite));
				}
			}
			
			log.info("Optimisation terminée : {} parcs ajoutés pour une surface totale de {:.2f} m²", 
					nbParcsAjoutes, surfaceTotaleAjoutee);
			log.info("Écart total à l'objectif : {}", objectif.getValue());
			
		} else {
			log.warn("Aucune solution trouvée par le solveur");
		}
	}
	
	/**
	 * Calcule une étape de proposition d'augmentation de parc en traitant le carré avec le plus grand déficit.
	 * 
	 * <p>Cette méthode implémente un algorithme itératif qui :</p>
	 * <ol>
	 *   <li>Identifie le carré avec le plus grand déficit en surface de parc par habitant</li>
	 *   <li>Calcule la surface de parc nécessaire pour atteindre la densité recommandée</li>
	 *   <li>Applique la proposition si elle respecte la surface minimale de 1000 m²</li>
	 *   <li>Met à jour les données du carré traité et de ses voisins dans le rayon d'accessibilité</li>
	 * </ol>
	 * 
	 * <p><strong>Contraintes appliquées :</strong></p>
	 * <ul>
	 *   <li>Surface minimale d'un parc : 1000 m²</li>
	 *   <li>Si la surface calculée est inférieure à 1000 m², aucun parc n'est proposé</li>
	 *   <li>Seuls les carrés avec un déficit supérieur au seuil minimum sont traités</li>
	 * </ul>
	 * 
	 * <p><strong>Algorithme :</strong></p>
	 * <pre>
	 * 1. Trier les carrés par déficit décroissant
	 * 2. Sélectionner le carré avec le plus grand déficit
	 * 3. Si déficit &lt; seuil minimum → arrêt (tous les carrés traités)
	 * 4. Calculer : surfaceÀAjouter = max((densitéReco - densitéActuelle) × population, 0)
	 * 5. Si surfaceÀAjouter ≥ 1000 m² :
	 *    - Appliquer la proposition au carré
	 *    - Mettre à jour sa densité
	 *    - Identifier les voisins dans le rayon d'accessibilité
	 *    - Mettre à jour la densité de chaque voisin
	 * 6. Sinon : marquer le carré comme non traité (localSurface = null)
	 * </pre>
	 * 
	 * <p><strong>Impact sur les données :</strong></p>
	 * <ul>
	 *   <li>{@code localSurface} : surface de parc proposée pour le carré (null si &lt; 1000 m²)</li>
	 *   <li>{@code surfacePerCapita} : recalculée pour le carré et ses voisins</li>
	 *   <li>{@code accessingSurface} : surface totale accessible mise à jour</li>
	 * </ul>
	 * 
	 * @param carreMap la carte des propositions de parc indexée par idInspire.
	 *                 Chaque {@link ParkProposalWork} contient les données démographiques et surfaciques.
	 *                 Cette map est modifiée par la méthode.
	 * @param minSquareMeterPerCapita seuil minimal de surface de parc par habitant (m²/hab).
	 *                                En dessous de ce seuil, le carré est considéré comme traité.
	 *                                Valeur typique : 8 m²/hab (zone urbaine) ou 10 m²/hab (zone péri-urbaine).
	 * @param recoSquareMeterPerCapita densité recommandée de surface de parc par habitant (m²/hab).
	 *                                 Objectif à atteindre pour chaque carré.
	 *                                 Valeur typique : 12 m²/hab (recommandation OMS).
	 * @param urbanDistance distance d'accessibilité en mètres définissant les voisins.
	 *                      Les carrés dans ce rayon sont considérés comme accessibles.
	 *                      Valeur typique : 300m (zone urbaine) ou 1000m (zone péri-urbaine).
	 * 
	 * @see #sortProposalsByDeficit(Map)
	 * @see #findNeighbors(String, Map, Integer)
	 * @see ParkProposalWork
	 * 
	 * @author github.com/cunvoas
	 */
	public void calculeEtapeProposition(Map<String, ParkProposalWork> carreMap,  Double minSquareMeterPerCapita, Double recoSquareMeterPerCapita, Integer urbanDistance) {
		List<ParkProposalWork> sorted = sortProposalsByDeficit(carreMap);
//		List<ParkProposal> sorted = sortProposalsByPersona(carreMap);
		
		if (!sorted.isEmpty()) {
			ParkProposalWork toProcess = sorted.get(0);
			if (toProcess.getSurfacePerCapita().doubleValue() > minSquareMeterPerCapita) {
				log.info("Toutes les propositions de la commune sont traitées.");
				return;
			}
			List<ParkProposalWork> neighbors = findNeighbors(toProcess.getIdInspire(), carreMap, urbanDistance);

			Double newParkSurface = Math.max(recoSquareMeterPerCapita-toProcess.getSurfacePerCapita().doubleValue(), 0) * toProcess.getAccessingPopulation().doubleValue();
			if (newParkSurface>=MIN_PARK_SURFACE) {
				// appliquer la proposition
				toProcess.setNewSurface(BigDecimal.valueOf(newParkSurface));
				toProcess.setNewMissingSurface(toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(newParkSurface)));
				
				// mettre à jour la surface par habitant
				Double newTotalSurface = toProcess.getAccessingSurface().doubleValue() + newParkSurface;
				Double newSurfacePerCapita = newTotalSurface / toProcess.getAccessingPopulation().doubleValue();
				toProcess.setSurfacePerCapita(BigDecimal.valueOf(newSurfacePerCapita));
				
//				carreMap.put(toProcess.getIdInspire(), toProcess);
				
//				log.warn("Proposition pour le carré {} : ajouter {} m² de parc.", toProcess.getIdInspire(), newParkSurface);
				
				// mettre à jour les voisins
				for (ParkProposalWork neighbor : neighbors) {
					Double neighborNewTotalSurface = neighbor.getAccessingSurface().doubleValue() + newParkSurface;
					
					Double neighborNewSurfacePerCapita = null;
					if ( neighbor.getAccessingPopulation().doubleValue()!=0)	{
						//log.error("neighbor {} accessingPopulation={}", neighbor.getIdInspire(), neighbor.getAccessingPopulation().doubleValue());
						
						neighborNewSurfacePerCapita = neighborNewTotalSurface / neighbor.getAccessingPopulation().doubleValue();
//						log.error("neighborNewSurfacePerCapita={}", neighborNewSurfacePerCapita);
						neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborNewSurfacePerCapita));
					
					} else {
						neighbor.setNewSurfacePerCapita(null);
					}
					neighbor.setNewSurface(new BigDecimal(neighborNewTotalSurface));
					neighbor.setNewMissingSurface( toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(newParkSurface)).max(BigDecimal.ZERO));

//					carreMap.put(neighbor.getIdInspire(), neighbor);
				}
				
				log.error("Proposition pour le carré {} : ajout de parc (surface proposée: {}).", 
						toProcess.getIdInspire(), newParkSurface);
				
			} else {
				log.info("Proposition pour le carré {} : pas d'ajout de parc (surface proposée: {}).", 
						toProcess.getIdInspire(), MIN_PARK_SURFACE);
				//toProcess.setNewSurface(null);
			}
		
		
		}
	}
	
	
	/**
	 * Trie les propositions par déficit décroissant
	 * @param carreMap
	 * @return
	 * test: 268566.01
	 */
	public List<ParkProposalWork> sortProposalsByDeficit(Map<String, ParkProposalWork> carreMap) {
		List<ParkProposalWork> proposals = new ArrayList<>(carreMap.values());
		
		// Trier les propositions par déficit décroissant
		proposals.sort((p1, p2) -> {
			Double deficit1 = p1.getNewMissingSurface()!=null?p1.getNewMissingSurface().doubleValue():0;
			Double deficit2 = p2.getNewMissingSurface()!=null?p2.getNewMissingSurface().doubleValue():0;
			return Double.compare(deficit2, deficit1);
		});
		return proposals;
	}
	

	
	/**
	 * Trie les propositions par déficit décroissant
	 * @param carreMap
	 * @return
	 * test: xxxs
	 */
	public List<ParkProposalWork> sortProposalsByPersona(Map<String, ParkProposalWork> carreMap) {
		List<ParkProposalWork> proposals = new ArrayList<>(carreMap.values());
		
		// Trier les propositions par déficit décroissant
		proposals.sort((p1, p2) -> {

			Double deficit1 = p1.getNewSurfacePerCapita()!=null?p1.getNewSurfacePerCapita().doubleValue():0;
			Double deficit2 = p2.getNewSurfacePerCapita()!=null?p2.getNewSurfacePerCapita().doubleValue():0;
			return Double.compare(deficit2, deficit1);
		});
		return proposals;
	}
	
	
	/**
	 * Trouve les N carrés voisins d'un carré donné selon la sensité.
	 * 
	 * @param idInspire identifiant du carré central
	 * @param annee année de référence
	 * @return liste des carrés voisins (max 24 ou 143 selon densité)
	 */
	public List<ParkProposalWork> findNeighbors(String idInspire, Map<String, ParkProposalWork> carreMap, Integer urbanDistance) {
		List<ParkProposalWork> neighbors = new ArrayList<>();
		
		// Récupérer le carré central
		ParkProposalWork centre = carreMap.get(idInspire);
		
		if (centre == null) {
			log.warn("Carré central {} introuvable dans la carte des carrés", idInspire);
			return neighbors;
		}
		
		// Récupérer le centroïde du carré central
		Geometry centreGeom = centre.getCentre();
		Coordinate centreCentroid = centreGeom.getCentroid().getCoordinate();
		
		// Rechercher les carrés dans cette zone
		for (Map.Entry<String, ParkProposalWork> parkProposal : carreMap.entrySet()) {
			if (!parkProposal.getKey().equals(idInspire)) {
				Double distance = 1_000 * DistanceHelper.crowFlyDistance(
						centreCentroid.y, 
						centreCentroid.x,
						parkProposal.getValue().getCentre().getCentroid().getY(),
						parkProposal.getValue().getCentre().getCentroid().getX());
				
				if (distance<urbanDistance+100) { // +100m pour le périmètre vs le centroïde
					neighbors.add(parkProposal.getValue());
				}
			}
		}

		log.info("Trouvé {} voisins pour le carré {}", neighbors.size(), idInspire);
		return neighbors;
	}
	
	
}
