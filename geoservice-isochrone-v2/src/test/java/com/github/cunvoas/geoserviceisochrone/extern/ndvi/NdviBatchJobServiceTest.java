package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.github.cunvoas.geoserviceisochrone.BaseUnitTest;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.admin.NdviJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviObservation;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.NdviJobRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.NdviObservationRepository;

@DisplayName("NdviBatchJobService")
class NdviBatchJobServiceTest extends BaseUnitTest {

    private static final long PARK_ID = 7L;
    private static final int ANNEE = 2019;

    @Mock private NdviJobRepository ndviJobRepository;
    @Mock private ParkAreaRepository parkAreaRepository;
    @Mock private ParkAreaComputedRepository parkAreaComputedRepository;
    @Mock private NdviObservationRepository ndviObservationRepository;
    @Mock private ClientCopernicusNdvi clientCopernicusNdvi;

    @InjectMocks private NdviBatchJobService service;

    private NdviJob job;
    private ParkArea park;
    private ParkAreaComputed row;
    private ParkAreaComputed olderRow;

    @BeforeEach
    void setUp() {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Polygon polygon = factory.createPolygon(new Coordinate[] {
                new Coordinate(2.0, 50.0), new Coordinate(2.1, 50.0),
                new Coordinate(2.1, 50.1), new Coordinate(2.0, 50.0) });

        park = new ParkArea();
        park.setId(PARK_ID);
        park.setPolygon(polygon);

        row = new ParkAreaComputed();
        row.setId(PARK_ID);
        row.setAnnee(ANNEE);

        olderRow = new ParkAreaComputed();
        olderRow.setId(PARK_ID);
        olderRow.setAnnee(2015);

        job = new NdviJob();
        job.setId(PARK_ID);
        job.setAnnee(ANNEE);

        when(clientCopernicusNdvi.isEnabled()).thenReturn(true);
        when(ndviJobRepository.findByStatusOrderByDemandAsc(eq(ComputeJobStatusEnum.TO_PROCESS), any()))
                .thenReturn(List.of(job));
        when(parkAreaRepository.findById(PARK_ID)).thenReturn(Optional.of(park));
        when(parkAreaComputedRepository.findAllByIdPark(PARK_ID)).thenReturn(List.of(row, olderRow));
    }

    private static NdviStatistics.IndexStatistics index(String mean, String min, String max, String stdDev) {
        return new NdviStatistics.IndexStatistics(
                new BigDecimal(mean), new BigDecimal(min), new BigDecimal(max), new BigDecimal(stdDev));
    }

    @Test
    @DisplayName("un job traité remplit le NDVI de toutes les années du parc")
    void processJobRemplitNdviToutesAnnees() {
        NdviStatistics stats = new NdviStatistics(
                index("0.42", "0.10", "0.80", "0.05"),
                index("0.31", "0.08", "0.60", "0.04"),
                index("0.55", "0.20", "0.90", "0.06"),
                new Date(3000L), new Date(2000L), 150);
        when(clientCopernicusNdvi.fetchNdviForPark(eq(PARK_ID), any())).thenReturn(Optional.of(stats));

        service.processJobs();

        assertEquals(ComputeJobStatusEnum.PROCESSED, job.getStatus());
        assertEquals(new BigDecimal("0.42"), row.getNdviMean());
        assertEquals(new BigDecimal("0.42"), olderRow.getNdviMean());
        assertEquals(3000L, olderRow.getNdviAcquisitionDate().getTime());
        assertEquals(new BigDecimal("0.31"), row.getNdreMean());
        assertEquals(new BigDecimal("0.31"), olderRow.getNdreMean());
        assertEquals(new BigDecimal("0.55"), row.getGndviMean());
        assertEquals(new BigDecimal("0.55"), olderRow.getGndviMean());
        assertNotNull(row.getNdviRequestDate());
        assertNotNull(olderRow.getNdviRequestDate());
        verify(parkAreaComputedRepository).saveAll(List.of(row, olderRow));

        ArgumentCaptor<NdviObservation> observationCaptor = ArgumentCaptor.forClass(NdviObservation.class);
        verify(ndviObservationRepository).save(observationCaptor.capture());
        NdviObservation observation = observationCaptor.getValue();
        assertEquals(PARK_ID, observation.getParkAreaId());
        assertEquals(2000L, observation.getPeriodStart().getTime());
        assertEquals(3000L, observation.getPeriodEnd().getTime());
        assertEquals(150, observation.getSampleCount());
        assertEquals(new BigDecimal("0.42"), observation.getNdviMean());
        assertEquals(new BigDecimal("0.31"), observation.getNdreMean());
        assertEquals(new BigDecimal("0.55"), observation.getGndviMean());
    }

    @Test
    @DisplayName("NDVI déjà présent sur une autre année : aucun appel fournisseur, donnée recopiée")
    void ndviDejaPresentSansAppel() {
        olderRow.setNdviMean(new BigDecimal("0.33"));
        olderRow.setNdviMin(new BigDecimal("0.20"));
        olderRow.setNdviMax(new BigDecimal("0.50"));
        olderRow.setNdviStddev(new BigDecimal("0.04"));
        olderRow.setNdviAcquisitionDate(new Date(4000L));
        olderRow.setNdviRequestDate(new Date(5000L));

        service.processJobs();

        assertEquals(ComputeJobStatusEnum.PROCESSED, job.getStatus());
        verify(clientCopernicusNdvi, never()).fetchNdviForPark(any(), any());
        assertEquals(new BigDecimal("0.33"), row.getNdviMean());
        assertEquals(4000L, row.getNdviAcquisitionDate().getTime());
        assertEquals(5000L, row.getNdviRequestDate().getTime());
        verify(parkAreaComputedRepository).saveAll(List.of(row, olderRow));
        verify(ndviObservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("demande NDVI pour un parc : utilise park_area.id et l'année de référence")
    void requestForParcEnqueue() {
        when(parkAreaRepository.findByIdParcEtJardin(PARK_ID)).thenReturn(park);

        assertTrue(service.requestForParc(PARK_ID, ANNEE));

        ArgumentCaptor<NdviJob> captor = ArgumentCaptor.forClass(NdviJob.class);
        verify(ndviJobRepository).save(captor.capture());
        assertEquals(PARK_ID, captor.getValue().getId());
        assertEquals(ANNEE, captor.getValue().getAnnee());
    }

    @Test
    @DisplayName("demande NDVI pour un parc : aucun job si le NDVI existe déjà")
    void requestForParcDejaNdvi() {
        when(parkAreaRepository.findByIdParcEtJardin(PARK_ID)).thenReturn(park);
        row.setNdviMean(BigDecimal.ONE);

        assertFalse(service.requestForParc(PARK_ID, ANNEE));

        verify(ndviJobRepository, never()).save(any());
    }

    @Test
    @DisplayName("fenêtre vide : aucun historique, mais la date de demande est posée")
    void fenetreVideSansHistorique() {
        when(clientCopernicusNdvi.fetchNdviForPark(eq(PARK_ID), any())).thenReturn(Optional.empty());

        service.processJobs();

        assertEquals(ComputeJobStatusEnum.PROCESSED, job.getStatus());
        assertNotNull(row.getNdviRequestDate());
        assertNotNull(olderRow.getNdviRequestDate());
        assertNull(row.getNdviMean());
        verify(ndviObservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("une erreur fournisseur laisse le job IN_ERROR et les lignes NDVI intactes")
    void erreurFournisseurLaisseJobEnErreur() {
        when(clientCopernicusNdvi.fetchNdviForPark(eq(PARK_ID), any()))
                .thenThrow(new NdviQuotaExceededException(10_000));

        service.processJobs();

        assertEquals(ComputeJobStatusEnum.IN_ERROR, job.getStatus());
        assertNull(row.getNdviRequestDate());
        assertNull(olderRow.getNdviRequestDate());
        assertNull(row.getNdviMean());
        verify(parkAreaComputedRepository, never()).saveAll(any());
        verify(ndviObservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("relance forcée : appelle Copernicus même si un NDVI existe déjà")
    void relanceForceeIgnoreNdviExistant() {
        olderRow.setNdviMean(new BigDecimal("0.33"));
        job.setForce(true);
        NdviStatistics stats = new NdviStatistics(
                index("0.42", "0.10", "0.80", "0.05"),
                index("0.31", "0.08", "0.60", "0.04"),
                index("0.55", "0.20", "0.90", "0.06"),
                new Date(3000L), new Date(2000L), 150);
        when(clientCopernicusNdvi.fetchNdviForPark(eq(PARK_ID), any())).thenReturn(Optional.of(stats));

        service.processJobs();

        assertEquals(ComputeJobStatusEnum.PROCESSED, job.getStatus());
        assertFalse(job.isForce());
        assertEquals(new BigDecimal("0.42"), row.getNdviMean());
        assertEquals(new BigDecimal("0.42"), olderRow.getNdviMean());
        verify(clientCopernicusNdvi).fetchNdviForPark(eq(PARK_ID), any());
    }

    @Test
    @DisplayName("relance forcée d'un EPCI : un job forcé par parc calculé")
    void relanceForceeEpci() {
        when(parkAreaComputedRepository.findParkIdsByAnneeAndCom2co(ANNEE, 1L))
                .thenReturn(List.of(PARK_ID));

        int count = service.requestForcedForCom2Co(1L, ANNEE);

        assertEquals(1, count);
        ArgumentCaptor<NdviJob> captor = ArgumentCaptor.forClass(NdviJob.class);
        verify(ndviJobRepository).save(captor.capture());
        assertTrue(captor.getValue().isForce());
    }

    @Test
    @DisplayName("un job bloqué IN_PROCESS est remis en file")
    void recycleJobBloque() {
        job.setStatus(ComputeJobStatusEnum.IN_PROCESS);
        when(ndviJobRepository.findOnStartUnfinishedProcessed(any())).thenReturn(List.of(job));

        service.recycleUndoneJobs();

        assertEquals(ComputeJobStatusEnum.TO_PROCESS, job.getStatus());
    }
}
