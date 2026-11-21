package com.github.cunvoas.geoserviceisochrone.service.aop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.aspectj.lang.JoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.cunvoas.geoserviceisochrone.controller.form.FormParkEntranceDetail;
import com.github.cunvoas.geoserviceisochrone.model.admin.ContributeurAction;
import com.github.cunvoas.geoserviceisochrone.service.dashboard.ActivityTrackerService;

@ExtendWith(MockitoExtension.class)
class TestAopSaveTracker {

	@Mock
	private ActivityTrackerService activityTrackerService;
	@Mock
	private JoinPoint joinPoint;

	@InjectMocks
	private AopSaveTracker tested;

	@Test
	void admin_save_is_tracked() {
		tested.trackMethodAfterAdmin(joinPoint);

		verify(activityTrackerService).incrementNbAdminActivity(any());
	}

	@Test
	void park_save_is_tracked() {
		tested.trackMethodAfterPark(joinPoint);

		verify(activityTrackerService).incrementNbParkActivity(any());
	}

	@Test
	void entrance_save_is_tracked() {
		tested.trackMethodAfterEntrance(joinPoint);

		verify(activityTrackerService).incrementNbEntranceActivity(any());
	}

	@Test
	void isochrone_merge_is_tracked() {
		tested.trackMethodAfterIsochrone(joinPoint);

		verify(activityTrackerService).incrementNbIsochroneActivity(any());
	}

	@Test
	void new_entrance_is_counted_as_add() {
		FormParkEntranceDetail form = new FormParkEntranceDetail();
		org.mockito.Mockito.when(joinPoint.getArgs()).thenReturn(new Object[] { form });

		tested.trackMethodAfterEntrance(joinPoint);

		ArgumentCaptor<ContributeurAction> captor = ArgumentCaptor.forClass(ContributeurAction.class);
		verify(activityTrackerService).incrementNbEntranceActivity(captor.capture());
		assertEquals(1L, captor.getValue().getNbEntranceAdd());
		assertEquals(0L, captor.getValue().getNbEntranceUpd());
	}

	@Test
	void existing_entrance_is_counted_as_update() {
		FormParkEntranceDetail form = new FormParkEntranceDetail();
		form.setEntranceId(12L);
		org.mockito.Mockito.when(joinPoint.getArgs()).thenReturn(new Object[] { form });

		tested.trackMethodAfterEntrance(joinPoint);

		ArgumentCaptor<ContributeurAction> captor = ArgumentCaptor.forClass(ContributeurAction.class);
		verify(activityTrackerService).incrementNbEntranceActivity(captor.capture());
		assertEquals(1L, captor.getValue().getNbEntranceUpd());
		assertEquals(0L, captor.getValue().getNbEntranceAdd());
	}
}
