package com.github.cunvoas.geoserviceisochrone.service.opendata;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeDensiteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeDensiteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.RegionRepository;

@Service
public class ServiceOpenData {

	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private InseeDensiteCommuneRepository inseeDensiteCommuneRepository;
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	public String getDistanceDense(City city) {
		String ret = "300";
		Optional<InseeDensiteCommune> idc = inseeDensiteCommuneRepository.findById(city.getInseeCode());
		if (idc.isPresent()) {
			String cd = idc.get().getCodeDensite();
			if (applicationBusinessProperties.getInseeCodeDensite().indexOf(cd)==-1) {
				ret = applicationBusinessProperties.getOmsSubUrbanDistance();
			} else {
				ret = applicationBusinessProperties.getOmsUrbanDistance();
			}
		}
		return ret;
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
}
