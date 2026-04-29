package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.service.map.CityService;

/**
 * REsT COntroler for city.
 */
@RestController
@RequestMapping("/map/city")
public class CityController {

    private final CityService cityService;

    @Autowired
    public CityController (CityService cityService) {
    	this.cityService=cityService;
    }

    /**
     * list all city.
     * @param pageable page
     * @return cities
     */
    @GetMapping
    public Page<City> getCityPage(Pageable pageable) {
        return cityService.findAll(pageable);
    }

    /**
     * list all city.
     * @param lat latitude
     * @param lon longitune
     * @param distanceM distance by meters
     * @return cities
     */
    @GetMapping("{lat}/{lon}/{distanceM}")
    public List<City> getCityNear(
            @PathVariable double lat,
            @PathVariable double lon,
            @PathVariable double distanceM) {
        return cityService.findAround(lat, lon, distanceM);
    }

}
