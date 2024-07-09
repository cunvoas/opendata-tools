package com.github.cunvoas.geoserviceisochrone.controller.mvc.geojson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

@RestController
@RequestMapping("/mvc/geojson")
public class GeoJsonForMvcController {

    @Autowired
    private GeoMapServiceV2 geoMapService;
	
	@GetMapping("/isochrones/entrance")
	public GeoJsonRoot getIsochroneEntrance(@RequestParam("idPark") Long idPark) {
		GeoJsonRoot isochrones=geoMapService.findIsochroneParkEntrance(idPark);
		return isochrones;
	}

	
	@GetMapping("/isochrones/park")
	public GeoJsonRoot getIsochronePark(@RequestParam("idPark") Long idPark) {
		GeoJsonRoot isochrones=geoMapService.findIsochronePark(idPark);
		return isochrones;
	}
	
	@GetMapping("/parkPrefectureByCorner")
    public GeoJsonRoot getParkPrefectureByArea(
    			@RequestParam("swLat") Double swLat,
    			@RequestParam("swLng") Double swLng,
    			@RequestParam("neLat") Double neLat,
    			@RequestParam("neLng") Double neLng
    		) {
        return geoMapService.findParkPrefectureByArea(swLat, swLng, neLat, neLng);
    }
	
	@GetMapping("/parkGardenByCorner")
    public GeoJsonRoot getParcJardinPyArea(
    			@RequestParam("swLat") Double swLat,
    			@RequestParam("swLng") Double swLng,
    			@RequestParam("neLat") Double neLat,
    			@RequestParam("neLng") Double neLng
    		) {
        return geoMapService.findParcEtJardinByArea(swLat, swLng, neLat, neLng);
    }
	
}
