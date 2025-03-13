package com.github.cunvoas.geoserviceisochrone.controller.mvc.geojson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

/**
 * REsT controler for geojson parks and entrances layer.
 */
@RestController
@RequestMapping("/mvc/geojson")
public class GeoJsonForMvcController {

    @Autowired
    private GeoMapServiceV2 geoMapService;
	
	/**
	 * get entrance.
	 * @param idPark id park
	 * @return geojson
	 */
	@GetMapping("/isochrones/entrance")
	public GeoJsonRoot getIsochroneEntrance(@RequestParam("idPark") Long idPark) {
		GeoJsonRoot isochrones=geoMapService.findIsochroneParkEntrance(idPark);
		return isochrones;
	}

	
	/**
	 * get parks
	 * @param idPark id
	 * @return geojson
	 */
	@GetMapping("/isochrones/park")
	public GeoJsonRoot getIsochronePark(@RequestParam("idPark") Long idPark) {
		GeoJsonRoot isochrones=geoMapService.findIsochronePark(idPark);
		return isochrones;
	}
	
	/**
	 * get park from prefecture.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return geojson
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
     * get park.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return geosjon
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
     * get park outline.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return geosjon
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
     * get cadastre.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
	 * @return geosjon
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
	
}
