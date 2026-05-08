package com.github.cunvoas.geoserviceisochrone.repo.proposal;

import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;

/**
 * Repo for ProjectSimulator.
 */
public interface ProjectSimulatorRepository extends JpaRepository<ProjectSimulator, Long> {

	@Query("SELECT p FROM project_simul p WHERE p.idCommune = :idCommune ORDER BY p.name ASC")
	List<ProjectSimulator> findByIdCommune(@Param("idCommune") Long idCommune);

	/** 
	 * findParkProposalInMapArea.
	 * @param mapArea shape in string
	 * @return list of ParkArea
	 */
	@Query(nativeQuery = true, 
			   value = "SELECT pp.* FROM project_simul pp WHERE ST_Intersects(pp.shape_area, ?1)")
	public List<ProjectSimulator> findInMapArea(String sPolygon);

	/**
	 * getSurface.
	 * @param polygon shape
	 * @return surface of shape
	 */
	@Query(nativeQuery = true,  value = "SELECT ST_Area(?1, true)")
	Long getSurface(Geometry polygon);
	
}
