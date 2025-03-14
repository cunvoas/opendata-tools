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
	
    /**
     * findByLogin.
     * @param login name
     * @return Contributeur
     */
    Optional<Contributeur> findByLogin(String login);
    
    /**
     * findByEmail.
     * @param email name
     * @return
     */
    Optional<Contributeur> findByEmail(String email);
    
    /**
     * existsByLogin.
     * @param login name
     * @return bbol
     */
    boolean existsByLogin(String login);
    
    /**
     * existsByEmail.
     * @param email name
     * @return bool
     */
    boolean existsByEmail(String email);
    
    /**
     * findByAssociation.
     * @param asso Association
     * @return list Contributeur
     */
    List<Contributeur> findByAssociation(Association asso);
	
}
