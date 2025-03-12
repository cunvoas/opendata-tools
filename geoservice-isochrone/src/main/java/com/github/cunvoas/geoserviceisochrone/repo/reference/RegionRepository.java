package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

/**
 * Spring JPA repository.
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, Long>{

	Region findByName(String name);
	
	@Query(nativeQuery = true,
			value="select * from adm_region order by name",
			countQuery="select count(1) from adm_region")
	List<Region> findAllOrderByName();

}
