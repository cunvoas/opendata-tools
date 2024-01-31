package com.github.cunvoas.geoserviceisochrone.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputed;

@Repository
public interface InseeCarre200mComputedRepository extends JpaRepository<InseeCarre200mComputed, String>{

}
