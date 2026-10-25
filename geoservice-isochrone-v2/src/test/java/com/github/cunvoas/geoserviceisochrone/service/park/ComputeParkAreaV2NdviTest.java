package com.github.cunvoas.geoserviceisochrone.service.park;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.github.cunvoas.geoserviceisochrone.BaseUnitTest;
import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.extern.ndvi.ClientCopernicusNdvi;
import com.github.cunvoas.geoserviceisochrone.extern.ndvi.NdviStatistics;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

/**
 * Vérifie l'intégration NDVI dans le pipeline OMS : rattrapage d'une ligne fraîche
 * sans NDVI et non-régression des indicateurs OMS en cas d'échec fournisseur.
 */
@DisplayName("ComputeParkAreaV2 - NDVI")
class ComputeParkAreaV2NdviTest extends BaseUnitTest {

    private static final Long PARK_ID = 42L;
    private static final Integer ANNEE = 2019;

    @Mock private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
    @Mock private IrisShapeRepository irisShapeRepository;
    @Mock private Filosofil200mRepository filosofil200mRepository;
    @Mock private ParkAreaComputedRepository parkAreaComputedRepository;
    @Mock private ParkJardinRepository parkJardinRepository;
    @Mock private ParkTypeService parkTypeService;
    @Mock private ServiceOpenData serviceOpenData;
    @Mock private ApplicationBusinessProperties applicationBusinessProperties;
    @Mock private ClientCopernicusNdvi clientCopernicusNdvi;

    @InjectMocks private ComputeParkAreaV2 service;

    private ParkArea park;
    private ParkAreaComputed row;
    private Date omsUpdated;

    @BeforeEach
    void setUp() {
        when(applicationBusinessProperties.getInseeAnnees()).thenReturn(new Integer[] { 2015 });
        when(applicationBusinessProperties.getIrisAnnees()).thenReturn(new Integer[] { ANNEE });

        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Polygon polygon = factory.createPolygon(new Coordinate[] {
                new Coordinate(2.0, 50.0), new Coordinate(2.1, 50.0),
                new Coordinate(2.1, 50.1), new Coordinate(2.0, 50.0) });

        park = new ParkArea();
        park.setId(PARK_ID);
        park.setName("Parc test");
        park.setPolygon(polygon);
        park.setUpdated(new Date(1000L));

        omsUpdated = new Date(2000L);
        row = new ParkAreaComputed();
        row.setId(PARK_ID);
        row.setAnnee(ANNEE);
        row.setUpdated(omsUpdated);
        row.setSurfacePerInhabitant(BigDecimal.TEN);

        when(parkAreaComputedRepository.findByIdAndAnnee(PARK_ID, ANNEE)).thenReturn(Optional.of(row));
        when(parkAreaComputedRepository.save(any(ParkAreaComputed.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // La fenêtre septembre/1 an est testée isolément : on force ici l'éligibilité
        service = spy(service);
        doReturn(true).when(service).isNdviRequestDue(any(ParkAreaComputed.class));
    }

    @Test
    @DisplayName("ligne OMS fraîche sans NDVI : NDVI rattrapé, OMS inchangés")
    void catchUpNdviOnFreshRow() {
        NdviStatistics stats = new NdviStatistics(
                new BigDecimal("0.4210"), new BigDecimal("0.1100"),
                new BigDecimal("0.8000"), new BigDecimal("0.0500"), new Date(3000L));
        when(clientCopernicusNdvi.isEnabled()).thenReturn(true);
        when(clientCopernicusNdvi.fetchNdviForPark(eq(PARK_ID), any())).thenReturn(Optional.of(stats));

        ParkAreaComputed result = service.computeGenericParkAreaV2(park, ANNEE);

        assertSame(row, result);
        assertEquals(new BigDecimal("0.4210"), result.getNdviMean());
        assertEquals(new BigDecimal("0.0500"), result.getNdviStddev());
        assertEquals(3000L, result.getNdviAcquisitionDate().getTime());
        assertEquals(omsUpdated, result.getUpdated());
        assertNull(result.getPopulation());
        verify(parkAreaComputedRepository, times(1)).save(row);
    }

    @Test
    @DisplayName("échec fournisseur : colonnes nulles, ligne OMS sauvegardée et intacte")
    void ndviFailureDoesNotBreakOms() {
        when(clientCopernicusNdvi.isEnabled()).thenReturn(true);
        when(clientCopernicusNdvi.fetchNdviForPark(eq(PARK_ID), any()))
                .thenThrow(new IllegalStateException("CDSE indisponible"));

        ParkAreaComputed result = service.computeGenericParkAreaV2(park, ANNEE);

        assertSame(row, result);
        assertNull(result.getNdviMean());
        assertNotNull(result.getNdviRequestDate(), "la demande doit être datée même en échec");
        assertEquals(omsUpdated, result.getUpdated());
        assertNull(result.getPopulation());
        verify(parkAreaComputedRepository, times(1)).save(row);
    }

    @Test
    @DisplayName("appel NDVI autorisé seulement à partir de septembre")
    void ndviAutoriseAPartirDeSeptembre() {
        assertFalse(service.isNdviRequestDue(row, LocalDate.of(2027, 8, 31)));
        assertTrue(service.isNdviRequestDue(row, LocalDate.of(2027, 9, 1)));
    }

    @Test
    @DisplayName("pas de nouvel appel NDVI avant un an")
    void ndviLimiteAUnAn() {
        row.setNdviRequestDate(Date.from(
                LocalDate.of(2026, 9, 15).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        assertFalse(service.isNdviRequestDue(row, LocalDate.of(2027, 9, 14)));
        assertTrue(service.isNdviRequestDue(row, LocalDate.of(2027, 9, 15)));
    }
}
