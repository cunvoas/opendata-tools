package com.github.cunvoas.geoserviceisochrone.repo.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurAction;

@Repository
public interface ContributeurActionRepository extends JpaRepository<ContributeurAction, Long>{
	
}
