package com.github.cunvoas.geoserviceisochrone.controller.rest;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.admin.AccessScopeService;
import com.github.cunvoas.geoserviceisochrone.service.compute.BatchJobService;
import com.github.cunvoas.geoserviceisochrone.util.LogSanitizer;
import com.github.cunvoas.geoserviceisochrone.util.NumberHelper;

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
	@Autowired
	private ParkJardinRepository parkJardinRepository;
	@Autowired
	private AccessScopeService accessScopeService;

	@PostMapping("/request")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','ASSO_MANAGER')")
	public ResponseEntity<ComputeJobResponse> request(@RequestBody ComputeJobRequest req) {		HttpStatus code = HttpStatus.OK;

		log.warn("ComputeJobRequest received: {}", LogSanitizer.sanitize(req));

		boolean isValid = tokenManagement.isTokenValid(req.getToken());
		if (!isValid) {
			log.error("Invalid token for ComputeJobRequest: {}", LogSanitizer.sanitize(req));
			return new ResponseEntity<ComputeJobResponse>(HttpStatus.UNAUTHORIZED);
		}
		Contributeur contrib = (Contributeur) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		ComputeJobResponse resp = new ComputeJobResponse(req);

		int scopes = (req.getParkId() != null ? 1 : 0)
				+ (req.getParkOverpass() != null ? 1 : 0)
				+ (req.getCityId() != null ? 1 : 0)
				+ (req.getCom2coId() != null ? 1 : 0);
		if (scopes != 1) {
			log.error("ComputeJobRequest with {} scope(s): {}", scopes, LogSanitizer.sanitize(req));
			return new ResponseEntity<ComputeJobResponse>(HttpStatus.BAD_REQUEST);
		}
		if (req.getRequestedYear() != null && !isYearAllowed(req.getRequestedYear())) {
			return new ResponseEntity<ComputeJobResponse>(HttpStatus.BAD_REQUEST);
		}

		Integer requestedYear = null;
		if (req.getRequestedYear() != null) {
			requestedYear = req.getRequestedYear();
		}

		if (req.getParkId() != null) {
			Long parkId = NumberHelper.toLong(req.getParkId());
			if (parkId == null) {
				return new ResponseEntity<ComputeJobResponse>(HttpStatus.BAD_REQUEST);
			}
			ParcEtJardin pj = parkJardinRepository.findById(parkId).orElse(null);
			if (pj == null) {
				return new ResponseEntity<ComputeJobResponse>(HttpStatus.NOT_FOUND);
			}
			if (!canAccessPark(contrib, pj)) {
				return new ResponseEntity<ComputeJobResponse>(resp, HttpStatus.FORBIDDEN);
			}
			resp.setParkId(req.getParkId());
			resp.setNbCarre(batchJobService.requestProcessParc(pj));
			code = HttpStatus.ACCEPTED;
		} else if (req.getParkOverpass() != null) {
			if (!accessScopeService.isAdmin(contrib)) {
				return new ResponseEntity<ComputeJobResponse>(resp, HttpStatus.FORBIDDEN);
			}
			// TODO OVERPASS
			resp.setParkOverpass(req.getParkOverpass());
			resp.setNbCarre(0);
			code = HttpStatus.ACCEPTED;
		} else if (req.getCityId() != null) {
			Long cityId = NumberHelper.toLong(req.getCityId());
			if (cityId == null) {
				return new ResponseEntity<ComputeJobResponse>(HttpStatus.BAD_REQUEST);
			}
			City city = cityRepository.findById(cityId).orElse(null);
			if (city == null) {
				return new ResponseEntity<ComputeJobResponse>(HttpStatus.NOT_FOUND);
			}
			if (!canAccessCity(contrib, city)) {
				return new ResponseEntity<ComputeJobResponse>(resp, HttpStatus.FORBIDDEN);
			}
			resp.setCityId(req.getCityId());
			resp.setNbCarre(batchJobService.requestProcessCity(city, requestedYear));
			code = HttpStatus.ACCEPTED;
		} else if (req.getCom2coId() != null) {
			Long com2coId = NumberHelper.toLong(req.getCom2coId());
			if (com2coId == null) {
				return new ResponseEntity<ComputeJobResponse>(HttpStatus.BAD_REQUEST);
			}
			CommunauteCommune com2co = communauteCommuneRepository.findById(com2coId).orElse(null);
			if (com2co == null) {
				return new ResponseEntity<ComputeJobResponse>(HttpStatus.NOT_FOUND);
			}
			if (!accessScopeService.canAccessEpci(contrib, com2coId)) {
				return new ResponseEntity<ComputeJobResponse>(resp, HttpStatus.FORBIDDEN);
			}
			resp.setCom2coId(req.getCom2coId());
			resp.setNbCarre(batchJobService.requestProcessCom2Co(com2co, requestedYear));
			code = HttpStatus.ACCEPTED;
		} else {
			log.error("ComputeJobRequest without valid id: {}", LogSanitizer.sanitize(req));
			code = HttpStatus.BAD_REQUEST;
		}

		return new ResponseEntity<ComputeJobResponse>(resp, code);
	}

	private boolean canAccessCity(Contributeur contrib, City city) {
		if (city == null || city.getCommunauteCommune() == null) {
			return accessScopeService.isAdmin(contrib);
		}
		return accessScopeService.canAccessEpci(contrib, city.getCommunauteCommune().getId());
	}

	private boolean canAccessPark(Contributeur contrib, ParcEtJardin park) {
		if (park == null || park.getCommune() == null) {
			return accessScopeService.isAdmin(contrib);
		}
		return canAccessCity(contrib, park.getCommune());
	}

	private boolean isYearAllowed(Integer year) {
		Integer[] years = applicationBusinessProperties.getInseeAnnees();
		return year != null && years != null && Arrays.stream(years).anyMatch(year::equals);
	}

	/**
	 * Relance forcée de l'appel Copernicus pour tous les parcs d'un EPCI ou d'une ville.
	 * Un gestionnaire d'association est limité à son EPCI.
	 * @param req demande (token, com2coId ou cityId, requestedYear facultatif)
	 * @return nombre de parcs relancés
	 */
	@PostMapping("/ndvi/request")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','ASSO_MANAGER')")
	public ResponseEntity<ComputeJobResponse> requestNdvi(@RequestBody ComputeJobRequest req) {
		if (!tokenManagement.isTokenValid(req.getToken())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Contributeur contrib = accessScopeService.current();
		Long cityId = NumberHelper.toLong(req.getCityId());
		Long com2coId = NumberHelper.toLong(req.getCom2coId());
		if (cityId == null && com2coId == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		if (cityId != null) {
			City city = cityRepository.findById(cityId).orElse(null);
			if (city == null) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			if (!canAccessCity(contrib, city)) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
		}
		if (com2coId != null && !accessScopeService.canAccessEpci(contrib, com2coId)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Integer annee = req.getRequestedYear() != null
				? req.getRequestedYear()
				: applicationBusinessProperties.getDerniereAnnee();
		ComputeJobResponse resp = new ComputeJobResponse(req);
		if (req.isIris()) {
			resp.setNbIris(cityId != null
					? ndviBatchJobService.requestIrisForCity(cityId)
					: ndviBatchJobService.requestIrisForCom2Co(com2coId));
			return new ResponseEntity<>(resp, HttpStatus.ACCEPTED);
		}
		resp.setNbCarre(cityId != null
				? ndviBatchJobService.requestForcedForCity(cityId, annee)
				: ndviBatchJobService.requestForcedForCom2Co(com2coId, annee));
		return new ResponseEntity<>(resp, HttpStatus.ACCEPTED);
	}

	/**
	 * Demande un calcul NDVI (sans force) pour un parc.
	 */
	@PostMapping("/ndvi/request/park")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','ASSO_MANAGER')")
	public ResponseEntity<ComputeJobResponse> requestNdviForPark(@RequestBody ComputeJobRequest req) {
		if (!tokenManagement.isTokenValid(req.getToken())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Long parkId = NumberHelper.toLong(req.getParkId());
		if (parkId == null) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		ParcEtJardin park = parkJardinRepository.findById(parkId).orElse(null);
		if (park == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!canAccessPark(accessScopeService.current(), park)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		ComputeJobResponse resp = new ComputeJobResponse(req);
		resp.setParkId(req.getParkId());
		resp.setNbCarre(ndviBatchJobService.requestForParc(parkId) ? 1 : 0);
		return new ResponseEntity<>(resp, HttpStatus.ACCEPTED);
	}
}
