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
import com.github.cunvoas.geoserviceisochrone.service.export.ServicePublicationExporter;

import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour la gestion des jobs de calcul (isochrones, carreaux, etc.).
 * Permet de lancer des traitements batch sur une ville, une communauté de communes ou un parc.
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
	
	/**
	 * Lance un job de calcul en fonction des paramètres fournis (parc, ville ou communauté de communes).
	 *
	 * @param req Requête contenant les identifiants nécessaires (parc, ville, communauté de communes)
	 * @return Réponse contenant le nombre de carreaux traités et le statut HTTP
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
				
				// mettre ce bloc traitement dans une future task
				try {
					servicePublicationExporter.writeGeoJsonCarreaux(com2co, requestedYear);
					statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllDense(requestedYear, c2cId);
					statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllSuburbs(requestedYear, c2cId);
					statsSurfaceService.getStatsSurfaceByCom2CoAndAnneeAllV2(requestedYear, c2cId);
				} catch (Exception e) {
					log.error("Publish exception", e);
				}
			}
			
			
			code= HttpStatus.ACCEPTED;
			
		} else {
			log.error("ComputeJobRequest without valid id: {}", req);
			code= HttpStatus.BAD_REQUEST;
		}
		
		return new ResponseEntity<ComputeJobResponse>(resp, code);
	}
}