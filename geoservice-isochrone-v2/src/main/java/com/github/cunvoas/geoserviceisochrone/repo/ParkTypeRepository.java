package com.github.cunvoas.geoserviceisochrone.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;

/**
 * Spring JPA repository.
 */
@Repository
public interface ParkTypeRepository extends JpaRepository<ParkType, Long>{
	
}
