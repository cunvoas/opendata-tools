package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobProgressStat;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.admin.NdviJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviResult;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviResultObjectType;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import com.github.cunvoas.geoserviceisochrone.repo.NdviResultRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.NdviJobRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Traitement par lots des rafraîchissements NDVI.
 *
 * <p>Remplace l'appel synchrone qui bloquait le calcul OMS pendant l'appel HTTP
 * CDSE. {@code ComputeParkAreaV2} dépose une demande dans {@code ndvi_job}, puis
 * cette tâche planifiée la consomme par paquets.</p>
 *
 * <p>Reprise : les jobs restés {@code IN_PROCESS} après un crash sont recyclés
 * toutes les 4h, les jobs {@code IN_ERROR} sont rejoués quotidiennement.</p>
 *
 * <p>Un seul appel Copernicus est fait par parc et par année de référence.
 * La réponse est stockée dans {@code ndvi_result} avec sa période de mesure et
 * les identifiants de contexte (zone calculée, parc, IRIS, cadastre, carreau 200m).
 * Si une mesure existe déjà, aucun appel n'est fait.</p>
 */
@Service
@Slf4j
public class NdviBatchJobService {

	private static final int PAGE_SIZE = 10;

	private final NdviJobRepository ndviJobRepository;
	private final ParkAreaRepository parkAreaRepository;
	private final ParkAreaComputedRepository parkAreaComputedRepository;
	private final NdviResultRepository ndviResultRepository;
	private final ParkJardinRepository parkJardinRepository;
	private final IrisShapeRepository irisShapeRepository;
	private final InseeCarre200mOnlyShapeRepository carre200mOnlyShapeRepository;
	private final ClientCopernicusNdvi clientCopernicusNdvi;

	private final AtomicBoolean running = new AtomicBoolean(false);

	public NdviBatchJobService(NdviJobRepository ndviJobRepository,
			ParkAreaRepository parkAreaRepository,
			ParkAreaComputedRepository parkAreaComputedRepository,
			NdviResultRepository ndviResultRepository,
			ParkJardinRepository parkJardinRepository,
			IrisShapeRepository irisShapeRepository,
			InseeCarre200mOnlyShapeRepository carre200mOnlyShapeRepository,
			ClientCopernicusNdvi clientCopernicusNdvi) {
		this.ndviJobRepository = ndviJobRepository;
		this.parkAreaRepository = parkAreaRepository;
		this.parkAreaComputedRepository = parkAreaComputedRepository;
		this.ndviResultRepository = ndviResultRepository;
		this.parkJardinRepository = parkJardinRepository;
		this.irisShapeRepository = irisShapeRepository;
		this.carre200mOnlyShapeRepository = carre200mOnlyShapeRepository;
		this.clientCopernicusNdvi = clientCopernicusNdvi;
	}

	/**
	 * @return vrai si le fournisseur NDVI est configuré et actif
	 */
	public boolean isEnabled() {
		return clientCopernicusNdvi.isEnabled();
	}

	/**
	 * Indique si une mesure NDVI existe déjà pour une zone de parc.
	 * @param parkId identifiant de la zone de parc
	 * @return vrai si une mesure est stockée
	 */
	public boolean hasResult(Long parkId) {
		return ndviResultRepository.existsByObjectTypeAndParkAreaComputedId(
				NdviResultObjectType.PARK_AREA_COMPUTED, parkId);
	}

	/**
	 * Dernière date de traitement NDVI connue pour un parc, toutes années confondues.
	 * @param parkId identifiant de la zone de parc
	 * @return date du dernier traitement, ou null
	 */
	public Date lastRequestDate(Long parkId) {
		return ndviJobRepository.findFirstByIdAndObjectTypeAndProcessedIsNotNullOrderByProcessedDesc(
				parkId, NdviResultObjectType.PARK_AREA_COMPUTED)
				.map(NdviJob::getProcessed)
				.orElse(null);
	}

	/**
	 * Statistiques d'avancement NDVI au niveau ville.
	 * @param idCommune filtre commune, facultatif
	 * @param idEpci filtre EPCI, facultatif
	 * @param idRegion filtre région, facultatif
	 * @param type filtre type NDVI, facultatif
	 * @return statistiques agrégées
	 */
	public List<ComputeJobProgressStat> getProgressStatsCityLevel(Long idCommune, Long idEpci,
			Long idRegion, String type) {
		return mapProgressStats(
				ndviJobRepository.getProgressStatsCityLevel(idCommune, idEpci, idRegion, type), true);
	}

	/**
	 * Statistiques d'avancement NDVI au niveau EPCI.
	 * @param idEpci filtre EPCI, facultatif
	 * @param idRegion filtre région, facultatif
	 * @param type filtre type NDVI, facultatif
	 * @return statistiques agrégées
	 */
	public List<ComputeJobProgressStat> getProgressStatsEpciLevel(Long idEpci, Long idRegion,
			String type) {
		return mapProgressStats(ndviJobRepository.getProgressStatsEpciLevel(idEpci, idRegion, type), false);
	}

	private List<ComputeJobProgressStat> mapProgressStats(List<Object[]> rawStats, boolean hasCityName) {
		List<ComputeJobProgressStat> stats = new ArrayList<>();
		for (Object[] row : rawStats) {
			ComputeJobProgressStat stat = new ComputeJobProgressStat();
			stat.setEpciName((String) row[0]);
			stat.setCityName(hasCityName ? (String) row[1] : null);
			stat.setType((String) row[2]);
			stat.setToProcess(((Number) row[3]).longValue());
			stat.setInProcess(((Number) row[4]).longValue());
			stat.setProcessed(((Number) row[5]).longValue());
			stat.setInError(((Number) row[6]).longValue());
			stats.add(stat);
		}
		return stats;
	}

	/**
	 * Dépose (ou redépose) une demande de rafraîchissement NDVI.
	 *
	 * <p>Un job déjà en attente ou en cours n'est pas dupliqué : la file est
	 * idempotente sur la clé (parc, PARK_AREA_COMPUTED).</p>
	 *
	 * @param parkId identifiant de la zone de parc
	 */
	public void request(Long parkId) {
		request(parkId, false);
	}

	/**
	 * Dépose une demande de rafraîchissement NDVI en ignorant le NDVI existant
	 * et la règle du un appel par an. Réservé à l'administration.
	 *
	 * @param parkId identifiant de la zone de parc
	 */
	public void requestForced(Long parkId) {
		request(parkId, true);
	}

	/**
	 * Dépose les relances forcées de tous les parcs calculés d'un EPCI.
	 *
	 * @param com2coId identifiant de la communauté de communes
	 * @param annee année de référence (sélection des parcs calculés)
	 * @return nombre de parcs relancés
	 */
	public int requestForcedForCom2Co(Long com2coId, Integer annee) {
		if (!isEnabled() || com2coId == null || annee == null) {
			return 0;
		}
		List<Long> parkIds = parkAreaComputedRepository.findParkIdsByAnneeAndCom2co(annee, com2coId);
		for (Long parkId : parkIds) {
			requestForced(parkId);
		}
		return parkIds.size();
	}

	private void request(Long parkId, boolean force) {
		// Une seule demande par parc : ligne PARK_AREA_COMPUTED, id_inspire = sentinelle.
		Optional<NdviJob> existing = ndviJobRepository
				.findByIdAndIdInspireAndObjectType(parkId, NdviJob.NO_ID_INSPIRE, NdviResultObjectType.PARK_AREA_COMPUTED);
		NdviJob job;
		if (existing.isPresent()) {
			job = existing.get();
			if (ComputeJobStatusEnum.IN_PROCESS.equals(job.getStatus())) {
				// Ne pas dupliquer un appel en cours ; la relance sera à refaire.
				return;
			}
			if (!force && ComputeJobStatusEnum.TO_PROCESS.equals(job.getStatus())) {
				return;
			}
			job.setStatus(ComputeJobStatusEnum.TO_PROCESS);
			job.setDemand(new Date());
			job.setProcessed(null);
			job.setForce(force);
		} else {
			job = new NdviJob();
			job.setId(parkId);
			job.setObjectType(NdviResultObjectType.PARK_AREA_COMPUTED);
			job.setForce(force);
		}
		ndviJobRepository.save(job);
	}

	/**
	 * Demande le NDVI pour un parc métier, si la donnée n'existe pas déjà.
	 *
	 * <p>Utilisé par le bouton « Calculer le parc » : le parc est identifié par
	 * {@code ParcEtJardin.id}, alors que la file NDVI utilise {@code ParkArea.id}.</p>
	 *
	 * @param parcEtJardinId identifiant du parc
	 * @return vrai si une demande a été déposée
	 */
	public boolean requestForParc(Long parcEtJardinId) {
		if (!isEnabled() || parcEtJardinId == null) {
			return false;
		}
		ParkArea park = parkAreaRepository.findByIdParcEtJardin(parcEtJardinId);
		if (park == null || park.getPolygon() == null) {
			return false;
		}
		if (hasResult(park.getId())) {
			return false;
		}
		request(park.getId());
		return true;
	}

	/**
	 * Consomme la file par lot de {@value #PAGE_SIZE}.
	 * Exécution toutes les 5 minutes, comme les autres jobs de calcul.
	 */
	@Scheduled(fixedDelayString = "${application.ndvi.batch.delay-seconds:300}",
			initialDelayString = "${application.ndvi.batch.initial-delay-seconds:60}",
			timeUnit = TimeUnit.SECONDS)
	public void processJobs() {
		if (!isEnabled() || !running.compareAndSet(false, true)) {
			return;
		}
		try {
			// shortcut: un seul lot par cycle car le scheduler est mono-thread,
			// augmenter si un TaskScheduler dédié est ajouté.
			// shortcut: seul PARK_AREA_COMPUTED est consommé, les autres types restent TO_PROCESS,
			// à activer quand leur fournisseur NDVI sera branché.
			List<NdviJob> jobs = ndviJobRepository
					.findByStatusAndObjectTypeOrderByDemandAsc(ComputeJobStatusEnum.TO_PROCESS,
							NdviResultObjectType.PARK_AREA_COMPUTED, Pageable.ofSize(PAGE_SIZE));
			for (NdviJob job : jobs) {
				processOne(job);
			}
		} finally {
			running.set(false);
		}
	}

	/**
	 * Traite un job : marque {@code IN_PROCESS} avant l'appel HTTP pour que la
	 * reprise puisse le détecter, puis enregistre la mesure dans {@code ndvi_result}.
	 *
	 * <p>Aucune colonne NDVI de {@code park_area_computed} n'est alimentée.</p>
	 */
	void processOne(NdviJob job) {
		job.setStatus(ComputeJobStatusEnum.IN_PROCESS);
		job.setProcessed(new Date());
		ndviJobRepository.save(job);

		try {
			ParkArea park = parkAreaRepository.findById(job.getId()).orElse(null);
			List<ParkAreaComputed> rows = parkAreaComputedRepository.findAllByIdPark(job.getId());
			if (park == null || park.getPolygon() == null || rows.isEmpty()) {
				log.warn("NDVI impossible pour le parc {}-{} : parc ou calcul absent",
						job.getId(), job.getObjectType());
				job.setStatus(ComputeJobStatusEnum.IN_ERROR);
				return;
			}

			// Mesure déjà disponible : ne pas rappeler le fournisseur.
			// Une relance forcée par un administrateur ignore ce court-circuit.
			if (!job.isForce() && hasResult(park.getId())) {
				job.setStatus(ComputeJobStatusEnum.PROCESSED);
				return;
			}

			Optional<NdviStatistics> stats = clientCopernicusNdvi
					.fetchNdviForPark(park.getId(), park.getPolygon());
			Date requestDate = new Date();
			if (stats.isPresent()) {
				ndviResultRepository.save(toResult(park, requestDate, stats.get()));
			}
			// Une période sans donnée ne crée pas de mesure : le dernier job traité
			// sert de référence pour la règle du un appel par an.
			job.setStatus(ComputeJobStatusEnum.PROCESSED);
		} catch (Exception e) {
			log.warn("NDVI non récupéré pour le parc {}-{} : {}",
					job.getId(), job.getObjectType(), e.getMessage());
			job.setStatus(ComputeJobStatusEnum.IN_ERROR);
		} finally {
			job.setProcessed(new Date());
			// Une relance forcée ne vaut que pour une tentative : évite de rejouer
			// chaque jour un appel Copernicus coûteux en cas d'erreur persistante.
			job.setForce(false);
			ndviJobRepository.save(job);
		}
	}

	/**
	 * Convertit une réponse Copernicus en mesure persistable.
	 *
	 * @param park zone de parc
	 * @param requestedAt date de l'appel
	 * @param stats statistiques retournées
	 * @return mesure prête à persister
	 */
	private NdviResult toResult(ParkArea park, Date requestedAt, NdviStatistics stats) {
		NdviResult result = new NdviResult();
		result.setObjectType(NdviResultObjectType.PARK_AREA_COMPUTED);
		result.setParkAreaComputedId(park.getId());
		result.setParcEtJardinId(park.getIdParcEtJardin());
		result.setCadastreId(cadastreId(park));
		String geometry = GeometryQueryHelper.toText(park.getPolygon());
		result.setIrisId(irisShapeRepository.findBestIrisCode(geometry));
		result.setCarre200mId(carre200mOnlyShapeRepository.findBestIdInspire(geometry));
		result.setRequestedAt(requestedAt);
		result.setPeriodStart(stats.periodStart());
		result.setPeriodEnd(stats.acquisitionDate());
		result.setSampleCount(stats.sampleCount());
		if (stats.ndvi() != null) {
			result.setNdviMean(stats.ndvi().mean());
			result.setNdviMin(stats.ndvi().min());
			result.setNdviMax(stats.ndvi().max());
			result.setNdviStddev(stats.ndvi().stdDev());
		}
		if (stats.ndre() != null) {
			result.setNdreMean(stats.ndre().mean());
			result.setNdreMin(stats.ndre().min());
			result.setNdreMax(stats.ndre().max());
			result.setNdreStddev(stats.ndre().stdDev());
		}
		if (stats.gndvi() != null) {
			result.setGndviMean(stats.gndvi().mean());
			result.setGndviMin(stats.gndvi().min());
			result.setGndviMax(stats.gndvi().max());
			result.setGndviStddev(stats.gndvi().stdDev());
		}
		return result;
	}

	private String cadastreId(ParkArea park) {
		if (park.getIdParcEtJardin() == null) {
			return null;
		}
		return parkJardinRepository.findById(park.getIdParcEtJardin())
				.map(ParcEtJardin::getCommune)
				.map(City::getInseeCode)
				.orElse(null);
	}

	/**
	 * Recycle les jobs bloqués en {@code IN_PROCESS} depuis plus de 8h15.
	 */
	@Scheduled(cron = "0 25 */4 * * *")
	public void recycleUndoneJobs() {
		Date oldDate = hoursAgo(8, 15);
		List<NdviJob> jobs = ndviJobRepository.findOnStartUnfinishedProcessed(oldDate);
		for (NdviJob job : jobs) {
			job.setDemand(new Date());
			job.setStatus(ComputeJobStatusEnum.TO_PROCESS);
			ndviJobRepository.save(job);
		}
		if (!jobs.isEmpty()) {
			log.warn("{} jobs NDVI bloqués remis en file", jobs.size());
		}
	}

	/**
	 * Rejoue quotidiennement les jobs NDVI en erreur depuis la veille.
	 */
	@Scheduled(cron = "0 20 0 * * *")
	public void recycleErrorJobs() {
		Date oldDate = hoursAgo(24, 0);
		List<NdviJob> jobs = ndviJobRepository.findOnErrorAndProcessed(oldDate);
		for (NdviJob job : jobs) {
			job.setDemand(new Date());
			job.setStatus(ComputeJobStatusEnum.TO_PROCESS);
			ndviJobRepository.save(job);
		}
		if (!jobs.isEmpty()) {
			log.warn("{} jobs NDVI en erreur remis en file", jobs.size());
		}
	}

	private static Date hoursAgo(int hours, int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, -hours);
		cal.add(Calendar.MINUTE, -minutes);
		return cal.getTime();
	}
}
