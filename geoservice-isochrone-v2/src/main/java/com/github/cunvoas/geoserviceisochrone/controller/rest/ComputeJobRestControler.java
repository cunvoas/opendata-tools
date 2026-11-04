package com.github.cunvoas.geoserviceisochrone.controller.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.ComputeJobRequest;
import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.ComputeJobResponse;
import com.github.cunvoas.geoserviceisochrone.extern.ndvi.NdviBatchJobService;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.compute.BatchJobService;
import com.github.cunvoas.geoserviceisochrone.util.LogSanitizer;

@RestController
@RequestMapping("/mvc/ajax/jobs/api")
public class ComputeJobRestControler {

	private static final Logger log = LoggerFactory.getLogger(ComputeJobRestControler.class);

	@Autowired
	private TokenManagement tokenManagement;
	@Autowired
	private BatchJobService batchJobService;
	@Autowired
	private NdviBatchJobService ndviBatchJobService;
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;

	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private CityRepository cityRepository;

	@PostMapping("/request")
	public ResponseEntity<ComputeJobResponse> request(@RequestBody ComputeJobRequest req) {
		HttpStatus code = HttpStatus.OK;

		log.warn("ComputeJobRequest received: {}", LogSanitizer.sanitize(req));

		Boolean isValid = tokenManagement.isTokenValid(req.getToken());
		if (!isValid) {
			log.error("Invalid token for ComputeJobRequest: {}", LogSanitizer.sanitize(req));
			return new ResponseEntity<ComputeJobResponse>(HttpStatus.UNAUTHORIZED);
		}
		Contributeur contrib = (Contributeur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		ComputeJobResponse resp = new ComputeJobResponse(req);

		Integer requestedYear = null;
		if (req.getRequestedYear() != null) {
			requestedYear = req.getRequestedYear();
		}

		if (req.getParkId() != null) {
			ParcEtJardin pj = new ParcEtJardin();
			pj.setId(Long.valueOf(req.getParkId()));
			resp.setParkId(req.getParkId());
			resp.setNbCarre(batchJobService.requestProcessParc(pj));
			// Le bouton « Calculer le parc » demande aussi le NDVI de l'année de référence.
			ndviBatchJobService.requestForParc(pj.getId(), applicationBusinessProperties.getDerniereAnnee());
			code = HttpStatus.ACCEPTED;
		} else if (req.getParkOverpass() != null) {
			// TODO OVERPASS
			resp.setParkOverpass(req.getParkOverpass());
//			batchJobService.processParkOverpass();
			resp.setNbCarre(0);
			code = HttpStatus.ACCEPTED;
		} else if (req.getCityId() != null && ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			City n = cityRepository.getReferenceById(Long.valueOf(req.getCityId()));
			resp.setCityId(req.getCityId());
			resp.setNbCarre(batchJobService.requestProcessCity(n, requestedYear));
			code = HttpStatus.ACCEPTED;
		} else if (req.getCom2coId() != null && ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			CommunauteCommune com2co = communauteCommuneRepository.getReferenceById(Long.valueOf(req.getCom2coId()));
			resp.setCom2coId(req.getCom2coId());
			resp.setNbCarre(batchJobService.requestProcessCom2Co(com2co, requestedYear));
			code = HttpStatus.ACCEPTED;
		} else if (! ContributeurRole.ADMINISTRATOR.equals(contrib.getRole())) {
			resp.setCom2coId(req.getCom2coId());
			resp.setCityId(req.getCityId());
			resp.setNbCarre(0);
			code = HttpStatus.FORBIDDEN;
		} else {
			log.error("ComputeJobRequest without valid id: {}", LogSanitizer.sanitize(req));
			code = HttpStatus.BAD_REQUEST;
		}

		return new ResponseEntity<ComputeJobResponse>(resp, code);
	}
}
