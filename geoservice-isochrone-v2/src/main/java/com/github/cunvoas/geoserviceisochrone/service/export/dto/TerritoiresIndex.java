package com.github.cunvoas.geoserviceisochrone.service.export.dto;

import java.util.List;

/**
 * Index des territoires publiés, écrit dans data/territoires.json du dépôt de données.
 * Consommé par le front pour les routes territoriales, le sitemap et le pré-rendu.
 *
 * @param communes communes disposant de statistiques publiées
 * @param epcis    EPCI à fiscalité propre disposant de statistiques publiées
 */
public record TerritoiresIndex(List<Commune> communes, List<Epci> epcis) {

	/**
	 * Commune publiée.
	 *
	 * @param insee      code INSEE
	 * @param id         identifiant technique (dossier de publication)
	 * @param nom        nom affichable
	 * @param population population totale issue des statistiques
	 */
	public record Commune(String insee, String id, String nom, int population) {
	}

	/**
	 * EPCI publié.
	 *
	 * @param siren   numéro Siren
	 * @param nom     nom de l'EPCI
	 * @param dossier dossier de statistiques (id technique)
	 */
	public record Epci(String siren, String nom, String dossier) {
	}
}
