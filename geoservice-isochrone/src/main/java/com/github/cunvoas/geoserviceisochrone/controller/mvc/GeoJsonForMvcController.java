package com.github.cunvoas.geoserviceisochrone.controller.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapService;

@RestController
@RequestMapping("/mvc/geojson")
public class GeoJsonForMvcController {

    @Autowired
    private GeoMapService geoMapService;
	
	@GetMapping("/isochrones")
	public GeoJsonRoot getIsochrone(@RequestParam("idPark") Long idPark) {
		GeoJsonRoot isochrones=geoMapService.findIsochroneParkEntrance(idPark);
		return isochrones;
	}
}
