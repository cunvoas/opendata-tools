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

/**
 * Business Service impl.
 */
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
	
	
	
	/**
	 * getRegion.
	 * @return list Region
	 */
	public List<Region> getRegion() {
		return regionRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	
	/**
	 * getParkAreaComputedById.
	 * @param id ParkAreaComputed
	 * @return ParkAreaComputed
	 */
	public ParkAreaComputed getParkAreaComputedById(Long id) {
		return this.getParkAreaComputedById(id, applicationBusinessProperties.getDerniereAnnee());
	}
	/**
	 * getParkAreaComputedById.
	 * @param id ParkAreaComputed
	 * @param annee year
	 * @return ParkAreaComputed
	 */
	public ParkAreaComputed getParkAreaComputedById(Long id, Integer annee) {
		Optional<ParkAreaComputed> opt = parkAreaComputedRepository.findByIdAndAnnee(id, annee);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}
	/**
	 * getParkAreaById.
	 * @param id ParkArea
	 * @return ParkArea
	 */
	public ParkArea getParkAreaById(Long id) {
		Optional<ParkArea> opt = parkAreaRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}
	/**
	 * getByIdParcEtJardin.
	 * @param id ParcEtJardin
	 * @return ParkArea
	 */
	public ParkArea getByIdParcEtJardin(Long id) {
		return  parkAreaRepository.findByIdParcEtJardin(id);
	}
	
	
	/**
	 * getCommunauteCommune.
	 * @return list CommunauteCommune
	 */
	public List<CommunauteCommune> getCommunauteCommune() {
		return communauteCommuneRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}
	/**
	 * getCommunauteByRegionId.
	 * @param id Region
	 * @return list CommunauteCommune
	 */
	public List<CommunauteCommune> getCommunauteByRegionId(Long id) {
		return communauteCommuneRepository.findByRegionId(id);
	}
	
	/**
	 * getCityByRegionId.
	 * @param id Region
	 * @return list City
	 */
	public List<City> getCityByRegionId(Long id) {
		return cityRepository.findByRegionId(id);
	}
	
	/**
	 * getCityById.
	 * @param id City
	 * @return City
	 */
	public City getCityById(Long id) {
		Optional<City> opt=cityRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}


	/**
	 * getCity.
	 * @param id City
	 * @return City
	 */
	public City getCity(Long id) {
		return cityRepository.getReferenceById(id);
	}
	
	/**
	 * getCoordinate.
	 * @param id Coordinate
	 * @return Coordinate
	 */
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
	
	
	/**
	 * getCityByCommunauteCommuneId.
	 * @param id CommunauteCommune
	 * @return list City
	 */
	public List<City> getCityByCommunauteCommuneId(Long id) {
		return cityRepository.findByCommunauteCommuneId(id);
	}

	/**
	 * getParcEtJardinByCityId.
	 * @param id City
	 * @return list ParcEtJardin
	 */
	public List<ParcEtJardin> getParcEtJardinByCityId(Long id) {
		return parkJardinRepository.findByCityId(id);
	}
	/**
	 * getParcEtJardinByCityId.
	 * @param idCommune City
	 * @param pageable page
	 * @return list ParcEtJardin
	 */
	public Page<ParcEtJardin> getParcEtJardinByCityId(Long idCommune, Pageable pageable) {
		return parkJardinRepository.findByCityId(idCommune, pageable);
	}
	
	/**
	 * getParcEtJardinByComm2coId.
	 * @param idComm2co CommunauteCommune
	 * @param parkCase use case
	 * @param pageable page
	 * @return list ParcEtJardin
	 */
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
	/**
	 * getParcEtJardinByCityId.
	 * @param id city
	 * @param parkCase use case
	 * @param pageable page
	 * @return list ParcEtJardin
	 */
	public Page<ParcEtJardin> getParcEtJardinByCityId(Long id, String parkCase, Pageable pageable) {
		if ("merge".equalsIgnoreCase(parkCase)) {
			return parkJardinRepository.findByCityIdToMerge(id, pageable);
		} else if ("compute".equalsIgnoreCase(parkCase)) {
			return parkJardinRepository.findByCityIdToCompute(id, pageable);
		} else {
			return parkJardinRepository.findByCityId(id, pageable);
		}
	}
	
	/**
	 * getParcEtJardinById.
	 * @param id ParcEtJardin
	 * @return ParcEtJardin
	 */
	public ParcEtJardin getParcEtJardinById(Long id) {
		Optional<ParcEtJardin> opt=parkJardinRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
	}

	/**
	 * getParcPrefectureById.
	 * @param id ParcPrefecture
	 * @return ParcPrefecture
	 */
	public Optional<ParcPrefecture> getParcPrefectureById(Long id) {
		Optional<ParcPrefecture> opt=parcPrefectureRepository.findById(id);
		return opt;
	}
	
	/**
	 * getParcPrefectureByParcEtJardinId.
	 * @param id  ParcEtJardin
	 * @return ParcPrefecture
	 */
	public ParcPrefecture getParcPrefectureByParcEtJardinId(Long id) {
		List<ParcPrefecture> lst=parcPrefectureRepository.findByParcEtJardinId(id);
		if (!CollectionUtils.isEmpty(lst)) {
			return lst.get(0);
		}
		return null;
	}

	/**
	 * getEntranceByParkId.
	 * @param id Park
	 * @return list ParkEntrance
	 */
	public List<ParkEntrance> getEntranceByParkId(Long id) {
		return parkEntranceRepository.findByParkId(id);
	}
	/**
	 * getEntranceById.
	 * @param id ParkEntrance
	 * @return ParkEntrance
	 */
	public ParkEntrance getEntranceById(Long id) {
		Optional<ParkEntrance> opt=parkEntranceRepository.findById(id);
		if (opt.isPresent()) {
			return opt.get();
		}
		return null;
		
	}
	
	/**
	 * getParcSource.
	 * @return list enum
	 */
	public List<ParcSourceEnum> getParcSource() {
		List<ParcSourceEnum> l = new ArrayList<>();
		l.add(ParcSourceEnum.OPENDATA);
		l.add(ParcSourceEnum.PREFECTURE);
		l.add(ParcSourceEnum.AUTMEL);
		return l;
	}
}
