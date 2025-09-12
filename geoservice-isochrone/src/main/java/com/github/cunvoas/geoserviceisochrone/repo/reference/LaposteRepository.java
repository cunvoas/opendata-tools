package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Laposte;

/**
 * Repository Spring Data JPA pour l'accès aux données de La Poste.
 * Permet de rechercher par code postal.
 */
@Repository
public interface LaposteRepository extends JpaRepository<Laposte, String>{
	/**
	 * findByPostalCode
	 * @param postalCode code
	 * @return list Laposte
	 */
	List<Laposte> findByPostalCode(String postalCode);
}