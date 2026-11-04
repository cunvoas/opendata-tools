package com.github.cunvoas.geoserviceisochrone.controller.mvc;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController.NdviSummaries;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;

/**
 * Liste des restitutions NDVI par EPCI.
 */
@Controller
@RequestMapping("/mvc/ndvi")
public class MvcNdviControler {

	/** Une zone géographique disposant de données NDVI. */
	public record NdviZone(Long id, String name, String regionName, long total, long withNdvi,
			String latestAcquisitionDate, String publicUrl) {
	}

	private final ParkAreaComputedRepository parkAreaComputedRepository;
	private final CommunauteCommuneRepository communauteCommuneRepository;
	private final NdviController ndviController;
	private final ApplicationBusinessProperties applicationBusinessProperties;

	public MvcNdviControler(ParkAreaComputedRepository parkAreaComputedRepository,
			CommunauteCommuneRepository communauteCommuneRepository,
			NdviController ndviController,
			ApplicationBusinessProperties applicationBusinessProperties) {
		this.parkAreaComputedRepository = parkAreaComputedRepository;
		this.communauteCommuneRepository = communauteCommuneRepository;
		this.ndviController = ndviController;
		this.applicationBusinessProperties = applicationBusinessProperties;
	}

	/**
	 * Affiche les cartes NDVI des EPCI disposant de parcs calculés.
	 * @param model modèle de la vue
	 * @return page des zones NDVI
	 */
	@GetMapping
	public String list(Model model) {
		Integer annee = applicationBusinessProperties.getDerniereAnnee();
		List<Long> ids = parkAreaComputedRepository.findCom2coIdsWithComputedParks();
		String publicBase = publicBase();
		List<NdviZone> zones = communauteCommuneRepository.findAllById(ids).stream()
				.sorted(Comparator
						.comparing((CommunauteCommune c) -> c.getRegion() == null ? "" : c.getRegion().getName())
						.thenComparing(CommunauteCommune::getName))
				.map(com2co -> toZone(com2co, annee, publicBase))
				.toList();
		model.addAttribute("zones", zones);
		model.addAttribute("annee", annee);
		return "listNdviZones";
	}

	private NdviZone toZone(CommunauteCommune com2co, Integer annee, String publicBase) {
		NdviSummaries summaries = ndviController.summaries(annee, com2co.getId());
		Region region = com2co.getRegion();
		// La région permet au sélecteur de localisation de la page publique d'afficher l'EPCI.
		String regionParam = region == null ? "" : "region=" + region.getId() + "&";
		String publicUrl = publicBase + "/ndvi?" + regionParam + "com2co=" + com2co.getId();
		String regionName = region == null ? null : region.getName();
		return new NdviZone(com2co.getId(), com2co.getName(), regionName,
				summaries.total(), summaries.withNdvi(), summaries.latestAcquisitionDate(), publicUrl);
	}

	private String publicBase() {
		String base = applicationBusinessProperties.getSitePublique();
		if (base == null) {
			return "";
		}
		return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
	}
}
