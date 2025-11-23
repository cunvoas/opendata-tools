package com.github.cunvoas.geoserviceisochrone.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.ComputeJobRequest;
import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.ComputeJobResponse;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.compute.BatchJobService;

import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour la gestion des jobs de calcul (isochrones, carreaux, etc.).
 * Permet de lancer des traitements batch sur une ville, une communauté de communes ou un parc.
 */
@RestController
@RequestMapping("/mvc/ajax/jobs/api")
@Slf4j
public class ComputeJobRestControler {

	@Autowired
	private TokenManagement tokenManagement;
	@Autowired
	private BatchJobService batchJobService;
	
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private CityRepository cityRepository;
	
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
		
		if (req.getParkId()!=null) {
			ParcEtJardin pj = new ParcEtJardin();
			pj.setId(Long.valueOf(req.getParkId()));
			resp.setParkId(req.getParkId());
			resp.setNbCarre(batchJobService.requestProcessParc(pj));
			code= HttpStatus.ACCEPTED;
			
		} else if (req.getCityId()!=null) {
			City n = cityRepository.getReferenceById(Long.valueOf(req.getCityId()));
			resp.setCityId(req.getCityId());
			resp.setNbCarre(batchJobService.requestProcessCity(n));
			code= HttpStatus.ACCEPTED;
			
		} else if (req.getCom2coId()!=null) {
			CommunauteCommune com2co = communauteCommuneRepository.getReferenceById(Long.valueOf(req.getCom2coId()));
			resp.setCom2coId(req.getCom2coId());
			resp.setNbCarre(batchJobService.requestProcessCom2Co(com2co));
			code= HttpStatus.ACCEPTED;
			
		} else {
			log.error("ComputeJobRequest without valid id: {}", req);
			code= HttpStatus.BAD_REQUEST;
		}
		
		return new ResponseEntity<ComputeJobResponse>(resp, code);
	}
}