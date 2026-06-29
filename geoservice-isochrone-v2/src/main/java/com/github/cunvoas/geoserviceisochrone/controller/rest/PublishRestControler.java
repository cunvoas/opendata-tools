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
			resp.setCom2coId(req.getCom2coId());
			
			List<ComputeJobProgressStat> stats =  batchJobService.getProgressStatsEpciLevel(
					c2cId, null, requestedYear);
			if (stats!=null && stats.size()==1) {
				ComputeJobProgressStat stat = stats.get(0);
				resp.setNbCarre(stat.getProcessed().intValue());
				
				// Vérification que tout est complet (aucun en cours, aucun en erreur, aucun à traiter)
				Boolean canPublish = this.canPublish(stat);
				resp.setCanPublish(canPublish);
				
				if (canPublish) {
					// Lancement du traitement de publication en asynchrone via l'ID
					publishService.publishAsync(c2cId, requestedYear);
					code= HttpStatus.ACCEPTED;
				} else {
					log.warn("Publication request ignored: jobs not complete for com2co {} and year {}", c2cId, requestedYear);
					code= HttpStatus.PRECONDITION_FAILED;
				}
			} else {
				code = HttpStatus.NOT_FOUND;
			}
			
		} else {
			log.error("ComputeJobRequest without valid id: {}", req);
			code= HttpStatus.BAD_REQUEST;
		}
		
		return new ResponseEntity<ComputeJobResponse>(resp, code);
	}

	/**
	 * Vérifie le statut de publication pour une communauté de communes et une année.
	 * 
	 * @param req Requête contenant le token, l'id de la com2co et l'année
	 * @return Réponse contenant les statistiques et si la publication est possible
	 */
	@PostMapping("/status")
	public ResponseEntity<ComputeJobResponse> status(@RequestBody ComputeJobRequest req) {
		Boolean isValid = tokenManagement.isTokenValid(req.getToken());
		if (!isValid) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		ComputeJobResponse resp = new ComputeJobResponse(req);
		if (req.getCom2coId() != null) {
			Long c2cId = Long.valueOf(req.getCom2coId());
			List<ComputeJobProgressStat> stats = batchJobService.getProgressStatsEpciLevel(
					c2cId, null, req.getRequestedYear());
			
			if (stats != null && stats.size() == 1) {
				ComputeJobProgressStat stat = stats.get(0);
				resp.setNbCarre(stat.getProcessed().intValue());
				resp.setCanPublish(this.canPublish(stat));
			}
			return new ResponseEntity<>(resp, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	/**
	 * Vérification que tout est complet (aucun en cours, aucun en erreur, aucun à traiter)
	 * @param stat
	 * @return
	 */
	protected Boolean canPublish(ComputeJobProgressStat stat) {
		boolean canPublish = stat.getToProcess() == 0 && stat.getInProcess() == 0 && stat.getInError() == 0 && stat.getProcessed() > 0;
		return canPublish;
		
	}
}