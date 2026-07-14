package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
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
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.compute.dto.ComputeDto;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import lombok.extern.slf4j.Slf4j;

/**
 * Version optimisée V4 du service de calcul des métriques carrés INSEE 200m.
 * <p>
 * Objectif : réduire les doublons de requêtes géographiques, batcher les accès
 * population Filosofil, mutualiser les calculs result/resultOms, et mettre en
 * cache les surfaces d'intersection. Activée par le toggle
 * {@code application.feature-flipping.carre200m-impl=v4}.
 * </p>
 * 
 * <p>Optimisations clés par rapport à V3 :</p>
 * <ul>
 *   <li>Batch Filosofil : {@link #loadFilosofilBatch} remplace les N appels
 *       {@code findByAnneeAndIdInspire} par une seule requête SQL JOIN</li>
 *   <li>Cache local surfaces : {@link SurfaceCache} évite de recalculer
 *       {@code ST_Area(ST_Intersection(...))} pour la même paire de géométries</li>
 *   <li>Mutualisation result/resultOms : si tous les parcs sont OMS, le calcul
 *       est fait une seule fois ({@link #computePopAndDensityMutualised})</li>
 *   <li>Surface des carrés constante {@link #SURFACE_CARRE} factorisée</li>
 * </ul>
 */
@Service
@Slf4j
@ConditionalOnProperty(
        name="application.feature-flipping.carre200m-impl",
        havingValue="v4")
public class ComputeCarreServiceV4 implements IComputeCarreService {

	// tip validé
	private static final String PICTO_SUFFICIENT	="\u2713 ";
	// tip croix
	private static final String PICTO_REFUSED		="\u2716 ";
	
    /**
     * Surface d'un carré INSEE 200m × 200m (40 000 m²).
     * Utilisée pour proratiser la population selon la surface d'intersection
     * entre un carré et une zone (isochrone, parc).
     */
    private static final Double SURFACE_CARRE = 40_000d;

    private final ApplicationBusinessProperties properties;
    private final ParkAreaRepository parkAreaRepository;
    private final ParkAreaComputedRepository parkAreaComputedRepository;
    private final InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;
    private final CityRepository cityRepository;
    private final CadastreRepository cadastreRepository;
    private final Filosofil200mRepository filosofil200mRepository;
    private final InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
    private final ParkJardinRepository parkJardinRepository;
    private final ServiceOpenData serviceOpenData;
    private final ParkTypeService parkTypeService;
    private final ParkService parkService;

    public ComputeCarreServiceV4(
        ApplicationBusinessProperties properties,
        ParkAreaRepository parkAreaRepository,
        ParkAreaComputedRepository parkAreaComputedRepository,
        InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository,
        CityRepository cityRepository,
        CadastreRepository cadastreRepository,
        Filosofil200mRepository filosofil200mRepository,
        InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository,
        ParkJardinRepository parkJardinRepository,
        ServiceOpenData serviceOpenData,
        ParkTypeService parkTypeService,
        ParkService parkService
    ) {
        this.properties = properties;
        this.parkAreaRepository = parkAreaRepository;
        this.parkAreaComputedRepository = parkAreaComputedRepository;
        this.inseeCarre200mComputedV2Repository = inseeCarre200mComputedV2Repository;
        this.cityRepository = cityRepository;
        this.cadastreRepository = cadastreRepository;
        this.filosofil200mRepository = filosofil200mRepository;
        this.inseeCarre200mOnlyShapeRepository = inseeCarre200mOnlyShapeRepository;
        this.parkJardinRepository = parkJardinRepository;
        this.serviceOpenData = serviceOpenData;
        this.parkTypeService = parkTypeService;
        this.parkService = parkService;
    }

    /**
     * Point d'entrée principal du batch de calcul.
     * <p>
     * Pour un carré INSEE et une année donnés, cette méthode :
     * <ol>
     *   <li>Recherche les parcs ({@link ParkArea}) qui intersectent le carré</li>
     *   <li>Pour chaque parc actif, récupère ou calcule {@link ParkAreaComputed}</li>
     *   <li>Fusionne les polygones des parcs pour créer l'isochrone globale</li>
     *   <li>Applique les règles OMS (surface minimale, durable)</li>
     *   <li>Calcule la population dans l'isochrone et proratise</li>
     *   <li>Calcule les surfaces manquantes selon les normes OMS</li>
     *   <li>Persiste le résultat {@link InseeCarre200mComputedV2}</li>
     * </ol>
     * </p>
     */
    @Override
    public Boolean computeCarreByComputeJob(ComputeJob job) {
        log.info("begin computeCarre v4 {}", job.getIdInspire());
        try {
            Optional<InseeCarre200mOnlyShape> oCarre = inseeCarre200mOnlyShapeRepository.findById(job.getIdInspire());
            if (oCarre.isPresent()) {
                InseeCarre200mOnlyShape carre = oCarre.get();
                Integer annee = job.getAnnee();
                Boolean isDense = serviceOpenData.isDistanceDense(carre.getCodeInsee());

                // Phase 1 : trouver les parcs dans la zone du carré
                List<ParkArea> parkAreas = parkAreaRepository.findParkInMapArea(
                    GeometryQueryHelper.toText(carre.getGeoShape()));
                parkTypeService.populate(parkAreas);

                // Phase 2 : charger les données population Filosofil en batch
                // AVERTISSEMENT : la zone utilisée pour charger Filosofil DOIT couvrir
                // TOUS les carreaux que les zones d'accès des parcs peuvent atteindre,
                // pas seulement le carré courant (200m). Un parc a une zone d'influence
                // (isochrone piéton 300-1200m) bien plus grande que le carré.
                // → on pré-fusionne les polygones des parcs (+ le carré) pour définir
                //   la zone de requête Filosofil, qui correspond à l'isochrone finale.
                Geometry shapeParkOnSquare = null;
                Geometry filosofilLoadArea = carre.getGeoShape();
                for (ParkArea parkArea : parkAreas) {
                    filosofilLoadArea = filosofilLoadArea.union(parkArea.getPolygon());
                }
                Map<String, Filosofil200m> filosofilMap = loadFilosofilBatch(
                    GeometryQueryHelper.toText(filosofilLoadArea), annee);
                SurfaceCache surfaceCache = new SurfaceCache();

                ComputeDto dto = new ComputeDto(carre);
                dto.isDense = isDense;
                dto.annee = annee;

                // Phase 3 : boucle sur chaque parc pour accumuler les surfaces OMS et non-OMS
                // count4checkOms sert à détecter si TOUS les parcs sont conformes OMS
                // (décrémenté pour chaque parc, incrémenté quand OMS → allAreOms si == taille liste)
                int count4checkOms = parkAreas.size();

                for (ParkArea parkArea : parkAreas) {
                    log.info("\tcompose {}", parkArea);

                    if (!isActive(parkArea, annee)) {
                        continue;
                    }

                    ParkAreaComputed pac;
                    Optional<ParkAreaComputed> opac = parkAreaComputedRepository.findByIdAndAnnee(parkArea.getId(), annee);
                    if (opac.isPresent()) {
                        pac = opac.get();
                        if (pac.getSurface() == null) {
                            pac = computeParkAreaOptim(parkArea, annee, filosofilMap);
                        }
                    } else {
                        pac = computeParkAreaOptim(parkArea, annee, filosofilMap);
                    }

                    // Tous les parcs sont candidats OMS, on décrémente pour suivre
                    count4checkOms--;

                    // Cumul des surfaces pour le résultat "tous parcs"
                    dto.result.surfaceTotalParks = dto.result.surfaceTotalParks.add(pac.getSurface());
                    dto.polygonParkAreas = dto.polygonParkAreas.union(parkArea.getPolygon());

                    // Si le parc est conforme OMS, on l'ajoute au périmètre OMS
                    if (pac.getOms()) {
                        if (shapeParkOnSquare == null) {
                            shapeParkOnSquare = parkArea.getPolygon();
                        } else {
                            shapeParkOnSquare = shapeParkOnSquare.union(parkArea.getPolygon());
                        }

                        // Vérification surface minimale OMS (ex: 5000 m² recommandé)
                        String sufficient = "";
                        Double rs = properties.getRecoAtLeastParkSurface();
                        if (rs <= pac.getSurface().doubleValue()) {
                            sufficient = PICTO_SUFFICIENT;
                            dto.withSufficient = Boolean.TRUE;
                            if (dto.polygonParkAreasSustainableOms == null) {
                                dto.polygonParkAreasSustainableOms = parkArea.getPolygon();
                            } else {
                                dto.polygonParkAreasSustainableOms =
                                    dto.polygonParkAreasSustainableOms.union(parkArea.getPolygon());
                            }
                        }

                        dto.parcNames.add(sufficient + parkArea.getName());
                        // Incrémente car ce parc est OMS → compense la décrémentation initiale
                        count4checkOms++;
                        dto.resultOms.surfaceTotalParks = dto.resultOms.surfaceTotalParks.add(pac.getSurface());
                        dto.polygonParkAreasOms = dto.polygonParkAreasOms.union(parkArea.getPolygon());
                    } else {
                        dto.parcNames.add(PICTO_REFUSED + parkArea.getName());
                    }
                }

                // Si count4checkOms == taille liste → tous les parcs sont OMS
                dto.allAreOms = count4checkOms == parkAreas.size();

                // Si aucun parc OMS, on utilise le centroïde du carré comme shapeParkOnSquare
                if (shapeParkOnSquare == null) {
                    shapeParkOnSquare = carre.getGeoPoint2d();
                }

                // Construction du commentaire HTML avec la liste des parcs
                StringBuilder sbParcName = new StringBuilder();
                if (!dto.parcNames.isEmpty()) {
                    Collections.sort(dto.parcNames);
                    for (String name : dto.parcNames) {
                        if (name != null) {
                            if (sbParcName.length() > 0) {
                                sbParcName.append("<br />");
                            }
                            sbParcName.append(" - ");
                            sbParcName.append(name);
                        }
                    }
                    dto.parcName = sbParcName.toString();
                }

                log.info("\tprocess merge isochrone v4");

                // Phase 4 : calcul mutualisé population + densité
                computePopAndDensityMutualised(dto, carre, shapeParkOnSquare, filosofilMap, surfaceCache);

                // Phase 5 : persistance du résultat
                InseeCarre200mComputedV2 computed = null;
                Optional<InseeCarre200mComputedV2> opt =
                    inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(annee, carre.getIdInspire());
                if (opt.isPresent()) {
                    computed = opt.get();
                } else {
                    computed = new InseeCarre200mComputedV2();
                    computed.setIdInspire(carre.getIdInspire());
                    computed.setAnnee(annee);
                }
                computed.setIsDense(isDense);
                computed.setUpdated(new Date());

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

                // Surface manquante pour atteindre les normes OMS (min et recommandé)
                computed.setMissingSurfaceMini(computeMissingSurface(dto, carre,
                    properties.getMinUrbSquareMeterPerCapita(),
                    properties.getMinSubUrbSquareMeterPerCapita()));
                computed.setMissingSurfaceAdvised(computeMissingSurface(dto, carre,
                    properties.getRecoUrbSquareMeterPerCapita(),
                    properties.getRecoSubUrbSquareMeterPerCapita()));

                if (Boolean.TRUE.equals(dto.withSufficient)) {
                    computed.setIsSustainablePark(Boolean.TRUE);
                    computed.setPopulationWithSustainablePark(null);
                } else {
                    computed.setIsSustainablePark(Boolean.FALSE);
                    computed.setPopulationWithSustainablePark(BigDecimal.ZERO);
                }

                computed.setComments(dto.parcName);

                log.info("\tsave computed v4 {}", computed.getIdInspire());
                inseeCarre200mComputedV2Repository.save(computed);
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        } catch (Exception e) {
            log.error("computeCarre v4 in error: {} {}", job.getIdInspire(), job.getAnnee(), e);
            return Boolean.FALSE;
        }
    }

    /**
     * Rafraîchit les entrées de parc pour un code INSEE donné.
     * Délègue à {@link #refreshParkEntrances(Cadastre)} après avoir chargé le cadastre.
     */
    @Override
    public void refreshParkEntrances(String inseeCode) {
        Optional<Cadastre> cadastreOpt = cadastreRepository.findById(inseeCode);
        if (cadastreOpt.isPresent()) {
            refreshParkEntrances(cadastreOpt.get());
        } else {
            log.warn("Aucun cadastre trouvé pour le code INSEE : {}", inseeCode);
        }
    }

    /**
     * Recalcule les isochrones d'accès piéton pour tous les parcs d'une commune.
     * <p>
     * Pour chaque parc de la commune :
     * <ol>
     *   <li>Récupère la distance piétonne selon la densité (300m urbain, 1200m périurbain)</li>
     *   <li>Pour chaque entrée du parc, appelle l'API IGN pour recalculer l'isochrone</li>
     *   <li>Fusionne les polygones des entrées pour former le polygone global du parc</li>
     * </ol>
     * </p>
     */
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void refreshParkEntrances(Cadastre cadastre) {
        log.warn(">> refreshParkEntrances v4");
        City city = cityRepository.findByInseeCode(cadastre.getIdInsee());
        if (city == null) {
            log.warn("Aucune ville trouvée pour le code INSEE cadastre : {}", cadastre.getIdInsee());
            return;
        }
        String distance = serviceOpenData.getDistanceDense(city);
        List<ParcEtJardin> pjs = parkJardinRepository.findByCityId(city.getId());
        for (ParcEtJardin parcEtJardin : pjs) {
            ParkArea pa = parkAreaRepository.findByIdParcEtJardin(parcEtJardin.getId());
            for (ParkEntrance pe : pa.getEntrances()) {
                parkService.refreshIsochrone(pe, distance);
            }
            parkService.mergeParkAreaEntrance(pa);
        }
        log.warn("<< refreshParkEntrances v4");
    }

    /**
     * Calcule {@link ParkAreaComputed} pour un {@link ParkArea} donné.
     * <p>
     * Itère sur toutes les années INSEE configurées (contrairement à la version
     * buggée initiale qui ne traitait que la première année). Pour chaque année,
     * charge les données Filosofil en batch et calcule la population proratisée.
     * </p>
     */
    @Override
    public ParkAreaComputed computeParkArea(ParkArea park) {
        if (park == null || park.getPolygon() == null) {
            return null;
        }
        Integer[] annees = properties.getInseeAnnees();
        ParkAreaComputed last = null;
        for (Integer annee : annees) {
            String wktPolygon = GeometryQueryHelper.toText(park.getPolygon());
            Map<String, Filosofil200m> filosofilMap = loadFilosofilBatch(wktPolygon, annee);
            last = computeParkAreaOptim(park, annee, filosofilMap);
        }
        return last;
    }

    /**
     * Calcule la surface d'une géométrie via PostGIS (ST_Area).
     * Délègue au repository pour une précision géodésique (true ellipsoid).
     */
    @Override
    public Long getSurface(Geometry geom) {
        return inseeCarre200mOnlyShapeRepository.getSurface(geom);
    }

    /**
     * Calcule la population dans l'isochrone et la surface de parc disponible par habitant.
     * <p>
     * Algorithme :
     * <ol>
     *   <li>Récupère tous les carrés INSEE intersectant la zone à analyser</li>
     *   <li>Pour chaque carré, récupère sa population dans la map Filosofil (batch)</li>
     *   <li>Proratise la population selon la surface d'intersection carré/isochrone</li>
     *   <li>Calcule la surface de parc accessible dans le carré actuel</li>
     *   <li>Calcule popIn/popExc (population avec/sans accès parc)</li>
     *   <li>Calcule popWithSufficient (population avec parc ≥ seuil OMS durable)</li>
     * </ol>
     * Le cache local {@link SurfaceCache} évite de ré-interroger PostGIS pour
     * des paires de géométries déjà rencontrées.
     * </p>
     */
    protected ComputeResultDto computePopAndDensityDetailOptim(
            ComputeDto dto,
            ComputeResultDto crDto,
            InseeCarre200mOnlyShape carreShape,
            Geometry geometryToAnalyse,
            Geometry shapeParkOnSquare,
            Map<String, Filosofil200m> filosofilMap,
            SurfaceCache surfaceCache) {

        Long surfacePopulationIso = 0L;
        List<InseeCarre200mOnlyShape> shapesWithIso =
            inseeCarre200mOnlyShapeRepository.findCarreInMapArea(
                GeometryQueryHelper.toText(geometryToAnalyse));
        for (InseeCarre200mOnlyShape carreWithIso : shapesWithIso) {
            Filosofil200m carreData = filosofilMap.get(carreWithIso.getIdInspire());
            if (carreData != null) {
                if (carreShape.getIdInspire().equals(carreData.getIdInspire())) {
                    dto.popAll = carreData.getNbIndividus();
                }
                Double nbHabCarre = carreData.getNbIndividus().doubleValue();
                // Proratisation : surface_intersection / 40000 × population_du_carré
                Long surfaceIsoSurCarre = surfaceCache.getOrCompute(
                    carreWithIso.getGeoShape(), geometryToAnalyse, this::getSurface);
                surfacePopulationIso += Math.round(nbHabCarre * surfaceIsoSurCarre / SURFACE_CARRE);
            }
        }
        if (surfacePopulationIso != 0L) {
            crDto.surfaceParkPerCapita = crDto.surfaceTotalParks.divide(
                BigDecimal.valueOf(surfacePopulationIso), RoundingMode.HALF_EVEN);
        }
        crDto.populationInIsochrone = BigDecimal.valueOf(surfacePopulationIso);

        Double inhabitant = dto.popAll.doubleValue();
        Geometry parkOnCarre = carreShape.getGeoShape().intersection(shapeParkOnSquare);
        Long surfaceParkAccess = getSurface(parkOnCarre);
        Long popIn = Math.round(inhabitant * surfaceParkAccess / SURFACE_CARRE);
        crDto.popInc = new BigDecimal(popIn);
        crDto.popExc = new BigDecimal(inhabitant - popIn);

        Long surfaceSustainable = 0L;
        if (dto.polygonParkAreasSustainableOms != null) {
            Geometry parkSustainable = carreShape.getGeoShape().intersection(
                dto.polygonParkAreasSustainableOms);
            surfaceSustainable = getSurface(parkSustainable);
        }
        Long popSustainable = Math.round(inhabitant * surfaceSustainable / SURFACE_CARRE);
        dto.popWithSufficient = new BigDecimal(popSustainable);

        return crDto;
    }

    /**
     * Calcule (ou récupère) les métriques {@link ParkAreaComputed} pour un parc
     * et une année donnés, en utilisant les données Filosofil déjà chargées en mémoire.
     * <p>
     * Algorithme :
     * <ol>
     *   <li>Récupère les carrés INSEE intersectant le polygone du parc</li>
     *   <li>Pour chaque carré, calcule l'intersection de surface et proratise la population</li>
     *   <li>Additionne les populations proratisées pour obtenir la population totale du parc</li>
     *   <li>Calcule la surface par habitant (surface_parc / population)</li>
     * </ol>
     * Optimisation : utilise la map Filosofil batchée au lieu de N requêtes individuelles.
     * </p>
     */
    protected ParkAreaComputed computeParkAreaOptim(ParkArea park, Integer annee,
            Map<String, Filosofil200m> filosofilMap) {
        ParkAreaComputed parcCpu = null;
        if (park.getPolygon() == null) {
            return null;
        }
        parkTypeService.populate(park);
        List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(
            GeometryQueryHelper.toText(park.getPolygon()));
        Optional<ParkAreaComputed> parcCpuOpt = parkAreaComputedRepository.findByIdAndAnnee(
            park.getId(), annee);
        if (parcCpuOpt.isPresent()) {
            parcCpu = parcCpuOpt.get();
        } else {
            parcCpu = new ParkAreaComputed();
            parcCpu.setId(park.getId());
            parcCpu.setAnnee(annee);
        }
        ParkType type = park.getType();
        if (type.getStrict()) {
            parcCpu.setOms(type.getOms());
        } else {
            parcCpu.setOms(type.getOms());
            if (park.getOmsCustom() != null) {
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
        BigDecimal population = BigDecimal.ZERO;
        for (InseeCarre200mOnlyShape carreShape : shapes) {
            Long surfIntersect = getSurface(
                carreShape.getGeoShape().intersection(park.getPolygon()));
            Filosofil200m carre = filosofilMap.get(carreShape.getIdInspire());
            Long popCar = 0L;
            if (carre != null) {
                popCar = Math.round(carre.getNbIndividus().doubleValue());
            }
            Long popIntersect = Math.round(popCar * surfIntersect / SURFACE_CARRE);
            population = population.add(new BigDecimal(popIntersect));
        }
        parcCpu.setPopulation(population);
        if (!BigDecimal.ZERO.equals(population)) {
            parcCpu.setSurfacePerInhabitant(
                parcCpu.getSurface().divide(population, 1, RoundingMode.HALF_EVEN));
        }
        parcCpu.setUpdated(new Date());
        parcCpu = parkAreaComputedRepository.save(parcCpu);
        return parcCpu;
    }

    /**
     * Calcule la densité de population et la surface de parc par habitant,
     * en mutualisant le calcul pour les résultats "tous parcs" et "parcs OMS".
     * <p>
     * Si tous les parcs sont conformes OMS ({@code dto.allAreOms == true}),
     * le calcul {@code result} est réutilisé pour {@code resultOms},
     * évitant ainsi une double exécution de {@link #computePopAndDensityDetailOptim}.
     * </p>
     */
    protected void computePopAndDensityMutualised(
            ComputeDto dto,
            InseeCarre200mOnlyShape carreShape,
            Geometry shapeParkOnSquare,
            Map<String, Filosofil200m> filosofilMap,
            SurfaceCache surfaceCache) {

        ComputeResultDto rDto = computePopAndDensityDetailOptim(
            dto, dto.result, carreShape, dto.polygonParkAreas,
            shapeParkOnSquare, filosofilMap, surfaceCache);
        dto.result = rDto;

        if (dto.allAreOms) {
            dto.resultOms = rDto;
        } else {
            rDto = computePopAndDensityDetailOptim(
                dto, dto.resultOms, carreShape, dto.polygonParkAreasOms,
                shapeParkOnSquare, filosofilMap, surfaceCache);
            dto.resultOms = rDto;
        }
    }

    /**
     * Calcule la surface de parc manquante pour atteindre un standard OMS
     * (minimum ou recommandé) dans ce carré.
     * <p>
     * Formule : MAX(0, standard × population - surface_existante)
     * </p>
     * Le standard est choisi selon la densité (urbain/périurbain).
     */
    protected BigDecimal computeMissingSurface(ComputeDto dto,
            InseeCarre200mOnlyShape carreShape,
            Double urbStandard, Double subUrbStandard) {
        Double standard = dto.isDense ? urbStandard : subUrbStandard;
        BigDecimal requiredSurface = BigDecimal.valueOf(
            standard * dto.resultOms.populationInIsochrone.doubleValue());
        BigDecimal missingSurface = requiredSurface.subtract(dto.resultOms.surfaceTotalParks);
        if (missingSurface.compareTo(BigDecimal.ZERO) > 0) {
            return missingSurface.setScale(2, RoundingMode.HALF_EVEN);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calcule et persist {@link InseeCarre200mComputedV2} pour un {@link ComputeDto}
     * déjà préparé par l'appelant.
     * <p>
     * ATTENTION : l'appelant doit avoir préparé le DTO (polygons fusionnés,
     * surfaceTotalParks, etc.). Cette méthode ne fait pas la phase de préparation
     * des parcs (contrairement à {@link #computeCarreByComputeJob}).
     * Elle charge les données Filosofil en batch et applique le calcul mutualisé,
     * puis persist le résultat complet (incluant missingSurface et sustainablePark).
     * </p>
     */
    public void compute(ComputeDto computeDto, InseeCarre200mOnlyShape carreShape) {
        Integer annee = computeDto.annee;
        String wktPolygon = GeometryQueryHelper.toText(computeDto.polygonParkAreas);
        Map<String, Filosofil200m> filosofilMap = loadFilosofilBatch(wktPolygon, annee);
        SurfaceCache surfaceCache = new SurfaceCache();

        Geometry shapeParkOnSquare = computeDto.polygonParkAreas;
        computePopAndDensityMutualised(computeDto, carreShape, shapeParkOnSquare,
            filosofilMap, surfaceCache);

        InseeCarre200mComputedV2 computed = null;
        Optional<InseeCarre200mComputedV2> opt =
            inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(
                annee, carreShape.getIdInspire());
        if (opt.isPresent()) {
            computed = opt.get();
        } else {
            computed = new InseeCarre200mComputedV2();
            computed.setIdInspire(carreShape.getIdInspire());
            computed.setAnnee(annee);
        }
        computed.setIsDense(computeDto.isDense);
        computed.setUpdated(new Date());
        computed.setSurfaceParkPerCapita(computeDto.result.surfaceParkPerCapita);
        computed.setSurfaceTotalPark(computeDto.result.surfaceTotalParks);
        computed.setPopulationInIsochrone(computeDto.result.populationInIsochrone);
        computed.setPopIncluded(computeDto.result.popInc);
        computed.setPopExcluded(computeDto.result.popExc);
        computed.setSurfaceParkPerCapitaOms(computeDto.resultOms.surfaceParkPerCapita);
        computed.setSurfaceTotalParkOms(computeDto.resultOms.surfaceTotalParks);
        computed.setPopulationInIsochroneOms(computeDto.resultOms.populationInIsochrone);
        computed.setPopIncludedOms(computeDto.resultOms.popInc);
        computed.setPopExcludedOms(computeDto.resultOms.popExc);

        computed.setMissingSurfaceMini(computeMissingSurface(computeDto, carreShape,
            properties.getMinUrbSquareMeterPerCapita(),
            properties.getMinSubUrbSquareMeterPerCapita()));
        computed.setMissingSurfaceAdvised(computeMissingSurface(computeDto, carreShape,
            properties.getRecoUrbSquareMeterPerCapita(),
            properties.getRecoSubUrbSquareMeterPerCapita()));

        if (Boolean.TRUE.equals(computeDto.withSufficient)) {
            computed.setIsSustainablePark(Boolean.TRUE);
            computed.setPopulationWithSustainablePark(null);
        } else {
            computed.setIsSustainablePark(Boolean.FALSE);
            computed.setPopulationWithSustainablePark(BigDecimal.ZERO);
        }

        computed.setComments(computeDto.parcName);
        inseeCarre200mComputedV2Repository.save(computed);
    }

    /**
     * Vérifie si un parc est actif pour une année donnée.
     * <p>
     * Un parc est actif si l'année demandée est comprise dans l'intervalle
     * [dateDebut, dateFin]. Si dateDebut est null → 1900 (actif depuis toujours).
     * Si dateFin est null → 2100 (actif jusqu'à nouvel ordre).
     * </p>
     */
    protected Boolean isActive(ParkArea pa, Integer annee) {
        Boolean active = false;
        Optional<ParcEtJardin> oPj = parkJardinRepository.findById(pa.getIdParcEtJardin());
        if (oPj.isPresent()) {
            ParcEtJardin pj = oPj.get();
            Date dd = pj.getDateDebut();
            Date df = pj.getDateFin();
            Calendar cal = Calendar.getInstance();
            int d = 1900;
            if (dd != null) {
                cal.setTime(dd);
                d = cal.get(Calendar.YEAR);
            }
            int f = 2100;
            if (df != null) {
                cal.setTime(df);
                f = cal.get(Calendar.YEAR);
            }
            active = d <= annee && annee <= f;
        }
        return active;
    }

    /**
     * Charge en une seule requête SQL tous les {@link Filosofil200m} intersectant
     * une zone polygonale pour une année donnée.
     * <p>
     * Optimisation clé V4 : remplace le N+1 de V3 ({@code findByAnneeAndIdInspire}
     * dans la boucle) par une unique requête avec JOIN spatial entre
     * {@code carre200onlyshape} et {@code filosofi_200m}.
     * Retourne une Map idInspire → Filosofil200m pour accès O(1) en mémoire.
     * </p>
     */
    Map<String, Filosofil200m> loadFilosofilBatch(String wktPolygon, Integer annee) {
        List<Filosofil200m> list = filosofil200mRepository.getAllCarreInMap(wktPolygon, annee);
        Map<String, Filosofil200m> map = new HashMap<>();
        for (Filosofil200m f : list) {
            map.put(f.getIdInspire(), f);
        }
        return map;
    }


    /**
     * Cache local pour les résultats de {@code ST_Area(ST_Intersection(a, b))}.
     * <p>
     * La clé est {@code hashCode(a) + "#" + hashCode(b)} (hash JTS de la géométrie,
     * plus léger que le WKT complet). Évite les appels SQL redondants quand la même
     * paire de géométries est rencontrée plusieurs fois (ex: même isochrone analysée
     * pour plusieurs carrés).
     * </p>
     * <p>
     * Non thread-safe par choix : une nouvelle instance est créée par job.
     * </p>
     */
    static class SurfaceCache {
        private final Map<String, Long> cache = new HashMap<>();
        public Long getOrCompute(Geometry a, Geometry b,
                java.util.function.Function<Geometry, Long> surfaceFunction) {
            String key = a.hashCode() + "#" + b.hashCode();
            return cache.computeIfAbsent(key, k -> surfaceFunction.apply(a.intersection(b)));
        }
    }

}
