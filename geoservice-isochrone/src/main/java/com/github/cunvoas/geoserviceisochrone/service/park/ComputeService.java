package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.exception.ExceptionGeo;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Laposte;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.LaposteRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import jakarta.transaction.Transactional;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ComputeService {

	// 200m x 200m = 4 10^4
	private static final Double SURFACE_CARRE = 40_000d;
	
	@Autowired
	private LaposteRepository laposteRepository;
	@Autowired
	private CadastreRepository cadastreRepository;
	
	@Autowired
	private InseeCarre200mShapeRepository inseeCarre200mShapeRepository;
	@Autowired
	private InseeCarre200mRepository inseeCare200mRepository;
	@Autowired
	private InseeCarre200mComputedRepository inseeCarre200mComputedRepository;
	
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
	 * Computes population that can access a park at once.
	 * @param cadastre
	 * 
	 * Algorithm:
	 *  step 1: Find area of the city.
	 *  step 2: Find 200m squares that matches.
	 *  step 3: for each, check and compute parks.
	 *  @deprecated
	 */
//	public void computeCarreByCadastre(Cadastre cadastre) {
//		
//		// find all square in city area
//		List<InseeCarre200mShape> shapes = inseeCarre200mShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(cadastre.getGeoShape()));
//		
//		// iterate on each
//		for (InseeCarre200mShape carreShape : shapes) {
//			
//			// get already computed square results
//			InseeCarre200mComputed computed = null;
//			Optional<InseeCarre200mComputed> opt = inseeCarre200mComputedRepository.findById(carreShape.getIdCarreHab());
//			if (opt.isPresent()) {
//				computed = opt.get();
//			} else {
//				// or create it
//				computed = new InseeCarre200mComputed();
//				computed.setIdCarre200(carreShape.getIdCarreHab());
//			} 
//			
//			// get insee data for the square
//			InseeCarre200m carre = inseeCare200mRepository.findById(carreShape.getIdCarreHab()).get();
//			
//			// find parks in square shape
//			List<ParkArea> parkAreas = parkAreaRepository.findParkInMapArea(GeometryQueryHelper.toText(carreShape.getGeoShape()));
//			
//			BigDecimal sumParkArea=BigDecimal.ZERO;
//			BigDecimal sumPopulation=BigDecimal.ZERO;
//			
//			// compute all surface of isochrone of parks 
//			Geometry polygonPark = null;
//			for (ParkArea parkArea : parkAreas) {
//				ParkAreaComputed pac = parkAreaComputedRepository.findById(parkArea.getId()).get();
//				sumParkArea = sumParkArea.add(pac.getSurface());
//				sumPopulation = sumPopulation.add(pac.getPopulation());
//				// merge areas for parks
//				if (polygonPark == null) {
//					polygonPark = parkArea.getPolygon();
//				} else {
//					polygonPark = polygonPark.union(parkArea.getPolygon());
//				}
//			}
//			
//			
//			Long surfaceParkAccess = 0L;
//			Double inhabitant = 0d;
//			
//			if (StringUtils.isNotBlank(carre.getPopulation())) {
//				inhabitant = Double.valueOf(carre.getPopulation());
//				computed.setPopAll(new BigDecimal(Math.round(inhabitant)));
//			}
//			
//			if (polygonPark!=null ) {
//
//				// compute surface intersects with parks
//				Geometry parkOnCarre = carreShape.getGeoShape().intersection(polygonPark);
//				surfaceParkAccess = getSurface(parkOnCarre);
//		
//				if (StringUtils.isNotBlank(carre.getPopulation())) {
//					// fix eventual round error
//					inhabitant = computed.getPopAll().doubleValue();
//					
//					Long popIn = Math.round(inhabitant*surfaceParkAccess/SURFACE_CARRE);
//					computed.setPopIncluded(new BigDecimal(popIn));
//					computed.setPopExcluded(new BigDecimal(inhabitant-popIn));
//				} else {
//					computed.setPopAll(BigDecimal.ZERO);
//					computed.setPopIncluded(BigDecimal.ZERO);
//					computed.setPopExcluded(BigDecimal.ZERO);
//				}
//			} else {
//
//				// no parks is accesible
//				computed.setPopIncluded(BigDecimal.ZERO);
//				computed.setPopExcluded(computed.getPopAll());
//			}
//			
//			computed.setUpdated(new Date());
//			inseeCarre200mComputedRepository.save(computed);
//		
//		}	
//	}
	
	/**
	 * Compute the park surface availibilies per capita (m² per inhabitants)
	 * @param geomery
	 * @param surfaceParkAreas
	 * @return
	 */
	protected ComputeDto computePopAndDensity(Geometry polygonPark, BigDecimal surfaceParkAreas) {
		ComputeDto dto = new ComputeDto();

		// serface per capita
		BigDecimal surfaceParkPerCapita=BigDecimal.ZERO;
		
		// population located in all the isochrones
		Long surfacePopulationIso = 0L;
		
		// ctrl des carres proches inclus dans les isochrones
		// recherche des population sur l'ensembles des isochrones mergées
		List<InseeCarre200mShape> shapesWithIso = inseeCarre200mShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(polygonPark));
		for (InseeCarre200mShape carreIso : shapesWithIso) {
			//nb habitant au carre
			Double nbHabCarre = carreIso.getNbHabCarre();
			// proratisation à la surface intersection(carre, isochrne)
			Geometry isoOnCarre = carreIso.getGeoShape().intersection(polygonPark);
			Long surfaceIsoSurCarre = getSurface(isoOnCarre);
			surfacePopulationIso += Math.round(nbHabCarre*surfaceIsoSurCarre/SURFACE_CARRE);
		}
		if (surfacePopulationIso!=0L) {
			surfaceParkPerCapita = surfaceParkAreas.divide(BigDecimal.valueOf(surfacePopulationIso), RoundingMode.HALF_EVEN);
		}
		
		dto.setSurfacePerCapitaForIsochroneOnSquare(surfaceParkPerCapita);
		dto.setPopulationInIsochrone(BigDecimal.valueOf(surfacePopulationIso));
		return dto;
	}

	
	
	protected void computeCarreShapeV2(InseeCarre200mShape carreShape, Boolean isDense) {
		log.warn(">> InseeCarre200mShape {}", carreShape.getIdCarreHab());
		
		boolean withSkip=false;
		
		// get already computed square results
		InseeCarre200mComputed computed = null;
		Optional<InseeCarre200mComputed> opt = inseeCarre200mComputedRepository.findById(carreShape.getIdCarreHab());
		if (opt.isPresent()) {
			computed = opt.get();
			
			Date currentDate = new Date();  
		    Date yesterdayDate = new Date(currentDate.getTime() - (1000 * 60 * 60 * 24));  
		    
			if (withSkip && computed.getUpdated().compareTo(yesterdayDate)>0) {
				return;
			}
			
//			if (!"LAEA200M_N15399E19170".equals(carreShape.getIdCarreHab())) {
//				continue;
//			}
			
		} else {
			// or create it
			computed = new InseeCarre200mComputed();
			computed.setIdCarre200(carreShape.getIdCarreHab());
		} 
		computed.setIsDense(isDense);
		
		if (carreShape.getGeoShape()==null) {
			int pause=0;// LAEA200M_N15402E19165
			log.error("getGeoShape is null");
		}
		
		// get insee data for the square
		InseeCarre200m carre = inseeCare200mRepository.findById(carreShape.getIdCarreHab()).get();
		
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
		for (ParkArea parkArea : parkAreas) {
			log.warn("\tcompose {}", parkArea);
			
			ParkAreaComputed pac;
			Optional<ParkAreaComputed> Opac = parkAreaComputedRepository.findById(parkArea.getId());
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
		
		
		if (StringUtils.isNotBlank(carre.getPopulation())) {
			// remove insee posibble decimal value
			Double inhabitant = Double.valueOf(carre.getPopulation());
			computed.setPopAll(BigDecimal.valueOf(Math.round(inhabitant)));
		} else {
			computed.setPopAll(BigDecimal.ZERO);
		}
		
		if ( polygonPark!=null ) {
			log.warn("\tprocess merge isochrone");
			//Compute all the population which is present in the isochrones of the current square.
			// and the park surface of these isochrones.
			// then, I compute the surface per capita (m²/inhabitant)
			
			ComputeDto dto = this.computePopAndDensity(polygonPark, surfaceParkAreas);
			computed.setSurfaceParkPerCapita(dto.getSurfacePerCapitaForIsochroneOnSquare());
			computed.setSurfaceTotalPark(surfaceParkAreas);
			computed.setPopulationInIsochrone(dto.getPopulationInIsochrone());
			
			// Do the same but only with OMS compliant parks
			if (allAreOms) {
				// this is unusual to recompute
				computed.setSurfaceParkPerCapitaOms(dto.getSurfacePerCapitaForIsochroneOnSquare());
				computed.setSurfaceTotalParkOms(surfaceParkAreas);
				computed.setPopulationInIsochroneOms(dto.getPopulationInIsochrone());
				
			} else {
				if (surfaceParkAreasOms!=null && !BigDecimal.ZERO.equals(surfaceParkAreasOms)) {
					dto = this.computePopAndDensity(polygonParkOms, surfaceParkAreasOms);
					computed.setSurfaceParkPerCapitaOms(dto.getSurfacePerCapitaForIsochroneOnSquare());
					computed.setSurfaceTotalParkOms(surfaceParkAreasOms);
					computed.setPopulationInIsochroneOms(dto.getPopulationInIsochrone());
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
			if (StringUtils.isNotBlank(carre.getPopulation())) {
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
				Long popIn = Math.round(inhabitant);
				computed.setPopAll(new BigDecimal(popIn));
				computed.setPopIncluded(BigDecimal.ZERO);
				computed.setPopExcluded(new BigDecimal(popIn));
				computed.setPopIncludedOms(BigDecimal.ZERO);
				computed.setPopExcludedOms(new BigDecimal(popIn));
			}
			

			
		} else {

			// no parks is accesible
			computed.setPopIncluded(BigDecimal.ZERO);
			computed.setPopExcluded(computed.getPopAll());
			computed.setSurfaceTotalPark(BigDecimal.ZERO);
			computed.setSurfaceParkPerCapita(BigDecimal.ZERO);
		}
		log.warn("\tsave computed {}\n", computed.getIdCarre200());
		computed.setUpdated(new Date());
		inseeCarre200mComputedRepository.save(computed);
		
	}
	
	/**
	 * @param cadastre
	 */
	public void computeCarreByParkV2(ParkArea parkArea) {
		log.warn(">> computeCarreByParkV2");
		
		Boolean isDense = Boolean.TRUE;
		Optional<ParcEtJardin> opj = parkJardinRepository.findById(parkArea.getIdParcEtJardin());
		if (opj.isPresent()) {
			isDense = serviceOpenData.isDistanceDense(opj.get().getCommune().getInseeCode());
		}

		// find all square in city area
		List<InseeCarre200mShape> shapes = inseeCarre200mShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(parkArea.getPolygon()));
		

		// iterate on each
		for (InseeCarre200mShape carreShape : shapes) {
			computeCarreShapeV2(carreShape, isDense);
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
		
		// find all square in city area
		List<InseeCarre200mShape> shapes = inseeCarre200mShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(cadastre.getGeoShape()));
		
		// iterate on each
		for (InseeCarre200mShape carreShape : shapes) {
			computeCarreShapeV2(carreShape, isDense);
		}	

		log.warn("<< computeCarreByCadastreV2");
	}
	

	/**
	 * Compute ParkEntrance from ParkArea and List<ParkEntrance>.
	 * @param park
	 * @return
	 */
	public ParkAreaComputed computeParkAreaV2(ParkArea park) {
		ParkAreaComputed parcCpu=null;
		log.info("computePark( {}-{} )",park.getId(), park.getName());
		
		// skip un-precalculated park
		if(park.getPolygon()==null) {
			log.warn("please process {}-{}  first", park.getId(), park.getName());
			throw new ExceptionGeo("park not merge"+park.toString());
		}
		
		// get type parameters
		parkTypeService.populate(park);
		
		
		Optional<ParkAreaComputed> parcCpuOpt = parkAreaComputedRepository.findById(park.getId());
		if (parcCpuOpt.isPresent()) {
			parcCpu = parcCpuOpt.get();
		} else {
			parcCpu = new ParkAreaComputed();
			parcCpu.setId(park.getId());
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
		// find carre200m shapes that match the park
		List<InseeCarre200mShape> shapes = inseeCarre200mShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(park.getPolygon()));
		for (InseeCarre200mShape carreShape : shapes) {
			Geometry parkOnCarre = carreShape.getGeoShape().intersection(park.getPolygon());
			Long surfIntersect = getSurface(parkOnCarre);
			
			//lookup for carre200m data
			InseeCarre200m carre =inseeCare200mRepository.findByIdInspire(carreShape.getIdInspire());
			Long popCar =Math.round(Double.valueOf(carre.getPopulation()));
			
			Long popIntersect = Math.round(popCar*surfIntersect/SURFACE_CARRE);
			population = population.add(new BigDecimal(popIntersect));
		}
		parcCpu.setPopulation(population);
		if (! BigDecimal.ZERO.equals(population)) {
			parcCpu.setSurfacePerInhabitant(parcCpu.getSurface().divide(population, 1, RoundingMode.HALF_EVEN));
		}
		parcCpu.setUpdated(new Date());
		
		log.warn("surface per inhabitant: {}", parcCpu.getSurfacePerInhabitant());
		parcCpu = parkAreaComputedRepository.save(parcCpu);
		
		return parcCpu;
	}
	
	public Long getSurface(Geometry geom) {
		return inseeCare200mRepository.getSurface(geom);
	}

}
