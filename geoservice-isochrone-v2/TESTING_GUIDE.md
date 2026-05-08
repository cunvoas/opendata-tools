# Guide des Tests Unitaires - Geoservice Isochrone V2

## Architecture des Tests

### 1. Classes de Base

#### `BaseUnitTest`
- Utilisée pour les tests unitaires **SANS** contexte Spring
- Initialise automatiquement MockitoAnnotations
- À utiliser pour tester: services, utilities, logique métier isolée

```java
class MyServiceTest extends BaseUnitTest {
    @Mock
    private DependencyA mockDependency;
    
    @InjectMocks
    private MyService tested;
    
    @Test
    void test_something() {
        // Test sans Spring context
    }
}
```

#### `BaseIntegrationTest`
- Utilisée pour les tests d'intégration **AVEC** contexte Spring
- Active le profil `test` automatiquement
- À utiliser pour: contrôleurs, services avec dépendances Spring réelles

```java
@SpringBootTest
@AutoConfigureMockMvc
class MyControllerTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void test_endpoint() {
        // Test avec Spring context
    }
}
```

### 2. Factory de Données de Test (`TestDataFactory`)

Centralise la création cohérente des objets de test :

```java
// Création avec valeurs par défaut
City city = TestDataFactory.createCity();

// Création avec paramètres personnalisés
City paris = TestDataFactory.createCity("Paris", "75056", "Île-de-France");

// Création de collections
List<City> cities = TestDataFactory.createCities(5);

// Réinitialiser les compteurs entre les tests
TestDataFactory.resetCounters();
```

## Patterns de Mock Généralisés

### Pattern 1: Mock avec Repository

```java
class TestCityService extends BaseUnitTest {
    
    @Mock
    private CityRepository cityRepository;
    
    @InjectMocks
    private CityService tested;
    
    @Test
    void test_getCity() {
        // Préparation
        City expectedCity = TestDataFactory.createCity("Paris", "75056", "IDF");
        when(cityRepository.findById(1L)).thenReturn(Optional.of(expectedCity));
        
        // Exécution
        Optional<City> result = tested.getCity(1L);
        
        // Assertion
        assertTrue(result.isPresent());
        assertEquals(expectedCity.getName(), result.get().getName());
        verify(cityRepository, times(1)).findById(1L);
    }
}
```

### Pattern 2: Mock avec ArgumentMatchers

```java
@Test
void test_createPark() {
    // Préparation
    ParcEtJardin newPark = TestDataFactory.createPark("New Park", "Address", 1000.0);
    when(parkRepository.save(any(ParcEtJardin.class))).thenReturn(newPark);
    
    // Exécution
    ParcEtJardin result = tested.createPark(newPark);
    
    // Assertion
    assertNotNull(result);
    verify(parkRepository, times(1)).save(any(ParcEtJardin.class));
}
```

### Pattern 3: Mock avec Verify

```java
@Test
void test_deletePark() {
    // Exécution
    tested.deletePark(1L);
    
    // Assertion
    verify(parkRepository, times(1)).deleteById(1L);
    verify(parkRepository, never()).save(any());
}
```

## Améliorations Couverture des Tests

### Cas de Test Essentiels

1. **Cas de Succès** - Le chemin heureux
2. **Cas d'Erreur** - Données invalides, nulles, inexistantes
3. **Cas Limites** - Valeurs zéro, très grandes, très petites
4. **Cas d'Exception** - Comportement en cas d'exception
5. **Cas d'Interaction** - Vérification des appels de dépendances

### Checklist de Couverture

Pour chaque service/classe :

- [ ] Instanciation réussie
- [ ] Opération CRUD réussie (Create, Read, Update, Delete)
- [ ] Retrieval avec données inexistantes
- [ ] Mutation d'attributs
- [ ] Gestion des valeurs nulles
- [ ] Gestion des valeurs limites
- [ ] Vérification des appels de mocks
- [ ] Tests des méthodes utilitaires

## Commandes Utiles

```bash
# Exécuter tous les tests
mvn clean test

# Exécuter une classe de test spécifique
mvn test -Dtest=TestCityService

# Exécuter une méthode de test spécifique
mvn test -Dtest=TestCityService#test_getCity_success

# Avec rapport de couverture JaCoCo
mvn clean test jacoco:report

# Voir le rapport JaCoCo
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux

# Tests avec profil spécifique
mvn test -P integration-tests
```

## Conventions de Nommage

- **Classe de test**: `Test<NomDeLaClasse>`
- **Méthode de test**: `test_<methodName>_<scenario>` ou `test_<scenario>`
- **Variable testée**: `tested`
- **Données mock**: `mock<NomDependence>` ou `<nomDependence>Repository`

## Bonnes Pratiques

### ✅ À Faire

1. **Utiliser BaseUnitTest et BaseIntegrationTest** pour une cohérence
2. **Utiliser TestDataFactory** pour créer des données cohérentes
3. **Nommer clairement** les tests avec @DisplayName
4. **Arranger-Act-Assert** (AAA pattern)
5. **Tester un seul comportement** par test
6. **Utiliser des mocks** pour isoler les dépendances
7. **Vérifier les interactions** avec verify()
8. **Réinitialiser** les compteurs entre les tests

### ❌ À Éviter

1. ❌ Créer des objets directement dans les tests
2. ❌ Tester plusieurs comportements dans un test
3. ❌ Utiliser System.out.println() dans les tests
4. ❌ Avoir des dépendances entre tests
5. ❌ Utiliser @Disabled sans raison documentée
6. ❌ Ignorer les valeurs nulles
7. ❌ Vérifier tous les appels sans importance
8. ❌ Mélanger tests unitaires et intégration

## Objectifs de Couverture

- **Minimum**: 80% de couverture de ligne
- **Idéal**: 85-90% de couverture de ligne
- **Cible**: 100% de couverture des classes publiques critiques

## Intégration Continue

Les tests s'exécutent automatiquement :
- À chaque commit local
- À chaque push
- À chaque PR

Réduire la couverture ou ajouter des @Disabled sans raison **bloquera le merge**.

---

**Version**: 1.0  
**Mise à jour**: 2026-04-29
