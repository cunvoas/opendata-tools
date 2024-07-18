package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
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
			computeCarreByCadastreV2Optim(cadastre);
		}
		
	}
	
	public void computeCarreByInseeCode(String inseeCode) {
		Cadastre cadastre = cadastreRepository.findById(inseeCode).get();
		computeCarreByCadastreV2Optim(cadastre);
	}
	
	public void computeCarreByCarre200m(String idInspire) {
		Optional<InseeCarre200mOnlyShape> oCarreShape = inseeCarre200mOnlyShapeRepository.findById(idInspire);
		if (oCarreShape.isPresent()) {
			InseeCarre200mOnlyShape carreShape = oCarreShape.get();
			Boolean isDense = serviceOpenData.isDistanceDense(carreShape.getCodeInsee());
			computeCarreShapeV2Optim(carreShape, isDense);
		}
	}

	
	
	/**
	 * Compute the surface of parks availlable per capita in the square.
	 * @param carreShape
	 * @param isDense
	 */
	protected void computeCarreShapeV2Optim(InseeCarre200mOnlyShape carreShape, Boolean isDense) {
		
		log.warn(">> InseeCarre200mOnlyShape {}", carreShape.getIdInspire());

		List<Integer> annes = List.of(applicationBusinessProperties.getInseeAnnees());
		
		// find parks in square shape
		List<ParkArea> parkAreas = parkAreaRepository.findParkInMapArea(GeometryQueryHelper.toText(carreShape.getGeoShape()));
		parkTypeService.populate(parkAreas);
		
		Map<Integer, ComputeDto> mapDto = new HashMap<>();
		
		Geometry shapeParkOnSquare=null;
		
		// prepare surface and polygons for parks coverage
		for (Integer annee : annes) {
			int count4checkOms=parkAreas.size();
			ComputeDto dto = new ComputeDto(carreShape);
			dto.isDense = isDense;
			dto.annee=annee;
			
			mapDto.put(annee, dto);
		
			for (ParkArea parkArea : parkAreas) {
				log.info("\tcompose {}", parkArea);
				
				ParkAreaComputed pac;
				Optional<ParkAreaComputed> opac = parkAreaComputedRepository.findByIdAndAnnee(parkArea.getId(), annee);
				if (opac.isPresent()) {
					pac = opac.get();
				} else {
					// rebuid
					pac = this.computeParkAreaV2(parkArea, annee);
				}
				
				//decrement for all
				count4checkOms--;
				
				// prepare ALL
				dto.result.surfaceTotalParks = dto.result.surfaceTotalParks.add(pac.getSurface());
				// merge areas for parks
				dto.polygonParkAreas = dto.polygonParkAreas.union(parkArea.getPolygon());
				
				// prepare with OMS compliance
				if (pac.getOms()) {
					
					if (shapeParkOnSquare==null) {
						shapeParkOnSquare = parkArea.getPolygon();
					} else {
						shapeParkOnSquare = shapeParkOnSquare.union(parkArea.getPolygon());
					}
					
					dto.parcNames.add(parkArea.getName());
					//increment for oms
					count4checkOms++;
					dto.resultOms.surfaceTotalParks = dto.resultOms.surfaceTotalParks.add(pac.getSurface());
					
					// merge areas for parks
					dto.polygonParkAreasOms = dto.polygonParkAreasOms.union(parkArea.getPolygon());
				} else {
					dto.parcNames.add("("+parkArea.getName()+")");
				}
			}  // end merge
			
			// all parks are OMS compliant
			dto.allAreOms = count4checkOms==parkAreas.size();
			
			// si shapeParkOnSquare est null; pas de parc OMS, on considère un point
			if (shapeParkOnSquare==null) {
				shapeParkOnSquare = carreShape.getGeoPoint2d();
			}
			
			
			StringBuilder sbParcName = new StringBuilder();
			if (!dto.parcNames.isEmpty()) {
				Collections.sort(dto.parcNames);
				for (String name : dto.parcNames) {
					if (name!=null) {
						if (sbParcName.length()>0) {
							sbParcName.append("<br />");
						}
						sbParcName.append(" - ");
						sbParcName.append(name);
					}
				}
				dto.parcName = sbParcName.toString();
			}
		}
		
		
		
		// get for each years, all square in parks area
		for (Integer annee : annes) {
			ComputeDto dto = mapDto.get(annee);	
			
			// get already computed square results
			InseeCarre200mComputedV2 computed = null;
			Optional<InseeCarre200mComputedV2> opt = inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(annee, carreShape.getIdInspire());
			if (opt.isPresent()) {
				computed = opt.get();
			} else {
				// or create it
				computed = new InseeCarre200mComputedV2();
				computed.setIdInspire(carreShape.getIdInspire());
				computed.setAnnee(annee);	
			}
			computed.setIsDense(isDense);
			computed.setUpdated(new Date());
			

			log.info("\tprocess merge isochrone");
			
			//Compute all the population which is present in the isochrones of the current square.
			// and the park surface of these isochrones.
			// then, I compute the surface per capita (m²/inhabitant)
			this.computePopAndDensityOptim(dto, carreShape, shapeParkOnSquare);
			
			computed.setSurfaceParkPerCapita(dto.result.surfaceParkPerCapita);
			computed.setSurfaceTotalPark(dto.result.surfaceTotalParks);
			computed.setPopulationInIsochrone(dto.result.populationInIsochrone);
			computed.setPopIncluded(dto.result.popInc);
			computed.setPopExcluded(dto.result.popExc);
			
			computed.setSurfaceParkPerCapitaOms(dto.resultOms.surfaceParkPerCapita);
			computed.setSurfaceTotalParkOms(dto.resultOms.surfaceTotalParks);
			computed.setPopulationInIsochroneOms(dto.resultOms.populationInIsochrone);
			computed.setPopIncludedOms(dto.resultOms.popInc);
			computed.setPopExcludedOms(dto.resultOms.popExc);
			
			

			log.info("\tsave computed {}\n", computed.getIdInspire());
			inseeCarre200mComputedV2Repository.save(computed);

		}
	}
	
	
	/**
	 * @param dto
	 * @param carreShape
	 * @param geometryToAnalyse
	 * @param shapeParkOnSquare
	 * @return
	 */
	protected ComputeResultDto computePopAndDensityDetailOptim(
			ComputeDto dto, 
			ComputeResultDto crDto,
			InseeCarre200mOnlyShape carreShape,
			Geometry geometryToAnalyse, 
			Geometry shapeParkOnSquare) {
		

		// population located in all the isochrones
		Long surfacePopulationIso = 0L;
		
		// ctrl des carres proches inclus dans les isochrones
		// recherche des population sur l'ensembles des isochrones mergées
		List<InseeCarre200mOnlyShape> shapesWithIso = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(geometryToAnalyse));
		for (InseeCarre200mOnlyShape carreWithIso : shapesWithIso) {
			Filosofil200m carreData = filosofil200mRepository.findByAnneeAndIdInspire(dto.annee, carreWithIso.getIdInspire());
			
			if (carreData!=null) {
				log.info("Filosofil200m     found,{},{}", dto.annee, carreWithIso.getIdInspire());
				// nb habitant au carre
				if (carreShape.getIdInspire().equals(carreData.getIdInspire())) {
					dto.popAll = carreData.getNbIndividus();
				}
				
				Double nbHabCarre = carreData.getNbIndividus().doubleValue();
				// proratisation à la surface intersection(carre, isochrne)
				Geometry isoOnCarre = carreWithIso.getGeoShape().intersection(geometryToAnalyse);
				Long surfaceIsoSurCarre = getSurface(isoOnCarre);
				surfacePopulationIso += Math.round(nbHabCarre*surfaceIsoSurCarre/SURFACE_CARRE);
			} else {
				log.info("Filosofil200m NOT FOUND,{},{}", dto.annee, carreWithIso.getIdInspire());
			}
		}
		
		if (surfacePopulationIso!=0L) {
			crDto.surfaceParkPerCapita = crDto.surfaceTotalParks.divide(BigDecimal.valueOf(surfacePopulationIso), RoundingMode.HALF_EVEN);
		}
		crDto.populationInIsochrone = BigDecimal.valueOf(surfacePopulationIso);
		
		
		// compute surface with accessible parks
		Geometry parkOnCarre = carreShape.getGeoShape().intersection(shapeParkOnSquare);
		Long surfaceParkAccess = getSurface(parkOnCarre);
		
		Double inhabitant = dto.popAll.doubleValue();
		// protata des surfaces pour habitants avec un parc
		Long popIn = Math.round(inhabitant*surfaceParkAccess/SURFACE_CARRE);
		
		crDto.popInc = new BigDecimal(popIn);
		crDto.popExc = new BigDecimal(inhabitant-popIn);
		
		return crDto;
	}
	
	/**
	 * @param dto
	 * @param carreShape
	 * @param shapeParkOnSquare
	 */
	protected void computePopAndDensityOptim(ComputeDto dto, InseeCarre200mOnlyShape carreShape, Geometry shapeParkOnSquare) {

		Geometry geometryToAnalyse =dto.polygonParkAreas;
		ComputeResultDto rDto = this.computePopAndDensityDetailOptim(dto, dto.result, carreShape, geometryToAnalyse, shapeParkOnSquare);
		dto.result = rDto;
		
		if (dto.allAreOms) {
			dto.resultOms = rDto;
		} else {
			geometryToAnalyse =dto.polygonParkAreasOms;
			rDto = this.computePopAndDensityDetailOptim(dto, dto.resultOms, carreShape, geometryToAnalyse, shapeParkOnSquare);
			dto.resultOms = rDto;
		}
	}
	
	
	
	/**
	 * Computes population that can access a park at once.
	 * @param cadastre
	 * 
	 * Algorithm:
	 *  step 1: Find area of the city.
	 *  step 2: Find 200m squares that matches.
	 *  step 3: for each, get surface and isochrone of parks.
	 *  step 4: compute with OMS parks and others.
	 */
	@Transactional
	public void computeCarreByCadastreV2Optim(Cadastre cadastre) {
		log.warn(">> computeCarreByCadastreV2Optim");
		
		Boolean isDense = serviceOpenData.isDistanceDense(cadastre.getIdInsee());
		
		// find all square that fits the city area
		List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(cadastre.getGeoShape()));
		
		// iterate on each
		for (InseeCarre200mOnlyShape carreShape : shapes) {
			computeCarreShapeV2Optim(carreShape, isDense);
		}	

		log.warn("<< computeCarreByCadastreV2Optim");
		
	}

	
	/**
	 *  Used for mass update and full recompute ParkAreaEntrance.
	 *  @param inseeCode
	 */
	public void refreshParkEntrances(String inseeCode) {
		Cadastre cadastre = cadastreRepository.findById(inseeCode).get();
		refreshParkEntrances(cadastre);
	}
	
	/**
	 * Used for mass update and full recompute ParkAreaEntrance.
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

	protected ParkAreaComputed computeParkAreaV2(ParkArea park, Integer annee) {
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
		
		// find carre200m shapes that match the park
		List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(park.getPolygon()));
		
		
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
		
		return parcCpu;
		
		
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