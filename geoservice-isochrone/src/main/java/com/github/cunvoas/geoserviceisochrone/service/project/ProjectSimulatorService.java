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
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorWork;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorlWorkRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

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
	//	@Autowired
	//	private ServiceOpenData serviceOpenData;
	@Autowired
	private ServiceReadReferences serviceReadReferences;
	@Autowired
	private Filosofil200mRepository filosofil200mRepository;

	@Autowired
	private ProjectSimulatorRepository projectSimulatorRepository;
	@Autowired
	private ProjectSimulatorlWorkRepository projectSimulatorlWorkRepository;




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
	public ProjectSimulator save(ProjectSimulator projectSimulator, List<ProjectSimulatorWork> items) {

		projectSimulator = projectSimulatorRepository.save(projectSimulator);

		if (items!=null && !items.isEmpty()) {
			for (ProjectSimulatorWork item : items) {
				item.setIdProjectSimulator(projectSimulator.getId());
			}
			projectSimulatorlWorkRepository.saveAll(items);
		}

		return projectSimulator;
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
	 * Simule l'impact d'un projet d'aménagement sur les surfaces disponibles/hab de parc.
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

		City city = serviceReadReferences.getCity(projectSimulator.getIdCommune());
		String insee=city.getInseeCode();


		Boolean dense = serviceReadReferences.isCityDense(projectSimulator.getIdCommune());
		//		Boolean dense = serviceOpenData.isDistanceDense(insee);
		// Distance OMS selon densité
		Integer urbanDistance = Integer.valueOf(dense ? 
				applicationBusinessProperties.getOmsUrbanDistance() :
					applicationBusinessProperties.getOmsSubUrbanDistance());
		// Surface recommandée par habitant
		Double recoSquareMeterPerCapita = dense ?
				applicationBusinessProperties.getRecoUrbSquareMeterPerCapita() :
					applicationBusinessProperties.getRecoSubUrbSquareMeterPerCapita();

		// calcul seulement pour la dernière année
		Integer annee = applicationBusinessProperties.getDerniereAnnee();

		projectSimulator.setAnnee(annee);
		projectSimulator.setIsDense(dense);
		projectSimulator.setInsee(insee);

		// tous les carres de la ville avec les données nécessaires
		Map<String, ProjectSimulatorWork> carreMap = populate(projectSimulator, urbanDistance, recoSquareMeterPerCapita);


		// Calcul de la projection du projet sur les parcs disponibles
		// 1. Calculer la surface du projet de parc
		Long surfaceParkProjet = projectSimulator.getSurfacePark() != null ? 
				projectSimulator.getSurfacePark().longValue() : 0L;


		List<ProjectSimulatorWork> items = new ArrayList<>();

		// 2. Pour chaque carré impacté, reporter les métriques du projet
		for (Map.Entry<String, ProjectSimulatorWork> elt : carreMap.entrySet()) {
			ProjectSimulatorWork carre = elt.getValue();
			items.add(carre);

			BigDecimal popAccessing = carre.getAccessingPopulation() != null ? carre.getAccessingPopulation() : BigDecimal.ZERO;
			BigDecimal surfaceParkOms = carre.getAccessingSurface() != null ? carre.getAccessingSurface() : BigDecimal.ZERO;

			// Nouvelle surface disponible après ajout du parc (répartie sur tous les carrés)
			BigDecimal newSurfacePark = surfaceParkOms.add(BigDecimal.valueOf(surfaceParkProjet));

			// Nouvelle surface par habitant
			BigDecimal newSurfacePerCapita = popAccessing.compareTo(BigDecimal.ZERO) > 0 ?
					newSurfacePark.divide(popAccessing, 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

			// Surface manquante après le projet
			Double densiteMissingAfter = Math.max(recoSquareMeterPerCapita - newSurfacePerCapita.doubleValue(), 0);
			BigDecimal missingAfter = BigDecimal.valueOf(densiteMissingAfter * popAccessing.doubleValue());

			// Mettre à jour les champs du carré
			carre.setNewSurface(newSurfacePark);
			carre.setNewSurfacePerCapita(newSurfacePerCapita);
			carre.setNewMissingSurface(missingAfter);
		}

		// 3. Mettre à jour le ProjectSimulator avec les résultats
		return this.save(projectSimulator, items);
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
	 * @param projectSimulator Le projet de simulation avec insee, année et densité
	 * @param recoSquareMeterPerCapita Surface recommandée par habitant selon l'OMS (en m²/hab)
	 * @return Map des carrés INSEE avec leurs données de proposition (clé: idInspire)
	 */
	public Map<String, ProjectSimulatorWork> populate(ProjectSimulator projectSimulator, Integer urbanDistance, Double recoSquareMeterPerCapita) {

		Integer annee=projectSimulator.getAnnee();
		String insee=projectSimulator.getInsee();
		Boolean dense=projectSimulator.getIsDense();

		// Récupérer les carrés de la commune
		List<InseeCarre200mOnlyShape> carreShapes = inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(insee, true);

		// Récupérer les carrés directement impactés du projet
		List<InseeCarre200mOnlyShape> carreShapesProjet = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(projectSimulator.getShapeArea());

		// carres impactés aux distances OMS par le projet (voisins du projet)
		Set<InseeCarre200mOnlyShape> carreForSimulation = new HashSet<>();
		for (InseeCarre200mOnlyShape carreShapeProject : carreShapesProjet) {
			List<InseeCarre200mOnlyShape>  neighbors = ProjectSimulatorHelper.findNeighbors(carreShapeProject, carreShapes, urbanDistance);
			carreForSimulation.addAll(neighbors);
		}


		//préparation des données pour le calcul
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		for (InseeCarre200mOnlyShape shape : carreForSimulation) {
			Optional<InseeCarre200mComputedV2> oCarreCputd = inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(annee, shape.getIdInspire());
			if (oCarreCputd.isPresent()) {
				InseeCarre200mComputedV2 carreCputd = oCarreCputd.get();
				Filosofil200m filo = filosofil200mRepository.findByAnneeAndIdInspire(annee, shape.getIdInspire());

				ProjectSimulatorWork parkProposal = new ProjectSimulatorWork();
				parkProposal.setAnnee(annee);
				parkProposal.setIdInspire(shape.getIdInspire());
				parkProposal.setIdProjectSimulator(projectSimulator.getId());

				parkProposal.setGeoShape(shape.getGeoShape());
				parkProposal.setCentre(shape.getGeoPoint2d());
				parkProposal.setIsDense(dense);
				parkProposal.setSurfacePerCapita(carreCputd.getSurfaceParkPerCapitaOms());

				// ( Seuil OMS – MAX (0, surface disponible  - seuil OMS) ) * Nb Habitant qui ont accès
				Double densiteMissing = Math.max(recoSquareMeterPerCapita - carreCputd.getSurfaceParkPerCapitaOms().doubleValue(), 0);

				BigDecimal accessingPop =carreCputd.getPopulationInIsochroneOms();
				if (accessingPop==null) {
					accessingPop=BigDecimal.ZERO;
				}
				parkProposal.setAccessingPopulation(accessingPop);
				parkProposal.setAccessingSurface(carreCputd.getSurfaceTotalParkOms());
				parkProposal.setMissingSurface(BigDecimal.valueOf(densiteMissing*accessingPop.doubleValue())); 

				parkProposal.setLocalPopulation(carreCputd.getPopAll()!=null?carreCputd.getPopAll():BigDecimal.ZERO);
				
				// Initialiser les valeurs "nouvelles" avec les valeurs actuelles
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

	/**
	 * Récupère les travaux de simulation associés à un projet.
	 * 
	 * @param projectId Identifiant du projet de simulation
	 * @return Liste des travaux de simulation pour ce projet
	 */
	public List<ProjectSimulatorWork> getProjectWorks(Long projectId) {

		return projectSimulatorlWorkRepository.findByIdProjectSimulator(projectId);
	}
}
