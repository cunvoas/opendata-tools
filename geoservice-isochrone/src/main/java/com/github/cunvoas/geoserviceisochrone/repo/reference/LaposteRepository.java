package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Laposte;

@Repository
public interface LaposteRepository extends JpaRepository<Laposte, String>{
	List<Laposte> findByPostalCode(String postalCode);
}
