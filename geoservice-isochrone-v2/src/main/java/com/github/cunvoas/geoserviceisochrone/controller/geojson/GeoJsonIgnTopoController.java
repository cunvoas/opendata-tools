package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import java.util.List;

import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.controller.geojson.view.IgnTopoVegetalView;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonFeature;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.model.ignTopo.IgnTopoVegetal;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.service.ignTopo.IgnTopoService;

import lombok.extern.slf4j.Slf4j;

/**
 * REsT Controler for Insee.
 */
@RestController
@RequestMapping("/map/ign-topo")
@Slf4j
public class GeoJsonIgnTopoController {

    @Autowired
    private IgnTopoService ignTopoService;

    @Autowired
    private GeometryQueryHelper geometryQueryHelper;

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
    @GetMapping("/vegetal")
    public GeoJsonRoot getIgnTopoVegetal(
			@RequestParam("swLat") Double swLat,
			@RequestParam("swLng") Double swLng,
			@RequestParam("neLat") Double neLat,
			@RequestParam("neLng") Double neLng,
			@RequestParam(name="com2coId", required = false) Long com2coId,
			@RequestParam(name="insee", required = false) String insee
		) {
    	
    	Polygon polygon = geometryQueryHelper.getPolygonFromBounds(swLat, swLng, neLat, neLng);
    	
    	GeoJsonRoot root = null;
    	if (insee!=null) {
    		root = this.getIgnTopoVegetal(insee, polygon);
    	} else if (com2coId!=null) {
    		root = this.getIgnTopoVegetal(com2coId, polygon);
    	} else {
    		root = this.getIgnTopoVegetal(polygon);
    	}
     	return root;
     	
    }


    /**
     * get IgnTopoVegetal by com2coId & polygon.
     * @param com2coId id of com2co
     * @param polygon seach polygon
     * @return  list of IgnTopoVegetal
     */
    protected GeoJsonRoot getIgnTopoVegetal(Polygon polygon) {
        List<IgnTopoVegetal> veges = ignTopoService.findAllCarreByArea(polygon);

		GeoJsonRoot root = null;
    	if (!veges.isEmpty()) {
	    	root = map(veges);
    	}
    	return root;
    }
    
    
    /**
     * get IgnTopoVegetal by com2coId & polygon.
     * @param com2coId id of com2co
     * @param polygon seach polygon
     * @return  list of IgnTopoVegetal
     */
    protected GeoJsonRoot getIgnTopoVegetal(Long com2coId, Polygon polygon) {
        List<IgnTopoVegetal> veges = ignTopoService.findAllCarreByCom2coIdAndArea(com2coId, polygon);

		GeoJsonRoot root = null;
    	if (!veges.isEmpty()) {
	    	root = map(veges);
    	}
    	return root;
    }
    

    /**
     * get IgnTopoVegetal by inseeId & polygon.
     * @param inseeId insee code
     * @param polygon seach polygon
     * @return  list of IgnTopoVegetal
     */
    protected GeoJsonRoot getIgnTopoVegetal(String inseeId, Polygon polygon) {
        List<IgnTopoVegetal> veges = ignTopoService.findAllCarreByInseeAndArea(inseeId, polygon);

		GeoJsonRoot root = null;
    	if (!veges.isEmpty()) {
	    	root = map(veges);
    	}
    	return root;
    }

    protected GeoJsonRoot convertToGeoJson(List<IgnTopoVegetal> veges) {
		log.warn("convertToGeoJsonList<IgnTopoVegetal> veges)");
		GeoJsonRoot root = map(veges);
    	return root;
	}

	protected GeoJsonRoot map(List<IgnTopoVegetal> veges) {
		GeoJsonRoot root = new GeoJsonRoot();
		
    	if (veges!=null && !veges.isEmpty()) {
    		for (IgnTopoVegetal itv : veges) {
    		
    			GeoJsonFeature feature = new GeoJsonFeature();
				root.getFeatures().add(feature);
				
				feature.setGeometry(itv.getGeometry());
				
				IgnTopoVegetalView property = new IgnTopoVegetalView();
				feature.setProperties(property);

				property.setId(itv.getId());
				property.setNature(itv.getNature());
				property.setSurface(itv.getSurface());
				
			}
    	}
		
		return root;
	}
	
}
