package com.github.cunvoas.geoserviceisochrone.repo.reference;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParkOverpass;

/**
 * repository de ParkOverpass.
 *  
 */
@Repository
public interface ParkOverpassRepository extends JpaRepository<ParkOverpass, Long> {

}
