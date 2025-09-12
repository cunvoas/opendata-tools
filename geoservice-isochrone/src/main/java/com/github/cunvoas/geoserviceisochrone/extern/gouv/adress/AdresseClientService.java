package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress;

import java.util.Set;

import com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto.AdressBo;

/**
 * Service de recherche de localisation d'adresses à partir d'une requête et d'un code INSEE.
 * <p>
 * Cette interface définit la méthode pour interroger un service d'adressage externe.
 */
public interface AdresseClientService {
    /**
     * Recherche les adresses correspondant à une requête et un code INSEE.
     *
     * @param insee   le code INSEE de la commune
     * @param requete la requête d'adresse (ex: nom de rue, numéro...)
     * @return un ensemble d'adresses trouvées
     */
    Set<AdressBo> getAdresses(String insee, String requete);
}