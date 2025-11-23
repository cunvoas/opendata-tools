package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;

/**
 * REsT COntroler for years.
 */
@RestController
@RequestMapping("/map/years")
public class YearController {

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;

    /**
     * list all city.
     * @param pageable page
     * @return cities	
     */
    @GetMapping
    public List<Integer> getCityPage(Pageable pageable) {
        return List.of(applicationBusinessProperties.getInseeAnnees());
    }

}
