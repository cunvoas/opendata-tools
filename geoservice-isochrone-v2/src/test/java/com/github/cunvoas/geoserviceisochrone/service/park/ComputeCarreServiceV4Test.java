package com.github.cunvoas.geoserviceisochrone.service.park;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.service.compute.dto.ComputeDto;
import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CityRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

import java.math.BigDecimal;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ComputeCarreServiceV4Test {

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

    @Mock private ParkArea mockParkArea;
    @Mock private ComputeJob mockJob;
    @Mock private Cadastre mockCadastre;
    @Mock private Geometry mockGeometry;
    @Mock private Geometry mockGeometry2;
    @Mock private Geometry mockIntersection;
    @Mock private Point mockPoint;
    @Mock private Polygon mockPolygon;

    private ComputeCarreServiceV4 service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ComputeCarreServiceV4(
            properties,
            parkAreaRepository,
            parkAreaComputedRepository,
            inseeCarre200mComputedV2Repository,
            cityRepository,
            cadastreRepository,
            filosofil200mRepository,
            inseeCarre200mOnlyShapeRepository,
            parkJardinRepository,
            serviceOpenData,
            parkTypeService,
            parkService
        );
    }

    // --- Existing tests (updated) ---

    @Test
    public void testGetSurface() {
        assertDoesNotThrow(() -> service.getSurface(mockGeometry));
    }

    @Test
    public void testComputeParkAreaReturnsNullIfNoPolygon() {
        when(mockParkArea.getPolygon()).thenReturn(null);
        assertNull(service.computeParkArea(mockParkArea));
    }

    @Test
    public void testRefreshParkEntrancesString() {
        when(cadastreRepository.findById("12345")).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> service.refreshParkEntrances("12345"));
    }

    @Test
    public void testRefreshParkEntrancesCadastre() {
        when(mockCadastre.getIdInsee()).thenReturn(null);
        when(cityRepository.findByInseeCode(null)).thenReturn(null);
        assertDoesNotThrow(() -> service.refreshParkEntrances(mockCadastre));
    }

    @Test
    public void testComputeCarreByComputeJobReturnsFalseIfNotFound() {
        when(mockJob.getIdInspire()).thenReturn("notfound");
        when(inseeCarre200mOnlyShapeRepository.findById("notfound")).thenReturn(Optional.empty());
        assertFalse(service.computeCarreByComputeJob(mockJob));
    }

    // --- Batch Filosofil ---

    @Test
    public void testLoadFilosofilBatch_returnsMapByIdInspire() {
        Filosofil200m f1 = new Filosofil200m();
        f1.setIdInspire("id1");
        f1.setNbIndividus(BigDecimal.valueOf(100));
        Filosofil200m f2 = new Filosofil200m();
        f2.setIdInspire("id2");
        f2.setNbIndividus(BigDecimal.valueOf(200));

        when(filosofil200mRepository.getAllCarreInMap(anyString(), eq(2020)))
            .thenReturn(List.of(f1, f2));

        Map<String, Filosofil200m> result = service.loadFilosofilBatch("POLYGON((0 0,1 0,1 1,0 1,0 0))", 2020);

        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(100), result.get("id1").getNbIndividus());
        assertEquals(BigDecimal.valueOf(200), result.get("id2").getNbIndividus());
    }

    // --- SurfaceCache tested through computePopAndDensityMutualised ---

    @Test
    public void testSurfaceCache_sameGeomPair_cachesSecondCall() {
        InseeCarre200mOnlyShape carreShape = new InseeCarre200mOnlyShape();
        carreShape.setIdInspire("test");
        carreShape.setGeoShape(mockPolygon);

        InseeCarre200mOnlyShape carreWithIso = new InseeCarre200mOnlyShape();
        carreWithIso.setIdInspire("iso1");
        carreWithIso.setGeoShape(mockPolygon);

        Filosofil200m f = new Filosofil200m();
        f.setIdInspire("iso1");
        f.setNbIndividus(BigDecimal.valueOf(200));

        ComputeDto dto = new ComputeDto();
        dto.annee = 2020;
        dto.popAll = BigDecimal.valueOf(1000);
        dto.result = new ComputeResultDto();
        dto.result.surfaceTotalParks = BigDecimal.valueOf(5000);
        dto.polygonParkAreas = mockGeometry;
        dto.polygonParkAreasOms = mockGeometry;

        when(inseeCarre200mOnlyShapeRepository.findCarreInMapArea(anyString()))
            .thenReturn(List.of(carreWithIso));
        when(filosofil200mRepository.getAllCarreInMap(anyString(), anyInt()))
            .thenReturn(List.of(f));
        when(inseeCarre200mOnlyShapeRepository.getSurface(any())).thenReturn(10000L);
        when(mockGeometry.intersection(mockGeometry2)).thenReturn(mockIntersection);
        when(mockGeometry.intersection(mockGeometry)).thenReturn(mockGeometry);

        Map<String, Filosofil200m> filosofilMap = service.loadFilosofilBatch("POLYGON((0 0,1 0,1 1,0 1,0 0))", 2020);
        ComputeCarreServiceV4.SurfaceCache surfaceCache = new SurfaceCacheHelper();
        surfaceCache.getOrCompute(mockGeometry2, mockGeometry, service::getSurface);
        surfaceCache.getOrCompute(mockGeometry2, mockGeometry, service::getSurface);

        verify(inseeCarre200mOnlyShapeRepository, times(1)).getSurface(any());
    }

    // helper to create SurfaceCache
    static class SurfaceCacheHelper extends ComputeCarreServiceV4.SurfaceCache {}

    // --- computePopAndDensityMutualised ---

    @Test
    public void testMutualisation_notAllOms_computesSeparately() {
        ComputeDto dto = new ComputeDto();
        dto.result = new ComputeResultDto();
        dto.resultOms = new ComputeResultDto();
        dto.allAreOms = false;
        dto.polygonParkAreas = mockGeometry;
        dto.polygonParkAreasOms = mockGeometry2;

        InseeCarre200mOnlyShape carreShape = new InseeCarre200mOnlyShape();
        carreShape.setIdInspire("test");
        carreShape.setGeoShape(mockPolygon);

        Map<String, Filosofil200m> filosofilMap = new HashMap<>();
        ComputeCarreServiceV4.SurfaceCache surfaceCache = new SurfaceCacheHelper();

        when(inseeCarre200mOnlyShapeRepository.findCarreInMapArea(anyString()))
            .thenReturn(Collections.emptyList());

        service.computePopAndDensityMutualised(dto, carreShape, mockGeometry, filosofilMap, surfaceCache);

        verify(inseeCarre200mOnlyShapeRepository, times(2)).findCarreInMapArea(anyString());
    }

    // --- computeMissingSurface ---

    @Test
    public void testComputeMissingSurface_urban() {
        ComputeDto dto = new ComputeDto();
        dto.isDense = true;
        dto.resultOms = new ComputeResultDto();
        dto.resultOms.populationInIsochrone = BigDecimal.valueOf(100);
        dto.resultOms.surfaceTotalParks = BigDecimal.valueOf(500);

        BigDecimal missing = service.computeMissingSurface(dto, null, 10.0, 5.0);
        assertEquals(0, BigDecimal.valueOf(500).compareTo(missing));
    }

    @Test
    public void testComputeMissingSurface_suburban_sufficient() {
        ComputeDto dto = new ComputeDto();
        dto.isDense = false;
        dto.resultOms = new ComputeResultDto();
        dto.resultOms.populationInIsochrone = BigDecimal.valueOf(100);
        dto.resultOms.surfaceTotalParks = BigDecimal.valueOf(800);

        BigDecimal missing = service.computeMissingSurface(dto, null, 10.0, 5.0);
        assertEquals(0, BigDecimal.ZERO.compareTo(missing));
    }

    // --- computeParkArea multiple years ---

    @Test
    public void testComputeParkArea_loopsAllYears() {
        when(mockParkArea.getPolygon()).thenReturn(mockPolygon);
        when(properties.getInseeAnnees()).thenReturn(new Integer[]{2019, 2020});
        when(mockParkArea.getId()).thenReturn(1L);
        when(mockParkArea.getIdParcEtJardin()).thenReturn(10L);
        when(mockParkArea.getName()).thenReturn("test park");

        doNothing().when(parkTypeService).populate(any(ParkArea.class));

        when(inseeCarre200mOnlyShapeRepository.findCarreInMapArea(anyString()))
            .thenReturn(Collections.emptyList());
        when(parkAreaComputedRepository.findByIdAndAnnee(anyLong(), anyInt()))
            .thenReturn(Optional.empty());

        ParkType parkType = new ParkType();
        parkType.setStrict(true);
        parkType.setOms(true);
        when(mockParkArea.getType()).thenReturn(parkType);

        when(parkAreaComputedRepository.save(any(ParkAreaComputed.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        City city = new City();
        city.setInseeCode("59350");
        ParcEtJardin pj = new ParcEtJardin();
        pj.setSurface(5000d);
        pj.setCommune(city);
        when(parkJardinRepository.findById(10L)).thenReturn(Optional.of(pj));
        when(serviceOpenData.isDistanceDense(city)).thenReturn(true);

        when(filosofil200mRepository.getAllCarreInMap(anyString(), anyInt()))
            .thenReturn(Collections.emptyList());

        ParkAreaComputed result = service.computeParkArea(mockParkArea);

        assertNotNull(result);
        verify(inseeCarre200mOnlyShapeRepository, times(2)).findCarreInMapArea(anyString());
    }

    // --- computeCarreByComputeJob full flow ---

    @Test
    public void testComputeCarreByComputeJob_returnsTrueOnSuccess() {
        InseeCarre200mOnlyShape carre = new InseeCarre200mOnlyShape();
        carre.setIdInspire("carre1");
        carre.setCodeInsee("59350");
        carre.setGeoShape(mockPolygon);
        carre.setGeoPoint2d(mockPoint);

        when(mockJob.getIdInspire()).thenReturn("carre1");
        when(mockJob.getAnnee()).thenReturn(2020);
        when(inseeCarre200mOnlyShapeRepository.findById("carre1")).thenReturn(Optional.of(carre));
        when(serviceOpenData.isDistanceDense("59350")).thenReturn(true);

        when(parkAreaRepository.findParkInMapArea(anyString())).thenReturn(Collections.emptyList());
        when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(2020, "carre1"))
            .thenReturn(Optional.empty());

        assertTrue(service.computeCarreByComputeJob(mockJob));
        verify(inseeCarre200mComputedV2Repository).save(any(InseeCarre200mComputedV2.class));
    }

    @Test
    public void testComputeCarreByComputeJob_handlesException() {
        when(mockJob.getIdInspire()).thenReturn("carre1");
        when(mockJob.getAnnee()).thenReturn(2020);
        when(inseeCarre200mOnlyShapeRepository.findById("carre1"))
            .thenThrow(new RuntimeException("DB error"));

        assertFalse(service.computeCarreByComputeJob(mockJob));
    }

    // --- isActive ---

    @Test
    public void testIsActive_withinYearRange_returnsTrue() {
        ParkArea pa = new ParkArea();
        pa.setIdParcEtJardin(1L);
        ParcEtJardin pj = new ParcEtJardin();
        Calendar cal = Calendar.getInstance();
        cal.set(2010, 0, 1);
        pj.setDateDebut(cal.getTime());
        cal.set(2025, 0, 1);
        pj.setDateFin(cal.getTime());

        when(parkJardinRepository.findById(1L)).thenReturn(Optional.of(pj));

        assertTrue(service.isActive(pa, 2020));
    }

    @Test
    public void testIsActive_outsideYearRange_returnsFalse() {
        ParkArea pa = new ParkArea();
        pa.setIdParcEtJardin(1L);
        ParcEtJardin pj = new ParcEtJardin();
        Calendar cal = Calendar.getInstance();
        cal.set(2010, 0, 1);
        pj.setDateDebut(cal.getTime());
        cal.set(2015, 0, 1);
        pj.setDateFin(cal.getTime());

        when(parkJardinRepository.findById(1L)).thenReturn(Optional.of(pj));

        assertFalse(service.isActive(pa, 2020));
    }

}
