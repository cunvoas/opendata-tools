package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

/**
 * REsT COntroler for park.
 */
@RestController
@RequestMapping("/map/park")
public class GeoJsonParkController {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
    @Autowired
    private GeoMapServiceV2 geoMapService;
    @Autowired
    private ApplicationBusinessProperties applicationBusinessProperties;


    /**
     * get isochrone by polygon.
     * @param polygon
     * @param annee
     * @return
     */
    @CrossOrigin(origins = "${web.cors.allowed-origins}")
    @GetMapping("/parkByPolygon")
    public GeoJsonRoot getParkIsochroneByArea(@RequestParam("polygon") Polygon polygon, @RequestParam("annee") Integer annee) {
    	if (annee==null) {
    		annee=applicationBusinessProperties.getDerniereAnnee();
    	}
        return geoMapService.findAllParkByArea(polygon, annee);
    }
    

    /**
     * get isochrone by area.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
     * @param annee year
     * @return list of isochrone
     */
    @CrossOrigin(origins = "${web.cors.allowed-origins}")
    @GetMapping("/area")
    public GeoJsonRoot getParkIsochroneByArea(
    			@RequestParam("swLat") Double swLat,
    			@RequestParam("swLng") Double swLng,
    			@RequestParam("neLat") Double neLat,
    			@RequestParam("neLng") Double neLng,
    			@RequestParam("annee") Integer annee
    		) {
    	if (annee==null) {
    		annee=applicationBusinessProperties.getDerniereAnnee();
    	}
        return geoMapService.findAllParkByArea(annee, swLat, swLng, neLat, neLng);
    }
    

    /**
     * get park outline by area.
     * @param swLat south-west latitude
     * @param swLng south-west longitude
     * @param neLat north-est latitude
     * @param neLng north-est longitude
     * @param annee year
     * @return list of park outline
     */
    @CrossOrigin(origins = "${web.cors.allowed-origins}")
    @GetMapping("/outline")
    public GeoJsonRoot getParkOutlineByArea(
    			@RequestParam("swLat") Double swLat,
    			@RequestParam("swLng") Double swLng,
    			@RequestParam("neLat") Double neLat,
    			@RequestParam("neLng") Double neLng,
    			@RequestParam("annee") Integer annee
    		) {
    	if (annee==null) {
    		annee=applicationBusinessProperties.getDerniereAnnee();
    	}
        return geoMapService.findAllParkByArea(annee, swLat, swLng, neLat, neLng);
    }
    


    /**
     * get park by city.
     * @param coords of a point(lat, lng)
     * @param zoom zoom index
     * @param annee year
     * @return list of city
     * @see https://gis.stackexchange.com/questions/284880/get-the-lat-lng-values-of-lines-polygons-drawn-by-leaflet-drawing-tools
     */
    @CrossOrigin(origins = "${web.cors.allowed-origins}")
    @GetMapping("/parkByCoordsZoom")
    public GeoJsonRoot getCityPage(
    		@RequestParam("coords") String coords, 
    		@RequestParam("zoom") Integer zoom,
    		@RequestParam("annee") Integer annee
    		) {
    	
    	if (annee==null) {
    		annee=applicationBusinessProperties.getDerniereAnnee();
    	}
    	Polygon polygon = factory.createPolygon();
    	//Lille 50, 3.
    	Point p = GeoShapeHelper.parsePointLatLng(coords);
    	
    	//TODO compute a rectangle shape with point at the center
        return geoMapService.findAllParkByArea(polygon, annee);
    }


}
