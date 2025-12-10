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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
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
import com.github.cunvoas.geoserviceisochrone.service.compute.dto.ComputeDto;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import lombok.extern.slf4j.Slf4j;

/**
 * Service métier pour le calcul des carrés de 200m (version 3).
 * <p>
 * Ce service permet de réaliser différents calculs et traitements sur les entités de type carré 200m,
 * notamment en fonction des codes postaux, codes INSEE ou identifiants Inspire.
 * Il gère également l'activité des parcs pour une année donnée.
 * <ul>
 *   <li>Calculs par code postal, code INSEE ou identifiant Inspire (méthodes commentées)</li>
 *   <li>Vérification de l'activité d'un parc pour une année donnée</li>
 * </ul>
 *
 * Les dépendances sont injectées via l'annotation @Autowired de Spring.
 */
@Service
@Slf4j
@ConditionalOnProperty(
		name="application.feature-flipping.carre200m-impl", 
		havingValue="v3")
public class ComputeCarreServiceV3 implements IComputeCarreService {

	// 200m x 200m = 4 10^4: insee data is 40000 +-1 accuracy
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
	
	
	/**
	 * computeCarreByInseeCode.
	 * @param inseeCode code
	 */
//	public void computeCarreByInseeCode(String inseeCode) {
//		Cadastre cadastre = cadastreRepository.findById(inseeCode).get();
//		computeCarreByCadastre(cadastre);
//	}
	
//	/**
//	 * computeCarreByCarre200m.
//	 * @param idInspire id
//	 */
//	public void computeCarreByCarre200m(String idInspire) {
//		Optional<InseeCarre200mOnlyShape> oCarreShape = inseeCarre200mOnlyShapeRepository.findById(idInspire);
//		if (oCarreShape.isPresent()) {
//			InseeCarre200mOnlyShape carreShape = oCarreShape.get();
//			Boolean isDense = serviceOpenData.isDistanceDense(carreShape.getCodeInsee());
//			computeCarreShape(carreShape, isDense);
//		}
//	}

	/**
	 * Vérifie si un parc est actif pour une année donnée.
	 * @param pa ParkArea à vérifier
	 * @param annee Année de référence
	 * @return TRUE si le parc est actif pour l'année, FALSE sinon
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
	 * Compute the surface of parks availlable per capita in the square.
	 * @param carreShape shape
	 * @param isDense witch density
	 * @TODO make optim to not recompute all years ( before 2027 )
	 */
	protected void computeCarreShape(ComputeJob job, InseeCarre200mOnlyShape carreShape, Boolean isDense) {
		
		log.warn(">> InseeCarre200mOnlyShape {}", carreShape.getIdInspire());

		
		// find parks in square shape
		List<ParkArea> parkAreas = parkAreaRepository.findParkInMapArea(GeometryQueryHelper.toText(carreShape.getGeoShape()));
		parkTypeService.populate(parkAreas);
		
//		Map<Integer, ComputeDto> mapDto = new HashMap<>();
		
		Geometry shapeParkOnSquare=null;
		
		// prepare surface and polygons for parks coverage
		Integer annee = job.getAnnee();
		
		
		int count4checkOms=parkAreas.size();
		ComputeDto dto = new ComputeDto(carreShape);
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
				pac = this.computeParkArea(parkArea, annee);
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
		
		
		
		
		// get for each years, all square in parks area

//		ComputeDto dto = mapDto.get(annee);	
		
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
		this.computePopAndDensity(dto, carreShape, shapeParkOnSquare);
		
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
		
		// Compute missing surface to reach minimum and advised OMS standards
		computed.setMissingSurfaceMini(computeMissingSurface(dto, carreShape, applicationBusinessProperties.getMinUrbSquareMeterPerCapita(), applicationBusinessProperties.getMinSubUrbSquareMeterPerCapita()));
		computed.setMissingSurfaceAdvised(computeMissingSurface(dto, carreShape, applicationBusinessProperties.getRecoUrbSquareMeterPerCapita(), applicationBusinessProperties.getRecoSubUrbSquareMeterPerCapita()));
		
		
		if (Boolean.TRUE.equals(dto.withSufficient)) {
			computed.setIsSustainablePark(Boolean.TRUE);
			//TODO 
			computed.setPopulationWithSustainablePark(null);
		} else {
			computed.setIsSustainablePark(Boolean.FALSE);
			computed.setPopulationWithSustainablePark(BigDecimal.ZERO);
		}
		
		computed.setComments(dto.parcName);

		log.info("\tsave computed {}\n", computed.getIdInspire());
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
	protected ComputeResultDto computePopAndDensityDetail(
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
				//FIXME bug here ?
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
		

		Double inhabitant = dto.popAll.doubleValue();
		
		
		// compute surface with accessible parks
		Geometry parkOnCarre = carreShape.getGeoShape().intersection(shapeParkOnSquare);
		Long surfaceParkAccess = getSurface(parkOnCarre);

		// protata des surfaces pour habitants avec un parc
		Long popIn = Math.round(inhabitant*surfaceParkAccess/SURFACE_CARRE);
		
		crDto.popInc = new BigDecimal(popIn);
		crDto.popExc = new BigDecimal(inhabitant-popIn);
		

		Long surfaceSustainable = 0L;
		// compute surface with accessible parks
		if (dto.polygonParkAreasSustainableOms!=null) {
			Geometry parkSustainable = carreShape.getGeoShape().intersection(dto.polygonParkAreasSustainableOms);
			surfaceSustainable = getSurface(parkSustainable);
		}

		// protata des surfaces pour habitants avec un parc
		Long popSustainable = Math.round(inhabitant*surfaceSustainable/SURFACE_CARRE);
		dto.popWithSufficient = new BigDecimal(popSustainable);
		
		return crDto;
	}
	
	/**
	 * computePopAndDensity.
	 * @param dto ComputeDto
	 * @param carreShape shap
	 * @param shapeParkOnSquare shape
	 */
	protected void computePopAndDensity(ComputeDto dto, InseeCarre200mOnlyShape carreShape, Geometry shapeParkOnSquare) {

		Geometry geometryToAnalyse =dto.polygonParkAreas;
		ComputeResultDto rDto = this.computePopAndDensityDetail(dto, dto.result, carreShape, geometryToAnalyse, shapeParkOnSquare);
		dto.result = rDto;
		
		if (dto.allAreOms) {
			dto.resultOms = rDto;
		} else {
			geometryToAnalyse =dto.polygonParkAreasOms;
			rDto = this.computePopAndDensityDetail(dto, dto.resultOms, carreShape, geometryToAnalyse, shapeParkOnSquare);
			dto.resultOms = rDto;
		}
	}
	
	/**
	 * computeCarreByComputeJob.
	 * @param job ComputeJob
	 * @return true if done
	 */
	public Boolean computeCarreByComputeJob(ComputeJob job) {
		log.info("begin computeCarre {}", job.getIdInspire());
		Boolean ret = Boolean.FALSE;
		
		Optional<InseeCarre200mOnlyShape> oCarre = inseeCarre200mOnlyShapeRepository.findById(job.getIdInspire());
		if (oCarre.isPresent()) {
			try {
				InseeCarre200mOnlyShape carre = oCarre.get();
				Boolean isDense = serviceOpenData.isDistanceDense(carre.getCodeInsee());
				this.computeCarreShape(job, carre, isDense);
				ret = Boolean.TRUE;
				
			} catch (Exception e) {
				log.error("computeCarre in error: {} {}", job.getIdInspire(), job.getAnnee());
			}
		}
		
		return ret;
	}
		
	/**
	 *  Used for mass update and full recompute ParkAreaEntrance.
	 *  @param inseeCode code
	 */
	public void refreshParkEntrances(String inseeCode) {
		Cadastre cadastre = cadastreRepository.findById(inseeCode).get();
		refreshParkEntrances(cadastre);
	}
	
	/**
	 * Used for mass update and full recompute ParkAreaEntrance.
	 * @param cadastre Cadastre
	 */
	@Transactional(isolation = Isolation.READ_COMMITTED)
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
	 * computeParkAreaV2.
	 * @param park ParkArea
	 * @param annee year
	 * @return ParkAreaComputed
	 */
	protected ParkAreaComputed computeParkArea(ParkArea park, Integer annee) {
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
		/*
		 * Algorithme d'intersection de surface :
		 * Pour chaque carré de 200m intersectant le parc, on calcule l'intersection géométrique entre la forme du carré et celle du parc.
		 * La surface de cette intersection est ensuite utilisée pour proratiser la population du carré (selon la proportion de surface intersectée).
		 * On additionne la population proratisée de chaque carré pour obtenir la population totale du parc.
		 * Enfin, la surface par habitant est calculée en divisant la surface totale du parc par la population totale obtenue.
		 * Ce calcul permet de tenir compte de la répartition spatiale réelle des habitants par rapport à la surface accessible du parc.
		 */
		BigDecimal population = BigDecimal.ZERO;
		for (InseeCarre200mOnlyShape carreShape : shapes) {
			Geometry parkOnCarre = carreShape.getGeoShape().intersection(park.getPolygon());
			//when park is too complex, use convex hull
			// real problem is base original shape
//				parkOnCarre = carreShape.getGeoShape().intersection(park.getPolygon().convexHull());

			
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
	 * @return ParkAreaComputed
	 * @TODO to be reviewed
	 */
	public ParkAreaComputed computeParkArea(ParkArea park) {
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
			
			
			
			/*
			 * Algorithme d'intersection de surface :
			 * Pour chaque carré de 200m intersectant le parc, on calcule l'intersection géométrique entre la forme du carré et celle de l'isochone d'accessibilité du parc.
			 * La surface de cette intersection est ensuite utilisée pour proratiser la population du carré (selon la proportion de surface intersectée).
			 * On additionne la population proratisée de chaque carré pour obtenir la population totale du parc.
			 * Enfin, la surface par habitant est calculée en divisant la surface totale du parc par la population totale obtenue.
			 * Ce calcul permet de tenir compte de la répartition spatiale réelle des habitants par rapport à la surface accessible du parc.
			 */
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

	
	/**
	 * Compute missing surface to reach OMS standards (minimum or advised).
	 * <p>
	 * Formula: MAX(0, (standardPerCapita * populationInIsochroneOms) - surfaceTotalParkOms)
	 * </p>
	 * The standard is selected based on density:
	 * - Urban (isDense = true): use urbStandard
	 * - Suburban (isDense = false): use subUrbStandard
	 * 
	 * @param dto ComputeDto containing the computation results
	 * @param carreShape the square shape being analyzed
	 * @param urbStandard the standard per capita for urban zones (in m²/inhabitant)
	 * @param subUrbStandard the standard per capita for suburban zones (in m²/inhabitant)
	 * @return missing surface to reach the standard, or ZERO if already sufficient
	 */
	protected BigDecimal computeMissingSurface(ComputeDto dto, InseeCarre200mOnlyShape carreShape, 
			Double urbStandard, Double subUrbStandard) {
		
		// Select standard based on density
		Double standard = dto.isDense ? urbStandard : subUrbStandard;
		
		// Calculate required surface: standard * population
		BigDecimal requiredSurface = BigDecimal.valueOf(standard * dto.resultOms.populationInIsochrone.doubleValue());
		
		// Calculate missing surface: required - available
		BigDecimal missingSurface = requiredSurface.subtract(dto.resultOms.surfaceTotalParks);
		
		// Return maximum of 0 and calculated missing surface
		if (missingSurface.compareTo(BigDecimal.ZERO) > 0) {
			return missingSurface.setScale(2, RoundingMode.HALF_EVEN);
		}
		return BigDecimal.ZERO;
	}

	
	/**
	 * getSurface.
	 * @param geom  Geometry
	 * @return surface of Geometry
	 */
	public Long getSurface(Geometry geom) {
		return inseeCarre200mOnlyShapeRepository.getSurface(geom);
	}


}
