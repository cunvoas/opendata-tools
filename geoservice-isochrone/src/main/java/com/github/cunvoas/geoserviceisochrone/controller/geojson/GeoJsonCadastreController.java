package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapService;

@RestController
@RequestMapping("/map/cadastre")
public class GeoJsonCadastreController {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
    @Autowired
    private GeoMapService geoMapService;
    
    @CrossOrigin(origins = {"http://localhost:8081", "https://autmel-maps.duckdns.org/"})
    @GetMapping("/area")
    public GeoJsonRoot getCadastreByArea(
			@RequestParam("swLat") Double swLat,
			@RequestParam("swLng") Double swLng,
			@RequestParam("neLat") Double neLat,
			@RequestParam("neLng") Double neLng
		) {
        return geoMapService.findAllCadastreByArea(swLat, swLng, neLat, neLng);	
    }

}
