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

import com.github.cunvoas.geoserviceisochrone.extern.helper.GeoShapeHelper;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.service.map.GeoMapServiceV2;

@RestController
@RequestMapping("/map/park")
public class GeoJsonParkController {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
    @Autowired
    private GeoMapServiceV2 geoMapService;

    @GetMapping("/parkByPolygon")
    public GeoJsonRoot getParkByArea(@RequestParam("polygon") Polygon polygon) {
        return geoMapService.findAllParkByArea(polygon);
    }
    
    //FIXME use global conf
    @CrossOrigin(origins = {"http://localhost:8081", "https://autmel-maps.duckdns.org/"})
    @GetMapping("/area")
    public GeoJsonRoot getParkByArea(
    			@RequestParam("swLat") Double swLat,
    			@RequestParam("swLng") Double swLng,
    			@RequestParam("neLat") Double neLat,
    			@RequestParam("neLng") Double neLng
    		) {
        return geoMapService.findAllParkByArea(swLat, swLng, neLat, neLng);
    }
    
    

//    /**
//     * @return get parks for Lille
//     */
//    @GetMapping("/lille")
//    public GeoJsonRoot getParksLille() {
//        return geoMapService.findAllParkByArea(GeoJsonCarreInseeController.makeLille());
//    }

    @GetMapping("/parkByCoordsZoom")
    //https://gis.stackexchange.com/questions/284880/get-the-lat-lng-values-of-lines-polygons-drawn-by-leaflet-drawing-tools
    public GeoJsonRoot getCityPage(@RequestParam("coords") String coords,  @RequestParam("zoom") Integer zoom) {
    	Polygon polygon = factory.createPolygon();
    	//Lille 50, 3.
    	Point p = GeoShapeHelper.parsePointLatLng(coords);
    	
    	//TODO compute a rectangle shape with point at the center
    	
        return geoMapService.findAllParkByArea(polygon);
    }


}
