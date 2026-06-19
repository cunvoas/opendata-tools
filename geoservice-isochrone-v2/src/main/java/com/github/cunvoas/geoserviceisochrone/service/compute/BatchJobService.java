package com.github.cunvoas.geoserviceisochrone.service.compute;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeIrisJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobProgressStat;
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
 * Service métier de traitement par lots (batch) pour le calcul des carreaux
 * INSEE 200m et des IRIS.
 *
 * <h2>Rôle</h2>
 * Orchestre l'ensemble du pipeline de calcul : création des jobs, file d'attente,
 * exécution asynchrone, recyclage des jobs bloqués, et arrêt gracieux.
 *
 * <h2>Pipeline</h2>
 * <ol>
 *   <li><b>Entrées</b> : modification d'un parc ({@link #requestProcessParc}),
 *       demande manuelle par commune ({@link #requestProcessCity}),
 *       ou par EPCI ({@link #requestProcessCom2Co}).</li>
 *   <li><b>File d'attente</b> : {@link #appendCarre} et {@link #appendIris}
 *       créent ou mettent à jour des jobs ({@link ComputeJob}, {@link ComputeIrisJob})
 *       avec deux filtres d'optimisation :
 *       <ul>
 *         <li>Cycle de vie du parc ({@link #getActiveAnneesForParc}) : seules les
 *             années configurées où le parc était actif sont traitées.</li>
 *         <li>Date de mise à jour ({@code upd.before(processed)}) : si le job a
 *             déjà été calculé après la dernière modification du parc, on ignore.</li>
 *       </ul>
 *   </li>
 *   <li><b>Exécution</b> : {@link #processShapes} (toutes les 300s) traite les
 *       jobs par lots de 10, en excluant les traitements concurrents
 *       ({@link #launchOrFinish}).</li>
 *   <li><b>Maintenance</b> : {@link #recycleUndoneShapes} (toutes les 4h) et
 *       {@link #recycleErrorShapes} (quotidien) réparent les jobs bloqués.</li>
 *   <li><b>Arrêt</b> : {@link #destroy()} (SIGTERM) pose un flag et attend la
 *       fin du job en cours avant de rendre la main.</li>
 * </ol>
 *
 * <h2>Optimisations</h2>
 * <ul>
 *   <li><b>Cycle de vie</b> : {@link ParcEtJardin#getDateDebut()} et
 *       {@link ParcEtJardin#getDateFin()} filtrent les années configurées
 *       (défaut 1900-2100 si null).</li>
 *   <li><b>Date de mise à jour</b> : {@link ParkArea#getUpdated()} comparé à
 *       {@link ComputeJob#getProcessed()} pour éviter les recalculs inutiles.</li>
 *   <li><b>Liste d'années</b> : les méthodes {@code append*} acceptent
 *       {@link List}&lt;{@link Integer}&gt; pour ne parcourir qu'un sous-ensemble
 *       d'années au lieu de toutes les années configurées.</li>
 * </ul>
 *
 * @see ComputeCarreServiceV4 Service de calcul effectif des carreaux
 * @see ServiceOpenData Distances piétonnes selon la densité
 */
@Service
@Slf4j
public class BatchJobService implements DisposableBean{

	private static final DateFormat DF =new SimpleDateFormat("yyyy-MM-dd_hh:mm:ss");

	/** Flag d'arrêt gracieux positionné par {@link #destroy()}. */
	private Boolean shutdownRequired = Boolean.FALSE;
	/** Flag indiquant que le thread de traitement est prêt à s'arrêter. */
	private Boolean shutdownReady = Boolean.FALSE;
	/** Flag d'exclusion mutuelle pour le traitement des jobs. */
	private Boolean jobIsRunning=Boolean.FALSE;

	private final ApplicationBusinessProperties applicationBusinessProperties;
	private final CadastreRepository cadastreRepository;
	private final InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	private final IrisShapeRepository irisShapeRepository;
	private final ComputeJobRepository computeJobCarreRepository;
	private final ParkAreaRepository parkAreaRepository;
	private final IComputeCarreService computeCarreService;
	private final ComputeJobIrisRepository computeJobIrisRepository;
	private final IComputeIrisService computeIrisService;

	public BatchJobService(ApplicationBusinessProperties applicationBusinessProperties,
			CadastreRepository cadastreRepository,
			InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository,
			IrisShapeRepository irisShapeRepository,
			ComputeJobRepository computeJobCarreRepository,
			ParkAreaRepository parkAreaRepository,
			IComputeCarreService computeCarreService,
			ComputeJobIrisRepository computeJobIrisRepository,
			IComputeIrisService computeIrisService) {
		this.applicationBusinessProperties = applicationBusinessProperties;
		this.cadastreRepository = cadastreRepository;
		this.inseeCarre200mOnlyShapeRepository = inseeCarre200mOnlyShapeRepository;
		this.irisShapeRepository = irisShapeRepository;
		this.computeJobCarreRepository = computeJobCarreRepository;
		this.parkAreaRepository = parkAreaRepository;
		this.computeCarreService = computeCarreService;
		this.computeJobIrisRepository = computeJobIrisRepository;
		this.computeIrisService = computeIrisService;
	}

	/** Pose le flag d'arrêt gracieux. */
	public void shutdown() {
		this.shutdownRequired=Boolean.TRUE;
	}


	/**
	 * Crée ou met à jour les jobs de calcul pour un parc modifié.
	 *
	 * <p>Déclenché à chaque modification d'un parc (création, édition, déplacement
	 * du polygone). La méthode :
	 * <ol>
	 *   <li>Récupère le {@link ParkArea} associé au parc (polygone + date mise à jour).</li>
	 *   <li>Filtre les années configurées via {@link #getActiveAnneesForParc}
	 *       pour ne traiter que les années où le parc était actif.</li>
	 *   <li>Recherche les carreaux INSEE 200m et les IRIS qui intersectent le polygone.</li>
	 *   <li>Délègue à {@link #appendCarre} et {@link #appendIris} la création des jobs.</li>
	 * </ol>
	 *
	 * @param pj le parc modifié ({@link ParcEtJardin})
	 * @return le nombre total de shapes (carreaux + IRIS) intersectant le parc
	 */
	public int requestProcessParc(ParcEtJardin pj) {
		log.info("requestProcessParc");
		int nbShapes=0;
		ParkArea pa = parkAreaRepository.findByIdParcEtJardin(pj.getId());
		Geometry geo = pa.getPolygon();

		Date upd= pa.getUpdated();
		List<Integer> activeAnnees = getActiveAnneesForParc(pj);

		List<InseeCarre200mOnlyShape> carreShapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(geo));
		appendCarre(carreShapes, upd, activeAnnees);
		nbShapes = carreShapes.size();

		List<IrisShape> irisSpape = irisShapeRepository.findIrisInMapArea(GeometryQueryHelper.toText(geo));
		appendIris(irisSpape, upd, activeAnnees);
		nbShapes += irisSpape.size();

		return nbShapes;
	}

	/**
	 * Filtre les années INSEE configurées selon le cycle de vie du parc.
	 *
	 * <p>Un parc n'affecte les calculs que pour les années où il était actif.
	 * La règle est la même que {@code isActive()} dans {@link ComputeCarreServiceV4} :
	 * <ul>
	 *   <li>Si {@code dateDebut} est null → actif depuis 1900.</li>
	 *   <li>Si {@code dateFin} est null → actif jusqu'en 2100.</li>
	 *   <li>Actif si {@code dateDebut.annee ≤ année ≤ dateFin.annee}.</li>
	 * </ul>
	 *
	 * @param pj le parc dont on veut les années actives
	 * @return liste des années configurées où le parc était actif, ou liste vide
	 */
	private List<Integer> getActiveAnneesForParc(ParcEtJardin pj) {
		Integer[] cfgAnnees = applicationBusinessProperties.getInseeAnnees();
		if (cfgAnnees == null || cfgAnnees.length == 0) {
			return List.of();
		}
		Calendar cal = Calendar.getInstance();
		int debut = 1900;
		if (pj.getDateDebut() != null) {
			cal.setTime(pj.getDateDebut());
			debut = cal.get(Calendar.YEAR);
		}
		int fin = 2100;
		if (pj.getDateFin() != null) {
			cal.setTime(pj.getDateFin());
			fin = cal.get(Calendar.YEAR);
		}
		int d = debut;
		int f = fin;
		return Arrays.stream(cfgAnnees)
				.filter(a -> d <= a && a <= f)
				.toList();
	}


	/**
	 * Crée ou met à jour les jobs de calcul IRIS pour une liste de formes et d'années.
	 *
	 * <p>Méthode factorisée qui détermine la liste des années à traiter selon le
	 * paramètre {@code requestedAnnees} :
	 * <ul>
	 *   <li>{@code null} → toutes les années configurées.</li>
	 *   <li>liste vide → retour immédiat (rien à faire).</li>
	 *   <li>liste non-vide → uniquement ces années.</li>
	 * </ul>
	 *
	 * <p>Pour chaque (année, IRIS), deux cas :
	 * <ul>
	 *   <li><b>Job existant</b> : si le job est déjà {@code PROCESSED} et que
	 *       la date de mise à jour source est postérieure au dernier traitement,
	 *       on le repasse en {@code TO_PROCESS}.</li>
	 *   <li><b>Job inexistant</b> : création d'un nouveau {@link ComputeIrisJob}.</li>
	 * </ul>
	 *
	 * @param shapes liste des formes IRIS intersectant la zone
	 * @param upd date de mise à jour de la source (null si ville/EPCI)
	 * @param requestedAnnees années demandées, null = toutes, vide = skip
	 */
	protected void appendIris(List<IrisShape> shapes, Date upd, List<Integer> requestedAnnees) {
		log.info("appendIris");
		List<ComputeIrisJob> jobs = new ArrayList<>();
		List<Integer> annes;
		if (requestedAnnees != null && !requestedAnnees.isEmpty()) {
			annes = requestedAnnees;
		} else if (requestedAnnees != null && requestedAnnees.isEmpty()) {
			return;
		} else {
			Integer[] inseeAnnees = applicationBusinessProperties.getInseeAnnees();
			annes = inseeAnnees != null ? Arrays.asList(inseeAnnees) : List.of();
		}

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
	 * Crée ou met à jour les jobs de calcul carreau 200m pour une liste de formes
	 * et d'années.
	 *
	 * <p>Même logique que {@link #appendIris} mais pour les carreaux INSEE 200m
	 * ({@link ComputeJob}). Les deux filtres d'optimisation s'appliquent :
	 * <ol>
	 *   <li><b>Filtre des années</b> : seules les années de la liste {@code requestedAnnees}
	 *       sont parcourues (cycle de vie du parc ou demande explicite).</li>
	 *   <li><b>Filtre date de mise à jour</b> : pour chaque job existant déjà traité,
	 *       on compare {@code upd} (date de modif du parc) à {@code processed}
	 *       (date du dernier calcul). Si le calcul est plus récent, on ignore.</li>
	 * </ol>
	 *
	 * @param shapes liste des carreaux 200m intersectant la zone
	 * @param upd date de mise à jour de la source (null si ville/EPCI)
	 * @param requestedAnnees années demandées, null = toutes, vide = skip
	 */
	protected void appendCarre(List<InseeCarre200mOnlyShape> shapes, Date upd, List<Integer> requestedAnnees) {
		log.info("appendCarre");
		List<ComputeJob> jobs = new ArrayList<>();

		List<Integer> annes;
		if (requestedAnnees != null && !requestedAnnees.isEmpty()) {
			annes = requestedAnnees;
		} else if (requestedAnnees != null && requestedAnnees.isEmpty()) {
			return;
		} else {
			Integer[] inseeAnnees = applicationBusinessProperties.getInseeAnnees();
			annes = inseeAnnees != null ? Arrays.asList(inseeAnnees) : List.of();
		}

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
					boolean skip = upd!=null && job.getProcessed()!=null ? upd.before(job.getProcessed()) : false;

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
	 * Crée les jobs de calcul pour toutes les communes d'un EPCI.
	 *
	 * <p>Itère sur les villes composant la communauté de communes
	 * et délègue à {@link #requestProcessCity(City, Integer)} pour chacune.
	 *
	 * @param com2co la communauté de communes (EPCI)
	 * @param requestedYear année demandée (null = toutes)
	 * @return nombre total de shapes (carreaux + IRIS) pour toutes les villes
	 */
	public int requestProcessCom2Co(CommunauteCommune com2co, Integer requestedYear) {
		log.info("requestProcessCom2Co");
		int t=0;
		for (City c : com2co.getCities()) {
			t += this.requestProcessCity(c, requestedYear);
		}
		return t;
	}

	/**
	 * Crée les jobs de calcul pour une commune à partir de son objet {@link City}.
	 *
	 * @param city la commune
	 * @param requestedYear année demandée (null = toutes)
	 * @return nombre de shapes intersectant la commune
	 */
	public int requestProcessCity(City city, Integer requestedYear) {
		log.info("requestProcessCity");

		String inseeCode = city.getInseeCode();
		return this.requestProcessCity(inseeCode, requestedYear);
	}

	/**
	 * Crée les jobs de calcul pour une commune à partir de son code INSEE.
	 *
	 * <p>Contrairement à {@link #requestProcessParc}, ce chemin ne passe pas
	 * de date de mise à jour ({@code upd = null}), donc tous les carreaux/IRIS
	 * de la commune sont traités sans le filtre "déjà à jour".
	 * Utilisé pour les recalculs manuels depuis l'interface admin.
	 *
	 * @param inseeCode code INSEE de la commune
	 * @param requestedYear année demandée (null = toutes)
	 * @return nombre total de shapes (carreaux + IRIS)
	 */
	public int requestProcessCity(String inseeCode, Integer requestedYear) {
		log.info("requestProcessCity");

		int nbShapes=0;
		List<Integer> annees = requestedYear != null ? List.of(requestedYear) : null;

		List<InseeCarre200mOnlyShape> carreShapes = inseeCarre200mOnlyShapeRepository.findByCodeInsee(inseeCode);
		appendCarre(carreShapes, null, annees);
		nbShapes = carreShapes.size();

		List<IrisShape> irisShapes = irisShapeRepository.findByCodeInsee(inseeCode);
		appendIris(irisShapes, null, annees);
		nbShapes += irisShapes.size();

		return nbShapes;
	}



	/**
	 * Gère l'exclusion mutuelle pour l'exécution des jobs.
	 *
	 * <p>Protège {@link #processShapes} contre les exécutions concurrentes :
	 * <ul>
	 *   <li>{@code launchOrFinish(true)} : demande le verrou pour démarrer.
	 *       Retourne {@code true} si aucun traitement n'est en cours.</li>
	 *   <li>{@code launchOrFinish(false)} : libère le verrou en fin de traitement.
	 *       Lève une exception si aucun verrou n'était posé.</li>
	 * </ul>
	 *
	 * @param toLaunch true pour demander le verrou, false pour le libérer
	 * @return true si l'action est autorisée
	 * @throws ExceptionAdmin si on libère un verrou jamais posé
	 */
	protected synchronized Boolean launchOrFinish(Boolean toLaunch) {
		Boolean possibleLaunch = false;
		log.info("changeStatus");

		if (jobIsRunning) {
			if (toLaunch) {
				possibleLaunch = false;
			} else {
				possibleLaunch = true;
				jobIsRunning=false;
			}
		} else {
			if (toLaunch) {
				possibleLaunch = true;
				jobIsRunning=true;
			} else {
				possibleLaunch = false;
				throw (new ExceptionAdmin(ExceptionAdmin.RG_IMPOSSIBLE_CASE));
			}

		}

		log.info("\tpossibleLaunch = {}", possibleLaunch);
		return possibleLaunch;
	}


	/**
	 * Traite un carreau unitairement (débogage).
	 *
	 * <p>Point d'entrée pour le débogage unitaire ou les appels REST.
	 * Cherche le job par sa clé (année + idInspire) et lance le calcul
	 * immédiatement, sans passer par la file d'attente.
	 *
	 * @param annee année du carreau
	 * @param idInspire identifiant Inspire du carreau
	 */
	public void processCarres(Integer annee, String idInspire) {

		InseeCarre200mComputedId id = new InseeCarre200mComputedId();
		id.setAnnee(annee);
		id.setIdInspire(idInspire);

		Optional<ComputeJob> oJob = computeJobCarreRepository.findById(id);
		if (oJob.isPresent()) {
			ComputeJob job = oJob.get();
			Boolean processed = computeCarreService.computeCarreByComputeJob(job);

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
	 * Recycle les jobs restés en statut {@code IN_PROCESS} depuis plus de 8h15.
	 *
	 * <p>Tâche planifiée toutes les 4h (cron {@code 0 15 *&#47;4 * * *}) qui détecte
	 * les jobs de calcul dont le traitement a été interrompu (crash applicatif,
	 * redémarrage, etc.) et les remet dans la file d'attente en statut
	 * {@code TO_PROCESS}.
	 *
	 * <p>La fenêtre de détection est de 8h15 pour éviter de toucher des jobs
	 * légitimement longs (calculs géométriques complexes).
	 *
	 * @see ComputeJobRepository#findOnStartUnfinishedProcessed(Date)
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


		List<ComputeJob> jobs = computeJobCarreRepository.findOnStartUnfinishedProcessed(oldDate);
		if (!jobs.isEmpty()) {
			log.warn("{} jobs runs incorrecly , new demande time is {}", jobs.size(), newDate);

			for (ComputeJob computeJob : jobs) {
				computeJob.setDemand(newDate);
				computeJob.setStatus(ComputeJobStatusEnum.TO_PROCESS);
				computeJobCarreRepository.save(computeJob);
			}
		}

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
	 * Recycle les jobs en statut {@code IN_ERROR} datant de la veille.
	 *
	 * <p>Tâche planifiée quotidiennement à 00h10 (cron {@code 0 10 0 * * *}).
	 * Les jobs en erreur depuis au moins un jour sont remis dans la file
	 * d'attente ({@code TO_PROCESS}) pour une nouvelle tentative.
	 *
	 * @see ComputeJobRepository#findOnErrorAndProcessed(Date)
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


		List<ComputeJob> jobs = computeJobCarreRepository.findOnErrorAndProcessed(oldDate);
		if (!jobs.isEmpty()) {
			log.warn("{} jobs runs incorrecly , new demande time is {}", jobs.size(), newDate);

			for (ComputeJob computeJob : jobs) {
				computeJob.setDemand(newDate);
				computeJob.setStatus(ComputeJobStatusEnum.TO_PROCESS);
				computeJobCarreRepository.save(computeJob);
			}
		}

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
	 * Détecte si on s'exécute dans un environnement de test JUnit.
	 *
	 * <p>Utilisé par {@link #processShapes} pour éviter de lancer le traitement
	 * planifié pendant les tests unitaires.
	 *
	 * @return true si la stack trace contient une classe JUnit
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
	 * Traite les jobs en file d'attente par lots.
	 *
	 * <p>Tâche planifiée toutes les 300 secondes (5 minutes) qui :
	 * <ol>
	 *   <li>Vérifie qu'aucun traitement n'est déjà en cours ({@link #launchOrFinish}).</li>
	 *   <li>Récupère jusqu'à 10 jobs carreau 200m en statut {@code TO_PROCESS}.</li>
	 *   <li>Pour chaque job : pose {@code IN_PROCESS}, lance le calcul
	 *       ({@link IComputeCarreService#computeCarreByComputeJob}), pose
	 *       {@code PROCESSED} ou {@code IN_ERROR}.</li>
	 *   <li>Même boucle pour les jobs IRIS.</li>
	 *   <li>Répète par pages de 10 jusqu'à 100 itérations (1000 jobs/cycle max).</li>
	 *   <li>Libère le verrou via {@link #launchOrFinish}.</li>
	 * </ol>
	 *
	 * <p>L'ordre de traitement est FIFO (plus ancienne demande en premier) en
	 * production, et LIFO (plus récente en premier) sur la machine de dev
	 * "P20230205".
	 *
	 * @FIXME need to be smartest by hours.
	 */
	@Scheduled(fixedDelay = 300, initialDelay = 60, timeUnit = TimeUnit.SECONDS)
	public void processShapes() {

		if (isJUnitTest() ) {
			return;
		}

		if (this.shutdownRequired) {
			log.warn("shutdown required, skip processShapes");
			return;
		}

		int pageSize=10;

		log.error("processShapes at {}", DF.format(new Date()));

		if (this.launchOrFinish(true)) {

			Pageable page = Pageable.ofSize(pageSize);

			int possibleMax=100;

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

				for (ComputeJob job : jobs) {

					job.setProcessed(new Date());
					job.setStatus(ComputeJobStatusEnum.IN_PROCESS);
					computeJobCarreRepository.save(job);

					Boolean processed = computeCarreService.computeCarreByComputeJob(job);

					if ( Boolean.TRUE.equals(processed)) {
						job.setStatus(ComputeJobStatusEnum.PROCESSED);
					} else {
						job.setStatus(ComputeJobStatusEnum.IN_ERROR);
					}
					job.setProcessed(new Date());
					computeJobCarreRepository.save(job);
				}


				if (this.shutdownRequired) {
					log.warn("shutdown required, stop processShapes");
					break;
				}

				List<ComputeIrisJob> irisJobs = null;
				if (onDev()) {
					irisJobs = computeJobIrisRepository.findByStatusOrderByDemandDesc(ComputeJobStatusEnum.TO_PROCESS, page);
				} else {
					irisJobs = computeJobIrisRepository.findByStatusOrderByDemandAsc(ComputeJobStatusEnum.TO_PROCESS, page);
				}
				possibleNext = irisJobs!=null?jobs.size()==pageSize:false;

				for (ComputeIrisJob irisJob : irisJobs) {
					irisJob.setProcessed(new Date());
					irisJob.setStatus(ComputeJobStatusEnum.IN_PROCESS);
					computeJobIrisRepository.save(irisJob);

					Boolean processed = computeIrisService.computeIrisByComputeJob(irisJob);

					if ( Boolean.TRUE.equals(processed)) {
						irisJob.setStatus(ComputeJobStatusEnum.PROCESSED);
					} else {
						irisJob.setStatus(ComputeJobStatusEnum.IN_ERROR);
					}
					irisJob.setProcessed(new Date());
					computeJobIrisRepository.save(irisJob);
				}

				if (this.shutdownRequired) {
					log.warn("shutdown required, stop processShapes");
					break;
				}

			}

		}
		this.launchOrFinish(false);
	}

	/**
	 * Détecte la machine de développement pour adapter l'ordre de traitement.
	 *
	 * <p>La machine de dev "P20230205" utilise un ordre descendant (LIFO)
	 * pour traiter les jobs les plus récents en premier, facilitant le débogage.
	 * En production, l'ordre ascendant (FIFO) garantit un traitement équitable.
	 *
	 * @return true si le hostname correspond à la machine de dev
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
	 * Retourne les statistiques globales d'avancement des jobs carreau.
	 *
	 * @return liste de {@link ComputeJobStat} groupées par statut
	 */
	public List<ComputeJobStat> getGlobalStats() {
		List<Object[]> objs = computeJobCarreRepository.getGlobalStats();
		return map(objs);
	}

	/**
	 * Retourne les statistiques d'avancement pour une commune donnée.
	 *
	 * @param insee code INSEE de la commune
	 * @return liste de {@link ComputeJobStat} groupées par statut
	 */
	public List<ComputeJobStat> getStatsByCity(String insee) {
		List<Object[]> objs = computeJobCarreRepository.getStatsByCodeInsee(insee);
		return map(objs);
	}

	/**
	 * Convertit les résultats bruts de requête en objets {@link ComputeJobStat}.
	 *
	 * @param objs tableau de colonnes (nb, status[, codeInsee])
	 * @return liste de {@link ComputeJobStat}
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
	 * Convertit un index numérique en {@link ComputeJobStatusEnum}.
	 *
	 * @param idx index en base (0=TO_PROCESS, 1=IN_PROCESS, 2=PROCESSED, 3=IN_ERROR)
	 * @return le statut correspondant
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

	/**
	 * Retourne les statistiques d'avancement groupées par EPCI, ville, année.
	 *
	 * @return liste de {@link ComputeJobProgressStat}
	 */
	public List<ComputeJobProgressStat> getGroupedProgressStats() {
		List<Object[]> rawStats = computeJobCarreRepository.getGroupedProgressStats();
		List<ComputeJobProgressStat> stats = new ArrayList<>();
		for (Object[] row : rawStats) {
			ComputeJobProgressStat stat = new ComputeJobProgressStat();
			stat.setEpciName((String) row[0]);
			stat.setCityName((String) row[1]);
			stat.setAnnee(((Number) row[2]).intValue());
			stat.setToProcess(((Number) row[3]).longValue());
			stat.setInProcess(((Number) row[4]).longValue());
			stat.setProcessed(((Number) row[5]).longValue());
			stat.setInError(((Number) row[6]).longValue());
			stats.add(stat);
		}
		return stats;
	}

	/**
	 * Statistiques d'avancement au niveau ville avec filtres optionnels.
	 * <p>
	 * Utilisé quand une commune est sélectionnée ou en vue générale.
	 *
	 * @param idCommune filtre commune (null = toutes)
	 * @param idEpci filtre EPCI (null = tous)
	 * @param idRegion filtre région (null = toutes)
	 * @param annee filtre année (null = toutes)
	 * @return liste de {@link ComputeJobProgressStat}
	 */
	public List<ComputeJobProgressStat> getProgressStatsCityLevel(Long idCommune, Long idEpci, Long idRegion, Integer annee) {
		List<Object[]> rawStats = computeJobCarreRepository.getProgressStatsCityLevel(idCommune, idEpci, idRegion, annee);
		return mapProgressStats(rawStats, true);
	}

	/**
	 * Statistiques d'avancement au niveau EPCI (sans détail par ville).
	 * <p>
	 * Utilisé quand un EPCI est sélectionné sans commune.
	 *
	 * @param idEpci filtre EPCI (null = tous)
	 * @param idRegion filtre région (null = toutes)
	 * @param annee filtre année (null = toutes)
	 * @return liste de {@link ComputeJobProgressStat}
	 */
	public List<ComputeJobProgressStat> getProgressStatsEpciLevel(Long idEpci, Long idRegion, Integer annee) {
		List<Object[]> rawStats = computeJobCarreRepository.getProgressStatsEpciLevel(idEpci, idRegion, annee);
		return mapProgressStats(rawStats, false);
	}

	/**
	 * Convertit les résultats bruts de requête en objets {@link ComputeJobProgressStat}.
	 *
	 * @param rawStats tableau de colonnes (epciName, [cityName,] annee, toProcess, inProcess, processed, inError)
	 * @param hasCityName true si la colonne cityName est présente
	 * @return liste de {@link ComputeJobProgressStat}
	 */
	private List<ComputeJobProgressStat> mapProgressStats(List<Object[]> rawStats, boolean hasCityName) {
		List<ComputeJobProgressStat> stats = new ArrayList<>();
		for (Object[] row : rawStats) {
			ComputeJobProgressStat stat = new ComputeJobProgressStat();
			stat.setEpciName((String) row[0]);
			stat.setCityName(hasCityName ? (String) row[1] : null);
			stat.setAnnee(((Number) row[2]).intValue());
			stat.setToProcess(((Number) row[3]).longValue());
			stat.setInProcess(((Number) row[4]).longValue());
			stat.setProcessed(((Number) row[5]).longValue());
			stat.setInError(((Number) row[6]).longValue());
			stats.add(stat);
		}
		return stats;
	}

	/**
	 * Arrêt gracieux du service lors de la réception d'un signal SIGTERM.
	 *
	 * <p>Le mécanisme d'arrêt suit ces étapes :
	 * <ol>
	 *   <li>Pose le flag {@code shutdownRequired = true}.</li>
	 *   <li>Attend en boucle que {@code jobIsRunning == false} ou que
	 *       {@code shutdownReady == true} (le job en cours se termine).</li>
	 *   <li>Les méthodes {@link #appendCarre}, {@link #appendIris} et
	 *       {@link #processShapes} vérifient périodiquement le flag
	 *       {@code shutdownRequired} et positionnent {@code shutdownReady}
	 *       quand elles se terminent prématurément.</li>
	 *   <li>Le thread principal peut alors procéder à l'arrêt complet.</li>
	 * </ol>
	 *
	 * @throws Exception si l'attente est interrompue
	 */
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
