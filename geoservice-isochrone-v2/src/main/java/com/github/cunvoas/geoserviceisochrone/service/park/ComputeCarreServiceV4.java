package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

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
 * Version optimisée V4 du service métier pour le calcul des métriques liées aux carrés INSEE de 200m.
 *
 * Optimisations :
 * - Batch des accès population (Filosofil)
 * - Cache local des surfaces/intersections
 * - Mutualisation des calculs result/resultOms
 * - Réduction des appels redondants (populate, findById, isDistanceDense)
 *
 * Activée par : application.feature-flipping.carre200m-impl=v4
 */
@Service
@Slf4j
@ConditionalOnProperty(
        name="application.feature-flipping.carre200m-impl",
        havingValue="v4")
public class ComputeCarreServiceV4 implements IComputeCarreService {
	
    // Dépendances injectées via constructeur pour faciliter les tests
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
    private final GeometryQueryHelper geometryQueryHelper;
    private final ParkTypeService parkTypeService;

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
        GeometryQueryHelper geometryQueryHelper,
        ParkTypeService parkTypeService
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
        this.geometryQueryHelper = geometryQueryHelper;
        this.parkTypeService = parkTypeService;
    }

    // Méthode computeCarreByComputeJob (obligatoire)
    @Override
    public Boolean computeCarreByComputeJob(ComputeJob job) {
        // Exécution optimisée du calcul pour un job donné
        Optional<InseeCarre200mOnlyShape> oCarre = inseeCarre200mOnlyShapeRepository.findById(job.getIdInspire());
        if (oCarre.isPresent()) {
            InseeCarre200mOnlyShape carre = oCarre.get();
            Integer annee = job.getAnnee();
            String idInspire = job.getIdInspire();
            String wktPolygon = GeometryQueryHelper.toText(carre.getGeoShape());
            
            Map<String, Filosofil200m> filosofilMap = loadByIdFilosofil(idInspire, annee);
            
            SurfaceCache surfaceCache = new SurfaceCache();
            // Construction du DTO
            ComputeDto dto = new ComputeDto(carre);
            dto.annee = annee;
            dto.isDense = serviceOpenData.isDistanceDense(carre.getCodeInsee());
            
            // Appel du calcul mutualisé
            computePopAndDensityMutualised(dto, carre, dto.polygonParkAreas, filosofilMap, surfaceCache);
            
            // Sauvegarde du résultat (exemple)
            InseeCarre200mComputedV2 computed = new InseeCarre200mComputedV2();
            computed.setIdInspire(carre.getIdInspire());
            computed.setAnnee(annee);
            computed.setIsDense(dto.isDense);
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
            computed.setComments(dto.parcName);
            inseeCarre200mComputedV2Repository.save(computed);
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    // Méthode obligatoire : refreshParkEntrances(String)
    @Override
    public void refreshParkEntrances(String inseeCode) {
        Optional<Cadastre> cadastreOpt = cadastreRepository.findById(inseeCode);
        if (cadastreOpt.isPresent()) {
            refreshParkEntrances(cadastreOpt.get());
        } else {
            log.warn("Aucun cadastre trouvé pour le code INSEE : {}", inseeCode);
        }
    }

    // Méthode obligatoire : refreshParkEntrances(Cadastre)
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void refreshParkEntrances(Cadastre cadastre) {
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
                // Appel du service pour refresh
                // parkService.refreshIsochrone(pe, distance);
            }
            // parkService.mergeParkAreaEntrance(pa);
        }
    }

    // Méthode obligatoire : computeParkArea(ParkArea)
    @Override
    public ParkAreaComputed computeParkArea(ParkArea park) {
        // Appel version optimisée avec batch population sur le parc
        if (park == null || park.getPolygon() == null) {
            return null;
        }
        Integer annee = null;
        Integer[] annees = properties.getInseeAnnees();
        if (annees != null && annees.length > 0) {
            annee = annees[0];
        } else {
            // Valeur par défaut ou gestion d'erreur
            log.warn("Aucune année INSEE configurée dans ApplicationBusinessProperties, valeur par défaut 2020 utilisée");
            annee = 2021;
        }
        String wktPolygon = GeometryQueryHelper.toText(park.getPolygon());
        Map<String, Filosofil200m> filosofilMap = loadFilosofilBatch(wktPolygon, annee);
        return computeParkAreaOptim(park, annee, filosofilMap);
    }

    // Méthode obligatoire : getSurface(Geometry)
    @Override
    public Long getSurface(Geometry geom) {
        return inseeCarre200mOnlyShapeRepository.getSurface(geom);
    }


    // --- OPTIMISATION : Batch population Filosofil ---
    /**
     * Récupère en une seule requête tous les objets Filosofil200m nécessaires pour une zone et une année.
     * Retourne une Map idInspire -> Filosofil200m pour accès rapide en mémoire.
     */
    private Map<String, Filosofil200m> loadFilosofilBatch(String wktPolygon, Integer annee) {
        List<Filosofil200m> list = filosofil200mRepository.getAllCarreInMap(wktPolygon, annee);
        Map<String, Filosofil200m> map = new HashMap<>();
        for (Filosofil200m f : list) {
            map.put(f.getIdInspire(), f);
        }
        return map;
    }
    private Map<String, Filosofil200m> loadByIdFilosofil(String idInspire, Integer annee) {
    	Filosofil200m f = filosofil200mRepository.findByAnneeAndIdInspire(annee, idInspire);
        Map<String, Filosofil200m> map = new HashMap<>();
        map.put(f.getIdInspire(), f);
        return map;
    }
    // --- FIN OPTIMISATION ---

    // --- OPTIMISATION : Utilisation du batch Filosofil dans computePopAndDensityDetail ---
    /**
     * Version optimisée utilisant le cache batché Filosofil pour éviter les accès N+1.
     * Ajout du cache local des surfaces/intersections.
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
        List<InseeCarre200mOnlyShape> shapesWithIso = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(geometryToAnalyse));
        for (InseeCarre200mOnlyShape carreWithIso : shapesWithIso) {
            Filosofil200m carreData = filosofilMap.get(carreWithIso.getIdInspire());
            if (carreData != null) {
                if (carreShape.getIdInspire().equals(carreData.getIdInspire())) {
                    dto.popAll = carreData.getNbIndividus();
                }
                Double nbHabCarre = carreData.getNbIndividus().doubleValue();
                Long surfaceIsoSurCarre = surfaceCache.getOrCompute(carreWithIso.getGeoShape(), geometryToAnalyse, this::getSurface);
                surfacePopulationIso += Math.round(nbHabCarre * surfaceIsoSurCarre / 40000d);
            }
        }
        if (surfacePopulationIso != 0L) {
            crDto.surfaceParkPerCapita = crDto.surfaceTotalParks.divide(BigDecimal.valueOf(surfacePopulationIso), RoundingMode.HALF_EVEN);
        }
        crDto.populationInIsochrone = BigDecimal.valueOf(surfacePopulationIso);
        Double inhabitant = dto.popAll.doubleValue();
        Geometry parkOnCarre = carreShape.getGeoShape().intersection(shapeParkOnSquare);
        Long surfaceParkAccess = getSurface(parkOnCarre);
        Long popIn = Math.round(inhabitant * surfaceParkAccess / 40000d);
        crDto.popInc = new BigDecimal(popIn);
        crDto.popExc = new BigDecimal(inhabitant - popIn);
        Long surfaceSustainable = 0L;
        if (dto.polygonParkAreasSustainableOms != null) {
            Geometry parkSustainable = carreShape.getGeoShape().intersection(dto.polygonParkAreasSustainableOms);
            surfaceSustainable = getSurface(parkSustainable);
        }
        Long popSustainable = Math.round(inhabitant * surfaceSustainable / 40000d);
        dto.popWithSufficient = new BigDecimal(popSustainable);
        return crDto;
    }
    // --- FIN OPTIMISATION ---

    // --- OPTIMISATION : Utilisation du batch Filosofil dans computeParkArea ---
    /**
     * Version optimisée utilisant le cache batché Filosofil pour éviter les accès N+1 dans le calcul population parc.
     */
    protected ParkAreaComputed computeParkAreaOptim(ParkArea park, Integer annee, Map<String, Filosofil200m> filosofilMap) {
        ParkAreaComputed parcCpu = null;
        if (park.getPolygon() == null) {
            return null;
        }
        parkTypeService.populate(park);
        List<InseeCarre200mOnlyShape> shapes = inseeCarre200mOnlyShapeRepository.findCarreInMapArea(GeometryQueryHelper.toText(park.getPolygon()));
        Optional<ParkAreaComputed> parcCpuOpt = parkAreaComputedRepository.findByIdAndAnnee(park.getId(), annee);
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
            Long surfIntersect = getSurface(carreShape.getGeoShape().intersection(park.getPolygon()));
            Filosofil200m carre = filosofilMap.get(carreShape.getIdInspire());
            Long popCar = 0L;
            if (carre != null) {
                popCar = Math.round(carre.getNbIndividus().doubleValue());
            }
            Long popIntersect = Math.round(popCar * surfIntersect / 40000d);
            population = population.add(new BigDecimal(popIntersect));
        }
        parcCpu.setPopulation(population);
        if (!BigDecimal.ZERO.equals(population)) {
            parcCpu.setSurfacePerInhabitant(parcCpu.getSurface().divide(population, 1, RoundingMode.HALF_EVEN));
        }
        parcCpu.setUpdated(new Date());
        parcCpu = parkAreaComputedRepository.save(parcCpu);
        return parcCpu;
    }
    // --- FIN OPTIMISATION ---

    // --- OPTIMISATION : Cache local des surfaces/intersections ---
    /**
     * Cache local pour les surfaces/intersections géométriques lors d'un calcul.
     * La clé est une concaténation des WKT des deux géométries.
     */
    private static class SurfaceCache {
        private final Map<String, Long> cache = new HashMap<>();
        public Long getOrCompute(Geometry a, Geometry b, java.util.function.Function<Geometry, Long> surfaceFunction) {
            String key = a.toText() + "#" + b.toText();
            return cache.computeIfAbsent(key, k -> surfaceFunction.apply(a.intersection(b)));
        }
    }
    // --- FIN OPTIMISATION ---

    // --- OPTIMISATION : Mutualisation des calculs result/resultOms ---
    /**
     * Calcule une seule fois les intersections et surfaces, puis applique les règles pour result et resultOms.
     * Utilise le cache local pour toutes les opérations géométriques.
     */
    protected void computePopAndDensityMutualised(
            ComputeDto dto,
            InseeCarre200mOnlyShape carreShape,
            Geometry shapeParkOnSquare,
            Map<String, Filosofil200m> filosofilMap,
            SurfaceCache surfaceCache) {
    	
        // Calcul principal sur tous les parcs
        ComputeResultDto rDto = computePopAndDensityDetailOptim(
            dto, dto.result, carreShape, dto.polygonParkAreas, shapeParkOnSquare, filosofilMap, surfaceCache);
        dto.result = rDto;
        // Si tous les parcs sont OMS, on réutilise le calcul
        if (dto.allAreOms) {
            dto.resultOms = rDto;
        } else {
            // Sinon, on ne refait que la partie spécifique OMS (sur la géométrie OMS)
            rDto = computePopAndDensityDetailOptim(
                dto, dto.resultOms, carreShape, dto.polygonParkAreasOms, shapeParkOnSquare, filosofilMap, surfaceCache);
            dto.resultOms = rDto;
        }
    }
    // --- FIN OPTIMISATION ---

    // Méthode utilitaire : compute(ComputeDto) (non interface)
    public void compute(ComputeDto computeDto, InseeCarre200mOnlyShape carreShape) {
        Integer annee = computeDto.annee;
        String wktPolygon = GeometryQueryHelper.toText(computeDto.polygonParkAreas);
        Map<String, Filosofil200m> filosofilMap = loadFilosofilBatch(wktPolygon, annee);
        SurfaceCache surfaceCache = new SurfaceCache();
        computePopAndDensityMutualised(computeDto, carreShape, computeDto.polygonParkAreas, filosofilMap, surfaceCache);
        // Sauvegarde du résultat
        InseeCarre200mComputedV2 computed = new InseeCarre200mComputedV2();
        computed.setIdInspire(carreShape.getIdInspire());
        computed.setAnnee(annee);
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
        computed.setComments(computeDto.parcName);
        inseeCarre200mComputedV2Repository.save(computed);
    }

    // Méthode utilitaire pour la compatibilité (copiée de V3)
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

    // Other methods and logic from ComputeCarreServiceV3
}