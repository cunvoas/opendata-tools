package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

/**
 * Repository Spring Data JPA pour l'accès aux régions.
 * Permet de rechercher par nom ou de lister toutes les régions.
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, Long>{

	/**
	 * findByName.
	 * @param name name
	 * @return Region
	 */
	Region findByName(String name);
	
	/**
	 * findAllOrderByName.
	 * @return list of Region
	 */
	@Query(nativeQuery = true,
			value="select * from adm_region order by name",
			countQuery="select count(1) from adm_region")
	List<Region> findAllOrderByName();

}