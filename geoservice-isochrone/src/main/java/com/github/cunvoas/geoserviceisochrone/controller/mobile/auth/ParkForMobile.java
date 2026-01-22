package com.github.cunvoas.geoserviceisochrone.controller.mobile.auth;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;
import com.github.cunvoas.geoserviceisochrone.service.park.ParkJardinService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/mobile/park")
@PreAuthorize("isAuthenticated()")
@Slf4j
@ConditionalOnProperty(
		name="application.feature-flipping.mobile-api", 
		havingValue="true")
public class ParkForMobile {

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	@Autowired
	private ServiceReadReferences serviceReadReferences;
	@Autowired
	private ParkJardinService serviceParkJardinService;
	
	@PostMapping("/save")
	public ResponseEntity<ParcEtJardin> save(
			@RequestBody ParcEtJardin parcEtJardin,
			@RequestParam(name = "updSurfaceShape", required = false, defaultValue = "false") boolean updSurfaceShape) {
		ParcEtJardin saved = serviceParkJardinService.save(parcEtJardin, updSurfaceShape);
		return new ResponseEntity<>(saved, HttpStatus.OK);
	}

	@GetMapping("/list")
	public ResponseEntity<List<ParcEtJardin>> getParks(@RequestParam(name = "cityId", required = true) Long cityId) {
		List<ParcEtJardin> pjs = serviceReadReferences.getParcEtJardinByCityId(cityId);
		return new ResponseEntity<>(pjs, HttpStatus.OK);
	}
	
	@GetMapping("/{parkId}")
	public ResponseEntity<ParcEtJardin> getPark(@PathVariable(name = "parkId", required = true) Long parkId) {
		ParcEtJardin pj = serviceReadReferences.getParcEtJardinById(parkId);
		return new ResponseEntity<>(pj, HttpStatus.OK);
	}
	
	@PostMapping
    public ResponseEntity<ParcEtJardin> create(@RequestBody ParcEtJardin pj) {
        return ResponseEntity.status(HttpStatus.CREATED).body(serviceParkJardinService.save(pj, true));
    }

    @PutMapping("/{parkId}")
    public ResponseEntity<ParcEtJardin> update(@PathVariable Long parkId, @RequestBody ParcEtJardin updatedUser) {
    	ParcEtJardin pj = serviceReadReferences.getParcEtJardinById(parkId);
    	pj = serviceParkJardinService.save(pj, false);
		return new ResponseEntity<>(pj, HttpStatus.OK);
    	
//    	
//        return serviceParkJardinService.findById(parkId).map(user -> {
//            user.setName(updatedUser.getName());
//            return ResponseEntity.ok(userRepository.save(user));
//        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{parkId}")
    public ResponseEntity<Void> delete(@PathVariable Long parkId) {
    	ParcEtJardin pj = serviceReadReferences.getParcEtJardinById(parkId);
    	pj.setDateSuppression(new Date());
    	serviceParkJardinService.save(pj, false);
        return ResponseEntity.noContent().build();
    }

}
