package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.admin.NdviJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviObservation;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputedId;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.NdviJobRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.NdviObservationRepository;

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
 * <p>Un seul appel Copernicus est fait par parc et par année de référence ;
 * le NDVI étant lissé sur plusieurs années, le résultat est recopié sur toutes
 * les lignes {@code park_area_computed} du parc pour économiser le quota.
 * Si un NDVI est déjà présent, aucun appel n'est fait : la donnée existante est
 * réutilisée sur les lignes qui n'en ont pas.</p>
 *
 * <p>Chaque réponse contenant des statistiques est aussi conservée dans
 * {@code ndvi_observation} : période, date d'acquisition et valeurs. Cet
 * historique permet de reventiler les NDVI a posteriori et de reconstituer
 * l'histoire végétale du parc, indépendamment de la recopie annuelle.</p>
 */
@Service
@Slf4j
public class NdviBatchJobService {

	private static final int PAGE_SIZE = 10;

	private final NdviJobRepository ndviJobRepository;
	private final ParkAreaRepository parkAreaRepository;
	private final ParkAreaComputedRepository parkAreaComputedRepository;
	private final NdviObservationRepository ndviObservationRepository;
	private final ClientCopernicusNdvi clientCopernicusNdvi;

	private final AtomicBoolean running = new AtomicBoolean(false);

	public NdviBatchJobService(NdviJobRepository ndviJobRepository,
			ParkAreaRepository parkAreaRepository,
			ParkAreaComputedRepository parkAreaComputedRepository,
			NdviObservationRepository ndviObservationRepository,
			ClientCopernicusNdvi clientCopernicusNdvi) {
		this.ndviJobRepository = ndviJobRepository;
		this.parkAreaRepository = parkAreaRepository;
		this.parkAreaComputedRepository = parkAreaComputedRepository;
		this.ndviObservationRepository = ndviObservationRepository;
		this.clientCopernicusNdvi = clientCopernicusNdvi;
	}

	/**
	 * @return vrai si le fournisseur NDVI est configuré et actif
	 */
	public boolean isEnabled() {
		return clientCopernicusNdvi.isEnabled();
	}

	/**
	 * Dépose (ou redépose) une demande de rafraîchissement NDVI.
	 *
	 * <p>Un job déjà en attente ou en cours n'est pas dupliqué : la file est
	 * idempotente sur la clé (parc, année).</p>
	 *
	 * @param parkId identifiant de la zone de parc
	 * @param annee année de référence ({@code park_area_computed.annee})
	 */
	public void request(Long parkId, Integer annee) {
		// Clé = ligne de référence du parc : une seule demande par parc et par année de référence.
		ParkAreaComputedId key = new ParkAreaComputedId();
		key.setId(parkId);
		key.setAnnee(annee);

		Optional<NdviJob> existing = ndviJobRepository.findById(key);
		NdviJob job;
		if (existing.isPresent()) {
			job = existing.get();
			if (ComputeJobStatusEnum.TO_PROCESS.equals(job.getStatus())
					|| ComputeJobStatusEnum.IN_PROCESS.equals(job.getStatus())) {
				return;
			}
			job.setStatus(ComputeJobStatusEnum.TO_PROCESS);
			job.setDemand(new Date());
			job.setProcessed(null);
		} else {
			job = new NdviJob();
			job.setId(parkId);
			job.setAnnee(annee);
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
	 * @param annee année de référence
	 * @return vrai si une demande a été déposée
	 */
	public boolean requestForParc(Long parcEtJardinId, Integer annee) {
		if (!isEnabled() || parcEtJardinId == null || annee == null) {
			return false;
		}
		ParkArea park = parkAreaRepository.findByIdParcEtJardin(parcEtJardinId);
		if (park == null || park.getPolygon() == null) {
			return false;
		}
		boolean hasNdvi = parkAreaComputedRepository.findAllByIdPark(park.getId()).stream()
				.anyMatch(row -> row.getNdviMean() != null);
		if (hasNdvi) {
			return false;
		}
		request(park.getId(), annee);
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
			List<NdviJob> jobs = ndviJobRepository
					.findByStatusOrderByDemandAsc(ComputeJobStatusEnum.TO_PROCESS, Pageable.ofSize(PAGE_SIZE));
			for (NdviJob job : jobs) {
				processOne(job);
			}
		} finally {
			running.set(false);
		}
	}

	/**
	 * Traite un job : marque {@code IN_PROCESS} avant l'appel HTTP pour que la
	 * reprise puisse le détecter, puis met à jour {@code park_area_computed}.
	 *
	 * <p>La réponse est enregistrée dans {@code ndvi_observation} avant la recopie
	 * annuelle : l'historique reste disponible même si la mise à jour des lignes
	 * échoue et que le job est rejoué.</p>
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
						job.getId(), job.getAnnee());
				job.setStatus(ComputeJobStatusEnum.IN_ERROR);
				return;
			}

			// NDVI déjà disponible : ne pas rappeler le fournisseur, réutiliser la donnée.
			if (rows.stream().anyMatch(row -> row.getNdviMean() != null)) {
				reuseExistingNdvi(rows);
				job.setStatus(ComputeJobStatusEnum.PROCESSED);
				return;
			}

			Optional<NdviStatistics> stats = clientCopernicusNdvi
					.fetchNdviForPark(park.getId(), park.getPolygon());
			Date requestDate = new Date();
			NdviStatistics observation = stats.orElse(null);
			if (observation != null) {
				// Une ligne d'historique par réponse contenant des statistiques, toutes années confondues.
				ndviObservationRepository.save(toObservation(park.getId(), requestDate, observation));
			}
			// Copernicus lisse le NDVI sur plusieurs années : une seule réponse est
			// recopiée sur toutes les lignes du parc pour économiser le quota mensuel.
			// shortcut: recopie sur toutes les années en attendant la reventilation par
			// période ; ndvi_observation conserve les dates nécessaires à ce post-traitement.
			for (ParkAreaComputed row : rows) {
				if (observation != null) {
					row.setNdviMean(observation.mean());
					row.setNdviMin(observation.min());
					row.setNdviMax(observation.max());
					row.setNdviStddev(observation.stdDev());
					row.setNdviAcquisitionDate(observation.acquisitionDate());
				}
				// Date posée aussi sans donnée (fenêtre vide) : évite de rejouer chaque année
				row.setNdviRequestDate(requestDate);
			}
			// Un seul appel Copernicus alimente toutes les années du parc.
			parkAreaComputedRepository.saveAll(rows);

			job.setStatus(ComputeJobStatusEnum.PROCESSED);
		} catch (Exception e) {
			log.warn("NDVI non récupéré pour le parc {}-{} : {}",
					job.getId(), job.getAnnee(), e.getMessage());
			job.setStatus(ComputeJobStatusEnum.IN_ERROR);
		} finally {
			job.setProcessed(new Date());
			ndviJobRepository.save(job);
		}
	}

	/**
	 * Convertit une réponse Copernicus en ligne d'historique.
	 *
	 * @param parkAreaId identifiant de la zone de parc
	 * @param requestedAt date de l'appel
	 * @param stats statistiques retournées
	 * @return observation prête à persister
	 */
	private static NdviObservation toObservation(Long parkAreaId, Date requestedAt, NdviStatistics stats) {
		NdviObservation observation = new NdviObservation();
		observation.setParkAreaId(parkAreaId);
		observation.setRequestedAt(requestedAt);
		observation.setPeriodStart(stats.periodStart());
		observation.setPeriodEnd(stats.acquisitionDate());
		observation.setSampleCount(stats.sampleCount());
		observation.setNdviMean(stats.mean());
		observation.setNdviMin(stats.min());
		observation.setNdviMax(stats.max());
		observation.setNdviStddev(stats.stdDev());
		return observation;
	}

	/**
	 * Recopie un NDVI déjà stocké sur les lignes du parc qui n'en ont pas.
	 * Aucun appel fournisseur : économise le quota et garde la donnée existante.
	 *
	 * @param rows lignes {@code park_area_computed} du parc
	 */
	private void reuseExistingNdvi(List<ParkAreaComputed> rows) {
		ParkAreaComputed source = rows.stream()
				.filter(row -> row.getNdviMean() != null)
				.findFirst().orElse(null);
		if (source == null) {
			return;
		}
		boolean changed = false;
		for (ParkAreaComputed row : rows) {
			if (row.getNdviMean() == null) {
				row.setNdviMean(source.getNdviMean());
				row.setNdviMin(source.getNdviMin());
				row.setNdviMax(source.getNdviMax());
				row.setNdviStddev(source.getNdviStddev());
				row.setNdviAcquisitionDate(source.getNdviAcquisitionDate());
				if (source.getNdviRequestDate() != null) {
					row.setNdviRequestDate(source.getNdviRequestDate());
				}
				changed = true;
			}
		}
		if (changed) {
			parkAreaComputedRepository.saveAll(rows);
		}
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
