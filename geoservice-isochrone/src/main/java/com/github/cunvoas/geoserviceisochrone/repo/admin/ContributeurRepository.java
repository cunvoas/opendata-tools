package com.github.cunvoas.geoserviceisochrone.repo.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.admin.Association;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;

/**
 * Spring JPA repository.
 */
@Repository
public interface ContributeurRepository extends JpaRepository<Contributeur, Long>{
    Optional<Contributeur> findByLogin(String login);
    Optional<Contributeur> findByEmail(String email);
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);
    
    List<Contributeur> findByAssociation(Association asso);
	
}
