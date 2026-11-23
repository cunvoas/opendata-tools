package com.github.cunvoas.geoserviceisochrone.controller.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.mvc.validator.TokenManagement;
import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.ComputeJobRequest;
import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.ComputeJobResponse;
import com.github.cunvoas.geoserviceisochrone.controller.rest.bo.PublicationCommitRequest;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobProgressStat;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurRole;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;
import com.github.cunvoas.geoserviceisochrone.service.admin.AccessScopeService;
import com.github.cunvoas.geoserviceisochrone.service.compute.BatchJobService;
import com.github.cunvoas.geoserviceisochrone.service.export.PublishService;
import com.github.cunvoas.geoserviceisochrone.service.export.ServicePublicationExporter;
import com.github.cunvoas.geoserviceisochrone.service.export.dashboard.ExportDashboardService;
import com.github.cunvoas.geoserviceisochrone.util.LogSanitizer;
import com.github.cunvoas.geoserviceisochrone.util.NumberHelper;

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
	@Autowired
	private ExportDashboardService exportDashboardService;
	@Autowired
	private AccessScopeService accessScopeService;
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
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
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','ASSO_MANAGER')")
	public ResponseEntity<ComputeJobResponse> request(@RequestBody ComputeJobRequest req) {
		HttpStatus code= HttpStatus.OK;
		
		log.warn("ComputeJobRequest received: {}", LogSanitizer.sanitize(req));
		
		boolean isValid = tokenManagement.isTokenValid(req.getToken());
		if (!isValid) {
			log.error("Invalid token for ComputeJobRequest: {}", LogSanitizer.sanitize(req));
			return new ResponseEntity<ComputeJobResponse>(HttpStatus.UNAUTHORIZED);
		}
		
		ComputeJobResponse resp = new ComputeJobResponse(req);
		
		Integer requestedYear=null;
		if (req.getRequestedYear()!=null ) {
			requestedYear = req.getRequestedYear();
		}
		if (!isYearAllowed(requestedYear)) {
			resp.setCanPublish(false);
			resp.setMessage("Année de publication obligatoire et limitée aux millésimes configurés.");
			return new ResponseEntity<ComputeJobResponse>(resp, HttpStatus.BAD_REQUEST);
		}
		
		if (req.getCom2coId()!=null) {
			Long c2cId = NumberHelper.toLong(req.getCom2coId());
			if (c2cId == null) {
				return new ResponseEntity<ComputeJobResponse>(HttpStatus.BAD_REQUEST);
			}
			if (!accessScopeService.canAccessEpci(accessScopeService.current(), c2cId)) {
				return new ResponseEntity<ComputeJobResponse>(resp, HttpStatus.FORBIDDEN);
			}
			resp.setCom2coId(req.getCom2coId());

			String refusSiren = refusSiren(c2cId);
			if (refusSiren != null) {
				resp.setCanPublish(false);
				resp.setMessage(refusSiren);
				return new ResponseEntity<ComputeJobResponse>(resp, HttpStatus.PRECONDITION_FAILED);
			}
			
			List<ComputeJobProgressStat> stats =  batchJobService.getProgressStatsEpciLevel(
					c2cId, null, requestedYear);
			if (stats!=null && stats.size()==1) {
				ComputeJobProgressStat stat = stats.get(0);
				resp.setNbCarre(stat.getProcessed().intValue());
				
				// Vérification que tout est complet (aucun en cours, aucun en erreur, aucun à traiter)
				Boolean canPublish = this.canPublish(stat);
				resp.setCanPublish(canPublish);
				
				if (canPublish) {
					if (publishService.isRunning(c2cId, requestedYear)) {
						resp.setCanPublish(false);
						resp.setMessage("Publication déjà en cours pour cet EPCI et cette année.");
						return new ResponseEntity<ComputeJobResponse>(resp, HttpStatus.CONFLICT);
					}
					// Création synchrone du statut : le premier /status ne renvoie pas null.
					publishService.prepare(c2cId, requestedYear);
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
			log.error("ComputeJobRequest without valid id: {}", LogSanitizer.sanitize(req));
			code= HttpStatus.BAD_REQUEST;
		}
		
		return new ResponseEntity<ComputeJobResponse>(resp, code);
	}

	/**
	 * Publie le référentiel (régions, EPCI, villes, index des territoires), sans passage par un EPCI.
	 * Réservé aux administrateurs. Traitement asynchrone.
	 *
	 * @param req requête contenant le token
	 * @return 202 si la publication est lancée, 401 ou 403 sinon
	 */
	@PostMapping("/referential")
	@PreAuthorize("hasAuthority('ADMINISTRATOR')")
	public ResponseEntity<ComputeJobResponse> referential(@RequestBody ComputeJobRequest req) {
		if (!tokenManagement.isTokenValid(req.getToken())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Contributeur contrib = accessScopeService.current();
		if (!accessScopeService.isAdmin(contrib)) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		publishService.publishReferentialAsync();
		return new ResponseEntity<>(new ComputeJobResponse(req), HttpStatus.ACCEPTED);
	}

	/**
	 * Commit final unique des fichiers d'export validés dans le dashboard.
	 * @param req token, hash du snapshot et message optionnel
	 * @return SHA du commit et état du push
	 */
	@PostMapping("/commit")
	@PreAuthorize("hasAnyAuthority('ADMINISTRATOR','ASSO_MANAGER')")
	public ResponseEntity<?> commit(@RequestBody PublicationCommitRequest req) {
		if (req == null || !tokenManagement.isTokenValid(req.getToken())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Contributeur contrib = accessScopeService.current();
		if (!accessScopeService.isAdmin(contrib) && !accessScopeService.isManager(contrib)) {
			return new ResponseEntity<>(Map.of("message", "Rôle insuffisant pour commiter les exports."),
					HttpStatus.FORBIDDEN);
		}
		Long scope = accessScopeService.scopedEpciId(contrib);
		if (accessScopeService.isManager(contrib) && scope == null) {
			return new ResponseEntity<>(Map.of("message", "Aucun EPCI associé à ce compte gestionnaire."),
					HttpStatus.FORBIDDEN);
		}
		try {
			return ResponseEntity.ok(exportDashboardService.commit(req.getSnapshotHash(), req.getMessage(), scope));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
		}
	}

	/**
	 * Vérifie le statut de publication pour une communauté de communes et une année.
	 * 
	 * @param req Requête contenant le token, l'id de la com2co et l'année
	 * @return Réponse contenant les statistiques et si la publication est possible
	 */
	@PostMapping("/status")
	@PreAuthorize("hasAnyAuthority('ADMINISTRATOR','ASSO_MANAGER')")
	public ResponseEntity<ComputeJobResponse> status(@RequestBody ComputeJobRequest req) {
		boolean isValid = tokenManagement.isTokenValid(req.getToken());
		if (!isValid) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		ComputeJobResponse resp = new ComputeJobResponse(req);
		if (req.getCom2coId() != null) {
			Long c2cId = NumberHelper.toLong(req.getCom2coId());
			if (c2cId == null) {
				return new ResponseEntity<ComputeJobResponse>(HttpStatus.BAD_REQUEST);
			}
			if (!accessScopeService.canAccessEpci(accessScopeService.current(), c2cId)) {
				return new ResponseEntity<ComputeJobResponse>(resp, HttpStatus.FORBIDDEN);
			}
			List<ComputeJobProgressStat> stats = batchJobService.getProgressStatsEpciLevel(
					c2cId, null, req.getRequestedYear());
			
			if (stats != null && stats.size() == 1) {
				ComputeJobProgressStat stat = stats.get(0);
				resp.setNbCarre(stat.getProcessed().intValue());
				resp.setCanPublish(this.canPublish(stat));
			}
			String refusSiren = refusSiren(c2cId);
			if (refusSiren != null) {
				resp.setCanPublish(false);
				resp.setMessage(refusSiren);
			}
			resp.setPublication(publishService.getStatus(c2cId, req.getRequestedYear()));
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

	private boolean isYearAllowed(Integer year) {
		Integer[] years = applicationBusinessProperties.getInseeAnnees();
		return year != null && years != null && Arrays.stream(years).anyMatch(year::equals);
	}

	/**
	 * Refuse la publication d'un EPCI sans numéro Siren, en nommant l'intercommunalité.
	 * @param c2cId identifiant de l'EPCI
	 * @return message de refus, null si la publication est autorisée
	 */
	protected String refusSiren(Long c2cId) {
		CommunauteCommune com2co = communauteCommuneRepository.findById(c2cId).orElse(null);
		if (com2co == null) {
			return "Intercommunalité introuvable (id " + c2cId + ").";
		}
		if (!StringUtils.hasText(com2co.getSiren())) {
			return "Publication refusée : l'intercommunalité " + com2co.getName() + " n'a pas de numéro Siren.";
		}
		return null;
	}
}
