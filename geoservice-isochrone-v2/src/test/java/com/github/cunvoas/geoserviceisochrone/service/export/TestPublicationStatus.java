package com.github.cunvoas.geoserviceisochrone.service.export;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class TestPublicationStatus {

	@Test
	void progressTracksStepAndCity() {
		PublicationStatus status = status();
		status.startStep("stats-cities");
		status.city(5, 10, "59350", "Lille");

		assertEquals("stats-cities", status.getCurrentStepCode());
		assertEquals(2, status.getCurrentStepIndex());
		assertEquals(2, status.getCurrentStepTotal());
		assertEquals(5, status.getCityIndex());
		assertEquals(10, status.getCityTotal());
		assertEquals("Lille", status.getCityName());
	}

	@Test
	void etaIsComputedOnlyAfterEnoughProgress() {
		PublicationStatus status = status();
		status.startStep("referentiel");
		status.endStep("referentiel");
		status.startStep("stats-cities");
		status.city(5, 10, "59350", "Lille");

		assertNotNull(status.getEtaSeconds());
		assertTrue(status.getEtaSeconds() >= 0);
	}

	@Test
	void missingExpectedFileIsAProductionError() {
		PublicationStatus status = status();
		status.setFilesExpected(2);
		status.addFile("stats", "/tmp/a.json", true, 1, LocalDateTime.now(), null);
		status.addFile("stats", "/tmp/b.json", false, 0, null, null);

		assertTrue(status.hasProductionError());
		assertEquals(1, status.getFilesProduced());
		assertEquals(2, status.getFilesExpected());
	}

	@Test
	void copyDoesNotShareMutableCollections() {
		PublicationStatus status = status();
		status.startStep("referentiel");
		status.objects("villes", 2);

		PublicationStatus copy = status.copy();
		copy.getSteps().get(0).setState(PublicationStatus.StepState.DONE);
		copy.getObjects().put("villes", 99L);

		assertEquals(PublicationStatus.StepState.RUNNING, status.getSteps().get(0).getState());
		assertEquals(2L, status.getObjects().get("villes"));
		assertFalse(copy.getSteps().isEmpty());
	}

	private PublicationStatus status() {
		PublicationStatus status = new PublicationStatus();
		status.setStartedAt(LocalDateTime.now().minusMinutes(10));
		status.setSteps(new ArrayList<>(List.of(
				new PublicationStatus.StepStatus("referentiel", "Référentiel", 50),
				new PublicationStatus.StepStatus("stats-cities", "Stats par ville", 50))));
		return status;
	}
}
