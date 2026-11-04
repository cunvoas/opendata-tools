package com.github.cunvoas.geoserviceisochrone.repo.admin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviObservation;

/**
 * Historique des observations NDVI par parc.
 */
@Repository
public interface NdviObservationRepository extends JpaRepository<NdviObservation, Long> {

	/**
	 * Histoire végétale d'un parc, de la plus ancienne à la plus récente.
	 * @param parkAreaId identifiant de la zone de parc
	 * @return observations NDVI
	 */
	List<NdviObservation> findByParkAreaIdOrderByPeriodStartAsc(Long parkAreaId);
}
