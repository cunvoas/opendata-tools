package com.github.cunvoas.geoserviceisochrone.service.opendata;

import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeDensiteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeDensiteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.RegionRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service métier pour la gestion des données ouvertes liées aux régions,
 * communautés de communes, villes et informations cadastrales.
 * <p>
 * Ce service fournit des méthodes pour :
 * <ul>
 *   <li>Calculer la distance à pied à retenir selon l'OMS et la densité urbaine</li>
 *   <li>Vérifier si une ville ou un code INSEE est considéré comme dense</li>
 *   <li>Enregistrer ou mettre à jour des entités Region et CommunauteCommune</li>
 *   <li>Calculer le carré englobant (enveloppe) d'une communauté sur la carte</li>
 * </ul>
 *
 * Les dépendances sont injectées via l'annotation @Autowired de Spring.
 */
@Service
@Slf4j
public class ServiceOpenData {

	@Autowired
	private CadastreRepository cadastreRepository;
	
	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private InseeDensiteCommuneRepository inseeDensiteCommuneRepository;
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
    /**
     * Calcule la distance à pied à retenir selon l'OMS et la densité urbaine.
     *
     * @param city la ville pour laquelle calculer la distance
     * @return la distance en mètres (String)
     */
	public String getDistanceDense(City city) {
		String ret = "300";
		Optional<InseeDensiteCommune> idc = inseeDensiteCommuneRepository.findById(city.getInseeCode());
		if (idc.isPresent()) {
			// CodeDensite pix contenie plusieurs code insee
			String cd = idc.get().getCodeDensite();
			if (applicationBusinessProperties.getInseeCodeDensite().indexOf(cd) == -1) {
				ret = applicationBusinessProperties.getOmsSubUrbanDistance();
			} else {
				ret = applicationBusinessProperties.getOmsUrbanDistance();
			}
		}
		return ret;
	}
	
    /**
     * Détermine si un code INSEE correspond à une zone dense.
     *
     * @param idInsee le code INSEE de la ville
     * @return TRUE si dense, FALSE sinon
     */
	public Boolean isDistanceDense(String idInsee) {
		Boolean isdense=Boolean.TRUE;
		Optional<InseeDensiteCommune> idc = inseeDensiteCommuneRepository.findById(idInsee);
		if (idc.isPresent()) {
			String cd = idc.get().getCodeDensite();
			if (applicationBusinessProperties.getInseeCodeDensite().indexOf(cd)==-1) {
				isdense=Boolean.FALSE;
			} else {
				isdense=Boolean.TRUE;
			}
		} else {
			log.warn("City not found, insee={}", idInsee);
		}
		return isdense;
	}
	
    /**
     * Détermine si une ville est considérée comme dense.
     *
     * @param city la ville à vérifier
     * @return TRUE si dense, FALSE sinon
     */
	public Boolean isDistanceDense(City city) {
		return isDistanceDense(city.getInseeCode());
	}

    /**
     * Enregistre ou met à jour une entité Region.
     * Si la région existe (par id ou nom), met à jour son nom ; sinon, crée une nouvelle région.
     *
     * @param region la région à enregistrer ou mettre à jour
     * @return la région enregistrée ou mise à jour
     */
	public Region save(Region region) {
		if (region != null) {
			Region pRegion = null;
			Optional<Region> oRegion = Optional.of(new Region());

			if (region.getId() != null) {
				oRegion = regionRepository.findById(region.getId());
				if (oRegion.isPresent()) {
					pRegion = oRegion.get();
					pRegion.setName(region.getName());

				}

			} else if (region.getName() != null) {
				pRegion = regionRepository.findByName(region.getName());

			}

			region = regionRepository.save(pRegion);
		} else {
			region = regionRepository.save(region);
		}
		return region;

	}

    /**
     * Enregistre ou met à jour une entité CommunauteCommune.
     * Si la communauté existe (par id), met à jour son nom ; sinon, crée une nouvelle communauté.
     *
     * @param comm2co la communauté à enregistrer ou mettre à jour
     * @return la communauté enregistrée ou mise à jour
     */
	public CommunauteCommune save(CommunauteCommune comm2co) {
		if (comm2co != null) {
			CommunauteCommune pComm2co = null;
			if (comm2co.getId() != null) {
				Optional<CommunauteCommune> oComm2co = communauteCommuneRepository.findById(comm2co.getId());
				if (oComm2co.isPresent()) {
					pComm2co = oComm2co.get();
					pComm2co.setName(comm2co.getName());
				}

				comm2co = communauteCommuneRepository.save(pComm2co);
			} else {
				comm2co = communauteCommuneRepository.save(comm2co);
			}
		}
		return comm2co;
	}
	
    /**
     * Calcule le carré englobant (enveloppe) sur la carte contenant toutes les villes
     * de la communauté donnée. Met à jour la communauté avec cette géométrie.
     *
     * @param comm2co la communauté pour laquelle calculer le carré englobant
     * @return le polygone représentant l'enveloppe calculée
     */
	public Polygon computeSquareOnMap(CommunauteCommune comm2co) {
		Polygon poly=null;
		for (City city : comm2co.getCities()) {
			Optional<Cadastre> opt = cadastreRepository.findById(city.getInseeCode());
			if (opt.isPresent()) {
				Geometry shape = opt.get().getGeoShape();
				if (shape!=null) {
					// extraction de la boite depuis le cadastre
					Polygon p= (Polygon)shape.getEnvelope();
					
					if (poly==null) {
						poly = p;
					} else {
						// union de l boite précédante avec le cadastre de la ville actuelle.
						// et recalcul de la nouvelle boite
						poly= (Polygon) poly.union(p).getEnvelope();
					}
				}
			}
		}
		comm2co.setCarreCarte(poly);
		communauteCommuneRepository.save(comm2co);
		return poly;
	}
}
