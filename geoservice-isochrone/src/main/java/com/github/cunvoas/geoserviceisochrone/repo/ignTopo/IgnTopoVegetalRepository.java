package com.github.cunvoas.geoserviceisochrone.repo.ignTopo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.ignTopo.IgnTopoVegetal;

@Repository
public interface IgnTopoVegetalRepository  extends JpaRepository<IgnTopoVegetal, Long>{

	Optional<IgnTopoVegetal> findByInseeId(String inseeId);
}
