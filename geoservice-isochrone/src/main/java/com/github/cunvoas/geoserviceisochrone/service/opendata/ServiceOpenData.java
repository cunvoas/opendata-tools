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
 * Business Service impl.
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
	 * calcul de la distance à pied à retenir VS OMS et la densité urbaine.
	 * @param city
	 * @return
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
	 * @param idInsee
	 * @return
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
	public Boolean isDistanceDense(City city) {
		return isDistanceDense(city.getInseeCode());
	}

	public Region save(Region region) {
		if (region != null) {
			Region pRegion = null;
			Optional<Region> oRegion = null;

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
	 * calcule le carré dans lequel tiends la communauté de commune sur la carte.
	 * @param comm2co
	 * @return
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
