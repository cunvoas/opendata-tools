package com.github.cunvoas.geoserviceisochrone.service.park;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionGeo;
import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcSourceEnum;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service métier pour la gestion des parcs et jardins.
 * <p>
 * Ce service fournit des méthodes pour la création, la mise à jour et la gestion des entités ParcEtJardin,
 * ainsi que leur association avec les communes, cadastres et types de parcs.
 * Les dépendances sont injectées via l'annotation @Autowired de Spring.
 */
@Service
@Slf4j
public class ParkJardinService {
	
	@Autowired 
	private ParkJardinRepository parkJardinRepository;

	@Autowired 
	private ParkAreaRepository parkAreaRepository;
	

	@Autowired 
	private ParkTypeService parkTypeService;

	@Autowired 
	private CadastreRepository cadastreRepository;
	@Autowired 
	private CityRepository cityRepository;

	@Autowired 
	private InseeCarre200mOnlyShapeRepository surfaceRepo;
	
	
	/**
	 * save ParcEtJardin.
	 * @param parcEtJardin model
	 * @param updSurfaceShape recompute surface
	 * @return ParcEtJardin
	 */
	public ParcEtJardin save(ParcEtJardin parcEtJardin, boolean updSurfaceShape) {
		
		log.warn("ParkJardinService.save");
		//fix for new park
		if (parcEtJardin.getCoordonnee()==null && parcEtJardin.getContour()!=null) {
			parcEtJardin.setCoordonnee(parcEtJardin.getContour().getCentroid());
		}
		
		if (parcEtJardin.getId()==null) {
			log.warn("ParkJardinService.save is a new one");
			// check park vs city location
			City city = parcEtJardin.getCommune();
			boolean checkIntersect = true;
			boolean match = false;
			if (city.getCoordinate()!=null && parcEtJardin.getCoordonnee()!=null) {
				Double distance = DistanceHelper.crowFlyDistance(city.getCoordinate(), parcEtJardin.getCoordonnee());
				if(distance<1) {
					// detect close to city
					checkIntersect = false;
					match=true;
				}
			}

			log.warn("ParkJardinService.save park match a city checkIntersect={} match={}", checkIntersect, match);
			
			// detect close to city but compute expensive
			if (checkIntersect) {
				Cadastre c = cadastreRepository.getReferenceById(city.getInseeCode());
				match = c.getGeoShape().contains(parcEtJardin.getCoordonnee());

				log.warn("ParkJardinService.save match with cadastre checkIntersect={} match={}", checkIntersect, match);
			}
			
			
			// it match, so change the city defined by user to not lost the park
			if (!match) {
				// get park and relocate to the good place
				Long idRegion = city.getRegion().getId();
				Long idCom2co = null;
				if (city.getCommunauteCommune()!=null) {
					idCom2co = city.getCommunauteCommune().getId();
				}
					
				
				Cadastre ca = null;
				if (idCom2co!=null) {
					ca = cadastreRepository.findMyCadastreWithComm2Co(parcEtJardin.getCoordonnee(), idCom2co);
				} else if (idRegion!=null) {
					ca = cadastreRepository.findMyCadastreWithRegion(parcEtJardin.getCoordonnee(), idRegion);
				}
				
				if (ca==null) {
					// très lent mais pas mieux
					ca = cadastreRepository.findMyCadastre(parcEtJardin.getCoordonnee());
				}
				
				if (ca!=null) {
					City ci = cityRepository.findByInseeCode(ca.getIdInsee());
					parcEtJardin.setCommune(ci);
					log.warn("PARK_NOT_IN_CITY relocation to {}", ci);
				}
				//throw new ExceptionGeo("PARK_NOT_IN_CITY");
			}
			
		} else {
			// MàJ
			ParcEtJardin prev = parkJardinRepository.getReferenceById(parcEtJardin.getId());
			if (!prev.getCommune().equals(parcEtJardin.getCommune())) {
				throw new ExceptionGeo("PARK_CHANGE_CITY");
			}
			
			ParkArea pa = parkAreaRepository.findByIdParcEtJardin(parcEtJardin.getId());
			if (pa!=null && omsChanged(parcEtJardin, pa)) {
				pa.setUpdated(new Date());
				pa.setToCompute(Boolean.TRUE);
				ParkType pt = parkTypeService.get(parcEtJardin.getTypeId());
				pa.setType(pt);
				if (parcEtJardin.getOmsCustom()==null) {
					parcEtJardin.setOmsCustom(pt.getOms());
				}
				pa.setOmsCustom(parcEtJardin.getOmsCustom());
				parkAreaRepository.save(pa);
			}
			
		}
		log.warn("ParkJardinService.save getSurface");
		Long s = null;
		if (ParcSourceEnum.AUTMEL.equals(parcEtJardin.getSource())) {
			s = surfaceRepo.getSurface(parcEtJardin.getContour());
			parcEtJardin.setSurface(s.doubleValue());
		}
		if (updSurfaceShape) {
			if (s==null) {
				s = surfaceRepo.getSurface(parcEtJardin.getContour());
			}
			parcEtJardin.setSurfaceContour(s.doubleValue());
		}
		log.warn("ParkJardinService.save saveAndFlush");
		
		
		return parkJardinRepository.saveAndFlush(parcEtJardin);
	}

	
	/**
	 * check if type or custom change.
	 * @param pj ParcEtJard 
	 * @param pa ParkArea
	 * @return data has change
	 */
	private Boolean omsChanged(ParcEtJardin pj, ParkArea pa) {
		Boolean ret = Boolean.FALSE;
		if (pa==null) {
			return ret;
		}
		ParkType paType = pa.getType();
		Boolean paOms = pa.getOmsCustom();
		
		if (paType==null ) {
			ret = Boolean.TRUE;
		} else {			
			if ( ! paType.getId().equals(pj.getTypeId()) ) {
				ret = Boolean.TRUE;
			} else {
				Boolean strict = pa.getType().getStrict();
				Boolean pjOms = pj.getOmsCustom();
				if (!strict && pjOms!=null && !pjOms.equals(paOms)) {
					ret = Boolean.TRUE;
				}
			}
		}
		
		return ret;
	}
}