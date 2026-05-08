package com.github.cunvoas.geoserviceisochrone.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.tools.ParkPhoto;

/**
 * Spring JPA repository.
 */
@Repository
public interface ParkPhotoRepository extends JpaRepository<ParkPhoto, Long>{

}
