package com.github.cunvoas.geoserviceisochrone.service.compute;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeIrisJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStat;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedId;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisId;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ComputeJobIrisRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ComputeJobRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.park.IComputeCarreService;
import com.github.cunvoas.geoserviceisochrone.service.park.IComputeIrisService;

import lombok.extern.slf4j.Slf4j;

/**
 * Business Service impl.
 */
@Service
@Slf4j
public class BatchJobService implements DisposableBean{

	private static final DateFormat DF =new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");
	
	private Boolean shutdownRequired = Boolean.FALSE;
	private Boolean shutdownReady = Boolean.FALSE;
	private Boolean jobIsRunning=Boolean.FALSE;
	public void shutdown() {
		this.shutdownRequired=Boolean.TRUE;
	}
	

	@Autowired
	private ApplicationBusinessProperties applicationBusinessProperties;
	
	@Autowired
	private CadastreRepository cadastreRepository;
	@Autowired
	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	
	@Autowired
	private IrisShapeRepository irisShapeRepository;
	@Autowired
	private ComputeJobRepository computeJobCarreRepository;
	@Autowired
	private ParkAreaRepository parkAreaRepository;
	
	@Autowired
	private IComputeCarreService computeCarreService;

	@Autowired
	private ComputeJobIrisRepository computeJobIrisRepository;
	@Autowired
	private IComputeIrisService computeIrisService;
	
	
	/**
	 * request for a park.
	 * @param pj ParcEtJardin
	 */
	public int requestProcessParc(ParcEtJardin pj) {
		log.info("requestProcessParc");	
		int nbShapes=0;
		ParkArea pa = parkAreaRepository.findByIdParcEtJardin(pj.getId());
		Geometry geo = pa.getPolygon();
		
		Date upd= pa.getUpdated();
		
		List<InseeCarre200mOnlyShape> carreShapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(geo));
		appendCarre(carreShapes, upd);
		nbShapes = carreShapes.size();
		
		List<IrisShape> irisSpape = irisShapeRepository.findIrisInMapArea(GeometryQueryHelper.toText(geo));
		appendIris(irisSpape, upd);
		nbShapes += irisSpape.size();
		
		return nbShapes;
	}
	
	
	/**
	 * appendIris: factorization method.
	 * @param shapes list of IrisShape
	 */
	protected void appendIris(List<IrisShape> shapes, Date upd) {
		log.info("appendIris");
		List<ComputeIrisJob> jobs = new ArrayList<>();	
		List<Integer> annes = List.of(applicationBusinessProperties.getInseeAnnees());

		for (Integer anne : annes) {
			
			for (IrisShape carreShape : shapes) {
				IrisId id = new IrisId();
				id.setIris(carreShape.getIris());
				id.setAnnee(anne);
				
				ComputeIrisJob job=null;
				Optional<ComputeIrisJob> ojob = computeJobIrisRepository.findById(id);
				if (ojob.isPresent())  {
					job = ojob.get();

					// check if update on source, if not, skip
					boolean skip = job.getProcessed()!=null && upd!=null?upd.before(job.getProcessed()):false;
					//skip=false;
					// if already processed, relaunch
					if (!skip && ComputeJobStatusEnum.PROCESSED.equals(job.getStatus()) ) {
						job.setStatus(ComputeJobStatusEnum.TO_PROCESS);
						job.setDemand(new Date());
						job.setProcessed(null);
					} else {
						continue;
					}
							
				} else {
					job = new ComputeIrisJob();
					job.setIris(carreShape.getIris());
					job.setAnnee(anne);
					job.setCodeInsee(carreShape.getCodeInsee());
				}
				jobs.add(job);
				
				if (this.shutdownRequired) {
					break;
				}
				
			}

			if (this.shutdownRequired) {
				break;
			}
		}
		computeJobIrisRepository.saveAll(jobs);

		if (this.shutdownRequired) {
			this.shutdownReady=Boolean.TRUE;
		}
	}
	
	/**
	 * appendCarre: factorization method.
	 * @param shapes list of InseeCarre200mOnlyShape
	 */
	protected void appendCarre(List<InseeCarre200mOnlyShape> shapes, Date upd) {
		log.info("appendCarre");
		List<ComputeJob> jobs = new ArrayList<>();	
		List<Integer> annes = List.of(applicationBusinessProperties.getInseeAnnees());

		//TODO Optim here, do not recompute old years.
		for (Integer anne : annes) {
			
			for (InseeCarre200mOnlyShape carreShape : shapes) {
				InseeCarre200mComputedId id = new InseeCarre200mComputedId();
				id.setIdInspire(carreShape.getIdInspire());
				id.setAnnee(anne);
				
				ComputeJob job=null;
				Optional<ComputeJob> ojob = computeJobCarreRepository.findById(id);
				if (ojob.isPresent())  {
					job = ojob.get();

					// check if update on source, if not, skip
					boolean skip = upd!=null?upd.before(job.getProcessed()):false;
					
					// if already processed, relaunch
					if (!skip && ComputeJobStatusEnum.PROCESSED.equals(job.getStatus()) ) {
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
		computeJobCarreRepository.saveAll(jobs);

	}
	

	/**
	 * request for one city.
	 * @param city city
	 * @return nb carre
	 */
	public int requestProcessCom2Co(CommunauteCommune com2co) {
		log.info("requestProcessCom2Co");
		int t=0;
		for (City c : com2co.getCities()) {
			t += this.requestProcessCity(c);
		}
		return t;
	}
	
	/**
	 * request for one city.
	 * @param city city
	 * @return nb carre
	 */
	public int requestProcessCity(City city) {
		log.info("requestProcessCity");
		
		String inseeCode = city.getInseeCode();
		return this.requestProcessCity(inseeCode);
	}
	
	/**
	 * @param inseeCode
	 * @returncity
	 * @return nb carre
	 */
	public int requestProcessCity(String inseeCode) {
		log.info("requestProcessCity");
		
		int nbShapes=0;
		
		//Cadastre cadastre = cadastreRepository.findById(inseeCode).get();
		List<InseeCarre200mOnlyShape> carreShapes = inseeCarre200mOnlyShapeRepository.findByCodeInsee(inseeCode);
		appendCarre(carreShapes, null);
		nbShapes = carreShapes.size();
		
		List<IrisShape> irisShapes = irisShapeRepository.findByCodeInsee(inseeCode);
		appendIris(irisShapes, null);
		nbShapes += irisShapes.size();
		
		return nbShapes;
	}
	
	
	
	/**
	 * manage treatments without competition.
	 * @param toLaunch true is want to start, false at finishing
	 * @return true if Launch is autorized
	 */
	protected synchronized Boolean launchOrFinish(Boolean toLaunch) {
		Boolean possibleLaunch = false;
		log.info("changeStatus");
		
		// job is running 
		if (jobIsRunning) {
			if (toLaunch) {
				// start request
				possibleLaunch = false;
			} else {
				// stop request
				possibleLaunch = true;
				jobIsRunning=false;
			}
		} else {
			// job is stopped 
			if (toLaunch) {
				// start request
				possibleLaunch = true;
				jobIsRunning=true;
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
	 * process one (debug).
	 * @param idInspire
	 */
	public void processCarres(Integer annee, String idInspire) {
		
		InseeCarre200mComputedId id = new InseeCarre200mComputedId();
		id.setAnnee(annee);
		id.setIdInspire(idInspire);
		
		Optional<ComputeJob> oJob = computeJobCarreRepository.findById(id);
		if (oJob.isPresent()) {
			ComputeJob job = oJob.get();
			Boolean processed = computeCarreService.computeCarreByComputeJob(job);
			
			// tag end
			if ( Boolean.TRUE.equals(processed)) {
				job.setStatus(ComputeJobStatusEnum.PROCESSED);
			} else {
				job.setStatus(ComputeJobStatusEnum.IN_ERROR);
			}

			job.setProcessed(new Date());
			computeJobCarreRepository.save(job);
		}
	}
	
	/**
	 * reset in queue jobs.
	 * arrives when when another app connection to DB.
	 * @see https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html
	 *		    second
	 *		    minute
	 *		    hour
	 *		    day of month
	 *		    month
	 *		    day of week
     *
	 * ie. all jobs from prev day 18:00
	 */
	@Scheduled(cron = "0 15 */4 * * *")
	public void recycleUndoneShapes() {
		
		if (this.shutdownRequired) {
			return;
		}
		
		Date newDate = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -8);
		cal.add(Calendar.MINUTE, -15);
		Date oldDate = cal.getTime();
		
		
		//recycle carres
		List<ComputeJob> jobs = computeJobCarreRepository.findOnStartUnfinishedProcessed(oldDate);
		if (!jobs.isEmpty()) {
			log.warn("{} jobs runs incorrecly , new demande time is {}", jobs.size(), newDate);
			
			for (ComputeJob computeJob : jobs) {
				computeJob.setDemand(newDate);
				computeJob.setStatus(ComputeJobStatusEnum.TO_PROCESS);
				computeJobCarreRepository.save(computeJob);
			}
		}
		
		//recycle iris
		List<ComputeIrisJob> irisJobs = computeJobIrisRepository.findOnStartUnfinishedProcessed(oldDate);
		if (!jobs.isEmpty()) {
			log.warn("{} jobs runs incorrecly , new demande time is {}", jobs.size(), newDate);
			
			for (ComputeIrisJob computeJob : irisJobs) {
				computeJob.setDemand(newDate);
				computeJob.setStatus(ComputeJobStatusEnum.TO_PROCESS);
				computeJobIrisRepository.save(computeJob);
			}
		}
	}

	
	/**
	 * reset in queue jobs.
	 * arrives when when another app connection to DB.
	 * @see https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html
	 *		    second
	 *		    minute
	 *		    hour
	 *		    day of month
	 *		    month
	 *		    day of week
     *
	 * ie. all jobs from prev day 18:00
	 */
	@Scheduled(cron = "0 10 0 * * *")
	public void recycleErrorShapes() {
		
		if (this.shutdownRequired) {
			return;
		}
		
		Date newDate = new Date();
		
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date oldDate = cal.getTime();
		
		
		//recycle carres
		List<ComputeJob> jobs = computeJobCarreRepository.findOnErrorAndProcessed(oldDate);
		if (!jobs.isEmpty()) {
			log.warn("{} jobs runs incorrecly , new demande time is {}", jobs.size(), newDate);
			
			for (ComputeJob computeJob : jobs) {
				computeJob.setDemand(newDate);
				computeJob.setStatus(ComputeJobStatusEnum.TO_PROCESS);
				computeJobCarreRepository.save(computeJob);
			}
		}
		
		//recycle iris
		List<ComputeIrisJob> irisJobs = computeJobIrisRepository.findOnErrorAndProcessed(oldDate);
		if (!jobs.isEmpty()) {
			log.warn("{} jobs runs incorrecly , new demande time is {}", jobs.size(), newDate);
			
			for (ComputeIrisJob computeJob : irisJobs) {
				computeJob.setDemand(newDate);
				computeJob.setStatus(ComputeJobStatusEnum.TO_PROCESS);
				computeJobIrisRepository.save(computeJob);
			}
		}
	}
	
	/**
	 * Detect JUnit environnement
	 * @return
	 */
	public static boolean isJUnitTest() {  
		  for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
		    if (element.getClassName().startsWith("org.junit.")) {
		      return true;
		    }           
		  }
		  return false;
		}
	
	/**
	 * scheduled task for Carres & Iris.
	 * @FIXME need to be smartest by hours.
	 * fixedDelay = 400_000 few times more to process 10 squares
	 */
	@Scheduled(fixedDelay = 300, initialDelay = 60, timeUnit = TimeUnit.SECONDS)
	public void processShapes() {
		
		// skip if JUnit
		if (isJUnitTest() ) {
			return;
		}
		
		// skip if shutdown required
		if (this.shutdownRequired) {
			log.warn("shutdown required, skip processShapes");
			return;
		}
		
		int pageSize=10;
		
		log.error("processShapes at {}", DF.format(new Date()));
		
		// launch process if possible
		if (this.launchOrFinish(true)) {

			Pageable page = Pageable.ofSize(pageSize);

			// safe infinite loop, so make a limit
			int possibleMax=100;
			
			// true by default for 1st iteration
			boolean possibleNext=true;
			
			while (possibleNext && possibleMax>0) {
				possibleMax--;
			
				List<ComputeJob> jobs = null;
				if (onDev()) {
					jobs = computeJobCarreRepository.findByStatusOrderByDemandDesc(ComputeJobStatusEnum.TO_PROCESS, page);
				} else {
					jobs = computeJobCarreRepository.findByStatusOrderByDemandAsc(ComputeJobStatusEnum.TO_PROCESS, page);
				}
				possibleNext = jobs!=null?jobs.size()==pageSize:false;
				
				// compute for CARRE
				for (ComputeJob job : jobs) {
					
					// tag begin
					job.setProcessed(new Date());
					job.setStatus(ComputeJobStatusEnum.IN_PROCESS);
					computeJobCarreRepository.save(job);
					
					// process
					Boolean processed = computeCarreService.computeCarreByComputeJob(job);
					
					// tag end
					if ( Boolean.TRUE.equals(processed)) {
						job.setStatus(ComputeJobStatusEnum.PROCESSED);
					} else {
						job.setStatus(ComputeJobStatusEnum.IN_ERROR);
					}
					job.setProcessed(new Date());
					computeJobCarreRepository.save(job);
				}
				
				
				// check shutdown
				if (this.shutdownRequired) {
					log.warn("shutdown required, stop processShapes");
					break;
				}
				
				// compute for IRIS
				List<ComputeIrisJob> irisJobs = null;
				if (onDev()) {
					irisJobs = computeJobIrisRepository.findByStatusOrderByDemandDesc(ComputeJobStatusEnum.TO_PROCESS, page);
				} else {
					irisJobs = computeJobIrisRepository.findByStatusOrderByDemandAsc(ComputeJobStatusEnum.TO_PROCESS, page);
				}
				possibleNext = irisJobs!=null?jobs.size()==pageSize:false;
				
				for (ComputeIrisJob irisJob : irisJobs) {
					// tag begin
					irisJob.setProcessed(new Date());
					irisJob.setStatus(ComputeJobStatusEnum.IN_PROCESS);
					computeJobIrisRepository.save(irisJob);
					
					// process
					Boolean processed = computeIrisService.computeIrisByComputeJob(irisJob);
					
					// tag end
					if ( Boolean.TRUE.equals(processed)) {
						irisJob.setStatus(ComputeJobStatusEnum.PROCESSED);
					} else {
						irisJob.setStatus(ComputeJobStatusEnum.IN_ERROR);
					}
					irisJob.setProcessed(new Date());
					computeJobIrisRepository.save(irisJob);
				}
				
				// check shutdown
				if (this.shutdownRequired) {
					log.warn("shutdown required, stop processShapes");
					break;
				}
				
			}//while

		}
		this.launchOrFinish(false);
	}
	
	/**
	 * detect dev platform.
	 * @return true if on dev
	 */
	private boolean onDev() {
		boolean ret = false;
		try {
			String name = InetAddress.getLocalHost().getHostName();
			ret = "P20230205".equalsIgnoreCase(name);
		} catch (UnknownHostException ignore) {
		}
		return ret;
	}
	
	
	/**
	 * getGlobalStats.
	 * @return list of stats
	 */
	public List<ComputeJobStat> getGlobalStats() {
		List<Object[]> objs = computeJobCarreRepository.getGlobalStats();
		return map(objs);
	}
	
	/**
	 * getStatsByCity.
	 * @param insee city code
	 * @return list of stats
	 */
	public List<ComputeJobStat> getStatsByCity(String insee) {
		List<Object[]> objs = computeJobCarreRepository.getStatsByCodeInsee(insee);
		return map(objs);
	}
	
	/**
	 * map
	 * @param objs lost of columns 
	 * @return  List<ComputeJobStat>
	 */
	public List<ComputeJobStat> map(List<Object[]> objs) {
		List<ComputeJobStat> stats = new ArrayList<>();
		if (objs!=null) {
			for (Object[] objects : objs) {
				ComputeJobStat stat = new ComputeJobStat();
				stats.add(stat);
				
				stat.setNb((Integer)objects[0]);
				Integer status = (Integer)objects[1];
				stat.setStatus(this.map(status));	
				
				if (objects.length>2) {
					stat.setCodeInsee((String)objects[2]);	
				}
			}
		}
		return stats;
	}
	
	/**
	 * map for enum.
	 * @param idx idc from DB.
	 * @return ComputeJobStatusEnum
	 */
	private ComputeJobStatusEnum map(int idx) {
		ComputeJobStatusEnum theEnum=ComputeJobStatusEnum.TO_PROCESS;
		switch (idx) {
			case 0: {
				theEnum=ComputeJobStatusEnum.TO_PROCESS;
				break;
			}
			case 1: {
				theEnum=ComputeJobStatusEnum.IN_PROCESS;
				break;
			}
			case 2: {
				theEnum=ComputeJobStatusEnum.PROCESSED;
				break;
			}
			case 3: {
				theEnum=ComputeJobStatusEnum.IN_ERROR;
				break;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + idx);
		}
		return theEnum;
	}
	
	@Override
	public void destroy() throws Exception {
		log.warn("SIGTERM detected");
		this.shutdown();
		
		log.warn("SIGTERM gracefull termination initiated");
		while (this.jobIsRunning && !this.shutdownReady) {
			log.info("SIGTERM gracefull termination is running");
			Thread.sleep(100L);
		}
		log.warn("SIGTERM gracefull termination finish destroy");
	}
	
}
