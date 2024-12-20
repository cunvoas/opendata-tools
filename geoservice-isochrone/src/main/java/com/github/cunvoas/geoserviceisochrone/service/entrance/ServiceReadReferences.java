package com.github.cunvoas.geoserviceisochrone.service.entrance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.Coordinate;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Laposte;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.LaposteRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParcPrefectureRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.RegionRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceReadReferences {
	

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	@Autowired
	private RegionRepository regionRepository;
	@Autowired
	private CommunauteCommuneRepository communauteCommuneRepository;
	@Autowired
	private CityRepository cityRepository;
	@Autowired
	private CadastreRepository cadastreRepository;
	@Autowired
	private LaposteRepository laposteRepository;
	@Autowired
	private ParkJardinRepository parkJardinRepository;
	@Autowired
	private ParkAreaRepository parkAreaRepository;
	@Autowired
	private ParkAreaComputedRepository parkAreaComputedRepository;
	@Autowired
	private ParkEntranceRepository parkEntranceRepository;
	@Autowired
	private ParcPrefectureRepository parcPrefectureRepository;
	
	
	
	public List<Region> getRegion() {
		return regionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	
	public ParkAreaComputed getParkAreaComputedById(Long id) {
		return this.getParkAreaComputedById(id, applicationBusinessProperties.getDerniereAnnee());
	}
	public ParkAreaComputed getParkAreaComputedById(Long id, Integer annee) {
		Optional<ParkAreaComputed> opt = parkAreaComputedRepository.findByIdAndAnnee(id, annee);
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

	public City getCity(Long id) {
		return cityRepository.getReferenceById(id);
	}
	
	public Coordinate getCoordinate(Long id) {
		Coordinate location=null;
		
		if (id!=null) {
			City city = cityRepository.getReferenceById(id);
			
			Cadastre cadastre=cadastreRepository.getReferenceById(city.getInseeCode());
			if (cadastre!=null && cadastre.getGeoShape()!=null) {
				org.locationtech.jts.geom.Coordinate coord=null;
				if ( cadastre.getGeoShape() instanceof Point) {
					coord = ((Point)cadastre.getGeoShape()).getCoordinate();
				} else if ( cadastre.getGeoShape() instanceof Polygon) {
					coord = ((Polygon)cadastre.getGeoShape()).getCentroid().getCoordinate();
				} else if ( cadastre.getGeoShape() instanceof MultiPolygon) {
					coord = ((MultiPolygon)cadastre.getGeoShape()).getCentroid().getCoordinate();
				}
				
				if (coord!=null) {
					location = new Coordinate(coord.getX(), coord.getY());
				}
			}
		
			if (location==null) {
				Laposte poste = laposteRepository.getReferenceById(city.getInseeCode());
				if (poste!=null) {
					String gps = poste.getCoordonneesGps();
					String[] coord = gps.split(", ");
					location = new Coordinate(Double.valueOf(coord[1]), Double.valueOf(coord[1]));
				}
			}
		}
		return location;
	}
	
	
	public List<City> getCityByCommunauteCommuneId(Long id) {
		return cityRepository.findByCommunauteCommuneId(id);
	}

	public List<ParcEtJardin> getParcEtJardinByCityId(Long id) {
		return parkJardinRepository.findByCityId(id);
	}
	public Page<ParcEtJardin> getParcEtJardinByCityId(Long idCommune, Pageable pageable) {
		return parkJardinRepository.findByCityId(idCommune, pageable);
	}
	
	public Page<ParcEtJardin> getParcEtJardinByComm2coId(Long idComm2co, String parkCase, Pageable pageable) {
		Page<ParcEtJardin> rets = null;
		
		if ("merge".equalsIgnoreCase(parkCase)) {
			rets = parkJardinRepository.findByComm2CoIdToMerge(idComm2co, pageable);
		} else if ("compute".equalsIgnoreCase(parkCase)) {
			rets = parkJardinRepository.findByComm2CoIdToCompute(idComm2co, pageable);
		} else {
			rets = parkJardinRepository.findByComm2CoId(idComm2co, pageable);
		}
		
		return rets;
	}
	public Page<ParcEtJardin> getParcEtJardinByCityId(Long id, String parkCase, Pageable pageable) {
		if ("merge".equalsIgnoreCase(parkCase)) {
			return parkJardinRepository.findByCityIdToMerge(id, pageable);
		} else if ("compute".equalsIgnoreCase(parkCase)) {
			return parkJardinRepository.findByCityIdToCompute(id, pageable);
		} else {
			return parkJardinRepository.findByCityId(id, pageable);
		}
	}
	
	public ParcEtJardin getParcEtJardinById(Long id) {
		Optional<ParcEtJardin> opt=parkJardinRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	public Optional<ParcPrefecture> getParcPrefectureById(Long id) {
		Optional<ParcPrefecture> opt=parcPrefectureRepository.findById(id);
		return opt;
	}
	
	public ParcPrefecture getParcPrefectureByParcEtJardinId(Long id) {
		List<ParcPrefecture> lst=parcPrefectureRepository.findByParcEtJardinId(id);
		if (!CollectionUtils.isEmpty(lst)) {
			return lst.get(0);
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
	
	public List<ParcSourceEnum> getParcSource() {
		List<ParcSourceEnum> l = new ArrayList<>();
		l.add(ParcSourceEnum.OPENDATA);
		l.add(ParcSourceEnum.PREFECTURE);
		l.add(ParcSourceEnum.AUTMEL);
		return l;
	}
}
