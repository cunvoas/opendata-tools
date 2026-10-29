package com.github.cunvoas.geoserviceisochrone.controller.rest;

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
@RequestMapping("/ndvi")
public class NdviRestControler {

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
	public NdviRestControler(ParkAreaComputedRepository parkAreaComputedRepository,
			ApplicationBusinessProperties applicationBusinessProperties) {
		this.parkAreaComputedRepository = parkAreaComputedRepository;
		this.applicationBusinessProperties = applicationBusinessProperties;
	}

	/**
	 * Mesures NDVI des parcs pour une année (année de référence par défaut).
	 * @param annee année demandée, facultative
	 * @return summaries
	 */
	@CrossOrigin(origins = "${web.cors.allowed-origins}")
	@GetMapping("/summaries")
	public NdviSummaries summaries(@RequestParam(required = false) Integer annee) {
		Integer target = annee != null ? annee : applicationBusinessProperties.getDerniereAnnee();
		List<NdviSummary> parks = parkAreaComputedRepository.findNdviSummariesByAnnee(target).stream()
				.map(NdviRestControler::toSummary)
				.toList();
		String latest = parks.stream()
				.map(NdviSummary::acquisitionDate)
				.filter(Objects::nonNull)
				.max(String::compareTo)
				.orElse(null);
		return new NdviSummaries(target, parkAreaComputedRepository.countByAnnee(target), parks.size(), latest, parks);
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
