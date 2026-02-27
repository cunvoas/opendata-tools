package com.github.cunvoas.geoserviceisochrone.controller.mvc.geojson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

/**
 * Contrôleur REST pour la fourniture des couches GeoJSON (parcs, entrées, isochrones, cadastre).
 * Permet de récupérer les données géographiques au format GeoJSON pour l'affichage cartographique.
 */
@RestController
@RequestMapping("/mvc/geojson")
public class GeoJsonForMvcController {

    @Autowired
    private GeoMapServiceV2 geoMapService;
	
	/**
	 * Récupère les isochrones d'entrée d'un parc.
	 * @param idPark Identifiant du parc
	 * @return Objet GeoJson des isochrones d'entrée
	 */
	@GetMapping("/isochrones/entrance")
	public GeoJsonRoot getIsochroneEntrance(@RequestParam("idPark") Long idPark) {
		GeoJsonRoot isochrones=geoMapService.findIsochroneParkEntrance(idPark);
		return isochrones;
	}

	
	/**
	 * Récupère les isochrones d'un parc.
	 * @param idPark Identifiant du parc
	 * @return Objet GeoJson des isochrones du parc
	 */
	@GetMapping("/isochrones/park")
	public GeoJsonRoot getIsochronePark(@RequestParam("idPark") Long idPark) {
		GeoJsonRoot isochrones=geoMapService.findIsochronePark(idPark);
		return isochrones;
	}
	
	/**
	 * Récupère les parcs de préfecture dans une zone géographique donnée.
	 * @param swLat Latitude sud-ouest
	 * @param swLng Longitude sud-ouest
	 * @param neLat Latitude nord-est
	 * @param neLng Longitude nord-est
	 * @return Objet GeoJson des parcs de préfecture
	 */
	@GetMapping("/parkPrefectureByCorner")
	public GeoJsonRoot getParkPrefectureByArea(
			@RequestParam("swLat") Double swLat,
			@RequestParam("swLng") Double swLng,
			@RequestParam("neLat") Double neLat,
			@RequestParam("neLng") Double neLng
		) {
        return geoMapService.findParkPrefectureByArea(swLat, swLng, neLat, neLng);
    }
	
	/**
	 * Récupère les parcs et jardins dans une zone géographique donnée.
	 * @param swLat Latitude sud-ouest
	 * @param swLng Longitude sud-ouest
	 * @param neLat Latitude nord-est
	 * @param neLng Longitude nord-est
	 * @return Objet GeoJson des parcs et jardins
	 */
	@GetMapping("/parkGardenByCorner")
	public GeoJsonRoot getParcJardinPyArea(
			@RequestParam("swLat") Double swLat,
			@RequestParam("swLng") Double swLng,
			@RequestParam("neLat") Double neLat,
			@RequestParam("neLng") Double neLng
		) {
        return geoMapService.findParcEtJardinByArea(swLat, swLng, neLat, neLng);
    }

	
	/**
	 * Récupère les contours des parcs et jardins dans une zone géographique donnée.
	 * @param swLat Latitude sud-ouest
	 * @param swLng Longitude sud-ouest
	 * @param neLat Latitude nord-est
	 * @param neLng Longitude nord-est
	 * @return Objet GeoJson des contours des parcs et jardins
	 */
	@GetMapping("/parkGardenOutlineByCorner")
	public GeoJsonRoot getParcJardinOutlineByArea(
			@RequestParam("swLat") Double swLat,
			@RequestParam("swLng") Double swLng,
			@RequestParam("neLat") Double neLat,
			@RequestParam("neLng") Double neLng
		) {
        return geoMapService.findAllParkOutlineByArea(swLat, swLng, neLat, neLng);
    }
	

	/**
	 * Récupère les contours des parcs et jardins dans une zone géographique donnée.
	 * @param swLat Latitude sud-ouest
	 * @param swLng Longitude sud-ouest
	 * @param neLat Latitude nord-est
	 * @param neLng Longitude nord-est
	 * @return Objet GeoJson des contours des parcs et jardins
	 */
	@GetMapping("/projectByCorner")
	public GeoJsonRoot findProjetSimulationByArea(
			@RequestParam("swLat") Double swLat,
			@RequestParam("swLng") Double swLng,
			@RequestParam("neLat") Double neLat,
			@RequestParam("neLng") Double neLng
		) {
        return geoMapService.findProjetSimulationByArea(swLat, swLng, neLat, neLng);
    }
	
	/**
	 * récupère les données d'un projet de simulation par son identifiant.
	 * @param idProject
	 * @return Objet GeoJson du projet de simulation
	 */
	@GetMapping("/projectWorkById")
	public GeoJsonRoot findProjetSimulationByArea(
			@RequestParam("id") Long idProject
		) {
        return geoMapService.findProjetSimulationWorkById(idProject);
    }
	
	/**
	 * Récupère les données cadastrales dans une zone géographique donnée.
	 * @param swLat Latitude sud-ouest
	 * @param swLng Longitude sud-ouest
	 * @param neLat Latitude nord-est
	 * @param neLng Longitude nord-est
	 * @return Objet GeoJson du cadastre
	 */
	@GetMapping("/cadastreByCorner")
	public GeoJsonRoot getCadastreByArea(
			@RequestParam("swLat") Double swLat,
			@RequestParam("swLng") Double swLng,
			@RequestParam("neLat") Double neLat,
			@RequestParam("neLng") Double neLng
		) {
        return geoMapService.findAllCadastreByArea(swLat, swLng, neLat, neLng);
    }
	
	@GetMapping("/proposal/{idMeta}")
	// chnager la signature et avoir l'ID meta projet
	public GeoJsonRoot getParkProposalByArea(
			@PathVariable("idMeta") Long idMeta
		) {
		return geoMapService.findParkProposalByArea(null, null, null, idMeta);
	}
	
}