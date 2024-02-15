package com.github.cunvoas.geoserviceisochrone.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;

//@Repository
public interface ParkTypeRepository extends JpaRepository<ParkType, Long>{
	
}
