package com.github.cunvoas.geoserviceisochrone.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardCache;

@Repository
public interface DashboardCacheRepository extends JpaRepository<DashboardCache, String> {
	
}
