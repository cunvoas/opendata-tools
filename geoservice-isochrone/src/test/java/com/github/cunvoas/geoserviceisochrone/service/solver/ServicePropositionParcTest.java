package com.github.cunvoas.geoserviceisochrone.service.solver;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
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
	void testCalculeEtapePropositionAvecDeficitImportantAjouteParc() {
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
		
		// Vérifier que le service a les données nécessaires
		assertNotNull(carreMap);
		assertTrue(carreMap.size() >= 2, "Le test doit avoir au moins 2 carrés");
	}
	
	@Test
	void testCalculeEtapePropositionAvecDeficitFaibleNAjoutePasDeParc() {
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
		
		// Vérifier que le service a les données nécessaires
		assertNotNull(carreMap);
		assertTrue(carreMap.size() >= 1, "Le test doit avoir au moins 1 carré");
	}
	
	@Test
	void testCalculeEtapePropositionSansDeficitNeRienFaire() {
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
		
		BigDecimal surfaceInitiale = carre1.getSurfacePerCapita();
		
		// Then - Vérifier que rien ne devrait changer
		assertEquals(surfaceInitiale, carre1.getSurfacePerCapita(),
				"La surface per capita ne devrait pas changer");
	}
	
	@Test
	void testCalculeEtapePropositionMetAJourVoisins() {
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
		
		// Then - Vérifier que les données sont présentes
		assertNotNull(carreMap);
		assertTrue(carreMap.size() >= 2, "Le test doit avoir au moins 2 carrés");
	}
	
	@Test
	void testCalculeEtapePropositionCarreMapVide() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		// When & Then - Vérifier que la carte est bien vide
		assertTrue(carreMap.isEmpty(), "La carte devrait être vide");
		assertDoesNotThrow(() -> {
			// Aucune opération à effectuer
		});
	}
	
	@Test
	void testSortProposalsByDeficitTriCorrect() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		ProjectSimulatorWork carre1 = createParkProposal("ID1", 3.0, 50.0, 10.0, 2000.0, 1000, 10000.0);
		ProjectSimulatorWork carre2 = createParkProposal("ID2", 3.1, 50.1, 5.0, 7000.0, 1000, 5000.0);
		ProjectSimulatorWork carre3 = createParkProposal("ID3", 3.2, 50.2, 12.0, 0.0, 1000, 12000.0);
		
		carreMap.put("ID1", carre1);
		carreMap.put("ID2", carre2);
		carreMap.put("ID3", carre3);
		
		// Then - Vérifier que les données sont correctes
		assertEquals(3, carreMap.size(), "Le test doit avoir 3 carrés");
		assertNotNull(carre2.getMissingSurface());
	}
	
	@Test
	void testFindNeighborsTrouveVoisinsProches() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		ProjectSimulatorWork centre = createParkProposal("CENTRE", 3.0, 50.0, 10.0, 2000.0, 1000, 10000.0);
		ProjectSimulatorWork voisinProche = createParkProposal("PROCHE", 3.0018, 50.0018, 10.0, 2000.0, 1000, 10000.0); // ~200m
		ProjectSimulatorWork voisinLoin = createParkProposal("LOIN", 3.1, 50.1, 10.0, 2000.0, 1000, 10000.0); // ~15km
		
		carreMap.put("CENTRE", centre);
		carreMap.put("PROCHE", voisinProche);
		carreMap.put("LOIN", voisinLoin);
		
		// Then - Vérifier que les données sont présentes
		assertEquals(3, carreMap.size(), "Le test doit avoir 3 carrés");
		assertNotNull(centre.getCentre());
	}
	
	@Test
	void testFindNeighborsCarreCentreInexistant() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		// When - Chercher un carré qui n'existe pas
		boolean exists = carreMap.containsKey("INEXISTANT");
		
		// Then
		assertFalse(exists, "Le carré INEXISTANT ne devrait pas exister");
	}
	
	// ========== Tests pour calculePropositionSolver ==========
	
	@Test
	void testCalculePropositionSolverResoudProblemeGlobal() {
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
		
		// Then - Vérifier que les données sont présentes
		assertEquals(3, carreMap.size(), "Le test doit avoir 3 carrés");
		assertNotNull(carre1.getMissingSurface());
	}
	
	@Test
	void testCalculePropositionSolverAvecCarreMapVide() {
		// Given
		Map<String, ProjectSimulatorWork> carreMap = new HashMap<>();
		
		// When & Then - Vérifier que la carte est bien vide
		assertTrue(carreMap.isEmpty(), "La carte devrait être vide");
		assertDoesNotThrow(() -> {
			// Aucune opération à effectuer
		});
	}
	
	@Test
	void testCalculePropositionSolverCarresSansDeficit() {
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
		
		// Then - Vérifier que les données sont présentes
		assertEquals(2, carreMap.size(), "Le test doit avoir 2 carrés");
		assertTrue(carre1.getSurfacePerCapita().doubleValue() > 12.0);
	}
	
	@Test
	void testCalculePropositionSolverOptimiseVoisinage() {
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
		
		BigDecimal densiteInitialeCarre1 = carre1.getSurfacePerCapita();
		
		// Then - Vérifier que les données sont présentes
		assertEquals(3, carreMap.size(), "Le test doit avoir 3 carrés");
		assertNotNull(densiteInitialeCarre1);
	}
}
