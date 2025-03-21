package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

/**
 * REsT Controler for Insee.
 */
@RestController
@RequestMapping("/map/insee")
public class GeoJsonCarreInseeController {

    @Autowired
    private GeoMapServiceV2 inseeCarre200mService;


    /**
     * get insee by area.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
     * @param annee year of data
     * @return list of carre insee
     */
    @CrossOrigin(origins = "${web.cors.allowed-origins}")
    @GetMapping("/carre200m/area")
    public GeoJsonRoot getCarreByArea(
			@RequestParam("swLat") Double swLat,
			@RequestParam("swLng") Double swLng,
			@RequestParam("neLat") Double neLat,
			@RequestParam("neLng") Double neLng,
			@RequestParam("annee") Integer annee
		) {
    	if (annee==null) {
    		return inseeCarre200mService.findAllCarreByArea(swLat, swLng, neLat, neLng);
    	} else {
    		return inseeCarre200mService.findAllCarreByArea(annee, swLat, swLng, neLat, neLng);
    	}
    }


    /**
     * get insee by polygon.
     * @param polygon seach polygon
     * @param annee year
     * @return  list of carre insee
     */
    @CrossOrigin(origins = "${web.cors.allowed-origins}")
    @GetMapping("/carre200m/polygon")
    public GeoJsonRoot getCarreByArea(@RequestParam("polygon") Polygon polygon, @RequestParam("annee") Integer annee) {
        if (annee==null) {
        	return inseeCarre200mService.findAllCarreByArea(polygon);
        } else {
        	return inseeCarre200mService.findAllCarreByArea(polygon, annee);	
        }
    	
    }

}
