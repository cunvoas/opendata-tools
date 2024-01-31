package com.github.cunvoas.geoserviceisochrone.repo.reference;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long>{

	Region findByName(String name);

}
