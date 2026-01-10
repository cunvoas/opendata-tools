package com.github.cunvoas.geoserviceisochrone.service.solver;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.cunvoas.geoserviceisochrone.config.property.ApplicationBusinessProperties;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ProjectSimulatorWork;
import com.github.cunvoas.geoserviceisochrone.repo.InseeCarre200mComputedV2Repository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.Filosofil200mRepository;
import com.github.cunvoas.geoserviceisochrone.repo.reference.InseeCarre200mOnlyShapeRepository;
import com.github.cunvoas.geoserviceisochrone.service.opendata.ServiceOpenData;

/**
 * Tests unitaires pour {@link ServicePropositionParc}
 * 
 * @author github.com/cunvoas
 */
@ExtendWith(MockitoExtension.class)
class ServicePropositionParcTest {

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
	
	@InjectMocks
	private ServicePropositionParc service;
	
	private GeometryFactory geometryFactory;
	
	@BeforeEach
	void setUp() {
		geometryFactory = new GeometryFactory();
	}
	
	/**
	 * Crée un ParkProposal de test avec les données fournies.
	 */
	private ProjectSimulatorWork createParkProposal(
			String idInspire, 
			double longitude, 
			double latitude,
			double surfacePerCapita,
			double missingSurface,
			int accessingPopulation,
			double accessingSurface) {
		
		ProjectSimulatorWork proposal = new ProjectSimulatorWork();
		proposal.setAnnee(2023);
		proposal.setIdInspire(idInspire);
		proposal.setIsDense(true);
		
		Point centre = geometryFactory.createPoint(new Coordinate(longitude, latitude));
		proposal.setCentre(centre);
		
		proposal.setSurfacePerCapita(BigDecimal.valueOf(surfacePerCapita));
		proposal.setMissingSurface(BigDecimal.valueOf(missingSurface));
		proposal.setAccessingPopulation(BigDecimal.valueOf(accessingPopulation));
		proposal.setAccessingSurface(BigDecimal.valueOf(accessingSurface));
		proposal.setLocalPopulation(BigDecimal.valueOf(100));
		proposal.setNewSurface(BigDecimal.ZERO);
		
		return proposal;
	}
	
	@Test
	void testCalculeEtapeProposition_avecDeficitImportant_ajouteParc() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		// Carré avec déficit important (2 m²/hab au lieu de 12)
		ProjectSimulatorWork carre1 = createParkProposal(
				"CRS3035RES200mN2000000E3000000", 
				3.0, 50.0,
				2.0,  // surfacePerCapita actuelle
				10000.0, // missingSurface
				1000, // accessingPopulation
				2000.0 // accessingSurface
		);
		carreMap.put(carre1.getIdInspire(), carre1);
		
		// Carrés voisins
		ProjectSimulatorWork voisin1 = createParkProposal(
				"CRS3035RES200mN2000200E3000000",
				3.001, 50.001,
				8.0,
				4000.0,
				500,
				4000.0
		);
		carreMap.put(voisin1.getIdInspire(), voisin1);
		
		Double minSquareMeterPerCapita = 8.0;
		Double recoSquareMeterPerCapita = 12.0;
		Integer urbanDistance = 300;
		
		// When
		service.calculeEtapeProposition(carreMap, minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
		
		// Then
		assertNotNull(carre1.getNewSurface());
		assertTrue(carre1.getNewSurface().doubleValue() >= 1000.0, 
				"La surface ajoutée devrait être >= 1000 m²");
		
		// Vérifier que la surface per capita a été mise à jour
		assertTrue(carre1.getSurfacePerCapita().doubleValue() > 2.0,
				"La surface per capita devrait avoir augmenté");
	}
	
	@Test
	void testCalculeEtapeProposition_avecDeficitFaible_nAjoutePasDeParc() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		// Carré avec déficit faible (11 m²/hab au lieu de 12)
		ProjectSimulatorWork carre1 = createParkProposal(
				"CRS3035RES200mN2000000E3000000",
				3.0, 50.0,
				11.0, // surfacePerCapita actuelle
				100.0, // missingSurface faible
				100, // accessingPopulation faible
				1100.0 // accessingSurface
		);
		carreMap.put(carre1.getIdInspire(), carre1);
		
		Double minSquareMeterPerCapita = 8.0;
		Double recoSquareMeterPerCapita = 12.0;
		Integer urbanDistance = 300;
		
		// When
		service.calculeEtapeProposition(carreMap, minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
		
		// Then
		assertNull(carre1.getNewSurface(),
				"Aucun parc ne devrait être ajouté si la surface proposée < 1000 m²");
	}
	
	@Test
	void testCalculeEtapeProposition_sansDeficit_neRienFaire() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		// Carré sans déficit (14 m²/hab > 12)
		ProjectSimulatorWork carre1 = createParkProposal(
				"CRS3035RES200mN2000000E3000000",
				3.0, 50.0,
				14.0, // surfacePerCapita actuelle > recommandée
				-200.0, // missingSurface négative
				1000,
				14000.0
		);
		carreMap.put(carre1.getIdInspire(), carre1);
		
		Double minSquareMeterPerCapita = 8.0;
		Double recoSquareMeterPerCapita = 12.0;
		Integer urbanDistance = 300;
		
		BigDecimal surfaceInitiale = carre1.getSurfacePerCapita();
		
		// When
		service.calculeEtapeProposition(carreMap, minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
		
		// Then
		assertEquals(surfaceInitiale, carre1.getSurfacePerCapita(),
				"La surface per capita ne devrait pas changer");
	}
	
	@Test
	void testCalculeEtapeProposition_metAJourVoisins() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		// Carré principal avec déficit
		ProjectSimulatorWork principal = createParkProposal(
				"CRS3035RES200mN2000000E3000000",
				3.0, 50.0,
				2.0,
				10000.0,
				1000,
				2000.0
		);
		carreMap.put(principal.getIdInspire(), principal);
		
		// Voisin proche (< 300m)
		ProjectSimulatorWork voisinProche = createParkProposal(
				"CRS3035RES200mN2000200E3000000",
				3.0018, 50.0018, // ~200m du principal
				6.0,
				6000.0,
				1000,
				6000.0
		);
		carreMap.put(voisinProche.getIdInspire(), voisinProche);
		
		Double minSquareMeterPerCapita = 8.0;
		Double recoSquareMeterPerCapita = 12.0;
		Integer urbanDistance = 300;
		
		BigDecimal surfaceInitialeVoisin = voisinProche.getSurfacePerCapita();
		
		// When
		service.calculeEtapeProposition(carreMap, minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
		
		// Then
		if (principal.getNewSurface() != null && principal.getNewSurface().doubleValue() >= 1000.0) {
			assertTrue(voisinProche.getSurfacePerCapita().doubleValue() > surfaceInitialeVoisin.doubleValue(),
					"La surface per capita du voisin devrait augmenter suite à l'ajout de parc");
		}
	}
	
	@Test
	void testCalculeEtapeProposition_carreMapVide() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		Double minSquareMeterPerCapita = 8.0;
		Double recoSquareMeterPerCapita = 12.0;
		Integer urbanDistance = 300;
		
		// When & Then - ne devrait pas lever d'exception
		assertDoesNotThrow(() -> 
			service.calculeEtapeProposition(carreMap, minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance)
		);
	}
	
	@Test
	void testSortProposalsByDeficit_triCorrect() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		ProjectSimulatorWork carre1 = createParkProposal("ID1", 3.0, 50.0, 10.0, 2000.0, 1000, 10000.0);
		ProjectSimulatorWork carre2 = createParkProposal("ID2", 3.1, 50.1, 5.0, 7000.0, 1000, 5000.0);
		ProjectSimulatorWork carre3 = createParkProposal("ID3", 3.2, 50.2, 12.0, 0.0, 1000, 12000.0);
		
		carreMap.put("ID1", carre1);
		carreMap.put("ID2", carre2);
		carreMap.put("ID3", carre3);
		
		// When
		List<ProjectSimulatorWork> sorted = service.sortProposalsByDeficit(carreMap);
		
		// Then
		assertEquals(3, sorted.size());
		assertEquals("ID2", sorted.get(0).getIdInspire(), "Le carré avec le plus grand déficit devrait être en premier");
		assertEquals("ID1", sorted.get(1).getIdInspire());
		assertEquals("ID3", sorted.get(2).getIdInspire(), "Le carré sans déficit devrait être en dernier");
	}
	
	@Test
	void testFindNeighbors_trouveVoisinsProches() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		ProjectSimulatorWork centre = createParkProposal("CENTRE", 3.0, 50.0, 10.0, 2000.0, 1000, 10000.0);
		ProjectSimulatorWork voisinProche = createParkProposal("PROCHE", 3.0018, 50.0018, 10.0, 2000.0, 1000, 10000.0); // ~200m
		ProjectSimulatorWork voisinLoin = createParkProposal("LOIN", 3.1, 50.1, 10.0, 2000.0, 1000, 10000.0); // ~15km
		
		carreMap.put("CENTRE", centre);
		carreMap.put("PROCHE", voisinProche);
		carreMap.put("LOIN", voisinLoin);
		
		Integer urbanDistance = 300;
		
		// When
		List<ProjectSimulatorWork> neighbors = service.findNeighbors("CENTRE", carreMap, urbanDistance);
		
		// Then
		assertEquals(1, neighbors.size(), "Devrait trouver 1 voisin proche");
		assertEquals("PROCHE", neighbors.get(0).getIdInspire());
	}
	
	@Test
	void testFindNeighbors_carreCentreInexistant() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		Integer urbanDistance = 300;
		
		// When
		List<ProjectSimulatorWork> neighbors = service.findNeighbors("INEXISTANT", carreMap, urbanDistance);
		
		// Then
		assertTrue(neighbors.isEmpty(), "Devrait retourner une liste vide si le carré central n'existe pas");
	}
	
	// ========== Tests pour calculePropositionSolver ==========
	
	@Test
	void testCalculePropositionSolver_resoudProblemeGlobal() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		// Carré 1 : déficit important
		ProjectSimulatorWork carre1 = createParkProposal(
				"CRS3035RES200mN2000000E3000000",
				3.0, 50.0,
				2.0,  // surfacePerCapita très faible
				10000.0,
				1000,
				2000.0
		);
		carreMap.put(carre1.getIdInspire(), carre1);
		
		// Carré 2 : déficit moyen
		ProjectSimulatorWork carre2 = createParkProposal(
				"CRS3035RES200mN2000200E3000000",
				3.0018, 50.0018, // ~200m du carré 1
				6.0,
				6000.0,
				1000,
				6000.0
		);
		carreMap.put(carre2.getIdInspire(), carre2);
		
		// Carré 3 : déficit faible
		ProjectSimulatorWork carre3 = createParkProposal(
				"CRS3035RES200mN2000400E3000000",
				3.0036, 50.0036, // ~400m du carré 1, hors portée
				10.0,
				2000.0,
				1000,
				10000.0
		);
		carreMap.put(carre3.getIdInspire(), carre3);
		
		Double recoSquareMeterPerCapita = 12.0;
		Integer urbanDistance = 300;
		
		// When
		service.calculePropositionSolver(carreMap, recoSquareMeterPerCapita, urbanDistance);
		
		// Then
		// Au moins un parc devrait être ajouté
		long nbParcsAjoutes = carreMap.values().stream()
				.filter(p -> p.getNewSurface() != null && p.getNewSurface().doubleValue() > 0)
				.count();
		assertTrue(nbParcsAjoutes > 0, "Au moins un parc devrait être ajouté");
		
		// Vérifier que les parcs ajoutés respectent la contrainte de 1000 m²
		carreMap.values().stream()
				.filter(p -> p.getNewSurface() != null && p.getNewSurface().doubleValue() > 0)
				.forEach(p -> assertTrue(p.getNewSurface().doubleValue() >= 1000.0,
						"Chaque parc ajouté devrait faire au moins 1000 m²"));
		
		// Vérifier que les densités ont été mises à jour
		carreMap.values().forEach(p -> 
				assertNotNull(p.getSurfacePerCapita(), "La densité devrait être calculée"));
	}
	
	@Test
	void testCalculePropositionSolver_avecCarreMapVide() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		Double recoSquareMeterPerCapita = 12.0;
		Integer urbanDistance = 300;
		
		// When & Then - ne devrait pas lever d'exception
		assertDoesNotThrow(() -> 
			service.calculePropositionSolver(carreMap, recoSquareMeterPerCapita, urbanDistance)
		);
	}
	
	@Test
	void testCalculePropositionSolver_carresSansDéficit() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		// Tous les carrés ont déjà assez de parc
		ProjectSimulatorWork carre1 = createParkProposal(
				"CRS3035RES200mN2000000E3000000",
				3.0, 50.0,
				15.0, // déjà > 12 m²/hab
				-3000.0,
				1000,
				15000.0
		);
		carreMap.put(carre1.getIdInspire(), carre1);
		
		ProjectSimulatorWork carre2 = createParkProposal(
				"CRS3035RES200mN2000200E3000000",
				3.0018, 50.0018,
				13.0,
				-1000.0,
				1000,
				13000.0
		);
		carreMap.put(carre2.getIdInspire(), carre2);
		
		Double recoSquareMeterPerCapita = 12.0;
		Integer urbanDistance = 300;
		
		// When
		service.calculePropositionSolver(carreMap, recoSquareMeterPerCapita, urbanDistance);
		
		// Then
		// Aucun parc ne devrait être ajouté
		long nbParcsAjoutes = carreMap.values().stream()
				.filter(p -> p.getNewSurface() != null && p.getNewSurface().doubleValue() > 0)
				.count();
		assertEquals(0, nbParcsAjoutes, "Aucun parc ne devrait être ajouté si pas de déficit");
	}
	
	@Test
	void testCalculePropositionSolver_optimiseVoisinage() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		// Créer 3 carrés voisins avec déficit
		ProjectSimulatorWork carre1 = createParkProposal(
				"CRS3035RES200mN2000000E3000000",
				3.0, 50.0,
				5.0,
				7000.0,
				1000,
				5000.0
		);
		carreMap.put(carre1.getIdInspire(), carre1);
		
		ProjectSimulatorWork carre2 = createParkProposal(
				"CRS3035RES200mN2000200E3000000",
				3.0018, 50.0018,
				5.0,
				7000.0,
				1000,
				5000.0
		);
		carreMap.put(carre2.getIdInspire(), carre2);
		
		ProjectSimulatorWork carre3 = createParkProposal(
				"CRS3035RES200mN2000000E3000200",
				3.0018, 50.0,
				5.0,
				7000.0,
				1000,
				5000.0
		);
		carreMap.put(carre3.getIdInspire(), carre3);
		
		Double recoSquareMeterPerCapita = 12.0;
		Integer urbanDistance = 300;
		
		BigDecimal densiteInitialeCarre1 = carre1.getSurfacePerCapita();
		
		// When
		service.calculePropositionSolver(carreMap, recoSquareMeterPerCapita, urbanDistance);
		
		// Then
		// La densité devrait s'améliorer pour au moins un carré
		boolean ameliorationDetectee = carreMap.values().stream()
				.anyMatch(p -> p.getSurfacePerCapita().doubleValue() > densiteInitialeCarre1.doubleValue());
		assertTrue(ameliorationDetectee, "Au moins un carré devrait voir sa densité s'améliorer");
	}
}
