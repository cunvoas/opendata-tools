package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.github.cunvoas.geoserviceisochrone.controller.geojson.NdviController.NdviSummary;
import com.github.cunvoas.geoserviceisochrone.repo.NdviResultRepository.NdviSummaryProjection;

class NdviControllerSummaryTest {

	/** Projection factice : un parc urbain au couvert clairsemé. */
	private static class FakeProjection implements NdviSummaryProjection {
		private final Integer sampleCount;

		FakeProjection(Integer sampleCount) {
			this.sampleCount = sampleCount;
		}

		@Override public Long getParkId() { return 7L; }
		@Override public String getName() { return "Square"; }
		@Override public BigDecimal getSurface() { return new BigDecimal("1200"); }
		@Override public BigDecimal getNdviMean() { return new BigDecimal("0.50"); }
		@Override public BigDecimal getNdviMin() { return new BigDecimal("0.20"); }
		@Override public BigDecimal getNdviMax() { return new BigDecimal("0.80"); }
		@Override public BigDecimal getNdviStddev() { return new BigDecimal("0.02"); }
		@Override public BigDecimal getNdreMean() { return new BigDecimal("0.30"); }
		@Override public BigDecimal getNdreMin() { return new BigDecimal("0.10"); }
		@Override public BigDecimal getNdreMax() { return new BigDecimal("0.50"); }
		@Override public BigDecimal getNdreStddev() { return new BigDecimal("0.01"); }
		@Override public BigDecimal getGndviMean() { return new BigDecimal("0.45"); }
		@Override public BigDecimal getGndviMin() { return new BigDecimal("0.20"); }
		@Override public BigDecimal getGndviMax() { return new BigDecimal("0.70"); }
		@Override public BigDecimal getGndviStddev() { return new BigDecimal("0.01"); }
		@Override public BigDecimal getSaviMean() { return new BigDecimal("0.30"); }
		@Override public BigDecimal getSaviMin() { return new BigDecimal("0.10"); }
		@Override public BigDecimal getSaviMax() { return new BigDecimal("0.50"); }
		@Override public BigDecimal getSaviStddev() { return new BigDecimal("0.02"); }
		@Override public Integer getSampleCount() { return sampleCount; }
		@Override public Date getAcquisitionDate() { return new Date(3000L); }
		@Override public Integer getAnnee() { return 2021; }
	}

	@Test
	void le_resume_expose_les_quatre_indices_et_leur_evaluation() {
		NdviSummary summary = NdviController.toSummary(new FakeProjection(120));

		assertEquals(new BigDecimal("0.50"), summary.ndviMean());
		assertEquals(new BigDecimal("0.30"), summary.ndreMean());
		assertEquals(new BigDecimal("0.45"), summary.gndviMean());
		assertEquals(new BigDecimal("0.30"), summary.saviMean());
		assertEquals(new BigDecimal("0.02"), summary.saviStddev());

		assertNotNull(summary.evaluation());
		assertEquals("Couvert faible / Pelouse ouverte ou arbustif jeune", summary.evaluation().recouvrement());
		assertEquals(36.6, summary.evaluation().scoreSante());
		assertEquals("Élevée", summary.evaluation().confiance());
	}

	@Test
	void l_evaluation_ne_plante_pas_sans_indices() {
		NdviSummary summary = NdviController.toSummary(new FakeProjection(null) {
			@Override public BigDecimal getNdviMean() { return null; }
			@Override public BigDecimal getNdviMin() { return null; }
			@Override public BigDecimal getNdviMax() { return null; }
			@Override public BigDecimal getNdviStddev() { return null; }
			@Override public BigDecimal getNdreMean() { return null; }
			@Override public BigDecimal getNdreMin() { return null; }
			@Override public BigDecimal getNdreMax() { return null; }
			@Override public BigDecimal getNdreStddev() { return null; }
			@Override public BigDecimal getGndviMean() { return null; }
			@Override public BigDecimal getGndviMin() { return null; }
			@Override public BigDecimal getGndviMax() { return null; }
			@Override public BigDecimal getGndviStddev() { return null; }
			@Override public BigDecimal getSaviMean() { return null; }
			@Override public BigDecimal getSaviMin() { return null; }
			@Override public BigDecimal getSaviMax() { return null; }
			@Override public BigDecimal getSaviStddev() { return null; }
		});

		assertNotNull(summary.evaluation());
		assertEquals("Nulle", summary.evaluation().confiance());
		assertNull(summary.evaluation().scoreSante());
	}
}
