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
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalWorkRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import lombok.extern.slf4j.Slf4j;
import com.github.cunvoas.geoserviceisochrone.service.solver.sort.ProposalSortStrategy;
import com.github.cunvoas.geoserviceisochrone.service.solver.sort.ProposalSortStrategyFactory;
import com.github.cunvoas.geoserviceisochrone.service.solver.sort.ProposalSortStrategyFactory.Type;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationStrategy;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationStrategyFactory;

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

	private static final double AT_LEAST_PARK_SURFACE = 1_000; // m²
	private static final double MIN_PARK_SURFACE = 650; // m²
	private static final double CARRE_SIZE = 200; // mètres (200m x 200m)	
	private static final double CARRE_SURFACE = 40_000; // m²
	
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
	@Autowired
	private ParkProposalRepository parkProposalRepository;
	
	
	public void saveProposals(Map<String, ParkProposalWork> proposals) {
		if (proposals!=null && !proposals.isEmpty()) {
			List<ParkProposalWork> list = new ArrayList<>(proposals.values());
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
		
		
		// ALGO 1 : approche déléguée via stratégie (itérative par défaut)
		ProposalComputationStrategy computation = ProposalComputationStrategyFactory.create(
				ProposalComputationStrategyFactory.Type.ITERATIVE, this, MIN_PARK_SURFACE);
		List<ParkProposal> proposals = computation.compute(carreMap, minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
		if (!proposals.isEmpty()) {
			parkProposalRepository.saveAll(proposals);
		}

		
		// ALGO 2 : approche solver global (exemple d'utilisation via stratégie)
		// ProposalComputationStrategy solver = ProposalComputationStrategyFactory.create(
		//         ProposalComputationStrategyFactory.Type.SOLVER, this, MIN_PARK_SURFACE);
		// solver.compute(carreMap, minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
		
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
	public ParkProposal calculeEtapeProposition(Double minParkSurface, Map<String, ParkProposalWork> carreMap,  Double minSquareMeterPerCapita, Double recoSquareMeterPerCapita, Integer urbanDistance) {
		List<ParkProposalWork> sorted = sortProposalsByDeficit(carreMap);
		//List<ParkProposalWork> sorted = sortProposalsByPersona(carreMap);
		
		ParkProposal proposalResult = null;
		if (!sorted.isEmpty()) {
			ParkProposalWork toProcess = sorted.get(0);
			if (toProcess.getSurfacePerCapita().doubleValue() > minSquareMeterPerCapita) {
				log.info("Toutes les propositions de la commune sont traitées.");
				return proposalResult;
			}
			List<ParkProposalWork> neighbors = findNeighbors(toProcess.getIdInspire(), carreMap, urbanDistance);

			// calcul de la surface de parc à ajouter pour atteindre la densité recommandée
			// comprise entre 0 et 40 000 m² (surface max d'un carré de 200m x 200m)
			Double newParkSurface = Math.min(Math.max(recoSquareMeterPerCapita-toProcess.getSurfacePerCapita().doubleValue(), 0), CARRE_SURFACE) * toProcess.getAccessingPopulation().doubleValue();
			if (newParkSurface>=minParkSurface) {
				
				
				proposalResult = new ParkProposal();
				proposalResult.setAnnee(toProcess.getAnnee());
				proposalResult.setIdInspire(toProcess.getIdInspire());
				proposalResult.setParkSurface(BigDecimal.valueOf(newParkSurface));
				proposalResult.setCentre(toProcess.getCentre());
				proposalResult.setIsDense(toProcess.getIsDense());
				
				// appliquer la proposition
				toProcess.setNewSurface(BigDecimal.valueOf(newParkSurface));
				toProcess.setNewMissingSurface(toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(newParkSurface)));
				
				// mettre à jour la surface par habitant
				Double newTotalSurface = toProcess.getAccessingSurface().doubleValue() + newParkSurface;
				Double newSurfacePerCapita = newTotalSurface / toProcess.getAccessingPopulation().doubleValue();
				toProcess.setSurfacePerCapita(BigDecimal.valueOf(newSurfacePerCapita));
				
				// mettre à jour les voisins
				for (ParkProposalWork neighbor : neighbors) {
					Double neighborNewTotalSurface = neighbor.getAccessingSurface().doubleValue() + newParkSurface;
					
					Double neighborNewSurfacePerCapita = null;
					if ( neighbor.getAccessingPopulation().doubleValue()!=0)	{
						//log.error("neighbor {} accessingPopulation={}", neighbor.getIdInspire(), neighbor.getAccessingPopulation().doubleValue());
						
						neighborNewSurfacePerCapita = neighborNewTotalSurface / neighbor.getAccessingPopulation().doubleValue();
						neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborNewSurfacePerCapita));
					
					} else {
						neighbor.setNewSurfacePerCapita(null);
					}
					neighbor.setNewSurface(new BigDecimal(String.valueOf(neighborNewTotalSurface)));
					neighbor.setNewMissingSurface( toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(newParkSurface)).max(BigDecimal.ZERO));
				}
				
				log.error("Proposition pour le carré {} : ajout de parc (surface proposée: {}).", 
						toProcess.getIdInspire(), newParkSurface);
				
			} else {
				log.info("Proposition pour le carré {} : pas d'ajout de parc (surface proposée: {}).", 
						toProcess.getIdInspire(), MIN_PARK_SURFACE);
				//toProcess.setNewSurface(null);
			}
		}
		return proposalResult;
	}
	
	
	/**
	 * Trie les propositions par déficit décroissant
	 * @param carreMap
	 * @return
	 * test: 268566.01
	 */
	public List<ParkProposalWork> sortProposalsByDeficit(Map<String, ParkProposalWork> carreMap) {
		ProposalSortStrategy strategy = ProposalSortStrategyFactory.create(Type.DEFICIT);
		return strategy.sort(carreMap);
	}
	

	
	/**
	 * Trie les propositions par déficit décroissant
	 * @param carreMap
	 * @return
	 * test: xxxs
	 */
	public List<ParkProposalWork> sortProposalsByPersona(Map<String, ParkProposalWork> carreMap) {
		ProposalSortStrategy strategy = ProposalSortStrategyFactory.create(Type.PERSONA);
		return strategy.sort(carreMap);
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
