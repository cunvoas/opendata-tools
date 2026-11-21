package com.github.cunvoas.geoserviceisochrone.service.project;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulator;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorWork;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorlIsochroneRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ProjectSimulatorlWorkRepository;
import com.github.cunvoas.geoserviceisochrone.service.entrance.ServiceReadReferences;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestProjectSimulatorService {

	@Mock private ApplicationBusinessProperties applicationBusinessProperties;
	@Mock private InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;
	@Mock private ServiceReadReferences serviceReadReferences;
	@Mock private ProjectSimulatorRepository projectSimulatorRepository;
	@Mock private ProjectSimulatorlWorkRepository projectSimulatorlWorkRepository;
	@Mock private ProjectSimulatorlIsochroneRepository projectSimulatorlIsochroneRepository;
	@Mock private CarreRechercheStrategyFactory carreRechercheStrategyFactory;
	@Mock private CarreRechercheStrategy strategy;

	@InjectMocks
	private ProjectSimulatorService tested;

	private static ProjectSimulator project() {
		ProjectSimulator p = new ProjectSimulator();
		p.setId(5L);
		p.setIdCommune(9L);
		p.setSurfacePark(BigDecimal.valueOf(1000));
		return p;
	}

	private static City city(String insee) {
		City c = new City();
		c.setInseeCode(insee);
		return c;
	}

	private static InseeCarre200mOnlyShape carre(String idInspire) {
		InseeCarre200mOnlyShape c = new InseeCarre200mOnlyShape();
		c.setIdInspire(idInspire);
		return c;
	}

	private static InseeCarre200mComputedV2 computed(String idInspire, double surfaceOms, double pop) {
		InseeCarre200mComputedV2 c = new InseeCarre200mComputedV2();
		c.setIdInspire(idInspire);
		c.setSurfaceParkPerCapitaOms(BigDecimal.valueOf(surfaceOms));
		c.setSurfaceTotalParkOms(BigDecimal.valueOf(2));
		c.setPopulationInIsochroneOms(BigDecimal.valueOf(pop));
		c.setPopAll(BigDecimal.valueOf(pop));
		return c;
	}

	@Test
	void getById_returns_null_when_unknown() {
		when(projectSimulatorRepository.findById(1L)).thenReturn(Optional.empty());

		assertNull(tested.getById(1L));
	}

	@Test
	void findByCity_returns_empty_list_without_commune() {
		assertTrue(tested.findByCity(null).isEmpty());
		verify(projectSimulatorRepository, never()).findByIdCommune(any());
	}

	@Test
	void findByCity_delegates_to_repository() {
		List<ProjectSimulator> list = List.of(project());
		when(projectSimulatorRepository.findByIdCommune(9L)).thenReturn(list);

		assertEquals(list, tested.findByCity(9L));
	}

	@Test
	void save_sets_project_id_on_each_work() {
		ProjectSimulator p = project();
		ProjectSimulatorWork w1 = new ProjectSimulatorWork();
		ProjectSimulatorWork w2 = new ProjectSimulatorWork();
		when(projectSimulatorRepository.save(p)).thenReturn(p);

		tested.save(p, List.of(w1, w2));

		assertEquals(5L, w1.getIdProjectSimulator());
		assertEquals(5L, w2.getIdProjectSimulator());
		verify(projectSimulatorlWorkRepository).saveAll(List.of(w1, w2));
	}

	@Test
	void save_without_items_does_not_write_works() {
		ProjectSimulator p = project();
		when(projectSimulatorRepository.save(p)).thenReturn(p);

		assertSame(p, tested.save(p, null));
		verify(projectSimulatorlWorkRepository, never()).saveAll(any());
	}

	@Test
	void delete_removes_project_and_returns_remaining_simulations() {
		List<ProjectSimulator> remaining = List.of(project());
		when(projectSimulatorRepository.findByIdCommune(9L)).thenReturn(remaining);

		assertEquals(remaining, tested.delete(9L, 5L));
		verify(projectSimulatorlWorkRepository).deleteByIdProjectSimulator(5L);
		verify(projectSimulatorRepository).deleteById(5L);
	}

	@Test
	void delete_without_commune_returns_empty_list() {
		assertTrue(tested.delete(null, 5L).isEmpty());
	}

	@Test
	void simulate_computes_new_surface_and_missing_surface_for_each_carre() {
		ProjectSimulator p = project();
		when(serviceReadReferences.getCity(9L)).thenReturn(city("69123"));
		when(serviceReadReferences.isCityDense(9L)).thenReturn(true);
		when(applicationBusinessProperties.getOmsUrbanDistance()).thenReturn("300");
		when(applicationBusinessProperties.getRecoUrbSquareMeterPerCapita()).thenReturn(10d);
		when(applicationBusinessProperties.getDerniereAnnee()).thenReturn(2024);
		when(carreRechercheStrategyFactory.getStrategy(any())).thenReturn(strategy);

		InseeCarre200mOnlyShape shape = carre("INSPIRE-1");
		when(strategy.findCarres(p, 300)).thenReturn(Set.of(shape));
		when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(2024, "INSPIRE-1"))
				.thenReturn(Optional.of(computed("INSPIRE-1", 4, 100)));
		when(projectSimulatorRepository.save(any(ProjectSimulator.class)))
				.thenAnswer(inv -> inv.getArgument(0));
		when(projectSimulatorlWorkRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

		ProjectSimulator ret = tested.simulate(p);

		assertEquals(2024, ret.getAnnee());
		assertEquals("69123", ret.getInsee());
		assertEquals(Boolean.TRUE, ret.getIsDense());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<ProjectSimulatorWork>> captor = ArgumentCaptor.forClass(List.class);
		verify(projectSimulatorlWorkRepository).saveAll(captor.capture());

		ProjectSimulatorWork work = captor.getValue().get(0);
		assertEquals(0, work.getNewSurface().compareTo(BigDecimal.valueOf(1002)));
		assertEquals(0, work.getNewSurfacePerCapita().compareTo(BigDecimal.TEN));
		// manque = (10 - 10) * 100 = 0
		assertEquals(0, work.getNewMissingSurface().compareTo(BigDecimal.ZERO));
	}

	@Test
	void populate_exposes_recommended_surface_per_capita() {
		ProjectSimulator p = project();
		p.setAnnee(2024);
		p.setInsee("69123");
		p.setIsDense(true);
		when(carreRechercheStrategyFactory.getStrategy(any())).thenReturn(strategy);
		when(strategy.findCarres(p, 300)).thenReturn(Set.of(carre("I1")));
		when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(2024, "I1"))
				.thenReturn(Optional.of(computed("I1", 4, 100)));

		ProjectSimulatorWork work = tested.populate(p, 300, 10d).get("I1");

		assertEquals(0, work.getSurfacePerCapita().compareTo(BigDecimal.valueOf(4)));
		assertEquals(0, work.getAccessingSurface().compareTo(BigDecimal.valueOf(2)));
		assertEquals(0, work.getAccessingPopulation().compareTo(BigDecimal.valueOf(100)));
		// manque = (10 - 4) * 100 = 600
		assertEquals(0, work.getMissingSurface().compareTo(BigDecimal.valueOf(600)));
		assertEquals(5L, work.getIdProjectSimulator());
	}

	@Test
	void populate_skips_carres_without_computed_data() {
		ProjectSimulator p = project();
		p.setAnnee(2024);
		when(carreRechercheStrategyFactory.getStrategy(any())).thenReturn(strategy);
		when(strategy.findCarres(p, 300)).thenReturn(Set.of(carre("I1")));
		when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(2024, "I1")).thenReturn(Optional.empty());

		assertTrue(tested.populate(p, 300, 10d).isEmpty());
	}

	@Test
	void getSurface_delegates_to_repository() {
		org.locationtech.jts.geom.Geometry geom = new org.locationtech.jts.geom.GeometryFactory().createPoint();
		when(projectSimulatorRepository.getSurface(geom)).thenReturn(123L);

		assertEquals(123L, tested.getSurface(geom));
	}
}
