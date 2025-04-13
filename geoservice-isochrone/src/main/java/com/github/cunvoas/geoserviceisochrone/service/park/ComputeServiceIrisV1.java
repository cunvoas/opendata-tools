package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeIrisJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.IrisDataComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisData;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.IrisDataComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisDataRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.LaposteRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import lombok.extern.slf4j.Slf4j;

/**
 * Business Service impl.
 */
@Service
@Slf4j
//@ConditionalOnProperty(
//		name="application.feature-flipping.carre200m-impl", 
//		havingValue="v3")
public class ComputeServiceIrisV1 {

	// 200m x 200m = 4 10^4: insee data is 40000 +-1 accuracy
	//private static final Double SURFACE_CARRE = 40_000d;
	
	@Autowired
	private LaposteRepository laposteRepository;
	@Autowired
	private CadastreRepository cadastreRepository;
	
	
	
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
	

	@Autowired
	private IrisDataComputedRepository inseeCarre200mComputedV2Repository;
	
	@Autowired
	private IrisShapeRepository irisShapeRepository;
	@Autowired
	private IrisDataRepository irisDataRepository;
	
	/**
	 * isActive.
	 * @param pa ParkArea
	 * @param annee year 
	 * @return park is active?
	 */
	protected Boolean isActive(ParkArea pa, Integer annee) {
		Boolean active=false;
		
		Optional<ParcEtJardin> oPj = parkJardinRepository.findById(pa.getIdParcEtJardin());
		if (oPj.isPresent()) {
			ParcEtJardin pj = oPj.get();
			Date dd = pj.getDateDebut();
			Date df = pj.getDateFin();
			
			Calendar cal = Calendar.getInstance();
			
			int d = 1900;
			if (dd!=null) {
				cal.setTime(dd);
				d = cal.get(Calendar.YEAR);
			}
			
			int f = 2100;
			if (df!=null) {
				cal.setTime(df);
				f = cal.get(Calendar.YEAR);
			}
			
			active = d<=annee && annee<=f;
		}
	
		return active;
	}
	
	/**
	 * Compute the surface of parks available per capita in the square.
	 * @param carreShape shape
	 * @param isDense witch density
	 * @TODO make optim to not recompute all years ( before 2027 )
	 */
	protected void computeIrisShapeV2Optim(ComputeIrisJob job, IrisShape carreShape, Boolean isDense) {
		
		log.warn(">> computeIrisShapeV2Optim {}", carreShape.getIris());

		
		// find parks in square shape
		List<ParkArea> parkAreas = parkAreaRepository.findParkInMapArea(GeometryQueryHelper.toText(carreShape.getContour()));
		parkTypeService.populate(parkAreas);
		
//		Map<Integer, ComputeIrisDto> mapDto = new HashMap<>();
		
		Geometry shapeParkOnSquare=null;
		
		// prepare surface and polygons for parks coverage
		Integer annee = job.getAnnee();
		
		
		int count4checkOms=parkAreas.size();
		ComputeIrisDto dto = new ComputeIrisDto(carreShape);
		dto.isDense = isDense;
		dto.annee=annee;
		
//		mapDto.put(annee, dto);
	
		for (ParkArea parkArea : parkAreas) {
			log.info("\tcompose {}", parkArea);
			
			// CHECK if park exists for the INSEE data
			if (!isActive(parkArea, annee)) {
				// TODO add full testing
				continue;
			}
			
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
				
				String sufficient="";
				Double rs = applicationBusinessProperties.getRecoAtLeastParkSurface();
				if (rs<=pac.getSurface().doubleValue()) {
					sufficient="✓ ";
					dto.withSufficient=Boolean.TRUE;
					
					if (dto.polygonParkAreasSustainableOms==null)  {
						dto.polygonParkAreasSustainableOms =parkArea.getPolygon(); 
					} else {
						dto.polygonParkAreasSustainableOms = dto.polygonParkAreasSustainableOms.union(parkArea.getPolygon());
					}
				}
			
				dto.parcNames.add(sufficient+ parkArea.getName());
				//increment for oms
				count4checkOms++;
				dto.resultOms.surfaceTotalParks = dto.resultOms.surfaceTotalParks.add(pac.getSurface());
				
				// merge areas for parks
				dto.polygonParkAreasOms = dto.polygonParkAreasOms.union(parkArea.getPolygon());
			} else {
				dto.parcNames.add("(✖ "+parkArea.getName()+")");
			}
		}  // end merge
		
		// all parks are OMS compliant
		dto.allAreOms = count4checkOms==parkAreas.size();
		
		// si shapeParkOnSquare est null; pas de parc OMS, on considère un point
		if (shapeParkOnSquare==null) {
			shapeParkOnSquare = carreShape.getCoordonnee();
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
		
		
		
		
		// get for each years, all square in parks area

//		ComputeIrisDto dto = mapDto.get(annee);	
		
		// get already computed square results
		IrisDataComputed computed = inseeCarre200mComputedV2Repository.findByAnneeAndIris(annee, carreShape.getIris());
		if (computed==null) {
			// or create it
			computed = new IrisDataComputed();
			computed.setIris(carreShape.getIris());
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
		
		if (Boolean.TRUE.equals(dto.withSufficient)) {
			computed.setIsSustainablePark(Boolean.TRUE);
			//TODO 
			computed.setPopulationWithSustainablePark(null);
		} else {
			computed.setIsSustainablePark(Boolean.FALSE);
			computed.setPopulationWithSustainablePark(BigDecimal.ZERO);
		}
		
		computed.setComments(dto.parcName);

		log.info("\tsave computed {}\n", computed.getIris());
		inseeCarre200mComputedV2Repository.save(computed);

	}
	
	
	/**
	 * computePopAndDensityDetailOptim.
	 * @param dto DTO with source data
	 * @param crDto DTO with result data
	 * @param carreShape square on process
	 * @param geometryToAnalyse analysed
	 * @param shapeParkOnSquare shape of park isochrones
	 * @return ComputeResultDto
	 */
	protected ComputeResultDto computePopAndDensityDetailOptim(
			ComputeIrisDto dto, 
			ComputeResultDto crDto,
			IrisShape carreShape,
			Geometry geometryToAnalyse, 
			Geometry shapeParkOnSquare) {
		

		// population located in all the isochrones
		Long surfacePopulationIso = 0L;
		
		// ctrl des carres proches inclus dans les isochrones
		// recherche des population sur l'ensembles des isochrones mergées
		List<IrisShape>  shapesWithIso = irisShapeRepository.findIrisInMapArea(GeometryQueryHelper.toText(geometryToAnalyse));
		for (IrisShape carreWithIso : shapesWithIso) {
			IrisData carreData = irisDataRepository.findByAnneeAndIris(dto.annee, carreWithIso.getIris());
			
			if (carreData!=null) {
				log.info("Filosofil200m     found,{},{}", dto.annee, carreWithIso.getIris());
				// nb habitant au carre
				if (carreShape.getIris().equals(carreData.getIris())) {
					dto.popAll = carreData.getP20pop();
				}
				
				Double nbHabCarre = carreData.getP20pop().doubleValue();
				// proratisation à la surface intersection(carre, isochrne)
				Geometry isoOnCarre = carreWithIso.getContour().intersection(geometryToAnalyse);
				Long surfaceIsoSurCarre = getSurface(isoOnCarre);
				surfacePopulationIso += Math.round(nbHabCarre*surfaceIsoSurCarre/carreWithIso.getSurface());
				
				
			} else {
				log.info("IrisData NOT FOUND,{},{}", dto.annee, carreWithIso.getIris());
			}
		}
		
		if (surfacePopulationIso!=0L) {
			crDto.surfaceParkPerCapita = crDto.surfaceTotalParks.divide(BigDecimal.valueOf(surfacePopulationIso), RoundingMode.HALF_EVEN);
		}
		crDto.populationInIsochrone = BigDecimal.valueOf(surfacePopulationIso);
		

		Double inhabitant = dto.popAll.doubleValue();
		
		
		// compute surface with accessible parks
		Geometry parkOnCarre = carreShape.getContour().intersection(shapeParkOnSquare);
		Long surfaceParkAccess = getSurface(parkOnCarre);

		// protata des surfaces pour habitants avec un parc
		Long popIn = Math.round(inhabitant*surfaceParkAccess/carreShape.getSurface());
		
		crDto.popInc = new BigDecimal(popIn);
		crDto.popExc = new BigDecimal(inhabitant-popIn);
		

		Long surfaceSustainable = 0L;
		// compute surface with accessible parks
		if (dto.polygonParkAreasSustainableOms!=null) {
			Geometry parkSustainable = carreShape.getContour().intersection(dto.polygonParkAreasSustainableOms);
			surfaceSustainable = getSurface(parkSustainable);
		}

		// protata des surfaces pour habitants avec un parc
		Long popSustainable = Math.round(inhabitant*surfaceSustainable/carreShape.getSurface());
		dto.popWithSufficient = new BigDecimal(popSustainable);
		
		return crDto;
	}
	
	/**
	 * computePopAndDensityOptim.
	 * @param dto ComputeIrisDto
	 * @param carreShape shap
	 * @param shapeParkOnIris shape
	 */
	protected void computePopAndDensityOptim(ComputeIrisDto dto, IrisShape carreShape, Geometry shapeParkOnIris) {

		Geometry geometryToAnalyse =dto.polygonParkAreas;
		ComputeResultDto rDto = this.computePopAndDensityDetailOptim(dto, dto.result, carreShape, geometryToAnalyse, shapeParkOnIris);
		dto.result = rDto;
		
		if (dto.allAreOms) {
			dto.resultOms = rDto;
		} else {
			geometryToAnalyse =dto.polygonParkAreasOms;
			rDto = this.computePopAndDensityDetailOptim(dto, dto.resultOms, carreShape, geometryToAnalyse, shapeParkOnIris);
			dto.resultOms = rDto;
		}
	}
	
	/**
	 * computeCarreByComputeJobV2Optim.
	 * @param job ComputeJob
	 * @return true if done
	 */
	public Boolean computeCarreByComputeJobV2Optim(ComputeIrisJob job) {
		log.info("begin computeCarre {}", job.getIris());
		Boolean ret = Boolean.FALSE;
		
		Optional<IrisShape> oCarre = irisShapeRepository.findById(job.getIris());
		if (oCarre.isPresent()) {
			try {
				IrisShape carre = oCarre.get();
				Boolean isDense = serviceOpenData.isDistanceDense(carre.getCodeInsee());
				this.computeIrisShapeV2Optim(job, carre, isDense);
				ret = Boolean.TRUE;
				
			} catch (Exception e) {
				log.error("computeCarre in error: {} {}", job.getIris(), job.getAnnee());
			}
		}
		
		return ret;
	}



	/**
	 * computeParkAreaV2.
	 * @param park ParkArea
	 * @param annee year
	 * @return ParkAreaComputed
	 */
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
		// find carre200m shapes that match the park
		List<IrisShape> shapes = irisShapeRepository.findIrisInMapArea(GeometryQueryHelper.toText(park.getPolygon()));
				
		for (IrisShape carreShape : shapes) {
			Geometry parkOnCarre = carreShape.getContour().intersection(park.getPolygon());
			Long surfIntersect = getSurface(parkOnCarre);
			
			//lookup for carre200m data
			IrisData carre = irisDataRepository.findByAnneeAndIris(annee, carreShape.getIris());
			Long popCar = 0L;
			if (carre!=null) {
				popCar =Math.round(carre.getP20pop().doubleValue());
			}
			Long popIntersect = Math.round(popCar*surfIntersect/carreShape.getSurface());
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
	 * @return ParkAreaComputed
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
		
		List<IrisShape> shapes = irisShapeRepository.findIrisInMapArea(GeometryQueryHelper.toText(park.getPolygon()));
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
			for (IrisShape carreShape : shapes) {
				Geometry parkOnCarre = carreShape.getContour().intersection(park.getPolygon());
				Long surfIntersect = getSurface(parkOnCarre);
				
				//lookup for carre200m data
				IrisData carre = irisDataRepository.findByAnneeAndIris(annee, carreShape.getIris());
				Long popCar = 0L;
				if (carre!=null) {
					popCar =Math.round(carre.getP20pop().doubleValue());
				}
				Long popIntersect = Math.round(popCar*surfIntersect/carreShape.getSurface());
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

	
	/**
	 * getSurface.
	 * @param geom  Geometry
	 * @return surface of Geometry
	 */
	public Long getSurface(Geometry geom) {
		return irisShapeRepository.getSurface(geom);
	}

}
