package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.rest.analytics.StatsSurfaceJson;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.service.analytics.NoStatsException;
import com.github.cunvoas.geoserviceisochrone.service.analytics.StatsSurfaceService;

/**
 * Restitution des statistiques de surface par habitant, au même format que les JSON publiés.
 * Lecture seule : les routes ne réécrivent aucun fichier.
 */
@RestController
@RequestMapping("/map/stats")
public class StatsController {

	private final StatsSurfaceService statsSurfaceService;
	private final CityRepository cityRepository;
	private final ApplicationBusinessProperties applicationBusinessProperties;

	@Autowired
	public StatsController(StatsSurfaceService statsSurfaceService, CityRepository cityRepository,
			ApplicationBusinessProperties applicationBusinessProperties) {
		this.statsSurfaceService = statsSurfaceService;
		this.cityRepository = cityRepository;
		this.applicationBusinessProperties = applicationBusinessProperties;
	}

	/**
	 * Statistiques d'une commune.
	 * @param insee code INSEE de la commune
	 * @param annee année, facultative (année de référence par défaut)
	 * @return statistiques, 404 si la commune est inconnue ou sans stats
	 */
	@CrossOrigin(origins = "${web.cors.allowed-origins}")
	@GetMapping("/city/{insee}")
	public ResponseEntity<StatsSurfaceJson> city(@PathVariable String insee,
			@RequestParam(required = false) Integer annee) {
		if (cityRepository.findByInseeCode(insee) == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(statsSurfaceService.getStatsSurfaceByInseeAndAnnee(insee, anneeOuDerniere(annee)));
	}

	/**
	 * Statistiques d'un EPCI pour un type de graphe.
	 * @param com2co identifiant de la communauté de communes
	 * @param graphType urbans, suburbs ou all
	 * @param annee année, facultative (année de référence par défaut)
	 * @return statistiques, 400 si le type est inconnu, 404 si l'EPCI est inconnu ou sans stats
	 */
	@CrossOrigin(origins = "${web.cors.allowed-origins}")
	@GetMapping("/com2co/{com2co}/{graphType}")
	public ResponseEntity<StatsSurfaceJson> com2co(@PathVariable Long com2co, @PathVariable String graphType,
			@RequestParam(required = false) Integer annee) {
		Integer target = anneeOuDerniere(annee);
		StatsSurfaceJson stats = switch (graphType) {
			case "urbans" -> statsSurfaceService.computeStatsSurfaceByCom2CoAndAnneeAllDense(target, com2co);
			case "suburbs" -> statsSurfaceService.computeStatsSurfaceByCom2CoAndAnneeAllSuburbs(target, com2co);
			case "all" -> statsSurfaceService.computeStatsSurfaceByCom2CoAndAnneeAllV2(target, com2co);
			default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "graphType inconnu : " + graphType);
		};
		if (stats == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(stats);
	}

	/**
	 * Commune ou EPCI sans ligne de statistiques pour l'année demandée.
	 * @param e exception levée par le service
	 * @return 404
	 */
	@ExceptionHandler(NoStatsException.class)
	public ResponseEntity<Void> sansStats(NoStatsException e) {
		return ResponseEntity.notFound().build();
	}

	private Integer anneeOuDerniere(Integer annee) {
		return annee != null ? annee : applicationBusinessProperties.getDerniereAnnee();
	}
}
