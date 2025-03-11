package com.github.cunvoas.geoserviceisochrone.repo.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.admin.Association;

/**
 * Spring JPA repository.
 */
@Repository
public interface AssociationRepository extends JpaRepository<Association, Long>{
    Optional<Association> findByNom(String nom);
    
    @Query(value ="select * from adm_asso a order by a.nom" , nativeQuery = true)
    List<Association> findAllOrderByNom();
}
