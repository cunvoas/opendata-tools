package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress;

import java.util.Set;

import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto.AdressBo;

/**
 * Service de recherche de localisation.
 */
public interface AdresseClientService {
	
	Set<AdressBo> getAdresses(String insee, String requete);

}
