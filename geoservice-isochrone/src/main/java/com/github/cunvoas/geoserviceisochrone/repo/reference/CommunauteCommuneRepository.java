package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;

/**
 * Repository Spring Data JPA pour l'accès aux entités CommunauteCommune.
 * Permet de récupérer les communautés de communes d'une région.
 */
@Repository
public interface CommunauteCommuneRepository extends JpaRepository<CommunauteCommune, Long>{
	/**
	 * findByRegionId.
	 * @param id region
	 * @return list of com2co
	 */
	@Query(	nativeQuery = true,
			value = "select * from adm_com2commune where id_region=:id order by name",
			countQuery = "select count(1) from adm_com2commune where id_region=:id")
	List<CommunauteCommune> findByRegionId(@Param("id")Long id);

}