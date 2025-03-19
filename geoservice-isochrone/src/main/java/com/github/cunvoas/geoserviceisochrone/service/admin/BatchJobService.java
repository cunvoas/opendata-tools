package com.github.cunvoas.geoserviceisochrone.service.admin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ComputeJobRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.park.ComputeServiceV2;

import lombok.extern.slf4j.Slf4j;

/**
 * Business Service impl.
 */
@Service
@Slf4j
public class BatchJobService {

	private static final DateFormat DF =new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	@Autowired
	private CadastreRepository cadastreRepository;
	@Autowired
	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Autowired
	private ComputeJobRepository computeJobRepository;
	@Autowired
	private ParkAreaRepository parkAreaRepository;

	@Autowired
	private ComputeServiceV2 computeService;
	
	
	/**
	 * request for a park.
	 * @param pj ParcEtJardin
	 */
	public void requestProcessParc(ParcEtJardin pj) {
		log.info("requestProcessParc");
		
		ParkArea pa = parkAreaRepository.findByIdParcEtJardin(pj.getId());
		Geometry geo = pa.getPolygon();
		List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(geo));
		
		appendCarre(shapes);
	}
	
	/**
	 * appendCarre: factorization method.
	 * @param shapes list of InseeCarre200mOnlyShape
	 */
	protected void appendCarre(List<InseeCarre200mOnlyShape> shapes) {
		log.info("appendCarre");
		List<ComputeJob> jobs = new ArrayList<>();	
		List<Integer> annes = List.of(applicationBusinessProperties.getInseeAnnees());
		
		for (InseeCarre200mOnlyShape carreShape : shapes) {
			for (Integer anne : annes) {
				InseeCarre200mComputedId id = new InseeCarre200mComputedId();
				id.setIdInspire(carreShape.getIdInspire());
				id.setAnnee(anne);
				
				ComputeJob job=null;
				Optional<ComputeJob> ojob = computeJobRepository.findById(id);
				if (ojob.isPresent())  {
					job = ojob.get();
					
					// if already processed, relaunch
					if ( ComputeJobStatusEnum.PROCESSED.equals(job.getStatus()) ) {
						job.setStatus(ComputeJobStatusEnum.TO_PROCESS);
						job.setDemand(new Date());
						job.setProcessed(null);
					} else {
						continue;
					}
							
				} else {
					job = new ComputeJob();
					job.setIdInspire(carreShape.getIdInspire());
					job.setAnnee(anne);
					job.setCodeInsee(carreShape.getCodeInsee());
				}
				jobs.add(job);
				
			}
		}
		computeJobRepository.saveAll(jobs);
	}
	
	
	/**
	 * request for one city.
	 * @param city city
	 */
	public void requestProcessCity(City city) {
		log.info("requestProcessCity");
		
		String inseeCode = city.getInseeCode();
		this.requestProcessCity(inseeCode);
	}
	
	public void requestProcessCity(String inseeCode) {
		log.info("requestProcessCity");
		
		//Cadastre cadastre = cadastreRepository.findById(inseeCode).get();
		//List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(cadastre.getGeoShape()));
		List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findByCodeInsee(inseeCode);
		appendCarre(shapes);
	}
	
	
	private Boolean isRunning=false;
	/**
	 * manage run at once.
	 * @param toLaunch true is want to start
	 * @return true id autorized
	 */
	protected synchronized Boolean changeStatus(Boolean toLaunch) {
		Boolean possibleLaunch = false;
		log.info("changeStatus");
		
		// job is running 
		if (isRunning) {
			if (toLaunch) {
				// start request
				possibleLaunch = false;
			} else {
				// stop request
				possibleLaunch = true;
				isRunning=false;
			}
		} else {
			// job is stopped 
			if (toLaunch) {
				// start request
				possibleLaunch = true;
				isRunning=true;
			} else {
				// stop request
				possibleLaunch = false;
				throw (new ExceptionAdmin(ExceptionAdmin.RG_IMPOSSIBLE_CASE));
			}
			
		}
		
		log.info("\tpossibleLaunch = {}", possibleLaunch);
		return possibleLaunch;
	}
	
	/**
	 * scheduled task.
	 * @FIXME need to be smartest by hours.
	 */
	@Scheduled(fixedDelay = 600000, initialDelay = 10000)
	public void processCarres() {
		log.error("processCarres at {}", DF.format(new Date()));
		
		if (this.changeStatus(true)) {
			Pageable page = Pageable.ofSize(10);
			List<ComputeJob> jobs = computeJobRepository.findByStatusOrderByDemandAsc(ComputeJobStatusEnum.TO_PROCESS, page);
			for (ComputeJob job : jobs) {
				
				// tag begin
				job.setProcessed(new Date());
				job.setStatus(ComputeJobStatusEnum.IN_PROCESS);
				computeJobRepository.save(job);
				
				// process
				Boolean processed = computeService.computeCarreByComputeJobV2Optim(job);
				
				// tag end
				if ( Boolean.TRUE.equals(processed)) {
					job.setStatus(ComputeJobStatusEnum.PROCESSED);
				} else {
					job.setStatus(ComputeJobStatusEnum.IN_ERROR);
				}
				job.setProcessed(new Date());
				computeJobRepository.save(job);
			}
		}
		this.changeStatus(false);
	}
	
}
