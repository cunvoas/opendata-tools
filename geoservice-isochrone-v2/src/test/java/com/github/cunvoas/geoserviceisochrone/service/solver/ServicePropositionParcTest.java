package com.github.cunvoas.geoserviceisochrone.service.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.InseeCarre200mComputedV2;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Filosofil200m;
import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalMeta;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalMetaRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalRepository;
import com.github.cunvoas.geoserviceisochrone.repo.proposal.ParkProposalWorkRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;
import com.github.cunvoas.geoserviceisochrone.service.solver.compute.ProposalComputationTypeAlgo;

/**
 * Tests unitaires pour {@link ServicePropositionParc}.
 * 
 * Stratégie : tous les collaborateurs (repositories, services) sont mockés avec Mockito.
 * Les tests vérifient le comportement de la méthode {@code calculeProposition()} :
 * appels aux mocks attendus, contenu de la Map résultante, gestion des cas limites.
 */
@ExtendWith(MockitoExtension.class)
@Disabled
class ServicePropositionParcTest {

	// ---- Collaborateurs mockés ----

	@Mock
	private ApplicationBusinessProperties applicationBusinessProperties;

	@Mock
	private ServiceOpenData serviceOpenData;

	@Mock
	private InseeCarre200mOnlyShapeRepository inseeCarre200mOnlyShapeRepository;

	@Mock
	private InseeCarre200mComputedV2Repository inseeCarre200mComputedV2Repository;

	@Mock
	private Filosofil200mRepository filosofil200mRepository;

	@Mock
	private ParkProposalWorkRepository parkProposalWorkRepository;

	@Mock
	private ParkProposalRepository parkProposalRepository;

	@Mock
	private ParkProposalMetaRepository parkProposalMetaRepository;

	@InjectMocks
	private ServicePropositionParc service;

	// ---- Constantes de test ----

	private static final GeometryFactory GEO_FACTORY = new GeometryFactory();
	private static final String INSEE = "59350";
	private static final Integer ANNEE = 2019;
	private static final String ID_INSPIRE = "CRS3035RES200mN3100000E3800000";
	private static final Double RECO_URB = 12.0;
	private static final Double MIN_URB = 10.0;
	private static final String OMS_DIST_URB = "300";

	// ---- Helpers ----

	private Point makePoint(double lon, double lat) {
		return GEO_FACTORY.createPoint(new Coordinate(lon, lat));
	}

	private InseeCarre200mOnlyShape makeShape(String idInspire) {
		InseeCarre200mOnlyShape shape = new InseeCarre200mOnlyShape();
		shape.setIdInspire(idInspire);
		shape.setCodeInsee(INSEE);
		shape.setGeoPoint2d(makePoint(3.0, 50.5));
		shape.setWithPop(true);
		return shape;
	}

	private InseeCarre200mComputedV2 makeComputed(String idInspire, double popAll, double surfPcapita, double surfTotal) {
		InseeCarre200mComputedV2 c = new InseeCarre200mComputedV2();
		c.setAnnee(ANNEE);
		c.setIdInspire(idInspire);
		c.setPopAll(BigDecimal.valueOf(popAll));
		c.setSurfaceParkPerCapitaOms(BigDecimal.valueOf(surfPcapita));
		c.setSurfaceTotalParkOms(BigDecimal.valueOf(surfTotal));
		return c;
	}

	private Filosofil200m makeFilo(String idInspire, double nbIndividus) {
		Filosofil200m filo = new Filosofil200m();
		filo.setIdInspire(idInspire);
		filo.setNbIndividus(BigDecimal.valueOf(nbIndividus));
		return filo;
	}

	private ParkProposalMeta makeMeta(Long id) {
		ParkProposalMeta meta = new ParkProposalMeta();
		meta.setId(id);
		meta.setAnnee(ANNEE);
		meta.setInsee(INSEE);
		return meta;
	}

	@BeforeEach
	void setUp() {
		// Valeurs OMS par défaut pour zone urbaine dense
		when(applicationBusinessProperties.getOmsUrbanDistance()).thenReturn(OMS_DIST_URB);
		when(applicationBusinessProperties.getRecoUrbSquareMeterPerCapita()).thenReturn(RECO_URB);
		when(applicationBusinessProperties.getMinUrbSquareMeterPerCapita()).thenReturn(MIN_URB);
	}

	// ========== Cas : aucun carreau trouvé pour la commune ==========

	@Test
	void calculeProposition_aucunCarre_retourneMapVide() {
		// Given
		when(serviceOpenData.isDistanceDense(INSEE)).thenReturn(true);
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(INSEE, true))
				.thenReturn(Collections.emptyList());
		when(parkProposalMetaRepository.findByAnneeAndInseeAndTypeAlgo(ANNEE, INSEE, ProposalComputationTypeAlgo.ITERATIVE_3))
				.thenReturn(makeMeta(1L));

		// When
		var result = service.calculeProposition(INSEE, ANNEE, ProposalComputationTypeAlgo.ITERATIVE_3);

		// Then
		assertNotNull(result, "Le résultat ne doit pas être null");
		assertTrue(result.isEmpty(), "La map doit être vide si aucun carreau n'est disponible");

		// Aucun accès aux données de calcul ne doit avoir eu lieu
		verify(inseeCarre200mComputedV2Repository, never()).findByAnneeAndIdInspire(anyInt(), anyString());
	}

	// ========== Cas : un carreau sans données de calcul ==========

	@Test
	void calculeProposition_carresSansComputed_retourneMapVide() {
		// Given
		when(serviceOpenData.isDistanceDense(INSEE)).thenReturn(true);
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(INSEE, true))
				.thenReturn(List.of(makeShape(ID_INSPIRE)));
		// findByAnneeAndIdInspire retourne vide → le carré est ignoré
		when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(ANNEE, ID_INSPIRE))
				.thenReturn(Optional.empty());
		when(parkProposalMetaRepository.findByAnneeAndInseeAndTypeAlgo(ANNEE, INSEE, ProposalComputationTypeAlgo.ITERATIVE_3))
				.thenReturn(makeMeta(1L));

		// When
		var result = service.calculeProposition(INSEE, ANNEE, ProposalComputationTypeAlgo.ITERATIVE_3);

		// Then
		assertTrue(result.isEmpty(), "La map doit être vide si les données calculées sont absentes");
		verify(filosofil200mRepository, never()).findByAnneeAndIdInspire(anyInt(), anyString());
	}

	// ========== Cas nominal : un carreau avec données complètes ==========

	@Test
	void calculeProposition_unCarreAvecDeficit_retourneEntreeAvecDonnees() {
		// Given
		InseeCarre200mOnlyShape shape = makeShape(ID_INSPIRE);
		// 2 m²/hab → déficit important (reco = 12 m²/hab), pop = 500
		InseeCarre200mComputedV2 computed = makeComputed(ID_INSPIRE, 500.0, 2.0, 1000.0);
		Filosofil200m filo = makeFilo(ID_INSPIRE, 490.0);
		ParkProposalMeta meta = makeMeta(42L);

		when(serviceOpenData.isDistanceDense(INSEE)).thenReturn(true);
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(INSEE, true))
				.thenReturn(List.of(shape));
		when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(ANNEE, ID_INSPIRE))
				.thenReturn(Optional.of(computed));
		when(filosofil200mRepository.findByAnneeAndIdInspire(ANNEE, ID_INSPIRE))
				.thenReturn(filo);
		when(parkProposalMetaRepository.findByAnneeAndInseeAndTypeAlgo(ANNEE, INSEE, ProposalComputationTypeAlgo.ITERATIVE_3))
				.thenReturn(meta);

		// When
		var result = service.calculeProposition(INSEE, ANNEE, ProposalComputationTypeAlgo.ITERATIVE_3);

		// Then
		assertNotNull(result);
		assertEquals(1, result.size(), "Un seul carreau doit être présent dans la map");

		ParkProposalWork work = result.get(ID_INSPIRE);
		assertNotNull(work, "L'entrée du carreau doit exister");
		assertEquals(ANNEE, work.getAnnee());
		assertEquals(ID_INSPIRE, work.getIdInspire());
		assertEquals(BigDecimal.valueOf(2.0), work.getSurfacePerCapita());
		assertEquals(BigDecimal.valueOf(490.0), work.getLocalPopulation());

		// Le déficit = (12 - 2) * 500 = 5000 m²
		assertEquals(0, work.getMissingSurface().compareTo(BigDecimal.valueOf(5000.0)),
				"MissingSurface doit être 5000 m²");
	}

	// ========== Cas : zone non dense → propriétés suburbaines utilisées ==========

	@Test
	void calculeProposition_zoneNonDense_utiliseProprietesSuburbaines() {
		// Given
		when(serviceOpenData.isDistanceDense(INSEE)).thenReturn(false);
		when(applicationBusinessProperties.getOmsSubUrbanDistance()).thenReturn("600");
		when(applicationBusinessProperties.getRecoSubUrbSquareMeterPerCapita()).thenReturn(15.0);
		when(applicationBusinessProperties.getMinSubUrbSquareMeterPerCapita()).thenReturn(10.0);

		InseeCarre200mOnlyShape shape = makeShape(ID_INSPIRE);
		InseeCarre200mComputedV2 computed = makeComputed(ID_INSPIRE, 200.0, 5.0, 1000.0);
		ParkProposalMeta meta = makeMeta(7L);

		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(INSEE, true))
				.thenReturn(List.of(shape));
		when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(ANNEE, ID_INSPIRE))
				.thenReturn(Optional.of(computed));
		when(filosofil200mRepository.findByAnneeAndIdInspire(ANNEE, ID_INSPIRE))
				.thenReturn(null);
		when(parkProposalMetaRepository.findByAnneeAndInseeAndTypeAlgo(ANNEE, INSEE, ProposalComputationTypeAlgo.ITERATIVE_3))
				.thenReturn(meta);

		// When
		var result = service.calculeProposition(INSEE, ANNEE, ProposalComputationTypeAlgo.ITERATIVE_3);

		// Then – les propriétés suburbaines ont bien été consultées
		verify(applicationBusinessProperties, times(1)).getOmsSubUrbanDistance();
		verify(applicationBusinessProperties, times(1)).getRecoSubUrbSquareMeterPerCapita();
		verify(applicationBusinessProperties, times(1)).getMinSubUrbSquareMeterPerCapita();
		verify(applicationBusinessProperties, never()).getOmsUrbanDistance();

		assertEquals(1, result.size());
		// Filosoil absent → localPopulation doit être 0
		assertEquals(BigDecimal.ZERO, result.get(ID_INSPIRE).getLocalPopulation());
	}

	// ========== Cas : meta inexistante → création et sauvegarde ==========

	@Test
	void calculeProposition_metaInexistante_creeEtSauvegardeMeta() {
		// Given
		when(serviceOpenData.isDistanceDense(INSEE)).thenReturn(true);
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(INSEE, true))
				.thenReturn(Collections.emptyList());
		// Aucune meta existante
		when(parkProposalMetaRepository.findByAnneeAndInseeAndTypeAlgo(ANNEE, INSEE, ProposalComputationTypeAlgo.ITERATIVE_3))
				.thenReturn(null);
		when(parkProposalMetaRepository.save(any(ParkProposalMeta.class)))
				.thenAnswer(inv -> {
					ParkProposalMeta m = inv.getArgument(0);
					m.setId(99L);
					return m;
				});

		// When
		service.calculeProposition(INSEE, ANNEE, ProposalComputationTypeAlgo.ITERATIVE_3);

		// Then – la meta doit avoir été sauvegardée une fois (création)
		verify(parkProposalMetaRepository, times(1)).save(any(ParkProposalMeta.class));
	}

	// ========== Cas : meta existante → pas de création ==========

	@Test
	void calculeProposition_metaExistante_neCreePassDeMeta() {
		// Given
		when(serviceOpenData.isDistanceDense(INSEE)).thenReturn(true);
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(INSEE, true))
				.thenReturn(Collections.emptyList());
		when(parkProposalMetaRepository.findByAnneeAndInseeAndTypeAlgo(ANNEE, INSEE, ProposalComputationTypeAlgo.ITERATIVE_3))
				.thenReturn(makeMeta(5L));

		// When
		service.calculeProposition(INSEE, ANNEE, ProposalComputationTypeAlgo.ITERATIVE_3);

		// Then – save ne doit pas être appelé (pas de nouvelles propositions, meta déjà existante)
		verify(parkProposalMetaRepository, never()).save(any(ParkProposalMeta.class));
	}

	// ========== Cas : carreau sans population → missingSurface = 0 ==========

	@Test
	void calculeProposition_carrePopulationZero_missingSurfaceEstZero() {
		// Given
		InseeCarre200mOnlyShape shape = makeShape(ID_INSPIRE);
		InseeCarre200mComputedV2 computed = makeComputed(ID_INSPIRE, 0.0, 0.0, 0.0);
		// popAll = 0 → déficit = max(12-0,0) * 0 = 0

		when(serviceOpenData.isDistanceDense(INSEE)).thenReturn(true);
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(INSEE, true))
				.thenReturn(List.of(shape));
		when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(ANNEE, ID_INSPIRE))
				.thenReturn(Optional.of(computed));
		when(filosofil200mRepository.findByAnneeAndIdInspire(ANNEE, ID_INSPIRE))
				.thenReturn(null);
		when(parkProposalMetaRepository.findByAnneeAndInseeAndTypeAlgo(ANNEE, INSEE, ProposalComputationTypeAlgo.ITERATIVE_3))
				.thenReturn(makeMeta(1L));

		// When
		var result = service.calculeProposition(INSEE, ANNEE, ProposalComputationTypeAlgo.ITERATIVE_3);

		// Then
		ParkProposalWork work = result.get(ID_INSPIRE);
		assertNotNull(work);
		// (12 - 0) * 0 = 0
		assertEquals(0, work.getMissingSurface().compareTo(BigDecimal.ZERO),
				"MissingSurface doit être 0 quand la population est 0");
	}

	// ========== Cas : plusieurs carrés ==========

	@Test
	void calculeProposition_plusieursCarres_tousPresentsInResultat() {
		// Given
		String id2 = "CRS3035RES200mN3100200E3800000";

		InseeCarre200mOnlyShape shape1 = makeShape(ID_INSPIRE);
		InseeCarre200mOnlyShape shape2 = makeShape(id2);
		shape2.setGeoPoint2d(makePoint(3.002, 50.502));

		InseeCarre200mComputedV2 computed1 = makeComputed(ID_INSPIRE, 300.0, 4.0, 1200.0);
		InseeCarre200mComputedV2 computed2 = makeComputed(id2, 100.0, 8.0, 800.0);

		ParkProposalMeta meta = makeMeta(10L);

		when(serviceOpenData.isDistanceDense(INSEE)).thenReturn(true);
		when(inseeCarre200mOnlyShapeRepository.findCarreByInseeCode(INSEE, true))
				.thenReturn(List.of(shape1, shape2));
		when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(ANNEE, ID_INSPIRE))
				.thenReturn(Optional.of(computed1));
		when(inseeCarre200mComputedV2Repository.findByAnneeAndIdInspire(ANNEE, id2))
				.thenReturn(Optional.of(computed2));
		when(filosofil200mRepository.findByAnneeAndIdInspire(eq(ANNEE), anyString()))
				.thenReturn(null);
		when(parkProposalMetaRepository.findByAnneeAndInseeAndTypeAlgo(ANNEE, INSEE, ProposalComputationTypeAlgo.ITERATIVE_3))
				.thenReturn(meta);

		// When
		var result = service.calculeProposition(INSEE, ANNEE, ProposalComputationTypeAlgo.ITERATIVE_3);

		// Then
		assertEquals(2, result.size(), "Les deux carrés doivent être dans la map");
		assertTrue(result.containsKey(ID_INSPIRE));
		assertTrue(result.containsKey(id2));
	}

	// ========== saveProposals ==========

	@Test
	void saveProposals_listNonVide_appeleRepository() {
		// Given
		ParkProposalWork work = new ParkProposalWork();
		work.setAnnee(ANNEE);
		work.setIdInspire(ID_INSPIRE);

		// When
		service.saveProposals(java.util.Map.of(ID_INSPIRE, work));

		// Then
		verify(parkProposalWorkRepository, times(1)).saveAll(any());
	}

	@Test
	void saveProposals_mapVide_nAppelePasRepository() {
		// When
		service.saveProposals(Collections.emptyMap());

		// Then
		verify(parkProposalWorkRepository, never()).saveAll(any());
	}

	@Test
	void saveProposals_listeProposals_appeleRepository() {
		// Given
		ParkProposal proposal = new ParkProposal();
		proposal.setAnnee(ANNEE);
		proposal.setIdInspire(ID_INSPIRE);

		// When
		service.saveProposals(List.of(proposal));

		// Then
		verify(parkProposalRepository, times(1)).saveAll(any());
	}
}
