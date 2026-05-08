package com.github.cunvoas.geoserviceisochrone.repo.reference;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.cunvoas.geoserviceisochrone.model.opendata.CadastreProche;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CadastreProcheId;

/**
 * Repository Spring Data JPA pour l'accès aux entités CadastreProche.
 * Permet de récupérer la liste des villes proches à partir d'un code INSEE donné.
 */
@Repository
public interface CadastreProcheRepository extends JpaRepository<CadastreProche, CadastreProcheId>{

	/**
	 * search near cities.
	 * @param insee of city to search
	 * @return list of near cities
	 */
	List<CadastreProche> findByIdInsee(String insee);
	
}