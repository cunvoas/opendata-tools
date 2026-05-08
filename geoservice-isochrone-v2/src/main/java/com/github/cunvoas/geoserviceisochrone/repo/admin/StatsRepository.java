package com.github.cunvoas.geoserviceisochrone.repo.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.admin.Stats;

/**
 * Spring JPA repository.
 */
@Repository
public interface StatsRepository extends JpaRepository<Stats, Long>{
}
