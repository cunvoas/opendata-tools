package com.github.cunvoas.geoserviceisochrone.service.dashboard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurAction;
import com.github.cunvoas.geoserviceisochrone.model.admin.Stats;
import com.github.cunvoas.geoserviceisochrone.model.dashboard.DashboardLive;
import com.github.cunvoas.geoserviceisochrone.repo.admin.ContributeurActionRepository;
import com.github.cunvoas.geoserviceisochrone.repo.admin.StatsRepository;

@ExtendWith(MockitoExtension.class)
class ActivityTrackerServiceTest {

	@Mock
	private StatsRepository statsRepository;
	@Mock
	private ContributeurActionRepository contributeurActionRepository;

	@InjectMocks
	private ActivityTrackerService tested;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	private static ContributeurAction action() {
		ContributeurAction ca = new ContributeurAction();
		ca.setIdContributor(7L);
		ca.setNbParkAdd(1L);
		return ca;
	}

	private static void logIn(Long id) {
		Contributeur c = new Contributeur();
		c.setId(id);
		c.setLogin("u" + id);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(c, null, java.util.List.of()));
	}

	@Test
	void unique_user_is_counted_once_per_hour_segment() {
		tested.incrementUniqueUser(1L);
		tested.incrementUniqueUser(1L);
		tested.incrementUniqueUser(2L);

		assertEquals(2, tested.getActivityOfDay().getNbUniqueUser());
	}

	@Test
	void change_hour_keeps_past_segment_and_resets_the_new_one() {
		tested.incrementUniqueUser(1L);
		tested.changeHour();

		assertEquals(0, tested.getActivityByHour().get(0).getNbUniqueUser());
		assertEquals(1, tested.getActivityOfDay().getNbUniqueUser());
	}

	@Test
	void change_day_forgets_registered_users() {
		tested.incrementUniqueUser(1L);
		tested.changeDay();
		tested.incrementUniqueUser(1L);

		assertEquals(1, tested.getActivityOfDay().getNbUniqueUser());
	}

	@Test
	void park_activity_is_aggregated_and_persisted() {
		logIn(7L);
		when(contributeurActionRepository.findById(7L)).thenReturn(Optional.empty());
		when(contributeurActionRepository.save(any(ContributeurAction.class)))
				.thenAnswer(inv -> inv.getArgument(0));

		tested.incrementNbParkActivity(action());

		assertEquals(1, tested.getActivityOfDay().getNbParcActivity());
		verify(statsRepository).save(any(Stats.class));
		verify(contributeurActionRepository).save(any(ContributeurAction.class));
	}

	@Test
	void activity_is_not_persisted_when_no_user_is_logged() {
		tested.incrementNbEntranceActivity(action());

		assertEquals(1, tested.getActivityOfDay().getNbEntranceActivity());
		verify(statsRepository, never()).save(any());
		verify(contributeurActionRepository, never()).save(any());
	}

	@Test
	void existing_action_row_is_merged_instead_of_created() {
		logIn(7L);
		ContributeurAction existing = action();
		existing.setFirstDate(new java.util.Date());
		when(contributeurActionRepository.findById(7L)).thenReturn(Optional.of(existing));
		when(contributeurActionRepository.save(existing)).thenReturn(existing);

		tested.incrementNbAdminActivity(action());

		verify(contributeurActionRepository).save(existing);
		assertEquals(1, tested.getActivityByHour().get(0).getNbAdminActivity());
	}

	@Test
	void get_activity_reads_last_ten_days() {
		List<ContributeurAction> list = List.of(action());
		when(contributeurActionRepository.findLastDays(10)).thenReturn(list);

		assertEquals(list, tested.getActivity());
	}

	@Test
	void activity_of_day_starts_empty_with_one_segment_per_hour() {
		DashboardLive live = tested.getActivityOfDay();

		assertEquals(24, tested.getActivityByHour().size());
		assertEquals(0, live.getNbUniqueUser());
	}
}
