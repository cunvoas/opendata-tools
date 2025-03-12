package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

/**
 * REsT COntroler for Cadastre.
 */
@RestController
@RequestMapping("/map/cadastre")
public class GeoJsonCadastreController {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
    @Autowired
    private GeoMapServiceV2 geoMapService;
    
    /**
     * 
     * get Cadastre by area.
     * @param swLat
     * @param swLng
     * @param neLat
     * @param neLng
     * @return
     */
    @CrossOrigin(origins = "${web.cors.allowed-origins}")
    @GetMapping("/area")
    public GeoJsonRoot getCadastreByArea(
			@RequestParam("swLat") Double swLat,
			@RequestParam("swLng") Double swLng,
			@RequestParam("neLat") Double neLat,
			@RequestParam("neLng") Double neLng
		) {
        return geoMapService.findAllCadastreByArea(swLat, swLng, neLat, neLng);	
    }
    

    /**
     * get Cadastre by comm2co.
     * @param id
     * @return
     */
    @CrossOrigin(origins = "${web.cors.allowed-origins}")
    @GetMapping("/area/{id}")
    public GeoJsonRoot getCadastreByCom2Com(@PathVariable ("id")Long id) {
    	 return geoMapService.findAllCadastreByComm2Co(id);
	}
    

    /**
     * get Cadastre by city.
     * @param id
     * @return
     */
    @CrossOrigin(origins = "${web.cors.allowed-origins}")
    @GetMapping("/area/city/{id}")
    public GeoJsonRoot getCadastreByCity(@PathVariable ("id")Long id) {
    	 return geoMapService.findCadastreByCity(id);
	}

}
