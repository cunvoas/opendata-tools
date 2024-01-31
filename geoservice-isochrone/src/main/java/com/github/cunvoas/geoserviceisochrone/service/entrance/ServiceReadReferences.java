package com.github.cunvoas.geoserviceisochrone.service.entrance;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.RegionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceReadReferences {
	
	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private CityRepository cityRepository;
	@Autowired
	private ParkJardinRepository parkJardinRepository;
	@Autowired
	private ParkAreaRepository parkAreaRepository;
	@Autowired
	private ParkAreaComputedRepository parkAreaComputedRepository;
	@Autowired
	private ParkEntranceRepository parkEntranceRepository;
	
	public List<Region> getRegion() {
		return regionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	
	public ParkAreaComputed getParkAreaComputedById(Long id) {
		Optional<ParkAreaComputed> opt = parkAreaComputedRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}
	public ParkArea getParkAreaById(Long id) {
		Optional<ParkArea> opt = parkAreaRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}
	public ParkArea getByIdParcEtJardin(Long id) {
		return  parkAreaRepository.findByIdParcEtJardin(id);
	}
	
	
	public List<CommunauteCommune> getCommunauteCommune() {
		return communauteCommuneRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
//	public List<CommunauteCommune> getCommunauteByRegion(Region region) {
//		return this.getCommunauteByRegionId(region.getId());
//	}
	public List<CommunauteCommune> getCommunauteByRegionId(Long id) {
		return communauteCommuneRepository.findByRegionId(id);
	}
	
	
//	public List<City> getCityByRegion(Region region) {
//		return this.getCityByRegionId(region.getId());
//	}
	public List<City> getCityByRegionId(Long id) {
		return cityRepository.findByRegionId(id);
	}
	public City getCityById(Long id) {
		Optional<City> opt=cityRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

//	public List<City> getCityByCommunauteCommune(CommunauteCommune communauteCommune) {
//		return this.getCityByCommunauteCommuneId(communauteCommune.getId());
//	}
	public List<City> getCityByCommunauteCommuneId(Long id) {
		return cityRepository.findByCommunauteCommuneId(id);
	}

	public List<ParcEtJardin> getParcEtJardinByCityId(Long id) {
		return parkJardinRepository.findByCityId(id);
	}
	public Page<ParcEtJardin> getParcEtJardinByCityId(Long id, Pageable pageable) {
		return parkJardinRepository.findByCityId(id, pageable);
	}
	
	public ParcEtJardin getParcEtJardinById(Long id) {
		Optional<ParcEtJardin> opt=parkJardinRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	public List<ParkEntrance> getEntranceByParkId(Long id) {
		return parkEntranceRepository.findByParkId(id);
	}
	public ParkEntrance getEntranceById(Long id) {
		Optional<ParkEntrance> opt=parkEntranceRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
		
	}
}
