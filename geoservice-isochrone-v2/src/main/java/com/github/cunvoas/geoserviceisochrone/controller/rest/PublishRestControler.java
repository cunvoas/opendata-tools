package com.github.cunvoas.geoserviceisochrone.controller.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.ComputeJobRequest;
import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.ComputeJobResponse;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobProgressStat;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;
import com.github.cunvoas.geoserviceisochrone.service.compute.BatchJobService;
import com.github.cunvoas.geoserviceisochrone.service.export.PublishService;
import com.github.cunvoas.geoserviceisochrone.service.export.ServicePublicationExporter;

import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour la gestion des jobs de publication.
 * Permet de lancer des traitements d'exportation de données et de génération de statistiques.
 * Les traitements sont exécutés de manière asynchrone pour ne pas bloquer l'appelant.
 */
@RestController
@RequestMapping("/mvc/ajax/publish/api")
@Slf4j
public class PublishRestControler {

	@Autowired
	private TokenManagement tokenManagement;

	@Autowired
	private ServicePublicationExporter servicePublicationExporter;
	@Autowired
	private StatsSurfaceService statsSurfaceService;
	@Autowired
	private BatchJobService batchJobService;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private PublishService publishService;
	
	/**
	 * Lance un job de publication en arrière-plan pour une communauté de communes.
	 * 
	 * Le traitement est asynchrone : si la requête est valide, elle renvoie un statut 202 (Accepted)
	 * et le travail continue en tâche de fond.
	 *
	 * @param req Requête contenant le token de sécurité, l'id de la communauté de communes (com2coId) et l'année (requestedYear)
	 * @return Réponse avec le statut HTTP 202 si lancé, ou 401/400 en cas d'erreur
	 */
	@PostMapping("/request")
	public ResponseEntity<ComputeJobResponse> request(@RequestBody ComputeJobRequest req) {
		HttpStatus code= HttpStatus.OK;
		
		log.warn("ComputeJobRequest received: {}", req);
		
		Boolean isValid = tokenManagement.isTokenValid(req.getToken());
		if (!isValid) {
			log.error("Invalid token for ComputeJobRequest: {}", req);
			return new ResponseEntity<ComputeJobResponse>(HttpStatus.UNAUTHORIZED);
		}
		
		ComputeJobResponse resp = new ComputeJobResponse(req);
		
		Integer requestedYear=null;
		if (req.getRequestedYear()!=null ) {
			requestedYear = req.getRequestedYear();
		}
		
		if (req.getCom2coId()!=null) {
			Long c2cId = Long.valueOf(req.getCom2coId());
			CommunauteCommune com2co = communauteCommuneRepository.getReferenceById(c2cId);
			resp.setCom2coId(req.getCom2coId());
			
			List<ComputeJobProgressStat> stats =  batchJobService.getProgressStatsEpciLevel(
					c2cId, null, requestedYear);
			if (stats!=null && stats.size()==1) {
				resp.setNbCarre(stats.get(0).getProcessed().intValue());
				
				// Lancement du traitement de publication en asynchrone
				publishService.publishAsync(com2co, requestedYear);
			}
			
			
			code= HttpStatus.ACCEPTED;
			
		} else {
			log.error("ComputeJobRequest without valid id: {}", req);
			code= HttpStatus.BAD_REQUEST;
		}
		
		return new ResponseEntity<ComputeJobResponse>(resp, code);
	}
}