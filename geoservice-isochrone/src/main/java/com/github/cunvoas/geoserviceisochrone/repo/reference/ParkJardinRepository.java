package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;

@Repository
public interface ParkJardinRepository extends JpaRepository<ParcEtJardin, Long> {


	ParcEtJardin findByName(String name);
	
	@Query(nativeQuery = true,
			value="select pj.* from parc_jardin pj where pj.id_city=:id",
			countQuery="select count(1) from parc_jardin pj where pj.id_city=:id")
	List<ParcEtJardin> findByCityId(Long id);
	
	
	@Query(nativeQuery = true,
	value="select pj.* from parc_jardin pj "
			+ " inner join city c on pj.id_city=c.id "
			+ " inner join adm_com2commune c2c on c2c.id=c.id_comm2co "
			+ " inner join park_area pa on pj.identifiant=pa.id_parc_jardin "
			+ " inner join park_entrance pe on pa.id=pe.area_id "
			+ " where c2c.id=:id"
			+ " and (pe.update_date>pa.updated or pa.updated isnull)",
	countQuery="select count(1) from parc_jardin pj inner join city c on c.id=pj.id_city inner join adm_com2commune c2c on c2c.id=c.id_comm2co where c2c.id=:id")
	Page<ParcEtJardin> findByComm2CoId(Long id, Pageable pageable);
	
	@Query(nativeQuery = true,
	value="select pj.* from parc_jardin pj "
			+ " inner join city c on pj.id_city=c.id "
			+ " inner join adm_com2commune c2c on c2c.id=c.id_comm2co "
			+ " inner join park_area pa on pj.identifiant=pa.id_parc_jardin "
			+ " inner join park_entrance pe on pa.id=pe.area_id "
			+ " where c2c.id=:id"
			+ " and (pe.update_date>pa.updated or pa.updated isnull)")
	Page<ParcEtJardin> findByComm2CoIdToMerge(Long id, Pageable pageable);
	
	@Query(nativeQuery = true,
	value="select pj.* from parc_jardin pj "
			+ " inner join city c on pj.id_city=c.id "
			+ " inner join adm_com2commune c2c on c2c.id=c.id_comm2co "
			+ " inner join park_area pa on pj.identifiant=pa.id_parc_jardin "
			+ " inner join park_area_computed pac on pa.id=pac.id "
			+ " where c2c.id=:id"
			+ " and (pa.updated>pac.updated or pac.updated isnull)"
			)
	Page<ParcEtJardin> findByComm2CoIdToCompute(Long id, Pageable pageable);
	
	
	@Query(nativeQuery = true,
			value="select pj.* from parc_jardin pj where pj.id_city=:id",
			countQuery="select count(1) from parc_jardin pj where pj.id_city=:id")
	Page<ParcEtJardin> findByCityId(Long id, Pageable pageable);
	
	@Query(nativeQuery = true,
			value="select distinct pj.* from parc_jardin pj "
					+ "inner join park_area pa on pj.identifiant=pa.id_parc_jardin "
					+ "inner join park_entrance pe on pa.id=pe.area_id "
					+ "where pj.id_city=:id "
					+ "and (pe.update_date>pa.updated or pa.updated isnull)"
			)
	Page<ParcEtJardin> findByCityIdToMerge(Long id, Pageable pageable);
	
	@Query(nativeQuery = true,
			value="select distinct pj.* from parc_jardin pj "
					+ "inner join park_area pa on pj.identifiant=pa.id_parc_jardin "
					+ "inner join park_area_computed pac on pa.id=pac.id "
					+ "where pj.id_city=:id "
					+ "and (pa.updated>pac.updated or pac.updated isnull)"
			)
	Page<ParcEtJardin> findByCityIdToCompute(Long id, Pageable pageable);
	
	
	
	
	
	
	
	
	@Query(value="SELECT * from parc_jardin where ST_Distance(coordonnee, :p) < :distanceM order by ST_Distance(coordonnee, :p) asc", nativeQuery = true)
	List<ParcEtJardin> findNearWithinDistance(Point p, double distanceM);
	
	
}
