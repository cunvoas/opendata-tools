package com.github.cunvoas.geoserviceisochrone.service.park;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Cadastre;
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
import com.github.cunvoas.geoserviceisochrone.repo.GeometryQueryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.locationtech.jts.geom.Geometry;

import static org.junit.jupiter.api.Assertions.*;
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
    @Mock private GeometryQueryHelper geometryQueryHelper;
    @Mock private ParkTypeService parkTypeService;

    @Mock private ParkArea mockParkArea;
    @Mock private ComputeJob mockJob;
    @Mock private Cadastre mockCadastre;
    @Mock private Geometry mockGeometry;

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
            geometryQueryHelper,
            parkTypeService
        );
    }

    @Test
    public void testGetSurface() {
        // Teste que la méthode ne lève pas d'exception (mock minimal)
        assertDoesNotThrow(() -> service.getSurface(mockGeometry));
    }

    @Test
    public void testComputeParkAreaReturnsNullIfNoPolygon() {
        when(mockParkArea.getPolygon()).thenReturn(null);
        assertNull(service.computeParkArea(mockParkArea));
    }

    @Test
    public void testRefreshParkEntrancesString() {
        // Doit s'exécuter sans lever d'exception (mock minimal)
        assertDoesNotThrow(() -> service.refreshParkEntrances("12345"));
    }

    @Test
    public void testRefreshParkEntrancesCadastre() {
        // Doit s'exécuter sans lever d'exception (mock minimal)
        assertDoesNotThrow(() -> service.refreshParkEntrances(mockCadastre));
    }

    @Test
    public void testComputeCarreByComputeJobReturnsFalseIfNotFound() {
        when(mockJob.getIdInspire()).thenReturn("notfound");
        assertFalse(service.computeCarreByComputeJob(mockJob));
    }
}