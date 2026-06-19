package com.github.cunvoas.geoserviceisochrone.service.park;

import com.github.cunvoas.geoserviceisochrone.BaseUnitTest;
import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ComputeCarreServiceV3V4PerformanceTest extends BaseUnitTest {

    private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);
    private static final int WARMUP_ITERS = 3;
    private static final int MEASURE_ITERS = 10;

    @Mock private ApplicationBusinessProperties properties;
    @Mock private ParkAreaRepository parkAreaRepository;
    @Mock private ParkAreaComputedRepository parkAreaComputedRepository;
    @Mock private InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;
    @Mock private CityRepository cityRepository;
    @Mock private CadastreRepository cadastreRepository;
    @Mock private Filosofil200mRepository filosofil200mRepository;
    @Mock private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
    @Mock private ParkJardinRepository parkJardinRepository;
    @Mock private ServiceOpenData serviceOpenData;
    @Mock private ParkTypeService parkTypeService;
    @Mock private ParkService parkService;

    // ============================================================
    // Helpers
    // ============================================================

    private ComputeCarreServiceV3 buildV3() {
        ComputeCarreServiceV3 s = new ComputeCarreServiceV3();
        ReflectionTestUtils.setField(s, "cadastreRepository", cadastreRepository);
        ReflectionTestUtils.setField(s, "inseeCarre200mOnlyShapeRepository", inseeCarre200mOnlyShapeRepository);
        ReflectionTestUtils.setField(s, "filosofil200mRepository", filosofil200mRepository);
        ReflectionTestUtils.setField(s, "inseeCarre200mComputedV2Repository", inseeCarre200mComputedV2Repository);
        ReflectionTestUtils.setField(s, "parkAreaRepository", parkAreaRepository);
        ReflectionTestUtils.setField(s, "parkAreaComputedRepository", parkAreaComputedRepository);
        ReflectionTestUtils.setField(s, "parkJardinRepository", parkJardinRepository);
        ReflectionTestUtils.setField(s, "parkTypeService", parkTypeService);
        ReflectionTestUtils.setField(s, "serviceOpenData", serviceOpenData);
        ReflectionTestUtils.setField(s, "parkService", parkService);
        ReflectionTestUtils.setField(s, "cityRepository", cityRepository);
        ReflectionTestUtils.setField(s, "applicationBusinessProperties", properties);
        return s;
    }

    private ComputeCarreServiceV4 buildV4() {
        return new ComputeCarreServiceV4(
                properties, parkAreaRepository, parkAreaComputedRepository,
                inseeCarre200mComputedV2Repository, cityRepository, cadastreRepository,
                filosofil200mRepository, inseeCarre200mOnlyShapeRepository,
                parkJardinRepository, serviceOpenData, parkTypeService, parkService);
    }

    private void configureDefaults() {
        lenient().when(properties.getRecoAtLeastParkSurface()).thenReturn(5000.0);
        lenient().when(properties.getInseeAnnees()).thenReturn(new Integer[]{2020});
        lenient().when(properties.getMinUrbSquareMeterPerCapita()).thenReturn(10.0);
        lenient().when(properties.getMinSubUrbSquareMeterPerCapita()).thenReturn(5.0);
        lenient().when(properties.getRecoUrbSquareMeterPerCapita()).thenReturn(20.0);
        lenient().when(properties.getRecoSubUrbSquareMeterPerCapita()).thenReturn(10.0);

        lenient().when(inseeCarre200mOnlyShapeRepository.getSurface(any())).thenReturn(40000L);
        lenient().when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(anyInt(), anyString()))
                .thenReturn(Optional.empty());
        lenient().when(inseeCarre200mComputedV2Repository.save(any())).thenAnswer(i -> i.getArgument(0));

        lenient().when(parkAreaComputedRepository.findByIdAndAnnee(anyLong(), anyInt()))
                .thenReturn(Optional.empty());
        lenient().when(parkAreaComputedRepository.save(any())).thenAnswer(i -> {
            ParkAreaComputed pac = i.getArgument(0);
            if (pac.getSurface() == null) pac.setSurface(BigDecimal.ZERO);
            return pac;
        });

        lenient().doNothing().when(parkTypeService).populate(any(ParkArea.class));
    }

    private static ComputeJob job(String id, int year) {
        ComputeJob j = new ComputeJob();
        j.setIdInspire(id);
        j.setAnnee(year);
        return j;
    }

    private static InseeCarre200mOnlyShape carreShape(String id, String insee, Polygon poly, Point pt) {
        InseeCarre200mOnlyShape c = new InseeCarre200mOnlyShape();
        c.setIdInspire(id);
        c.setCodeInsee(insee);
        c.setGeoShape(poly);
        c.setGeoPoint2d(pt);
        return c;
    }

    private static Polygon poly(double... coords) {
        Coordinate[] cs = new Coordinate[coords.length / 2 + 1];
        for (int i = 0; i < coords.length; i += 2)
            cs[i / 2] = new Coordinate(coords[i], coords[i + 1]);
        cs[cs.length - 1] = cs[0];
        return GF.createPolygon(cs);
    }

    private static Point pt(double x, double y) {
        return GF.createPoint(new Coordinate(x, y));
    }

    private static ParkArea park(long id, long pjId, String name, double surface, Polygon poly, boolean oms) {
        ParkArea p = new ParkArea();
        p.setId(id);
        p.setIdParcEtJardin(pjId);
        p.setName(name);
        p.setPolygon(poly);
        ParkType t = new ParkType();
        t.setStrict(true);
        t.setOms(oms);
        p.setType(t);
        return p;
    }

    private static Filosofil200m filosofil(String id, int year, double pop) {
        Filosofil200m f = new Filosofil200m();
        f.setIdInspire(id);
        f.setAnnee(year);
        f.setNbIndividus(BigDecimal.valueOf(pop));
        return f;
    }

    private static ParcEtJardin parcJardin(long id, int debut, int fin, double surface) {
        ParcEtJardin pj = new ParcEtJardin();
        pj.setId(id);
        pj.setSurface(surface);
        Calendar cal = Calendar.getInstance();
        cal.set(debut, 0, 1);
        pj.setDateDebut(cal.getTime());
        cal.set(fin, 0, 1);
        pj.setDateFin(cal.getTime());
        City city = new City();
        city.setInseeCode("59350");
        pj.setCommune(city);
        return pj;
    }

    private long measureNanos(Runnable task, int times) {
        long total = 0;
        for (int i = 0; i < times; i++) {
            long start = System.nanoTime();
            task.run();
            total += System.nanoTime() - start;
        }
        return total / times;
    }

    private long measureSingleNanos(Runnable task) {
        long start = System.nanoTime();
        task.run();
        return System.nanoTime() - start;
    }

    private long invocations(Object mock, String methodName) {
        return mockingDetails(mock).getInvocations().stream()
                .filter(i -> i.getMethod().getName().equals(methodName))
                .count();
    }

    @SuppressWarnings("unchecked")
    private void clearAllInvocations() {
        clearInvocations(
                properties, parkAreaRepository, parkAreaComputedRepository,
                inseeCarre200mComputedV2Repository, cityRepository, cadastreRepository,
                filosofil200mRepository, inseeCarre200mOnlyShapeRepository,
                parkJardinRepository, serviceOpenData, parkTypeService, parkService);
    }

    // ============================================================
    // Scenario 1: Single ComputeJob
    // ============================================================

    @Test
    void singleJob_v4MakesFewerFilosofilCalls() {
        Polygon carrePoly = poly(0, 0, 10, 0, 10, 10, 0, 10);
        InseeCarre200mOnlyShape carre = carreShape("carre1", "59350", carrePoly, pt(5, 5));
        ComputeJob job = job("carre1", 2020);

        ParkArea pOms = park(1L, 100L, "Parc OMS", 8000.0,
                poly(1, 1, 4, 1, 4, 4, 1, 4), true);
        ParkArea pNonOms = park(2L, 200L, "Parc non OMS", 3000.0,
                poly(6, 6, 9, 6, 9, 9, 6, 9), false);
        List<ParkArea> parks = List.of(pOms, pNonOms);

        InseeCarre200mOnlyShape iso1 = carreShape("iso1", "59350", poly(1, 1, 3, 1, 3, 3, 1, 3), pt(2, 2));
        InseeCarre200mOnlyShape iso2 = carreShape("iso2", "59350", poly(2, 2, 4, 2, 4, 4, 2, 4), pt(3, 3));
        InseeCarre200mOnlyShape iso3 = carreShape("iso3", "59350", poly(6, 6, 8, 6, 8, 8, 6, 8), pt(7, 7));
        List<InseeCarre200mOnlyShape> isoCarres = List.of(iso1, iso2, iso3);

        Filosofil200m f1 = filosofil("iso1", 2020, 500);
        Filosofil200m f2 = filosofil("iso2", 2020, 300);
        Filosofil200m f3 = filosofil("iso3", 2020, 200);
        List<Filosofil200m> allFilosofil = List.of(f1, f2, f3);

        ParcEtJardin pjOms = parcJardin(100L, 2000, 2030, 8000);
        ParcEtJardin pjNonOms = parcJardin(200L, 2000, 2030, 3000);

        configureDefaults();

        when(inseeCarre200mOnlyShapeRepository.findById("carre1")).thenReturn(Optional.of(carre));
        when(serviceOpenData.isDistanceDense("59350")).thenReturn(true);
        when(parkAreaRepository.findParkInMapArea(anyString())).thenReturn(parks);

        when(inseeCarre200mOnlyShapeRepository.findCarreInMapArea(anyString()))
                .thenAnswer(inv -> {
                    return isoCarres;
                });

        when(filosofil200mRepository.getAllCarreInMap(anyString(), eq(2020)))
                .thenReturn(allFilosofil);

        lenient().when(filosofil200mRepository.findByAnneeAndIdInspire(eq(2020), anyString()))
                .thenAnswer(inv -> {
                    String id = inv.getArgument(1);
                    return allFilosofil.stream()
                            .filter(f -> f.getIdInspire().equals(id))
                            .findFirst().orElse(null);
                });

        when(parkJardinRepository.findById(100L)).thenReturn(Optional.of(pjOms));
        when(parkJardinRepository.findById(200L)).thenReturn(Optional.of(pjNonOms));

        ComputeCarreServiceV3 v3 = buildV3();
        ComputeCarreServiceV4 v4 = buildV4();

        for (int i = 0; i < WARMUP_ITERS; i++) {
            v3.computeCarreByComputeJob(job("warmup" + i, 2020));
            v4.computeCarreByComputeJob(job("warmup" + i, 2020));
            clearAllInvocations();
        }

        when(inseeCarre200mOnlyShapeRepository.findById("carre1")).thenReturn(Optional.of(carre));

        clearAllInvocations();

        long v3Time = measureSingleNanos(() -> v3.computeCarreByComputeJob(job));
        long v3Filosofil = invocations(filosofil200mRepository, "findByAnneeAndIdInspire");
        long v3Surface = invocations(inseeCarre200mOnlyShapeRepository, "getSurface");
        long v3FindCarre = invocations(inseeCarre200mOnlyShapeRepository, "findCarreInMapArea");
        long v3FindPark = invocations(parkAreaRepository, "findParkInMapArea");

        clearAllInvocations();

        long v4Time = measureSingleNanos(() -> v4.computeCarreByComputeJob(job));
        long v4Filosofil = invocations(filosofil200mRepository, "findByAnneeAndIdInspire");
        long v4Batch = invocations(filosofil200mRepository, "getAllCarreInMap");
        long v4Surface = invocations(inseeCarre200mOnlyShapeRepository, "getSurface");
        long v4FindCarre = invocations(inseeCarre200mOnlyShapeRepository, "findCarreInMapArea");
        long v4FindPark = invocations(parkAreaRepository, "findParkInMapArea");

        System.out.println("=== Single Job — Call Counts ===");
        System.out.println(String.format("  %-50s %5s %5s", "", "V3", "V4"));
        System.out.println(String.format("  %-50s %5d %5d", "findByAnneeAndIdInspire (Filosofil)", v3Filosofil, v4Filosofil));
        System.out.println(String.format("  %-50s %5s %5d", "getAllCarreInMap (batch)", "-", v4Batch));
        System.out.println(String.format("  %-50s %5d %5d", "getSurface", v3Surface, v4Surface));
        System.out.println(String.format("  %-50s %5d %5d", "findCarreInMapArea", v3FindCarre, v4FindCarre));
        System.out.println(String.format("  %-50s %5d %5d", "findParkInMapArea", v3FindPark, v4FindPark));
        System.out.println(String.format("  %-50s %5dms %5dms", "Wall-clock time",
                TimeUnit.NANOSECONDS.toMillis(v3Time), TimeUnit.NANOSECONDS.toMillis(v4Time)));
        if (v4Time > 0) {
            System.out.println(String.format("  Speedup: x%.2f", (double) v3Time / v4Time));
        }

        assertTrue(v4Filosofil < v3Filosofil,
                "V4 should make fewer individual Filosofil calls (batch). V3=" + v3Filosofil + " V4=" + v4Filosofil);
        assertEquals(1, v4Batch, "V4 should make exactly 1 batch call");
    }

    // ============================================================
    // Scenario 2: List of ComputeJobs
    // ============================================================

    @Test
    void multipleJobs_v4MakesFewerFilosofilCalls() {
        String[] ids = {"carreA", "carreB", "carreC"};
        int year = 2020;

        List<ComputeJob> jobs = new ArrayList<>();
        List<InseeCarre200mOnlyShape> carres = new ArrayList<>();
        for (String id : ids) {
            jobs.add(job(id, year));
            carres.add(carreShape(id, "59350",
                    poly(0, 0, 10, 0, 10, 10, 0, 10), pt(5, 5)));
        }

        ParkArea pOms = park(1L, 100L, "Parc OMS", 8000.0,
                poly(1, 1, 4, 1, 4, 4, 1, 4), true);
        ParkArea pNonOms = park(2L, 200L, "Parc non OMS", 3000.0,
                poly(6, 6, 9, 6, 9, 9, 6, 9), false);
        List<ParkArea> parks = List.of(pOms, pNonOms);

        InseeCarre200mOnlyShape iso1 = carreShape("iso1", "59350", poly(1, 1, 3, 1, 3, 3, 1, 3), pt(2, 2));
        InseeCarre200mOnlyShape iso2 = carreShape("iso2", "59350", poly(2, 2, 4, 2, 4, 4, 2, 4), pt(3, 3));
        InseeCarre200mOnlyShape iso3 = carreShape("iso3", "59350", poly(6, 6, 8, 6, 8, 8, 6, 8), pt(7, 7));
        List<InseeCarre200mOnlyShape> isoCarres = List.of(iso1, iso2, iso3);

        Filosofil200m f1 = filosofil("iso1", year, 500);
        Filosofil200m f2 = filosofil("iso2", year, 300);
        Filosofil200m f3 = filosofil("iso3", year, 200);
        List<Filosofil200m> allFilosofil = List.of(f1, f2, f3);

        ParcEtJardin pjOms = parcJardin(100L, 2000, 2030, 8000);
        ParcEtJardin pjNonOms = parcJardin(200L, 2000, 2030, 3000);

        configureDefaults();

        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            when(inseeCarre200mOnlyShapeRepository.findById(id))
                    .thenReturn(Optional.of(carres.get(i)));
        }
        when(serviceOpenData.isDistanceDense("59350")).thenReturn(true);
        when(parkAreaRepository.findParkInMapArea(anyString())).thenReturn(parks);

        when(inseeCarre200mOnlyShapeRepository.findCarreInMapArea(anyString()))
                .thenReturn(isoCarres);

        when(filosofil200mRepository.getAllCarreInMap(anyString(), eq(year)))
                .thenReturn(allFilosofil);

        lenient().when(filosofil200mRepository.findByAnneeAndIdInspire(eq(year), anyString()))
                .thenAnswer(inv -> {
                    String id = inv.getArgument(1);
                    return allFilosofil.stream()
                            .filter(f -> f.getIdInspire().equals(id))
                            .findFirst().orElse(null);
                });

        when(parkJardinRepository.findById(100L)).thenReturn(Optional.of(pjOms));
        when(parkJardinRepository.findById(200L)).thenReturn(Optional.of(pjNonOms));

        ComputeCarreServiceV3 v3 = buildV3();
        ComputeCarreServiceV4 v4 = buildV4();

        for (int i = 0; i < WARMUP_ITERS; i++) {
            ComputeJob wj = job("warmupM" + i, year);
            when(inseeCarre200mOnlyShapeRepository.findById(wj.getIdInspire()))
                    .thenReturn(Optional.of(carres.get(0)));
            v3.computeCarreByComputeJob(wj);
            v4.computeCarreByComputeJob(wj);
        }

        clearAllInvocations();

        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            when(inseeCarre200mOnlyShapeRepository.findById(id))
                    .thenReturn(Optional.of(carres.get(i)));
        }

        clearAllInvocations();

        long v3Time = measureSingleNanos(() -> {
            for (ComputeJob j : jobs) {
                v3.computeCarreByComputeJob(j);
            }
        });
        long v3Filosofil = invocations(filosofil200mRepository, "findByAnneeAndIdInspire");
        long v3Surface = invocations(inseeCarre200mOnlyShapeRepository, "getSurface");
        long v3FindCarre = invocations(inseeCarre200mOnlyShapeRepository, "findCarreInMapArea");
        long v3FindPark = invocations(parkAreaRepository, "findParkInMapArea");

        clearAllInvocations();

        long v4Time = measureSingleNanos(() -> {
            for (ComputeJob j : jobs) {
                v4.computeCarreByComputeJob(j);
            }
        });
        long v4Filosofil = invocations(filosofil200mRepository, "findByAnneeAndIdInspire");
        long v4Batch = invocations(filosofil200mRepository, "getAllCarreInMap");
        long v4Surface = invocations(inseeCarre200mOnlyShapeRepository, "getSurface");
        long v4FindCarre = invocations(inseeCarre200mOnlyShapeRepository, "findCarreInMapArea");
        long v4FindPark = invocations(parkAreaRepository, "findParkInMapArea");

        System.out.println("=== Multiple Jobs (" + jobs.size() + " jobs) — Call Counts ===");
        System.out.println(String.format("  %-50s %5s %5s", "", "V3", "V4"));
        System.out.println(String.format("  %-50s %5d %5d", "findByAnneeAndIdInspire (Filosofil)", v3Filosofil, v4Filosofil));
        System.out.println(String.format("  %-50s %5s %5d", "getAllCarreInMap (batch)", "-", v4Batch));
        System.out.println(String.format("  %-50s %5d %5d", "getSurface", v3Surface, v4Surface));
        System.out.println(String.format("  %-50s %5d %5d", "findCarreInMapArea", v3FindCarre, v4FindCarre));
        System.out.println(String.format("  %-50s %5d %5d", "findParkInMapArea", v3FindPark, v4FindPark));
        System.out.println(String.format("  %-50s %5dms %5dms", "Wall-clock time",
                TimeUnit.NANOSECONDS.toMillis(v3Time), TimeUnit.NANOSECONDS.toMillis(v4Time)));
        if (v4Time > 0) {
            System.out.println(String.format("  Speedup: x%.2f", (double) v3Time / v4Time));
        }

        assertTrue(v4Filosofil < v3Filosofil / jobs.size(),
                "V4 should make < individual Filosofil calls per job vs V3");
    }

}
