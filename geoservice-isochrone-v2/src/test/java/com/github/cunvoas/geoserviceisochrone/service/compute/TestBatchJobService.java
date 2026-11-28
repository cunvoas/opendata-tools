package com.github.cunvoas.geoserviceisochrone.service.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.exception.ExceptionAdmin;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeIrisJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJob;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobProgressStat;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ComputeJobIrisRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ComputeJobRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.IrisShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.park.IComputeCarreService;
import com.github.cunvoas.geoserviceisochrone.service.park.IComputeIrisService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestBatchJobService {

	@Mock private ApplicationBusinessProperties applicationBusinessProperties;
	@Mock private CadastreRepository cadastreRepository;
	@Mock private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Mock private IrisShapeRepository irisShapeRepository;
	@Mock private ComputeJobRepository computeJobCarreRepository;
	@Mock private ParkAreaRepository parkAreaRepository;
	@Mock private IComputeCarreService computeCarreService;
	@Mock private ComputeJobIrisRepository computeJobIrisRepository;
	@Mock private IComputeIrisService computeIrisService;
	@Mock private CpuTemperatureService cpuTemperatureService;

	@InjectMocks
	private BatchJobService tested;

	private static InseeCarre200mOnlyShape carre(String id, String insee) {
		InseeCarre200mOnlyShape c = new InseeCarre200mOnlyShape();
		c.setIdInspire(id);
		c.setCodeInsee(insee);
		return c;
	}

	private static IrisShape iris(String iris, String insee) {
		IrisShape s = new IrisShape();
		s.setIris(iris);
		s.setCodeInsee(insee);
		return s;
	}

	@Test
	void request_process_parc_ignored_without_geometry() {
		ParcEtJardin pj = new ParcEtJardin();
		pj.setId(1L);
		when(parkAreaRepository.findByIdParcEtJardin(1L)).thenReturn(null);

		assertEquals(0, tested.requestProcessParc(pj));
		verify(computeJobCarreRepository, never()).saveAll(any());
	}

	@Test
	void request_process_city_creates_carre_and_iris_jobs() {
		when(inseeCarre200mOnlyShapeRepository.findByCodeInsee("69123"))
				.thenReturn(List.of(carre("I1", "69123"), carre("I2", "69123")));
		when(irisShapeRepository.findByCodeInsee("69123")).thenReturn(List.of(iris("IRIS1", "69123")));
		when(computeJobCarreRepository.findById(any())).thenReturn(Optional.empty());
		when(computeJobIrisRepository.findById(any())).thenReturn(Optional.empty());

		int nb = tested.requestProcessCity("69123", 2024);

		assertEquals(3, nb);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<ComputeJob>> carreJobs = ArgumentCaptor.forClass(List.class);
		verify(computeJobCarreRepository).saveAll(carreJobs.capture());
		assertEquals(2, carreJobs.getValue().size());
		assertEquals(2024, carreJobs.getValue().get(0).getAnnee());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<ComputeIrisJob>> irisJobs = ArgumentCaptor.forClass(List.class);
		verify(computeJobIrisRepository).saveAll(irisJobs.capture());
		assertEquals(1, irisJobs.getValue().size());
	}

	@Test
	void request_process_city_without_shapes_saves_nothing_meaningful() {
		when(inseeCarre200mOnlyShapeRepository.findByCodeInsee("69123")).thenReturn(List.of());
		when(irisShapeRepository.findByCodeInsee("69123")).thenReturn(List.of());

		assertEquals(0, tested.requestProcessCity("69123", 2024));
		verify(computeJobCarreRepository).saveAll(List.of());
	}

	@Test
	void request_process_com2co_sums_cities() {
		City c1 = new City();
		c1.setInseeCode("11111");
		City c2 = new City();
		c2.setInseeCode("22222");
		CommunauteCommune c2c = new CommunauteCommune();
		c2c.setCities(List.of(c1, c2));
		when(inseeCarre200mOnlyShapeRepository.findByCodeInsee("11111")).thenReturn(List.of(carre("A", "11111")));
		when(inseeCarre200mOnlyShapeRepository.findByCodeInsee("22222"))
				.thenReturn(List.of(carre("B", "22222"), carre("C", "22222")));
		when(irisShapeRepository.findByCodeInsee(anyString())).thenReturn(List.of());

		assertEquals(3, tested.requestProcessCom2Co(c2c, 2024));
	}

	@Test
	void launch_or_finish_is_exclusive() {
		assertTrue(tested.launchOrFinish(true));
		assertFalse(tested.launchOrFinish(true));
		assertTrue(tested.launchOrFinish(false));
		assertThrows(ExceptionAdmin.class, () -> tested.launchOrFinish(false));
	}

	@Test
	void process_carres_is_skipped_when_cpu_is_hot() {
		when(cpuTemperatureService.isCpuOverheated()).thenReturn(true);

		tested.processCarres(2024, "I1");

		verify(computeJobCarreRepository, never()).findById(any());
	}

	@Test
	void process_carres_marks_job_processed() {
		ComputeJob job = new ComputeJob();
		when(cpuTemperatureService.isCpuOverheated()).thenReturn(false);
		when(computeJobCarreRepository.findById(any())).thenReturn(Optional.of(job));
		when(computeCarreService.computeCarreByComputeJob(job)).thenReturn(Boolean.TRUE);

		tested.processCarres(2024, "I1");

		assertEquals(ComputeJobStatusEnum.PROCESSED, job.getStatus());
		assertNotNull(job.getProcessed());
		verify(computeJobCarreRepository).save(job);
	}

	@Test
	void process_carres_marks_job_in_error() {
		ComputeJob job = new ComputeJob();
		when(cpuTemperatureService.isCpuOverheated()).thenReturn(false);
		when(computeJobCarreRepository.findById(any())).thenReturn(Optional.of(job));
		when(computeCarreService.computeCarreByComputeJob(job)).thenReturn(Boolean.FALSE);

		tested.processCarres(2024, "I1");

		assertEquals(ComputeJobStatusEnum.IN_ERROR, job.getStatus());
	}

	@Test
	void recycle_undone_shapes_requeues_interrupted_jobs() {
		ComputeJob job = new ComputeJob();
		job.setStatus(ComputeJobStatusEnum.IN_PROCESS);
		when(computeJobCarreRepository.findOnStartUnfinishedProcessed(any(Date.class))).thenReturn(List.of(job));
		when(computeJobIrisRepository.findOnStartUnfinishedProcessed(any(Date.class))).thenReturn(List.of());

		tested.recycleUndoneShapes();

		assertEquals(ComputeJobStatusEnum.TO_PROCESS, job.getStatus());
		assertNotNull(job.getDemand());
		verify(computeJobCarreRepository).save(job);
	}

	@Test
	void recycle_is_skipped_after_shutdown_request() {
		tested.shutdown();

		tested.recycleUndoneShapes();
		tested.recycleErrorShapes();

		verify(computeJobCarreRepository, never()).findOnStartUnfinishedProcessed(any());
		verify(computeJobCarreRepository, never()).findOnErrorAndProcessed(any());
	}

	@Test
	void grouped_progress_stats_are_mapped() {
		when(computeJobCarreRepository.getGroupedProgressStats())
				.thenReturn(List.<Object[]>of(new Object[] { "EPCI", "Ville", 2024, 1L, 2L, 3L, 4L }));

		List<ComputeJobProgressStat> stats = tested.getGroupedProgressStats();

		assertEquals(1, stats.size());
		assertEquals("EPCI", stats.get(0).getEpciName());
		assertEquals("Ville", stats.get(0).getCityName());
		assertEquals(2024, stats.get(0).getAnnee());
		assertEquals(4L, stats.get(0).getInError());
	}

	@Test
	void progress_stats_city_level_without_city_column() {
		when(computeJobCarreRepository.getProgressStatsEpciLevel(1L, null, 2024))
				.thenReturn(List.<Object[]>of(new Object[] { "EPCI", null, 2024, 1L, 2L, 3L, 4L }));

		List<ComputeJobProgressStat> stats = tested.getProgressStatsEpciLevel(1L, null, 2024);

		assertEquals(1, stats.size());
		org.junit.jupiter.api.Assertions.assertNull(stats.get(0).getCityName());
		assertEquals(3L, stats.get(0).getProcessed());
	}

	@Test
	void is_junit_test_detects_test_runtime() {
		assertTrue(BatchJobService.isJUnitTest());
	}

	@Test
	void park_area_without_polygon_is_ignored() {
		ParcEtJardin pj = new ParcEtJardin();
		pj.setId(2L);
		ParkArea pa = new ParkArea();
		when(parkAreaRepository.findByIdParcEtJardin(2L)).thenReturn(pa);

		assertEquals(0, tested.requestProcessParc(pj));
	}
}
