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
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository.NdviSummaryProjection;

/**
 * Restitution des mesures NDVI par parc.
 */
@RestController
@RequestMapping("/map/ndvi")
public class NdviController {

	/** Une mesure NDVI de parc. */
	public record NdviSummary(String parkId, String name, BigDecimal surface,
			BigDecimal ndviMean, BigDecimal ndviMin, BigDecimal ndviMax, BigDecimal ndviStddev,
			String acquisitionDate, Integer annee) {
	}

	/** Réponse de l'écran récapitulatif. */
	public record NdviSummaries(Integer annee, long total, long withNdvi, String latestAcquisitionDate,
			List<NdviSummary> parks) {
	}

	private final ParkAreaComputedRepository parkAreaComputedRepository;
	private final ApplicationBusinessProperties applicationBusinessProperties;

	@Autowired
	public NdviController(ParkAreaComputedRepository parkAreaComputedRepository,
			ApplicationBusinessProperties applicationBusinessProperties) {
		this.parkAreaComputedRepository = parkAreaComputedRepository;
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
			projections = parkAreaComputedRepository.findNdviSummariesByAnneeAndCity(target, city);
			total = parkAreaComputedRepository.countByAnneeAndCity(target, city);
		} else if (com2co != null) {
			projections = parkAreaComputedRepository.findNdviSummariesByAnneeAndCom2co(target, com2co);
			total = parkAreaComputedRepository.countByAnneeAndCom2co(target, com2co);
		} else {
			projections = parkAreaComputedRepository.findNdviSummariesByAnnee(target);
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

	private static NdviSummary toSummary(NdviSummaryProjection projection) {
		return new NdviSummary(
				String.valueOf(projection.getParkId()),
				projection.getName(),
				projection.getSurface(),
				projection.getNdviMean(),
				projection.getNdviMin(),
				projection.getNdviMax(),
				projection.getNdviStddev(),
				formatDate(projection.getAcquisitionDate()),
				projection.getAnnee());
	}

	private static String formatDate(Date date) {
		if (date == null) {
			return null;
		}
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		return localDate.toString();
	}
}
