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
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobProgressStat;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.admin.NdviJob;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviResult;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.NdviResultObjectType;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.NdviResultRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.NdviJobRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

@DisplayName("NdviBatchJobService")
class NdviBatchJobServiceTest extends BaseUnitTest {

    private static final long PARK_ID = 7L;
    private static final long PARC_ID = 12L;
    private static final long CITY_ID = 2878L;
    private static final long COM2CO_ID = 1L;
    private static final long REGION_ID = 9L;
    private static final int ANNEE = 2019;

    @Mock private NdviJobRepository ndviJobRepository;
    @Mock private ParkAreaRepository parkAreaRepository;
    @Mock private ParkAreaComputedRepository parkAreaComputedRepository;
    @Mock private NdviResultRepository ndviResultRepository;
    @Mock private ParkJardinRepository parkJardinRepository;
    @Mock private IrisShapeRepository irisShapeRepository;
    @Mock private InseeCarre200mOnlyShapeRepository carre200mOnlyShapeRepository;
    @Mock private ClientCopernicusNdvi clientCopernicusNdvi;

    @InjectMocks private NdviBatchJobService service;

    private NdviJob job;
    private ParkArea park;
    private ParkAreaComputed row;

    @BeforeEach
    void setUp() {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        Polygon polygon = factory.createPolygon(new Coordinate[] {
                new Coordinate(2.0, 50.0), new Coordinate(2.1, 50.0),
                new Coordinate(2.1, 50.1), new Coordinate(2.0, 50.0) });

        park = new ParkArea();
        park.setId(PARK_ID);
        park.setIdParcEtJardin(PARC_ID);
        park.setPolygon(polygon);

        row = new ParkAreaComputed();
        row.setId(PARK_ID);
        row.setAnnee(ANNEE);

        job = new NdviJob();
        job.setId(PARK_ID);
        job.setAnnee(ANNEE);

        City city = new City();
        city.setInseeCode("59350");
        ParcEtJardin parc = new ParcEtJardin();
        parc.setId(PARC_ID);
        parc.setCommune(city);

        when(clientCopernicusNdvi.isEnabled()).thenReturn(true);
        when(ndviJobRepository.findByStatusOrderByDemandAsc(eq(ComputeJobStatusEnum.TO_PROCESS), any()))
                .thenReturn(List.of(job));
        when(parkAreaRepository.findById(PARK_ID)).thenReturn(Optional.of(park));
        when(parkAreaComputedRepository.findAllByIdPark(PARK_ID)).thenReturn(List.of(row));
        when(parkJardinRepository.findById(PARC_ID)).thenReturn(Optional.of(parc));
        when(irisShapeRepository.findBestIrisCode(any())).thenReturn("593500101");
        when(carre200mOnlyShapeRepository.findBestIdInspire(any())).thenReturn("INSPIRE-200");
    }

    private static NdviStatistics.IndexStatistics index(String mean, String min, String max, String stdDev) {
        return new NdviStatistics.IndexStatistics(
                new BigDecimal(mean), new BigDecimal(min), new BigDecimal(max), new BigDecimal(stdDev));
    }

    private static NdviStatistics stats() {
        return new NdviStatistics(
                index("0.42", "0.10", "0.80", "0.05"),
                index("0.31", "0.08", "0.60", "0.04"),
                index("0.55", "0.20", "0.90", "0.06"),
                new Date(3000L), new Date(2000L), 150);
    }

    @Test
    @DisplayName("un job traité enregistre une mesure NDVI avec ses identifiants")
    void processJobEnregistreNdviResult() {
        when(clientCopernicusNdvi.fetchNdviForPark(eq(PARK_ID), any())).thenReturn(Optional.of(stats()));

        service.processJobs();

        assertEquals(ComputeJobStatusEnum.PROCESSED, job.getStatus());
        verify(parkAreaComputedRepository, never()).saveAll(any());

        ArgumentCaptor<NdviResult> captor = ArgumentCaptor.forClass(NdviResult.class);
        verify(ndviResultRepository).save(captor.capture());
        NdviResult result = captor.getValue();
        assertEquals(NdviResultObjectType.PARK_AREA_COMPUTED, result.getObjectType());
        assertEquals(PARK_ID, result.getParkAreaComputedId());
        assertEquals(PARC_ID, result.getParcEtJardinId());
        assertEquals("593500101", result.getIrisId());
        assertEquals("59350", result.getCadastreId());
        assertEquals("INSPIRE-200", result.getCarre200mId());
        assertNotNull(result.getRequestedAt());
        assertEquals(2000L, result.getPeriodStart().getTime());
        assertEquals(3000L, result.getPeriodEnd().getTime());
        assertEquals(150, result.getSampleCount());
        assertEquals(new BigDecimal("0.42"), result.getNdviMean());
        assertEquals(new BigDecimal("0.31"), result.getNdreMean());
        assertEquals(new BigDecimal("0.55"), result.getGndviMean());
    }

    @Test
    @DisplayName("NDVI déjà présent : aucun appel fournisseur")
    void ndviDejaPresentSansAppel() {
        when(ndviResultRepository.existsByObjectTypeAndParkAreaComputedId(
                NdviResultObjectType.PARK_AREA_COMPUTED, PARK_ID)).thenReturn(true);

        service.processJobs();

        assertEquals(ComputeJobStatusEnum.PROCESSED, job.getStatus());
        verify(clientCopernicusNdvi, never()).fetchNdviForPark(any(), any());
        verify(ndviResultRepository, never()).save(any());
        verify(parkAreaComputedRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("demande NDVI pour un parc : utilise park_area.id et l'année de référence")
    void requestForParcEnqueue() {
        when(parkAreaRepository.findByIdParcEtJardin(PARC_ID)).thenReturn(park);

        assertTrue(service.requestForParc(PARC_ID, ANNEE));

        ArgumentCaptor<NdviJob> captor = ArgumentCaptor.forClass(NdviJob.class);
        verify(ndviJobRepository).save(captor.capture());
        assertEquals(PARK_ID, captor.getValue().getId());
        assertEquals(ANNEE, captor.getValue().getAnnee());
    }

    @Test
    @DisplayName("demande NDVI pour un parc : aucun job si le NDVI existe déjà")
    void requestForParcDejaNdvi() {
        when(parkAreaRepository.findByIdParcEtJardin(PARC_ID)).thenReturn(park);
        when(ndviResultRepository.existsByObjectTypeAndParkAreaComputedId(
                NdviResultObjectType.PARK_AREA_COMPUTED, PARK_ID)).thenReturn(true);

        assertFalse(service.requestForParc(PARC_ID, ANNEE));

        verify(ndviJobRepository, never()).save(any());
    }

    @Test
    @DisplayName("fenêtre vide : aucune mesure, mais le job est traité")
    void fenetreVideSansResultat() {
        when(clientCopernicusNdvi.fetchNdviForPark(eq(PARK_ID), any())).thenReturn(Optional.empty());

        service.processJobs();

        assertEquals(ComputeJobStatusEnum.PROCESSED, job.getStatus());
        verify(ndviResultRepository, never()).save(any());
        verify(parkAreaComputedRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("une erreur fournisseur laisse le job IN_ERROR et aucune mesure")
    void erreurFournisseurLaisseJobEnErreur() {
        when(clientCopernicusNdvi.fetchNdviForPark(eq(PARK_ID), any()))
                .thenThrow(new NdviQuotaExceededException(10_000));

        service.processJobs();

        assertEquals(ComputeJobStatusEnum.IN_ERROR, job.getStatus());
        verify(parkAreaComputedRepository, never()).saveAll(any());
        verify(ndviResultRepository, never()).save(any());
    }

    @Test
    @DisplayName("relance forcée : appelle Copernicus même si un NDVI existe déjà")
    void relanceForceeIgnoreNdviExistant() {
        when(ndviResultRepository.existsByObjectTypeAndParkAreaComputedId(
                NdviResultObjectType.PARK_AREA_COMPUTED, PARK_ID)).thenReturn(true);
        job.setForce(true);
        when(clientCopernicusNdvi.fetchNdviForPark(eq(PARK_ID), any())).thenReturn(Optional.of(stats()));

        service.processJobs();

        assertEquals(ComputeJobStatusEnum.PROCESSED, job.getStatus());
        assertFalse(job.isForce());
        verify(clientCopernicusNdvi).fetchNdviForPark(eq(PARK_ID), any());
        verify(ndviResultRepository).save(any(NdviResult.class));
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
    @DisplayName("dernière date de traitement NDVI d'un parc")
    void derniereDateTraitement() {
        job.setProcessed(new Date(6000L));
        when(ndviJobRepository.findFirstByIdAndProcessedIsNotNullOrderByProcessedDesc(PARK_ID))
                .thenReturn(Optional.of(job));

        assertEquals(6000L, service.lastRequestDate(PARK_ID).getTime());
    }

    @Test
    @DisplayName("synthèse NDVI par ville et par EPCI")
    void syntheseNdviParTerritoire() {
        when(ndviJobRepository.getProgressStatsCityLevel(CITY_ID, COM2CO_ID, REGION_ID, "PARC"))
                .thenReturn(List.<Object[]>of(new Object[] {"MEL", "Lille", "PARC", 1L, 2L, 3L, 4L}));
        when(ndviJobRepository.getProgressStatsEpciLevel(COM2CO_ID, REGION_ID, "PARC"))
                .thenReturn(List.<Object[]>of(new Object[] {"MEL", null, "PARC", 5L, 6L, 7L, 8L}));

        ComputeJobProgressStat city = service
                .getProgressStatsCityLevel(CITY_ID, COM2CO_ID, REGION_ID, "PARC").get(0);
        ComputeJobProgressStat epci = service
                .getProgressStatsEpciLevel(COM2CO_ID, REGION_ID, "PARC").get(0);

        assertEquals("MEL", city.getEpciName());
        assertEquals("Lille", city.getCityName());
        assertEquals("PARC", city.getType());
        assertNull(city.getAnnee());
        assertEquals(1L, city.getToProcess());
        assertEquals(4L, city.getInError());
        assertNull(epci.getCityName());
        assertEquals(7L, epci.getProcessed());
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
