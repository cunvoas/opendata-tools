package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import lombok.extern.slf4j.Slf4j;

/**
 * compute service for ParkAreaComputed.
 */
@Service
@Slf4j
public class ComputeParkAreaV2 {

	// 200m x 200m = 4 10^4: insee data is 40000 +-1 accuracy
	private static final Double SURFACE_CARRE = 40_000d;
	
	@Autowired
	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Autowired
	private IrisShapeRepository irisShapeRepository;
	
	@Autowired	//inseeCare200mRepository
	private Filosofil200mRepository filosofil200mRepository;
	
	@Autowired
	private ParkAreaComputedRepository parkAreaComputedRepository;
	@Autowired
	private ParkJardinRepository parkJardinRepository;
	@Autowired
	private ParkTypeService parkTypeService;
	@Autowired
	private ServiceOpenData serviceOpenData;
	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	

	/**
	 * 
	 * @param parcCpu
	 * @param park
	 * @param annee
	 */
	protected Boolean computeGenericHead(ParkAreaComputed parcCpu, ParkArea park, Integer annee) {
		
		// check update date
		Boolean alreadyProcessed = parcCpu.getUpdated().after(park.getUpdated());
		if (alreadyProcessed) {
			// check data insee
			alreadyProcessed = parcCpu.getSurfacePerInhabitant()!=null && parcCpu.getSurfacePerInhabitant()!=null;
		}
		
		if (alreadyProcessed) {
			return alreadyProcessed;
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
			
			if (pj.getSurface()==null && pj.getContour()!=null) {
				pj.setSurface(this.getSurface(pj.getContour()).doubleValue());
				parkJardinRepository.save(pj);
			}
			
			parcCpu.setSurface(new BigDecimal(Math.round(pj.getSurface())));
			
			Boolean isDense = serviceOpenData.isDistanceDense(pj.getCommune());
			parcCpu.setIsDense(isDense);
		}
		return alreadyProcessed;
	}
	
	/**
	 * compute data from carres.
	 * @param parcCpu
	 * @param park
	 * @param annee
	 */
	protected void  computeGenericCarre(ParkAreaComputed parcCpu, ParkArea park, Integer annee) {

		List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(park.getPolygon()));
		
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
	}
	
	/**
	 * compute data from iris.
	 * @param parcCpu
	 * @param park
	 * @param annee
	 */
	protected void  computeGenericIris(ParkAreaComputed parcCpu, ParkArea park, Integer annee) {
		
		List<IrisShape> shapesIris = irisShapeRepository.findIrisInMapArea(GeometryQueryHelper.toText(park.getPolygon()));

		// surface intersection algorithm
		BigDecimal population = BigDecimal.ZERO;
		for (IrisShape shapeIris : shapesIris) {
			//TODO optim ici avec la surface en base.
			Long lSurface = this.getSurface(shapeIris.getContour());
			Double surfaceIris = lSurface.doubleValue();
			
			Geometry parkOnCarre = shapeIris.getContour().intersection(park.getPolygon());
			Long surfIntersect = getSurface(parkOnCarre);
			
			//lookup for carre200m data
			Filosofil200m carre = filosofil200mRepository.findByAnneeAndIdInspire(annee, shapeIris.getIris());
			Long popCar = 0L;
			if (carre!=null) {
				popCar =Math.round(carre.getNbIndividus().doubleValue());
			}
			Long popIntersect = Math.round(popCar*surfIntersect/surfaceIris);
			population = population.add(new BigDecimal(popIntersect));
		}
		parcCpu.setPopulationIris(population);
		if (! BigDecimal.ZERO.equals(population)) {
			parcCpu.setSurfacePerInhabitantIris(parcCpu.getSurface().divide(population, 1, RoundingMode.HALF_EVEN));
		}
		
		
	}
	
	public ParkAreaComputed computeGenericParkAreaV2(ParkArea park) {
		return this.computeGenericParkAreaV2(park, null);
	}
	public ParkAreaComputed computeGenericParkAreaV2(ParkArea park, Integer wAnnee) {
		Calendar c1 = Calendar.getInstance();
		c1.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
		
		Date defaultData = c1.getTime();
		
		
		ParkAreaComputed parcCpu=null;
		log.info("computePark( {}-{} )",park.getId(), park.getName());
		
		// skip un-precalculated park
		if(park.getPolygon()==null) {
			log.warn("please process {}-{}  first", park.getId(), park.getName());
			return null;
		}
		
		// preset years
		List<Integer> carreAnnees = List.of(applicationBusinessProperties.getInseeAnnees());
		List<Integer> irisAnnees = List.of(applicationBusinessProperties.getIrisAnnees());
		
		HashSet<Integer> set = new  HashSet<>();
		set.addAll(carreAnnees);
		set.addAll(irisAnnees);
		List<Integer> allAnnees = new ArrayList<>(set);
		Collections.sort(allAnnees);
		Collections.reverse(allAnnees);
		
		// get type parameters
		parkTypeService.populate(park);
		
		if (wAnnee!=null) {
			Optional<ParkAreaComputed> parcCpuOpt = parkAreaComputedRepository.findByIdAndAnnee(park.getId(), wAnnee);
				
			if (parcCpuOpt.isPresent()) {
				parcCpu = parcCpuOpt.get();
			} else {
				parcCpu = new ParkAreaComputed();
				parcCpu.setId(park.getId());
				parcCpu.setAnnee(wAnnee);
				parcCpu.setUpdated(defaultData);
			}
			
			Boolean alreadyProcessed = this.computeGenericHead(parcCpu, park, wAnnee);
			if (alreadyProcessed) {
				return parcCpu;
			}
			
			if (carreAnnees.indexOf(wAnnee)!=-1) {
				this.computeGenericCarre(parcCpu, park, wAnnee);
			}

			if (irisAnnees.indexOf(wAnnee)!=-1) {
				this.computeGenericIris(parcCpu, park, wAnnee);
			}
			 
			parcCpu = parkAreaComputedRepository.save(parcCpu);
			return parcCpu;
		} else {
			
			for (Integer annee : allAnnees) {
				
				Optional<ParkAreaComputed> parcCpuOpt = parkAreaComputedRepository.findByIdAndAnnee(park.getId(), annee);
				if (parcCpuOpt.isPresent()) {
					parcCpu = parcCpuOpt.get();
				} else {
					parcCpu = new ParkAreaComputed();
					parcCpu.setId(park.getId());
					parcCpu.setAnnee(annee);
					parcCpu.setUpdated(defaultData);
				}

				Boolean alreadyProcessed = this.computeGenericHead(parcCpu, park, wAnnee);
				if (alreadyProcessed) {
					continue;
				}
				
				if (carreAnnees.indexOf(wAnnee)!=-1) {
					this.computeGenericCarre(parcCpu, park, wAnnee);
				}

				if (irisAnnees.indexOf(wAnnee)!=-1) {
					this.computeGenericIris(parcCpu, park, wAnnee);
				}
				 
				parcCpu.setUpdated(new Date());
				parcCpu = parkAreaComputedRepository.save(parcCpu);
			}
		}

		return null;
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
