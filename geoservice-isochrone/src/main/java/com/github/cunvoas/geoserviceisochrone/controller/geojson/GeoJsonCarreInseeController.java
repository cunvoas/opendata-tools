package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
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
@RequestMapping("/map/insee")
public class GeoJsonCarreInseeController {

	private static GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
	
    @Autowired
    private GeoMapService inseeCarre200mService;

    @CrossOrigin(origins = {"http://localhost:8081", "https://autmel-maps.duckdns.org/"})
    @GetMapping("/carre200m/area")
    public GeoJsonRoot getCarreByArea(
			@RequestParam("swLat") Double swLat,
			@RequestParam("swLng") Double swLng,
			@RequestParam("neLat") Double neLat,
			@RequestParam("neLng") Double neLng
		) {
        return inseeCarre200mService.findAllCarreByArea(swLat, swLng, neLat, neLng);	
    }

    @GetMapping("/carre200m/polygon")
    public GeoJsonRoot getCarreByArea(@RequestParam("polygon") Polygon polygon) {
        return inseeCarre200mService.findAllCarreByArea(polygon);	
    }
    
    @GetMapping("/lille")
    public GeoJsonRoot getLille(){
        return inseeCarre200mService.findAllCarreByArea( makeLille() );	
       // return inseeCarre200mService.findAllCarreComputedByArea( makeLille() );	
    }
    
    
   // swLat=50.587378338380866&swLng=2.970085144042969&neLat=50.67448797148435&neLng=3.159427642822266
    		
    		
    //fixme 
    static Polygon makeLille() {
    	Double x1=  2.970085144042969d;
    	Double x2=  3.159427642822266d;
    	Double y1= 50.587378338380866d;
    	Double y2= 50.67448797148435d;
    	
    	List<Coordinate> coords = new ArrayList<>();
    	coords.add(new Coordinate(x1,y1) );
    	coords.add(new Coordinate(x1,y2) );
    	coords.add(new Coordinate(x2,y2) );
    	coords.add(new Coordinate(x2,y1) );
    	coords.add(new Coordinate(x1,y1) );
    	
    	Coordinate[] array = coords.toArray(Coordinate[]::new);
		//return (Polygon)factory.createPolygon(array).getEnvelope();
		return (Polygon)factory.createPolygon(array);
    }

}
