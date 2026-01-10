package com.github.cunvoas.geoserviceisochrone.controller.mobile.auth;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/mobile/location")
@PreAuthorize("isAuthenticated()")
@Slf4j
public class LocationForMobile {

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;

	@Autowired
	private ServiceReadReferences serviceReadReferences;
	
	@GetMapping("/region")
	public ResponseEntity<List<Region>> getRegions() {
		return new ResponseEntity<>(serviceReadReferences.getRegion(), HttpStatus.OK);
	}
	
	@GetMapping("/epci")
	public ResponseEntity<List<CommunauteCommune>> getCommunauteCommunes(@RequestParam(name = "regionId", required = true) Long regionId) {
		return new ResponseEntity<>(serviceReadReferences.getCommunauteCommuneByRegionId(regionId), HttpStatus.OK);
	}
	
	@GetMapping("/city")
	public ResponseEntity<List<City>> getCities(@RequestParam(name = "epciId", required = true) Long epciId) {
		return new ResponseEntity<>(serviceReadReferences.getCityByCommunauteCommuneId(epciId), HttpStatus.OK);
	}


}
