package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.extern.ndvi.NdviStatistics;
import com.github.cunvoas.geoserviceisochrone.extern.ndvi.VegetationEvaluator;
import com.github.cunvoas.geoserviceisochrone.extern.ndvi.VegetationEvaluator.VegetationEvaluation;
import com.github.cunvoas.geoserviceisochrone.repo.NdviResultRepository;
import com.github.cunvoas.geoserviceisochrone.repo.NdviResultRepository.NdviSummaryProjection;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;

/**
 * Restitution des mesures NDVI par parc.
 */
@RestController
@RequestMapping("/map/ndvi")
public class NdviController {

	/** Une mesure NDVI de parc. */
	/**
	 * Résumé NDVI d'un parc : les quatre indices avec leur moyenne, min, max et écart-type,
	 * et l'évaluation de végétation qui en découle.
	 *
	 * @param parkId          identifiant du parc
	 * @param name            nom du parc
	 * @param surface         surface de la zone
	 * @param ndviMean        NDVI moyen
	 * @param ndviMin         NDVI minimal
	 * @param ndviMax         NDVI maximal
	 * @param ndviStddev      écart-type du NDVI
	 * @param ndreMean        NDRE moyen
	 * @param ndreMin         NDRE minimal
	 * @param ndreMax         NDRE maximal
	 * @param ndreStddev      écart-type du NDRE
	 * @param gndviMean       GNDVI moyen
	 * @param gndviMin        GNDVI minimal
	 * @param gndviMax        GNDVI maximal
	 * @param gndviStddev     écart-type du GNDVI
	 * @param saviMean        SAVI moyen
	 * @param saviMin         SAVI minimal
	 * @param saviMax         SAVI maximal
	 * @param saviStddev      écart-type du SAVI
	 * @param evaluation      évaluation de végétation (recouvrement, score, confiance, ...)
	 * @param acquisitionDate date d'acquisition de la mesure
	 * @param annee           année de calcul
	 */
	public record NdviSummary(String parkId, String name, BigDecimal surface,
			BigDecimal ndviMean, BigDecimal ndviMin, BigDecimal ndviMax, BigDecimal ndviStddev,
			BigDecimal ndreMean, BigDecimal ndreMin, BigDecimal ndreMax, BigDecimal ndreStddev,
			BigDecimal gndviMean, BigDecimal gndviMin, BigDecimal gndviMax, BigDecimal gndviStddev,
			BigDecimal saviMean, BigDecimal saviMin, BigDecimal saviMax, BigDecimal saviStddev,
			VegetationEvaluation evaluation,
			String acquisitionDate, Integer annee) {
	}

	/** Réponse de l'écran récapitulatif. */
	public record NdviSummaries(Integer annee, long total, long withNdvi, String latestAcquisitionDate,
			List<NdviSummary> parks) {
	}

	private final ParkAreaComputedRepository parkAreaComputedRepository;
	private final NdviResultRepository ndviResultRepository;
	private final ApplicationBusinessProperties applicationBusinessProperties;

	@Autowired
	public NdviController(ParkAreaComputedRepository parkAreaComputedRepository,
			NdviResultRepository ndviResultRepository,
			ApplicationBusinessProperties applicationBusinessProperties) {
		this.parkAreaComputedRepository = parkAreaComputedRepository;
		this.ndviResultRepository = ndviResultRepository;
		this.applicationBusinessProperties = applicationBusinessProperties;
	}

	/**
	 * Mesures NDVI des parcs pour une année (année de référence par défaut).
	 * @param annee année demandée, facultative
	 * @return summaries
	 */
	public NdviSummaries summaries(Integer annee) {
		return summaries(annee, null);
	}

	/**
	 * Mesures NDVI des parcs pour une année et, facultativement, un EPCI.
	 * @param annee année demandée, facultative
	 * @param com2co identifiant de la communauté de communes, facultatif
	 * @return summaries
	 */
	public NdviSummaries summaries(Integer annee, Long com2co) {
		return summaries(annee, com2co, null);
	}

	/**
	 * Mesures NDVI des parcs pour une année et, facultativement, un EPCI ou une commune
	 * (la commune prime sur l'EPCI).
	 * @param annee année demandée, facultative
	 * @param com2co identifiant de la communauté de communes, facultatif
	 * @param city code INSEE de la commune, facultatif
	 * @return summaries
	 */
	@CrossOrigin(origins = "${web.cors.allowed-origins}")
	@GetMapping("/summaries")
	public NdviSummaries summaries(@RequestParam(required = false) Integer annee,
			@RequestParam(required = false) Long com2co,
			@RequestParam(required = false) String city) {
		Integer target = annee != null ? annee : applicationBusinessProperties.getDerniereAnnee();
		List<NdviSummaryProjection> projections;
		long total;
		if (city != null) {
			projections = ndviResultRepository.findNdviSummariesByAnneeAndCity(target, city);
			total = parkAreaComputedRepository.countByAnneeAndCity(target, city);
		} else if (com2co != null) {
			projections = ndviResultRepository.findNdviSummariesByAnneeAndCom2co(target, com2co);
			total = parkAreaComputedRepository.countByAnneeAndCom2co(target, com2co);
		} else {
			projections = ndviResultRepository.findNdviSummariesByAnnee(target);
			total = parkAreaComputedRepository.countByAnnee(target);
		}
		List<NdviSummary> parks = projections.stream()
				.map(NdviController::toSummary)
				.toList();
		String latest = parks.stream()
				.map(NdviSummary::acquisitionDate)
				.filter(Objects::nonNull)
				.max(String::compareTo)
				.orElse(null);
		return new NdviSummaries(target, total, parks.size(), latest, parks);
	}

	static NdviSummary toSummary(NdviSummaryProjection projection) {
		return new NdviSummary(
				String.valueOf(projection.getParkId()),
				projection.getName(),
				projection.getSurface(),
				projection.getNdviMean(),
				projection.getNdviMin(),
				projection.getNdviMax(),
				projection.getNdviStddev(),
				projection.getNdreMean(),
				projection.getNdreMin(),
				projection.getNdreMax(),
				projection.getNdreStddev(),
				projection.getGndviMean(),
				projection.getGndviMin(),
				projection.getGndviMax(),
				projection.getGndviStddev(),
				projection.getSaviMean(),
				projection.getSaviMin(),
				projection.getSaviMax(),
				projection.getSaviStddev(),
				VegetationEvaluator.evaluate(toStatistics(projection)),
				formatDate(projection.getAcquisitionDate()),
				projection.getAnnee());
	}

	private static NdviStatistics toStatistics(NdviSummaryProjection projection) {
		return new NdviStatistics(
				index(projection.getNdviMean(), projection.getNdviMin(), projection.getNdviMax(), projection.getNdviStddev()),
				index(projection.getNdreMean(), projection.getNdreMin(), projection.getNdreMax(), projection.getNdreStddev()),
				index(projection.getGndviMean(), projection.getGndviMin(), projection.getGndviMax(), projection.getGndviStddev()),
				index(projection.getSaviMean(), projection.getSaviMin(), projection.getSaviMax(), projection.getSaviStddev()),
				null,
				null,
				projection.getSampleCount());
	}

	private static NdviStatistics.IndexStatistics index(BigDecimal mean, BigDecimal min, BigDecimal max, BigDecimal stdDev) {
		if (mean == null && min == null && max == null && stdDev == null) {
			return null;
		}
		return new NdviStatistics.IndexStatistics(mean, min, max, stdDev);
	}

	private static String formatDate(Date date) {
		if (date == null) {
			return null;
		}
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return localDate.toString();
	}
}
