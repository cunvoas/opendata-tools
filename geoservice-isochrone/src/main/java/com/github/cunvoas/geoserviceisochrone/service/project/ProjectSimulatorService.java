package com.github.cunvoas.geoserviceisochrone.service.project;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ParkProposalHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * Service de simulation de projets de création d'espaces verts et de logements.
 * 
 * Ce service gère la simulation d'impact des projets d'aménagement sur les surfaces
 * d'espaces verts disponibles au sein des communes. Il calcule les propositions
 * basées sur les données INSEE (carrés 200m x 200m) et les recommandations OMS
 * (Organisation Mondiale de la Santé).
 * 
 * Les calculs prennent en compte:
 * - La densité urbaine de la commune (dense ou non)
 * - Les distances recommandées par l'OMS (urbain vs sous-urbain)
 * - Les surfaces de parc recommandées par habitant
 * - Les populations affectées par le projet
 * 
 * @author cunvoas
 * @version 1.0
 */
@Service
@Slf4j
public class ProjectSimulatorService {
	

	/**
	 * Surface d'un carré INSEE 200m x 200m = 40 000 m²
	 * Représente l'unité de base pour les analyses géographiques.
	 * Cette valeur est légèrement approximative (±1) selon les données INSEE.
	 */
	private static final Double SURFACE_CARRE = 40_000d;
	

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	@Autowired
	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Autowired
	private InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;
	@Autowired
	private ServiceOpenData serviceOpenData;
	@Autowired
	private Filosofil200mRepository filosofil200mRepository;
	
	@Autowired
	private ProjectSimulatorRepository projectSimulatorRepository;
	

	
	
	/**
	 * Récupère un projet de simulation par son identifiant unique.
	 * 
	 * @param id Identifiant unique du projet
	 * @return Le projet correspondant, ou null si non trouvé
	 */
	public ProjectSimulator getById(Long id) {
		return projectSimulatorRepository.findById(id).orElse(null);
	}
	
	/**
	 * Enregistre ou met à jour un projet de simulation.
	 * 
	 * @param projectSimulator Le projet à enregistrer
	 * @return Le projet enregistré avec son identifiant généré (le cas échéant)
	 */
	public ProjectSimulator save(ProjectSimulator projectSimulator) {
		return projectSimulatorRepository.save(projectSimulator);
	}
	
	/**
	 * Récupère tous les projets de simulation d'une commune.
	 * 
	 * @param idCommune Identifiant INSEE de la commune
	 * @return Liste des projets de la commune, ou liste vide si idCommune est null
	 */
	public List<ProjectSimulator> findByCity(Long idCommune) {
		if (idCommune == null) {
			return List.of();
		}
		return projectSimulatorRepository.findByIdCommune(idCommune);
	}
	
	
	/**
	 * Calcule la surface totale d'une géométrie en mètres carrés.
	 * 
	 * @param geom Géométrie à mesurer (projection conforme requise)
	 * @return Surface totale de la géométrie en m²
	 */
	public Long getSurface(Geometry geom) {
		return projectSimulatorRepository.getSurface(geom);
	}

	/**
	 * Simule l'impact d'un projet d'aménagement sur les surfaces de parc.
	 * 
	 * Cette méthode:
	 * 1. Identifie les carrés INSEE impactés par la géométrie du projet
	 * 2. Détermine la densité urbaine de la zone
	 * 3. Applique les distances et seuils OMS appropriés
	 * 4. Calcule les surfaces de parc nécessaires par rapport aux recommandations
	 * 5. Évalue l'impact du projet sur la population affectée
	 * 
	 * @param projectSimulator Le projet à simuler, avec sa géométrie définie
	 * @return Le projet simulé avec les résultats du calcul
	 * @see #populate(Integer, String, Boolean, Double)
	 */
	public ProjectSimulator simulate(ProjectSimulator projectSimulator) {
		String insee=null;
		
		// get all squares in area
		List<InseeCarre200mOnlyShape> carreShapes4Area = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(projectSimulator.getShapeArea(), false);
		if (carreShapes4Area!=null && !carreShapes4Area.isEmpty()) {
			insee = carreShapes4Area.get(0).getCodeInsee();
		}
		
		Boolean dense = serviceOpenData.isDistanceDense(insee);
		// Distance OMS selon densité
		Integer urbanDistance = Integer.valueOf(dense ? 
				applicationBusinessProperties.getOmsUrbanDistance() :
					applicationBusinessProperties.getOmsSubUrbanDistance());
		// Surface recommandée par habitant
		Double recoSquareMeterPerCapita = dense ?
				applicationBusinessProperties.getRecoUrbSquareMeterPerCapita() :
					applicationBusinessProperties.getRecoSubUrbSquareMeterPerCapita();

		Integer annee = applicationBusinessProperties.getDerniereAnnee();
		
		// carre de la ville
		Map<String, ParkProposalWork> carreMap = populate(annee, insee, dense, recoSquareMeterPerCapita);
		
		// carres impactés par le projet
		Set<ParkProposalWork> carreForSimulation = new HashSet<>();
		for (InseeCarre200mOnlyShape inseeCarre200mOnlyShape : carreShapes4Area) {
			List<ParkProposalWork>  neighbors = ParkProposalHelper.findNeighbors(inseeCarre200mOnlyShape.getIdInspire(), carreMap, urbanDistance);
			carreForSimulation.addAll(neighbors);
		}
		
		// Calcul de la projection du projet sur les parcs disponibles
		// 1. Calculer la surface du projet de parc
		Long surfaceParkProjet = projectSimulator.getSurfacePark() != null ? 
				projectSimulator.getSurfacePark().longValue() : 0L;
		
		// 2. Pour chaque carré impacté, recalculer les métriques avec le nouveau parc
		BigDecimal totalPopAffected = BigDecimal.ZERO;
		BigDecimal totalSurfaceMissingBefore = BigDecimal.ZERO;
		BigDecimal totalSurfaceMissingAfter = BigDecimal.ZERO;
		BigDecimal totalSurfaceParkAvailable = BigDecimal.ZERO;
		
		for (ParkProposalWork carre : carreForSimulation) {
			BigDecimal popAccessing = carre.getAccessingPopulation() != null ? carre.getAccessingPopulation() : BigDecimal.ZERO;
			BigDecimal surfaceParkOms = carre.getAccessingSurface() != null ? carre.getAccessingSurface() : BigDecimal.ZERO;
			
			// Surface manquante avant le projet (déjà calculée dans ParkProposalWork)
			BigDecimal missingBefore = carre.getMissingSurface() != null ? carre.getMissingSurface() : BigDecimal.ZERO;
			
			// Nouvelle surface disponible après ajout du parc (répartie sur tous les carrés)
			BigDecimal newSurfacePark = surfaceParkOms.add(BigDecimal.valueOf(surfaceParkProjet));
			
			// Nouvelle surface par habitant
			BigDecimal newSurfacePerCapita = popAccessing.compareTo(BigDecimal.ZERO) > 0 ?
					newSurfacePark.divide(popAccessing, 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;
			
			// Surface manquante après le projet
			Double densiteMissingAfter = Math.max(recoSquareMeterPerCapita - newSurfacePerCapita.doubleValue(), 0);
			BigDecimal missingAfter = BigDecimal.valueOf(densiteMissingAfter * popAccessing.doubleValue());
			
			// Agrégation des résultats
			totalPopAffected = totalPopAffected.add(popAccessing);
			totalSurfaceMissingBefore = totalSurfaceMissingBefore.add(missingBefore);
			totalSurfaceMissingAfter = totalSurfaceMissingAfter.add(missingAfter);
			totalSurfaceParkAvailable = totalSurfaceParkAvailable.add(surfaceParkOms);
		}
		
		// 3. Mettre à jour le ProjectSimulator avec les résultats
		projectSimulator.setPopulation(totalPopAffected);
		
		// Stocker les métriques calculées dans des champs de commentaire (log pour traçabilité)
		log.info("Simulation du projet '{}' pour {} habitants affectés", 
				projectSimulator.getName(), totalPopAffected);
		log.info("Surface manquante AVANT projet: {} m² (recommandation OMS: {} m²/hab)", 
				totalSurfaceMissingBefore, recoSquareMeterPerCapita);
		log.info("Surface manquante APRÈS projet: {} m² (gain: {} m²)", 
				totalSurfaceMissingAfter, 
				totalSurfaceMissingBefore.subtract(totalSurfaceMissingAfter));
		log.info("Surface totale de parcs disponibles: {} m² (+ {} m² du projet)", 
				totalSurfaceParkAvailable, surfaceParkProjet);
		
		// 4. Vérifier si les critères OMS sont respectés après simulation
		if (totalSurfaceMissingAfter.compareTo(BigDecimal.ZERO) > 0) {
			// Les critères OMS ne sont pas respectés, proposer des ajustements
			BigDecimal totalSurfaceParkAfter = totalSurfaceParkAvailable.add(BigDecimal.valueOf(surfaceParkProjet));
			
			// Calculer la surface de parc supplémentaire nécessaire pour respecter l'OMS
			BigDecimal surfaceParkNecessaire = BigDecimal.valueOf(totalPopAffected.doubleValue() * recoSquareMeterPerCapita);
			BigDecimal surfaceParkManquante = surfaceParkNecessaire.subtract(totalSurfaceParkAfter);
			
			if (surfaceParkManquante.compareTo(BigDecimal.ZERO) > 0) {
				log.warn("⚠️ Critères OMS NON respectés - Surface de parc manquante: {} m²", surfaceParkManquante);
				log.info("💡 Proposition pour respecter l'OMS: augmenter la surface de parc de {} m²", surfaceParkManquante);
			}
			
			// Proposer une population réduite basée sur la surface plancher disponible
			if (projectSimulator.getSurfaceFloor() != null && 
				projectSimulator.getAvgAreaAccommodation() != null && 
				projectSimulator.getDensityPerAccommodation() != null &&
				projectSimulator.getAvgAreaAccommodation().compareTo(BigDecimal.ZERO) > 0) {
				
				// Calculer la population optimale: (surface plancher / surface par logement) * densité par logement
				BigDecimal nbLogements = projectSimulator.getSurfaceFloor().divide(
						projectSimulator.getAvgAreaAccommodation(), 2, BigDecimal.ROUND_HALF_UP);
				BigDecimal populationOptimale = nbLogements.multiply(projectSimulator.getDensityPerAccommodation());
				
				// Calculer la surface de parc nécessaire pour cette population
				BigDecimal surfaceParkOptimale = BigDecimal.valueOf(
						(totalPopAffected.doubleValue() - populationOptimale.doubleValue() + populationOptimale.doubleValue()) * recoSquareMeterPerCapita)
						.subtract(totalSurfaceParkAvailable);
				
				if (surfaceParkOptimale.compareTo(BigDecimal.ZERO) > 0) {
					log.info("💡 Alternative: réduire la population du projet à {} habitants (au lieu de {})", 
							populationOptimale, projectSimulator.getProjetPeople());
					log.info("   Surface de parc nécessaire: {} m² pour {} logements de {} m² avec {} hab/logement",
							surfaceParkOptimale, nbLogements.intValue(), 
							projectSimulator.getAvgAreaAccommodation(), 
							projectSimulator.getDensityPerAccommodation());
				}
			}
		} else {
			log.info("✅ Critères OMS respectés après simulation du projet");
		}
		
		return projectSimulatorRepository.save(projectSimulator);
	}
	
	
	/**
	 * Prépare les données de proposition de parc pour une commune.
	 * 
	 * Cette méthode enrichit les données INSEE avec:
	 * - Les calculs de surfaces disponibles et manquantes
	 * - Les populations affectées
	 * - Les surfaces recommandées par l'OMS
	 * - Les données de population locale (Filosofil)
	 * 
	 * @param annee Année d'analyse (dernière année disponible généralement)
	 * @param insee Code INSEE de la commune
	 * @param dense Indicateur de densité urbaine (true = dense, false = sous-urbain)
	 * @param recoSquareMeterPerCapita Surface recommandée par habitant selon l'OMS (en m²/hab)
	 * @return Map des carrés INSEE avec leurs données de proposition (clé: idInspire)
	 */
	public Map<String, ParkProposalWork> populate(Integer annee, String insee, Boolean dense, Double recoSquareMeterPerCapita) {

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
				
				BigDecimal popAll =carreCputd.getPopAll();
				if (popAll==null) {
					popAll=BigDecimal.ZERO;
				}
				parkProposal.setMissingSurface(BigDecimal.valueOf(densiteMissing*popAll.doubleValue())); 
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
			log.info("Calcul des propositions pour {} carrés dans la commune {} en {} (dense={}): reco={} m²/hab", 
					carreMap.size(), insee, annee, dense, recoSquareMeterPerCapita);
		}
		
		return carreMap;
	}
}
