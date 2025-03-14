package com.github.cunvoas.geoserviceisochrone.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;

/**
 * Spring JPA repository.
 */
@Repository
public interface InseeCarre200mComputedV2Repository extends JpaRepository<InseeCarre200mComputedV2, InseeCarre200mComputedId>{

	/**
	 * findByAnneeAndIdInspire.
	 * @param annee year
	 * @param idInspire code inspire
	 * @return InseeCarre200mComputedV2
	 */
	Optional<InseeCarre200mComputedV2> findByAnneeAndIdInspire(Integer annee, String idInspire);
	
}
