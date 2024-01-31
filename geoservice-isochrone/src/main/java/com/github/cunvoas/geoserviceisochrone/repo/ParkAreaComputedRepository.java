package com.github.cunvoas.geoserviceisochrone.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;

@Repository
public interface ParkAreaComputedRepository extends JpaRepository<ParkAreaComputed, Long>{
	
}
