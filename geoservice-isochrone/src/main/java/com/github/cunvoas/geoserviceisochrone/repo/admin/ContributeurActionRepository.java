package com.github.cunvoas.geoserviceisochrone.repo.admin;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurAction;

/**
 * Spring JPA repository.
 */
@Repository
public interface ContributeurActionRepository extends JpaRepository<ContributeurAction, Long>{
	
	@Query(nativeQuery = true, value="select ca.* from adm_contrib_action ca where ca.last_date>= current_date-:days")
	List<ContributeurAction> findLastDays(@Param("days") Integer days);
	
}
