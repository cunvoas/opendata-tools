package com.github.cunvoas.geoserviceisochrone.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.config.property.FeatureFlippingProperties;
import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStatusEnum;
import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardCache;
import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardSummary;
import com.github.cunvoas.geoserviceisochrone.repo.DashboardCacheRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaComputedRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkAreaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.ParkEntranceRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.AssociationRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ComputeJobIrisRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ComputeJobRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.NdviJobRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CadastreRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.CommunauteCommuneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.ParkJardinRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DashboadServiceTest {

	@Mock private ContributeurRepository contributeurRepository;
	@Mock private AssociationRepository associationRepository;
	@Mock private CadastreRepository cadastreRepository;
	@Mock private CommunauteCommuneRepository communauteCommuneRepository;
	@Mock private InseeCarre200mOnlyShapeRepository inseeCarre200mRepository;
	@Mock private Filosofil200mRepository filosofil200mRepository;
	@Mock private ParkJardinRepository parkJardinRepository;
	@Mock private ParkAreaRepository parkAreaRepository;
	@Mock private ParkAreaComputedRepository parkAreaComputedRepository;
	@Mock private ParkEntranceRepository parkEntranceRepository;
	@Mock private DashboardCacheRepository dashboardCacheRepository;
	@Mock private ApplicationBusinessProperties applicationBusinessProperties;
	@Mock private FeatureFlippingProperties featureFlippingProperties;
	@Mock private ComputeJobRepository computeJobRepository;
	@Mock private ComputeJobIrisRepository computeJobIrisRepository;
	@Mock private NdviJobRepository ndviJobRepository;

	@InjectMocks
	private DashboadService tested;

	@BeforeEach
	void stubIndicators() {
		when(applicationBusinessProperties.getInseeAnnees()).thenReturn(new Integer[] { 2020, 2021, 2022 });
		when(inseeCarre200mRepository.count()).thenReturn(11L);
		when(filosofil200mRepository.count()).thenReturn(12L);
		when(associationRepository.count()).thenReturn(13L);
		when(contributeurRepository.count()).thenReturn(14L);
		when(communauteCommuneRepository.count()).thenReturn(15L);
		when(cadastreRepository.count()).thenReturn(16L);
		when(parkAreaComputedRepository.count()).thenReturn(17L);
		when(parkAreaRepository.count()).thenReturn(18L);
		when(parkEntranceRepository.count()).thenReturn(19L);
		when(parkJardinRepository.count()).thenReturn(20L);
		when(ndviJobRepository.countByStatus(ComputeJobStatusEnum.TO_PROCESS)).thenReturn(21L);
		when(computeJobIrisRepository.countByStatus(ComputeJobStatusEnum.TO_PROCESS)).thenReturn(22L);
		when(computeJobRepository.countByStatus(ComputeJobStatusEnum.TO_PROCESS)).thenReturn(23L);
	}

	private void stubCache(boolean withComputeJob) {
		stubDashboardCache(DashboardCache.ANNES, 3L);
		stubDashboardCache(DashboardCache.CARREAUX, 11L);
		stubDashboardCache(DashboardCache.FILOSOFIL, 12L);
		stubDashboardCache(DashboardCache.ASSOS, 13L);
		stubDashboardCache(DashboardCache.CONTRIB, 14L);
		stubDashboardCache(DashboardCache.COM2CO, 15L);
		stubDashboardCache(DashboardCache.COMMUNE, 16L);
		stubDashboardCache(DashboardCache.PARC_CALC, 17L);
		stubDashboardCache(DashboardCache.PARC_CHCK, 18L);
		stubDashboardCache(DashboardCache.PARC_ENTREE, 19L);
		stubDashboardCache(DashboardCache.PARC_REF, 20L);
		stubDashboardCache(DashboardCache.NDVI_JOB_PENDING, 21L);
		stubDashboardCache(DashboardCache.IRIS_JOB_PENDING, 22L);
		if (withComputeJob) {
			stubDashboardCache(DashboardCache.COMPUTE_JOB_PENDING, 23L);
		}
	}

	private void stubDashboardCache(String code, Long value) {
		DashboardCache cache = new DashboardCache(code, value);
		when(dashboardCacheRepository.findById(code)).thenReturn(Optional.of(cache));
	}

	@Test
	void refresh_saves_all_indicators_with_compute_job_flag_on() {
		when(featureFlippingProperties.isDashboardCacheComputeJobPending()).thenReturn(true);

		tested.refresh();

		verify(dashboardCacheRepository).saveAll(any());
		verify(dashboardCacheRepository, never()).deleteById(anyString());
		verify(computeJobRepository).countByStatus(ComputeJobStatusEnum.TO_PROCESS);
	}

	@Test
	void refresh_removes_compute_job_indicator_when_flag_off() {
		when(featureFlippingProperties.isDashboardCacheComputeJobPending()).thenReturn(false);

		tested.refresh();

		verify(dashboardCacheRepository).deleteById(DashboardCache.COMPUTE_JOB_PENDING);
		verify(dashboardCacheRepository).saveAll(any());
	}

	@Test
	void getDashboard_reads_cached_indicators_without_refresh_when_count_matches() {
		when(featureFlippingProperties.isDashboardCacheComputeJobPending()).thenReturn(true);
		when(dashboardCacheRepository.count()).thenReturn(14L);
		stubCache(true);

		DashboardSummary summary = tested.getDashboard();

		assertEquals(14L, summary.getNbContributeur());
		assertEquals(13L, summary.getNbAssociation());
		assertEquals(3L, summary.getNbAnnee());
		assertEquals(17L, summary.getNbParcIsochrone());
		assertEquals(23L, summary.getNbComputeJobPending());
		verify(computeJobRepository, never()).countByStatus(any());
	}

	@Test
	void getDashboard_counts_compute_jobs_live_when_flag_off() {
		when(featureFlippingProperties.isDashboardCacheComputeJobPending()).thenReturn(false);
		when(dashboardCacheRepository.count()).thenReturn(13L);
		stubCache(false);

		DashboardSummary summary = tested.getDashboard();

		assertEquals(23L, summary.getNbComputeJobPending());
		verify(computeJobRepository).countByStatus(ComputeJobStatusEnum.TO_PROCESS);
	}

	@Test
	void getDashboard_triggers_refresh_when_cache_is_incomplete() {
		when(featureFlippingProperties.isDashboardCacheComputeJobPending()).thenReturn(true);
		when(dashboardCacheRepository.count()).thenReturn(3L);
		stubCache(true);

		tested.getDashboard();

		verify(dashboardCacheRepository).saveAll(any());
		verify(inseeCarre200mRepository).count();
	}

	@Test
	void getDashboardAndRefresh_refreshes_before_reading() {
		when(featureFlippingProperties.isDashboardCacheComputeJobPending()).thenReturn(true);
		when(dashboardCacheRepository.count()).thenReturn(14L);
		stubCache(true);

		tested.getDashboardAndRefresh();

		verify(dashboardCacheRepository, times(1)).saveAll(any());
	}
}
