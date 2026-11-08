package com.github.cunvoas.geoserviceisochrone.controller.mvc;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController;
import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController.NdviSummaries;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

/**
 * Liste des restitutions NDVI par territoire (région, EPCI ou commune).
 */
@Controller
@RequestMapping("/mvc/ndvi")
public class MvcNdviControler {

	/** Une zone géographique disposant de données NDVI. */
	public record NdviZone(Long id, String name, String regionName, long total, long withNdvi,
			String latestAcquisitionDate, String publicUrl, boolean city) {
	}

	private final ParkAreaComputedRepository parkAreaComputedRepository;
	private final CommunauteCommuneRepository communauteCommuneRepository;
	private final NdviController ndviController;
	private final ApplicationBusinessProperties applicationBusinessProperties;
	private final ServiceReadReferences serviceReadReferences;

	public MvcNdviControler(ParkAreaComputedRepository parkAreaComputedRepository,
			CommunauteCommuneRepository communauteCommuneRepository,
			NdviController ndviController,
			ApplicationBusinessProperties applicationBusinessProperties,
			ServiceReadReferences serviceReadReferences) {
		this.parkAreaComputedRepository = parkAreaComputedRepository;
		this.communauteCommuneRepository = communauteCommuneRepository;
		this.ndviController = ndviController;
		this.applicationBusinessProperties = applicationBusinessProperties;
		this.serviceReadReferences = serviceReadReferences;
	}

	/**
	 * Affiche les cartes NDVI du territoire sélectionné.
	 * @param idRegion région sélectionnée, facultative
	 * @param idCommunauteDeCommunes EPCI sélectionné, facultatif
	 * @param idCommune commune sélectionnée, facultative
	 * @param model modèle de la vue
	 * @return page des zones NDVI
	 */
	@GetMapping
	public String list(@RequestParam(name = "idRegion", required = false) Long idRegion,
			@RequestParam(name = "idCommunauteDeCommunes", required = false) Long idCommunauteDeCommunes,
			@RequestParam(name = "idCommune", required = false) Long idCommune,
			Model model) {
		Integer annee = applicationBusinessProperties.getDerniereAnnee();
		String publicBase = publicBase();

		List<Region> regions = serviceReadReferences.getRegion();
		List<CommunauteCommune> communautes = idRegion == null
				? List.of()
				: serviceReadReferences.getCommunauteByRegionId(idRegion);
		// Un EPCI ou une commune d'une autre branche du sélecteur est ignoré.
		Long selectedCom2co = idCommunauteDeCommunes != null && communautes.stream()
				.anyMatch(com2co -> com2co.getId().equals(idCommunauteDeCommunes))
						? idCommunauteDeCommunes
						: null;
		List<City> communes;
		if (selectedCom2co == null) {
			communes = idRegion == null ? List.of() : serviceReadReferences.getCityByRegionId(idRegion);
		} else {
			communes = serviceReadReferences.getCityByCommunauteCommuneId(selectedCom2co);
		}
		Long selectedCity = idCommune != null && communes.stream()
				.anyMatch(city -> city.getId().equals(idCommune)) ? idCommune : null;

		List<NdviZone> zones;
		if (selectedCity != null) {
			zones = List.of(toCityZone(serviceReadReferences.getCityById(selectedCity), annee, publicBase));
		} else if (selectedCom2co != null) {
			zones = List.of(toZone(serviceReadReferences.getCommunauteCommuneById(selectedCom2co),
					annee, publicBase));
		} else {
			zones = zonesOfRegion(idRegion, annee, publicBase);
		}

		City city = selectedCity == null ? null : serviceReadReferences.getCityById(selectedCity);
		model.addAttribute("zones", zones);
		model.addAttribute("annee", annee);
		model.addAttribute("regions", regions);
		model.addAttribute("communautesDeCommunes", communautes);
		model.addAttribute("communes", communes);
		model.addAttribute("selectedRegionId", idRegion);
		model.addAttribute("selectedCommunauteDeCommunesId", selectedCom2co);
		model.addAttribute("selectedCommuneId", selectedCity);
		model.addAttribute("selectedCommuneName", city == null ? "" : city.getName());
		return "listNdviZones";
	}

	private List<NdviZone> zonesOfRegion(Long idRegion, Integer annee, String publicBase) {
		List<Long> ids = parkAreaComputedRepository.findCom2coIdsWithComputedParks();
		return communauteCommuneRepository.findAllById(ids).stream()
				.filter(com2co -> idRegion == null
						|| (com2co.getRegion() != null && idRegion.equals(com2co.getRegion().getId())))
				.sorted(Comparator
						.comparing((CommunauteCommune c) -> c.getRegion() == null ? "" : c.getRegion().getName())
						.thenComparing(CommunauteCommune::getName))
				.map(com2co -> toZone(com2co, annee, publicBase))
				.toList();
	}

	private NdviZone toZone(CommunauteCommune com2co, Integer annee, String publicBase) {
		NdviSummaries summaries = ndviController.summaries(annee, com2co.getId());
		Region region = com2co.getRegion();
		// La région permet au sélecteur de localisation de la page publique d'afficher l'EPCI.
		String regionParam = region == null ? "" : "region=" + region.getId() + "&";
		String publicUrl = publicBase + "/ndvi?" + regionParam + "com2co=" + com2co.getId();
		String regionName = region == null ? null : region.getName();
		return new NdviZone(com2co.getId(), com2co.getName(), regionName,
				summaries.total(), summaries.withNdvi(), summaries.latestAcquisitionDate(), publicUrl, false);
	}

	private NdviZone toCityZone(City city, Integer annee, String publicBase) {
		NdviSummaries summaries = ndviController.summaries(annee, null, city.getInseeCode());
		CommunauteCommune com2co = city.getCommunauteCommune();
		Region region = city.getRegion() != null ? city.getRegion() : (com2co == null ? null : com2co.getRegion());
		StringBuilder url = new StringBuilder(publicBase).append("/ndvi?");
		if (region != null) {
			url.append("region=").append(region.getId()).append("&");
		}
		if (com2co != null) {
			url.append("com2co=").append(com2co.getId()).append("&");
		}
		url.append("city=").append(city.getInseeCode());
		return new NdviZone(com2co == null ? null : com2co.getId(), city.getName(),
				region == null ? null : region.getName(), summaries.total(), summaries.withNdvi(),
				summaries.latestAcquisitionDate(), url.toString(), true);
	}

	private String publicBase() {
		String base = applicationBusinessProperties.getSitePublique();
		if (base == null) {
			return "";
		}
		return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
	}
}
