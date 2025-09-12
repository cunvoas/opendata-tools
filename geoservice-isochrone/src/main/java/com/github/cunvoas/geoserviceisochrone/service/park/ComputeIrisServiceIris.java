package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeIrisJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.IrisDataComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisData;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.IrisDataComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ComputeJobIrisRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisDataRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.LaposteRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import lombok.extern.slf4j.Slf4j;

/**
 * Business Service impl.
 * @see https://geoservices.ign.fr/contoursiris
 */
@Service
@Slf4j
@ConditionalOnProperty(
		name="application.feature-flipping.carre200m-impl", 
		havingValue="v3")
public class ComputeIrisServiceIris extends AbstractComputeService implements IComputeIrisService {
	
	@Autowired
	public ComputeIrisServiceIris(
			ParkJardinRepository parkJardinRepository,
			InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository
			) {
		super(parkJardinRepository, inseeCarre200mOnlyShapeRepository);
	}

	@Autowired
	private ComputeJobIrisRepository computeJobIrisRepository;
	
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

	@Autowired
	private ComputeParkAreaV2 computeParkAreaV2;

	
	/**
	 * Compute the surface of parks available per capita in the square.
	 * @param carreShape shape
	 * @param isDense witch density
	 * @TODO make optim to not recompute all years ( before 2027 )
	 */
	protected void computeIrisShape(ComputeIrisJob job, IrisShape carreShape, Boolean isDense) {
		
		log.warn(">> computeIrisShape {}", carreShape.getIris());
		
		// find parks in iris shape
		List<ParkArea> parkAreasInIris = parkAreaRepository.findParkInMapArea(GeometryQueryHelper.toText(carreShape.getContour()));
		parkTypeService.populate(parkAreasInIris);
		
//		Map<Integer, ComputeIrisDto> mapDto = new HashMap<>();
		
		Geometry shapeParkOnIris=null;
		
		// prepare surface and polygons for parks coverage
		Integer annee = job.getAnnee();
		
		int count4checkOms=parkAreasInIris.size();
		ComputeIrisDto dto = new ComputeIrisDto(carreShape);
		dto.isDense = isDense;
		dto.annee=annee;
		
//		mapDto.put(annee, dto);
	
		for (ParkArea parkArea : parkAreasInIris) {
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
				pac = computeParkAreaV2.computeGenericParkAreaV2(parkArea, annee);
			}
			
			//decrement for all
			count4checkOms--;
			
			// prepare ALL
			dto.result.surfaceTotalParks = dto.result.surfaceTotalParks.add(pac.getSurface());
			// merge areas for parks
			dto.polygonParkAreas = dto.polygonParkAreas.union(parkArea.getPolygon());
			
			// prepare with OMS compliance
			if (pac.getOms()) {
				
				if (shapeParkOnIris==null) {
					shapeParkOnIris = parkArea.getPolygon();
				} else {
					shapeParkOnIris = shapeParkOnIris.union(parkArea.getPolygon());
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
		dto.allAreOms = count4checkOms==parkAreasInIris.size();
		
		// si shapeParkOnSquare est null; pas de parc OMS, on considère un point
		if (shapeParkOnIris==null) {
			shapeParkOnIris = carreShape.getCoordonnee();
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
		IrisDataComputed irisComputed = inseeCarre200mComputedV2Repository.findByAnneeAndIris(annee, carreShape.getIris());
		if (irisComputed==null) {
			// or create it
			irisComputed = new IrisDataComputed();
			irisComputed.setIris(carreShape.getIris());
			irisComputed.setAnnee(annee);	
		}
		irisComputed.setIsDense(isDense);
		irisComputed.setUpdated(new Date());
		

		log.info("\tprocess merge isochrone");
		
		//Compute all the population which is present in the isochrones of the current square.
		// and the park surface of these isochrones.
		// then, I compute the surface per capita (m²/inhabitant)
		this.computePopAndDensity(dto, carreShape, shapeParkOnIris);
		
		irisComputed.setSurfaceParkPerCapita(dto.result.surfaceParkPerCapita);
		irisComputed.setSurfaceTotalPark(dto.result.surfaceTotalParks);
		irisComputed.setPopulationInIsochrone(dto.result.populationInIsochrone);
		irisComputed.setPopIncluded(dto.result.popInc);
		irisComputed.setPopExcluded(dto.result.popExc);
		
		irisComputed.setSurfaceParkPerCapitaOms(dto.resultOms.surfaceParkPerCapita);
		irisComputed.setSurfaceTotalParkOms(dto.resultOms.surfaceTotalParks);
		irisComputed.setPopulationInIsochroneOms(dto.resultOms.populationInIsochrone);
		irisComputed.setPopIncludedOms(dto.resultOms.popInc);
		irisComputed.setPopExcludedOms(dto.resultOms.popExc);
		
		if (Boolean.TRUE.equals(dto.withSufficient)) {
			irisComputed.setIsSustainablePark(Boolean.TRUE);
			//TODO faire le calcul
			irisComputed.setPopulationWithSustainablePark(null);
		} else {
			irisComputed.setIsSustainablePark(Boolean.FALSE);
			irisComputed.setPopulationWithSustainablePark(BigDecimal.ZERO);
		}
		
		irisComputed.setComments(dto.parcName);

		log.info("\tsave computed {}\n", irisComputed.getIris());
		inseeCarre200mComputedV2Repository.save(irisComputed);
		

	}
	
	
	/**
	 * computePopAndDensityDetail.
	 * @param dto DTO with source data
	 * @param crDto DTO with result data
	 * @param carreShape square on process
	 * @param geometryToAnalyse analysed
	 * @param shapeParkOnSquare shape of park isochrones
	 * @return ComputeResultDto
	 */
	protected ComputeResultDto computePopAndDensityDetail(
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
			IrisData irisData = irisDataRepository.findByAnneeAndIris(dto.annee, carreWithIso.getIris());
			
			if (irisData!=null) {
				log.info("Filosofil200m     found,{},{}", dto.annee, carreWithIso.getIris());
				// nb habitant au carre
				if (carreShape.getIris().equals(irisData.getIris())) {
					dto.popAll = irisData.getPop();
				}
				
				Double nbHabCarre = irisData.getPop().doubleValue();
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
	 * computePopAndDensity.
	 * @param dto ComputeIrisDto
	 * @param carreShape shap
	 * @param shapeParkOnIris shape
	 */
	protected void computePopAndDensity(ComputeIrisDto dto, IrisShape carreShape, Geometry shapeParkOnIris) {

		Geometry geometryToAnalyse =dto.polygonParkAreas;
		ComputeResultDto rDto = this.computePopAndDensityDetail(dto, dto.result, carreShape, geometryToAnalyse, shapeParkOnIris);
		dto.result = rDto;
		
		if (dto.allAreOms) {
			dto.resultOms = rDto;
		} else {
			geometryToAnalyse =dto.polygonParkAreasOms;
			rDto = this.computePopAndDensityDetail(dto, dto.resultOms, carreShape, geometryToAnalyse, shapeParkOnIris);
			dto.resultOms = rDto;
		}
	}
	
	/**
	 * computeCarreByComputeJob entry point for batch.
	 * @param job ComputeJob
	 * @return true if done
	 */
	@Override
	public Boolean computeIrisByComputeJob(ComputeIrisJob job) {
		log.info("begin computeCarre {}", job.getIris());
		Boolean ret = Boolean.FALSE;
		
		Optional<IrisShape> oIris = irisShapeRepository.findById(job.getIris());
		if (oIris.isPresent()) {
			try {
				IrisShape irisShape = oIris.get();
				Boolean isDense = serviceOpenData.isDistanceDense(irisShape.getCodeInsee());
				this.computeIrisShape(job, irisShape, isDense);
				ret = Boolean.TRUE;
				
			} catch (Exception e) {
				log.error("computeCarre in error: {} {}", job.getIris(), job.getAnnee());
				log.error("stacktrace", e);
			}
		}
		
		return ret;
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
