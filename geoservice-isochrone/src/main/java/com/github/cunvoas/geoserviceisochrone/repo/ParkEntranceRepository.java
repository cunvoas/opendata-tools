package com.github.cunvoas.geoserviceisochrone.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;

@Repository
public interface ParkEntranceRepository extends JpaRepository<ParkEntrance, Long>{
	
	/**
	 * get Entrance by ParkArea.
	 * @param id
	 * @return
	 */
	//@Query("select * from ParkEntrance where parkArea.id=?1")
	List<ParkEntrance> findByParkArea(ParkArea parkArea);
	ParkEntrance findByParkAreaAndDescription(ParkArea parkArea, String description);
	

	@Query(nativeQuery = true, 
			   value = "SELECT e.* FROM park_entrance e "
			   				+ "INNER JOIN park_area a ON a.id=e.area_id "
			   				+ "WHERE a.id_parc_jardin=:id")
	public List<ParkEntrance> findByParkId(@Param("id")Long id);
	
}
