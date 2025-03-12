package com.github.cunvoas.geoserviceisochrone.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputedId;

/**
 * Spring JPA repository.
 */
@Repository
public interface ParkAreaComputedRepository extends JpaRepository<ParkAreaComputed, ParkAreaComputedId>{
	
	Optional<ParkAreaComputed> findByIdAndAnnee(Long id, Integer annee);
	
}
