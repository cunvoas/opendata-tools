package com.github.cunvoas.geoserviceisochrone.service.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonRoot;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualEvalWork;
import com.github.cunvoas.geoserviceisochrone.model.proposal.manual.ManualParkProposalMeta;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.manual.ManualEvalWorkRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.manual.ManualParkProposalMetaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManualProposalEvaluationServiceTest {

	@Mock private ApplicationBusinessProperties applicationBusinessProperties;
	@Mock private ServiceOpenData serviceOpenData;
	@Mock private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;
	@Mock private InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;
	@Mock private ManualProposalService manualProposalService;
	@Mock private ManualParkProposalMetaRepository manualParkProposalMetaRepository;
	@Mock private ManualEvalWorkRepository manualEvalWorkRepository;

	@InjectMocks
	private ManualProposalEvaluationService tested;

	private static ManualParkProposalMeta meta(long id) {
		ManualParkProposalMeta meta = new ManualParkProposalMeta();
		meta.setId(id);
		meta.setAnnee(2024);
		meta.setInsee("69123");
		return meta;
	}

	@Test
	void evaluate_without_year_returns_empty_root() {
		GeoJsonRoot root = tested.evaluate("69123", null);

		assertNotNull(root);
		assertTrue(root.getFeatures().isEmpty());
	}

	@Test
	void evaluate_without_carreaux_returns_empty_root() {
		when(serviceOpenData.isDistanceDense("69123")).thenReturn(true);
		when(applicationBusinessProperties.getOmsUrbanDistance()).thenReturn("300");
		when(applicationBusinessProperties.getRecoUrbSquareMeterPerCapita()).thenReturn(12d);
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode("69123", true)).thenReturn(List.of());

		assertTrue(tested.evaluate("69123", 2024).getFeatures().isEmpty());
	}

	@Test
	void evaluate_with_null_carreaux_returns_empty_root() {
		when(serviceOpenData.isDistanceDense("69123")).thenReturn(false);
		when(applicationBusinessProperties.getOmsSubUrbanDistance()).thenReturn("1200");
		when(applicationBusinessProperties.getRecoSubUrbSquareMeterPerCapita()).thenReturn(45d);
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode("69123", true)).thenReturn(null);

		assertTrue(tested.evaluate("69123", 2024).getFeatures().isEmpty());
	}

	@Test
	void load_evaluation_without_year_returns_empty_root() {
		assertTrue(tested.loadEvaluation("69123", null).getFeatures().isEmpty());
	}

	@Test
	void load_evaluation_without_meta_returns_empty_root() {
		when(manualParkProposalMetaRepository.findByAnneeAndInsee(2024, "69123")).thenReturn(null);

		assertTrue(tested.loadEvaluation("69123", 2024).getFeatures().isEmpty());
	}

	@Test
	void load_evaluation_without_works_returns_empty_root() {
		when(manualParkProposalMetaRepository.findByAnneeAndInsee(2024, "69123")).thenReturn(meta(1L));
		when(manualEvalWorkRepository.findByIdMetaOrderByIdInspire(1L)).thenReturn(List.of());

		assertTrue(tested.loadEvaluation("69123", 2024).getFeatures().isEmpty());
	}

	@Test
	void load_evaluation_builds_features_for_existing_shapes() {
		when(manualParkProposalMetaRepository.findByAnneeAndInsee(2024, "69123")).thenReturn(meta(1L));
		ManualEvalWork work = new ManualEvalWork();
		work.setIdInspire("I1");
		work.setIsDense(Boolean.TRUE);
		work.setLocalPopulation(java.math.BigDecimal.TEN);
		work.setAccessingPopulation(java.math.BigDecimal.TEN);
		work.setAccessingSurface(java.math.BigDecimal.TEN);
		work.setSurfacePerCapita(java.math.BigDecimal.ONE);
		work.setMissingSurface(java.math.BigDecimal.ONE);
		work.setNewAccessingSurface(java.math.BigDecimal.TEN);
		work.setNewSurfacePerCapita(java.math.BigDecimal.ONE);
		work.setNewMissingSurface(java.math.BigDecimal.ONE);
		when(manualEvalWorkRepository.findByIdMetaOrderByIdInspire(1L)).thenReturn(List.of(work));

		InseeCarre200mOnlyShape shape = new InseeCarre200mOnlyShape();
		shape.setIdInspire("I1");
		shape.setGeoShape(new org.locationtech.jts.geom.GeometryFactory().createPolygon());
		when(inseeCarre200mOnlyShapeRepository.findById("I1")).thenReturn(Optional.of(shape));

		GeoJsonRoot root = tested.loadEvaluation("69123", 2024);

		assertEquals(1, root.getFeatures().size());
		assertNotNull(root.getFeatures().get(0).getProperties());
	}

	@Test
	void load_evaluation_skips_shapes_not_found() {
		when(manualParkProposalMetaRepository.findByAnneeAndInsee(2024, "69123")).thenReturn(meta(1L));
		ManualEvalWork work = new ManualEvalWork();
		work.setIdInspire("I1");
		when(manualEvalWorkRepository.findByIdMetaOrderByIdInspire(1L)).thenReturn(List.of(work));
		when(inseeCarre200mOnlyShapeRepository.findById("I1")).thenReturn(Optional.empty());

		assertTrue(tested.loadEvaluation("69123", 2024).getFeatures().isEmpty());
	}
}
