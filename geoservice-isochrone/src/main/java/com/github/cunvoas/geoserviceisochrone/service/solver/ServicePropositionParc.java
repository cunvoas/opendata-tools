package com.github.cunvoas.geoserviceisochrone.service.solver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalMeta;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalMetaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalWorkRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.AbstractComputationtrategy;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationStrategy;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationStrategyFactory;

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
	@Autowired
	private ParkProposalMetaRepository parkProposalMetaRepository;
	
	
	public void saveProposals(Map<String, ParkProposalWork> proposals) {
		if (proposals!=null && !proposals.isEmpty()) {
			List<ParkProposalWork> list = new ArrayList<>(proposals.values());
			parkProposalWorkRepository.saveAll(list);
		}
	}
	public void saveProposals(List<ParkProposal> proposals) {
		if (proposals!=null && !proposals.isEmpty()) {
			parkProposalRepository.saveAll(proposals);
		}
	}
	
	/**
	 * Fait une propostion avec une approche par carré.
	 * @param insee
	 * @param annee
	 */
	public Map<String, ParkProposalWork> calculeProposition(String insee, Integer annee, ProposalComputationStrategyFactory.Type typeAlgo) {
		log.warn("Calcul des propositions de parc pour la commune {} en {}", insee, annee);
		
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
			log.info("Calcul des propositions pour {} carrés dans la commune {} en {} (dense={}): reco={} m²/hab, min={} m²/hab, distance={} m", 
					carreMap.size(), insee, annee, dense, recoSquareMeterPerCapita, minSquareMeterPerCapita, urbanDistance);
		}
		
		ParkProposalMeta ppm  = parkProposalMetaRepository.findByAnneeAndInseeAndTypeAlgo(annee, insee, typeAlgo);
		if (ppm==null) {
			ppm = new ParkProposalMeta();
			ppm.setAnnee(annee);
			ppm.setInsee(insee);
			ppm.setTypeAlgo(typeAlgo);
			ppm = parkProposalMetaRepository.save(ppm);
		}

		List<ParkProposal> proposals = null;
		
		// ALGO chargé via factory
		ProposalComputationStrategy computation = ProposalComputationStrategyFactory.create(typeAlgo, AbstractComputationtrategy.MIN_PARK_SURFACE);
		proposals = computation.compute(carreMap, minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
		
		
		if (proposals!=null && !proposals.isEmpty()) {
			// affecte l'Id de la proposition
			for (ParkProposal pp : proposals) {
				pp.setIdMeta(ppm.getId());
			}
			ppm.setNumberOfParks(proposals.size());
			ppm.setTotalSurfaceOfParks(
					proposals.stream()
					.mapToInt(p -> p.getParkSurface().intValue())
					.sum()
				);
			
			parkProposalRepository.saveAll(proposals);
			parkProposalMetaRepository.save(ppm);
		} else {
			log.warn("Aucune proposition calculée pour la commune {} en {}", insee, annee);
		}
		return carreMap;
	}
	
	public List<ProposalComputationStrategyFactory.Type> getAvailableAlgorithms() {
		return ProposalComputationStrategyFactory.availableTypes;
	}
	
}
