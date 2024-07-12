package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Laposte;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.LaposteRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
//@ConditionalOnProperty(
//		name="application.feature-flipping.carre200m-impl", 
//		havingValue="v2")
public class ComputeServiceV2 {

	// 200m x 200m = 4 10^4: insse data is 40000+-1 accuracy
	private static final Double SURFACE_CARRE = 40_000d;
	
	@Autowired
	private LaposteRepository laposteRepository;
	@Autowired
	private CadastreRepository cadastreRepository;
	
	@Autowired
	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Autowired	//inseeCare200mRepository
	private Filosofil200mRepository filosofil200mRepository;
	@Autowired
	private InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;
	
	@Autowired
	private ParkAreaRepository parkAreaRepository;
	@Autowired
	private ParkAreaComputedRepository parkAreaComputedRepository;
	@Autowired
	private ParkJardinRepository parkJardinRepository;
	@Autowired
	private ParkTypeService parkTypeService;
	@Autowired
	private ServiceOpenData serviceOpenData;
	@Autowired
	private ParkService parkService;
	@Autowired
	private CityRepository cityRepository;
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	
	
	public void computeCarreByPostalCode(String postalCode) {
		Set<Cadastre> uniques = new HashSet<>();
		List<Laposte> postes = laposteRepository.findByPostalCode(postalCode);
		for (Laposte laposte : postes) {
			Cadastre cadastre = cadastreRepository.findById(laposte.getIdInsee()).get();
			uniques.add(cadastre);
		}
		for (Cadastre cadastre : uniques) {
			computeCarreByCadastreV2(cadastre);
		}
		
	}
	
	public void computeCarreByInseeCode(String inseeCode) {
		Cadastre cadastre = cadastreRepository.findById(inseeCode).get();
		computeCarreByCadastreV2(cadastre);
	}
	
	
	/**
	 * Compute the park surface availibilies per capita (m² per inhabitants)
	 * @param geomery
	 * @param surfaceParkAreas
	 * @return
	 */
	protected ComputeDto computePopAndDensity(Geometry polygonPark, BigDecimal surfaceParkAreas, Integer annee) {
		ComputeDto dto = new ComputeDto();

		// serface per capita
		BigDecimal surfaceParkPerCapita=BigDecimal.ZERO;
		
		// population located in all the isochrones
		Long surfacePopulationIso = 0L;
		
		// ctrl des carres proches inclus dans les isochrones
		// recherche des population sur l'ensembles des isochrones mergées
		List<InseeCarre200mOnlyShape> shapesWithIso = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(polygonPark));
		for (InseeCarre200mOnlyShape carreWithIso : shapesWithIso) {
			Filosofil200m carreData = filosofil200mRepository.findByAnneeAndIdInspire(annee, carreWithIso.getIdInspire());
			
			if (carreData!=null) {
				log.error("Filosofil200m     found,{},{}", annee, carreWithIso.getIdInspire());
				//nb habitant au carre
				Double nbHabCarre = carreData.getNbIndividus().doubleValue();
				// proratisation à la surface intersection(carre, isochrne)
				Geometry isoOnCarre = carreWithIso.getGeoShape().intersection(polygonPark);
				Long surfaceIsoSurCarre = getSurface(isoOnCarre);
				surfacePopulationIso += Math.round(nbHabCarre*surfaceIsoSurCarre/SURFACE_CARRE);
			} else {
				log.error("Filosofil200m NOT FOUND,{},{}", annee, carreWithIso.getIdInspire());
			}
		}
		if (surfacePopulationIso!=0L) {
			surfaceParkPerCapita = surfaceParkAreas.divide(BigDecimal.valueOf(surfacePopulationIso), RoundingMode.HALF_EVEN);
		}
		
		dto.setSurfacePerCapitaForIsochroneOnSquare(surfaceParkPerCapita);
		dto.setPopulationInIsochrone(BigDecimal.valueOf(surfacePopulationIso));
		return dto;
	}

	
	
	protected void computeCarreShapeV2(InseeCarre200mOnlyShape carreShape, Boolean isDense, Integer annee) {
		log.warn(">> InseeCarre200mOnlyShape {}", carreShape.getIdInspire());
		
		boolean withSkip=false;
		
		// get already computed square results
		InseeCarre200mComputedV2 computed = null;
		InseeCarre200mComputedId idPk = new InseeCarre200mComputedId();
		idPk.setAnnee(annee);
		idPk.setIdInspire(carreShape.getIdInspire());
		
		Optional<InseeCarre200mComputedV2> opt = inseeCarre200mComputedV2Repository.findById(idPk);
		if (opt.isPresent()) {
			computed = opt.get();
			
			Date currentDate = new Date();  
		    Date yesterdayDate = new Date(currentDate.getTime() - (1000 * 60 * 60 * 24));  
		    
			if (withSkip && computed.getUpdated().compareTo(yesterdayDate)>0) {
				return;
			}
			
		} else {
			// or create it
			computed = new InseeCarre200mComputedV2();
			computed.setIdInspire(carreShape.getIdInspire());
			computed.setAnnee(annee);	
		} 
		computed.setIsDense(isDense);
		
		if (carreShape.getGeoShape()==null) {
			int pause=0;
			log.error("getGeoShape is null");
		}
		
		// get insee data for the square
		Filosofil200m carre = filosofil200mRepository.findByAnneeAndIdInspire(annee, carreShape.getIdInspire());
		
		// find parks in square shape
		List<ParkArea> parkAreas = parkAreaRepository.findParkInMapArea(GeometryQueryHelper.toText(carreShape.getGeoShape()));
		parkTypeService.populate(parkAreas);
		
		// compute all surface of isochrone of parks 
		BigDecimal surfaceParkAreas=BigDecimal.ZERO;
		Geometry polygonPark = null;

		BigDecimal surfaceParkAreasOms=BigDecimal.ZERO;
		Geometry polygonParkOms = null;
		
		
		int checkOms=parkAreas.size();
		Boolean allAreOms = Boolean.FALSE;
		List<String> parcNames = new ArrayList();
		
		for (ParkArea parkArea : parkAreas) {
			log.warn("\tcompose {}", parkArea);
			
			
			ParkAreaComputed pac;
			Optional<ParkAreaComputed> Opac = parkAreaComputedRepository.findByIdAndAnnee(parkArea.getId(), annee);
			if (Opac.isPresent()) {
				pac = Opac.get();
			} else {
				pac = this.computeParkAreaV2(parkArea);
			}
			
			//decrement for all
			checkOms--;
			// prepare ALL
			surfaceParkAreas = surfaceParkAreas.add(pac.getSurface());
			
			if (polygonPark == null) {
				polygonPark = parkArea.getPolygon();
			} else {
				// merge areas for parks
				polygonPark = polygonPark.union(parkArea.getPolygon());
			}
			
			// prepare with OMS compliance
			if (pac.getOms()) {
				
				parcNames.add(parkArea.getName());
				//increment for oms
				checkOms++;
				surfaceParkAreasOms = surfaceParkAreasOms.add(pac.getSurface());
				
				if (polygonParkOms == null) {
					polygonParkOms = parkArea.getPolygon();
				} else {
					// merge areas for parks
					polygonParkOms = polygonParkOms.union(parkArea.getPolygon());
				}
			}
		}  // end merge
		
		// all parks are OMS compliant
		allAreOms = checkOms==parkAreas.size();
		
		if (carre!=null) {
			computed.setPopAll(carre.getNbIndividus());
		} else {
			computed.setPopAll(BigDecimal.ZERO);
		}
		StringBuilder sbParcName = new StringBuilder();
		if (!parcNames.isEmpty()) {
			Collections.sort(parcNames);
			for (String name : parcNames) {
				if (name!=null) {
					if (sbParcName.length()>0) {
						sbParcName.append("<br />");
					}
					sbParcName.append(" - ");
					sbParcName.append(name);
				}
			}
		}
		
		if ( polygonPark!=null ) {
			log.warn("\tprocess merge isochrone");
			//Compute all the population which is present in the isochrones of the current square.
			// and the park surface of these isochrones.
			// then, I compute the surface per capita (m²/inhabitant)
			
			ComputeDto dto = this.computePopAndDensity(polygonPark, surfaceParkAreas, annee);
			computed.setSurfaceParkPerCapita(dto.getSurfacePerCapitaForIsochroneOnSquare());
			computed.setSurfaceTotalPark(surfaceParkAreas);
			computed.setPopulationInIsochrone(dto.getPopulationInIsochrone());
			
			
			
			// Do the same but only with OMS compliant parks
			if (allAreOms) {
				// this is unusual to recompute
				computed.setSurfaceParkPerCapitaOms(dto.getSurfacePerCapitaForIsochroneOnSquare());
				computed.setSurfaceTotalParkOms(surfaceParkAreas);
				computed.setPopulationInIsochroneOms(dto.getPopulationInIsochrone());
				computed.setComments(sbParcName.toString());
				
			} else {
				if (surfaceParkAreasOms!=null && !BigDecimal.ZERO.equals(surfaceParkAreasOms)) {
					dto = this.computePopAndDensity(polygonParkOms, surfaceParkAreasOms, annee);
					computed.setSurfaceParkPerCapitaOms(dto.getSurfacePerCapitaForIsochroneOnSquare());
					computed.setSurfaceTotalParkOms(surfaceParkAreasOms);
					computed.setPopulationInIsochroneOms(dto.getPopulationInIsochrone());
					computed.setComments(sbParcName.toString());
				} else {
					computed.setSurfaceParkPerCapitaOms(BigDecimal.ZERO);
					computed.setSurfaceTotalParkOms(BigDecimal.ZERO);
					computed.setPopulationInIsochroneOms(BigDecimal.ZERO) ;
				}
			}
			
			
			
			// compute surface with accessible parks
			Geometry parkOnCarre = carreShape.getGeoShape().intersection(polygonPark);
			Long surfaceParkAccess = getSurface(parkOnCarre);
			
			Double inhabitant = computed.getPopAll().doubleValue();
			
			// protata des surfces pour habitants avec un parc
			Long popIn = Math.round(inhabitant*surfaceParkAccess/SURFACE_CARRE);
			computed.setPopIncluded(new BigDecimal(popIn));
			computed.setPopExcluded(new BigDecimal(inhabitant-popIn));

			// same for OMS parks
			if (allAreOms) {
				computed.setPopIncludedOms(computed.getPopIncluded());
				computed.setPopExcludedOms(computed.getPopExcluded());
				
			} else {
				// OMS <> opendata
				if (surfaceParkAreasOms!=null && !BigDecimal.ZERO.equals(surfaceParkAreasOms)) {
					// compute surface with accessible parks
					parkOnCarre = carreShape.getGeoShape().intersection(polygonParkOms);
					surfaceParkAccess = getSurface(parkOnCarre);

					// protata des surfaces pour habitants avec un parc
					popIn = Math.round(inhabitant*surfaceParkAccess/SURFACE_CARRE);
					computed.setPopIncludedOms(new BigDecimal(popIn));
					computed.setPopExcludedOms(new BigDecimal(inhabitant-popIn));
				} else {
					computed.setPopIncludedOms(BigDecimal.ZERO);
					computed.setPopExcludedOms(new BigDecimal(popIn));
				}
			}
				

			

			
		} else {

			// no parks is accesible
			computed.setPopIncluded(BigDecimal.ZERO);
			computed.setPopExcluded(computed.getPopAll());
			computed.setSurfaceTotalPark(BigDecimal.ZERO);
			computed.setSurfaceParkPerCapita(BigDecimal.ZERO);
		}
		log.warn("\tsave computed {}\n", computed.getIdInspire());
		computed.setUpdated(new Date());
		inseeCarre200mComputedV2Repository.save(computed);
		
	}
	
	/**
	 * @param cadastre
	 * @TODO optimise / annee
	 */
	public void computeCarreByParkV2(ParkArea parkArea, Integer annee) {
		log.warn(">> computeCarreByParkV2");
		
		Boolean isDense = Boolean.TRUE;
		Optional<ParcEtJardin> opj = parkJardinRepository.findById(parkArea.getIdParcEtJardin());
		if (opj.isPresent()) {
			isDense = serviceOpenData.isDistanceDense(opj.get().getCommune().getInseeCode());
		}

		// find all square in city area
		List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(parkArea.getPolygon()));
		

		// iterate on each
		for (InseeCarre200mOnlyShape carreShape : shapes) {
			computeCarreShapeV2(carreShape, isDense, annee);
		}	
		log.warn("<< computeCarreByParkV2");
		
	}
	
	/**
	 * Computes population that can access a park at once.
	 * @param cadastre
	 * 
	 * Algorithm:
	 *  step 1: Find area of the city.
	 *  step 2: Find 200m squares that matches.
	 *  step 3: for each, check and compute parks.
	 */
	@Transactional
	public void computeCarreByCadastreV2(Cadastre cadastre) {
		
		log.warn(">> computeCarreByCadastreV2");
		
		Boolean isDense = serviceOpenData.isDistanceDense(cadastre.getIdInsee());
		
		List<Integer> annnes = List.of(applicationBusinessProperties.getInseeAnnees());
		
		// find all square in city area
		List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(cadastre.getGeoShape()));
		
		for (Integer annne : annnes) {
			// iterate on each
			for (InseeCarre200mOnlyShape carreShape : shapes) {
				computeCarreShapeV2(carreShape, isDense, annne);
			}	
		}

		log.warn("<< computeCarreByCadastreV2");
	}

	
	/**
	 *  Used for mass update and full recompute.
	 *  @param inseeCode
	 */
	public void refreshParkEntrances(String inseeCode) {
		Cadastre cadastre = cadastreRepository.findById(inseeCode).get();
		refreshParkEntrances(cadastre);
	}
	
	/**
	 * Used for mass update and full recompute.
	 * @param cadastre
	 */
	@Transactional
	public void refreshParkEntrances(Cadastre cadastre) {
		log.warn(">> refreshParkEntrances");

		City city = cityRepository.findByInseeCode(cadastre.getIdInsee());
		String distance = serviceOpenData.getDistanceDense(city);
		
		List<ParcEtJardin> pjs = parkJardinRepository.findByCityId(city.getId());
		for (ParcEtJardin parcEtJardin : pjs) {
			ParkArea pa = parkAreaRepository.findByIdParcEtJardin(parcEtJardin.getId());
			
			for (ParkEntrance pe : pa.getEntrances()) {
				parkService.refreshIsochrone(pe, distance);
			}
			parkService.mergeParkAreaEntrance(pa);
		}
		log.warn("<< refreshParkEntrances");
	}

	/**
	 * Compute ParkEntrance from ParkArea and List<ParkEntrance>.
	 * @param park
	 * @return
	 * @TODO to be reviewed
	 */
	public ParkAreaComputed computeParkAreaV2(ParkArea park) {
		ParkAreaComputed parcCpu=null;
		log.info("computePark( {}-{} )",park.getId(), park.getName());
		
		// skip un-precalculated park
		if(park.getPolygon()==null) {
			log.warn("please process {}-{}  first", park.getId(), park.getName());
			return null;
//			throw new ExceptionGeo("park not merge"+park.toString());
		}
		
		// get type parameters
		parkTypeService.populate(park);
		
		List<Integer> annees = List.of(applicationBusinessProperties.getInseeAnnees());
		
		
		// find carre200m shapes that match the park
		List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(park.getPolygon()));
		
		for (Integer annee : annees) {
		
			Optional<ParkAreaComputed> parcCpuOpt = parkAreaComputedRepository.findByIdAndAnnee(park.getId(), annee);
			if (parcCpuOpt.isPresent()) {
				parcCpu = parcCpuOpt.get();
			} else {
				parcCpu = new ParkAreaComputed();
				parcCpu.setId(park.getId());
				parcCpu.setAnnee(annee);
			}
	
			// process OMS feature
			ParkType type = park.getType();
			if (type.getStrict()) {
				parcCpu.setOms(type.getOms());
			} else {
				parcCpu.setOms(type.getOms());
				
				if (park.getOmsCustom()!=null) {
					parcCpu.setOms(park.getOmsCustom());
				}
			}
			
			Optional<ParcEtJardin> pjOpt = parkJardinRepository.findById(park.getIdParcEtJardin());
			if (pjOpt.isPresent()) {
				ParcEtJardin pj = pjOpt.get();
				parcCpu.setSurface(new BigDecimal(Math.round(pj.getSurface())));
				
				Boolean isDense = serviceOpenData.isDistanceDense(pj.getCommune());
				parcCpu.setIsDense(isDense);
			}
			
			
			
			// surface intersection algorithm
			BigDecimal population = BigDecimal.ZERO;
			for (InseeCarre200mOnlyShape carreShape : shapes) {
				Geometry parkOnCarre = carreShape.getGeoShape().intersection(park.getPolygon());
				Long surfIntersect = getSurface(parkOnCarre);
				
				//lookup for carre200m data
				Filosofil200m carre = filosofil200mRepository.findByAnneeAndIdInspire(annee, carreShape.getIdInspire());
				Long popCar = 0L;
				if (carre!=null) {
					popCar =Math.round(carre.getNbIndividus().doubleValue());
				}
				Long popIntersect = Math.round(popCar*surfIntersect/SURFACE_CARRE);
				population = population.add(new BigDecimal(popIntersect));
			}
			parcCpu.setPopulation(population);
			if (! BigDecimal.ZERO.equals(population)) {
				parcCpu.setSurfacePerInhabitant(parcCpu.getSurface().divide(population, 1, RoundingMode.HALF_EVEN));
			}
			parcCpu.setUpdated(new Date());
			
			log.warn("surface per inhabitant: {}- {}", annee, parcCpu.getSurfacePerInhabitant());
			parcCpu = parkAreaComputedRepository.save(parcCpu);
		
		}
		return parcCpu;
	}
	
	public Long getSurface(Geometry geom) {
		return inseeCarre200mOnlyShapeRepository.getSurface(geom);
	}

}
