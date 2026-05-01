# 🚀 Démarrage Rapide - Tests Unitaires

## 30 Secondes pour Créer un Nouveau Test

### Étape 1: Ouvrir le Template
```
src/test/java/com/github/cunvoas/geoserviceisochrone/service/template/TestServiceTemplate.java
```

### Étape 2: Copier-Coller et Adapter

```java
// AVANT (❌ Mauvais)
@SpringBootTest
class TestMyService {
    private MyService tested = new MyService();
    @Test
    void test() { /* ... */ }
}

// APRÈS (✅ Correct)
@DisplayName("Tests pour MyService")
class TestMyService extends BaseUnitTest {
    @Mock private DependencyRepository mockRepository;
    @InjectMocks private MyService tested;
    
    @BeforeEach
    void setUp() { TestDataFactory.resetCounters(); }
    
    @Test
    @DisplayName("retrieval réussi")
    void test_get_success() {
        Entity data = TestDataFactory.create[Entity]();
        when(mockRepository.find(1L)).thenReturn(Optional.of(data));
        Optional<Entity> result = tested.get(1L);
        assertTrue(result.isPresent());
        verify(mockRepository, times(1)).find(1L);
    }
}
```

### Étape 3: Ajouter Des Données

```java
// Créer facilement des données
City paris = TestDataFactory.createCity("Paris", "75056", "Île-de-France");
ParcEtJardin park = TestDataFactory.createPark("Parc A", "123 Rue", 1500.0);
List<City> cities = TestDataFactory.createCities(5);

// Créer des points géométriques
Point point = TestDataFactory.createPoint(2.3522, 48.8566);
```

## Les 7 Patterns Essentiels

### 1️⃣ Retrieval Simple
```java
when(mockRepo.findById(1L)).thenReturn(Optional.of(entity));
Optional<Entity> result = tested.getById(1L);
assertTrue(result.isPresent());
```

### 2️⃣ Create/Save
```java
when(mockRepo.save(any(Entity.class))).thenReturn(entity);
Entity result = tested.create(entity);
assertNotNull(result);
```

### 3️⃣ Delete
```java
tested.delete(1L);
verify(mockRepo, times(1)).deleteById(1L);
```

### 4️⃣ Liste
```java
List<Entity> entities = TestDataFactory.create[Entities](3);
when(mockRepo.findAll()).thenReturn(entities);
List<Entity> result = tested.getAll();
assertEquals(3, result.size());
```

### 5️⃣ Not Found
```java
when(mockRepo.findById(9999L)).thenReturn(Optional.empty());
Optional<Entity> result = tested.getById(9999L);
assertTrue(result.isEmpty());
```
objectMapper
### 6️⃣ Erreur
```java
try {
    tested.create(null);
} catch (NullPointerException | IllegalArgumentException e) {
    assertNotNull(e.getMessage());
}
```

### 7️⃣ Plusieurs Appels
```java
tested.method1();
tested.method2();
verify(mockRepo, times(2)).save(any());
```

## Commandes Essentielles

```bash
# Exécuter les tests
mvn test

# Classe spécifique
mvn test -Dtest=TestMyService

# Méthode spécifique
mvn test -Dtest=TestMyService#test_create_success

# Avec couverture JaCoCo
mvn clean test jacoco:report && open target/site/jacoco/index.html
```

## Nommage des Méthodes de Test

```
test_[methodName]_[scenario]

✅ Bons exemples :
- test_getCity_byId_success
- test_create_withNullParameter_throwsException
- test_delete_verifyRepositoryCalled
- test_list_returnsEmptyWhenNoData

❌ Mauvais exemples :
- test1, test2 (pas clair)
- testMethod (pas de scénario)
- testSomethingBad (non spécifique)
```

## Astuces Rapides

### ✅ DO

```java
// ✅ Utiliser BaseUnitTest
class TestMyService extends BaseUnitTest { ... }

// ✅ Utiliser @Mock et @InjectMocks
@Mock private Repository repo;
@InjectMocks private MyService service;

// ✅ Utiliser TestDataFactory
City city = TestDataFactory.createCity(...);

// ✅ Ajouter @DisplayName
@DisplayName("retrieval avec ID valide")
void test_get_success() { ... }

// ✅ Pattern AAA
void test_something() {
    // Arrange
    Data data = TestDataFactory.createData();
    when(mock.find()).thenReturn(data);
    
    // Act
    Result result = service.doSomething();
    
    // Assert
    assertTrue(result.isValid());
    verify(mock, times(1)).find();
}
```

### ❌ DON'T

```java
// ❌ Ne pas faire @SpringBootTest pour unit tests
@SpringBootTest
class TestMyService { ... }

// ❌ Ne pas créer directement
MyService service = new MyService(); // Pas de dépendances injectées!

// ❌ Ne pas utiliser System.out.println()
System.out.println(result); // Debug?

// ❌ Ne pas tester plusieurs choses
@Test
void test() {
    service.create(...);
    service.delete(...);
    service.update(...); // TROP!
}

// ❌ Ne pas ignorer les résultats des assertions
Optional<Entity> result = service.get(1L);
// ... nothing checked
```

## Besoin d'Aide ?

### Mes Tests Ne Compilent Pas

1. Vérifier que vous étendez `BaseUnitTest` ou `BaseIntegrationTest`
2. Vérifier que `@Mock` et `@InjectMocks` sont importés de `org.mockito`
3. Vérifier que vous avez un `@BeforeEach` qui appelle `TestDataFactory.resetCounters()`

### Mes Mocks Ne Fonctionnent Pas

1. Vérifier que `mockRepository` est annoté avec `@Mock`
2. Vérifier que vous appelez `when(...).thenReturn(...)` avant d'utiliser le mock
3. Vérifier que vous appelez `verify()` APRÈS l'exécution du test

### Mon Test Est Trop Complexe

1. Le diviser en plusieurs petits tests
2. Un test = un comportement testé
3. Si c'est trop compliqué, extraire de la logique dans une classe utilitaire

## Fichiers Clés

```
src/test/java/
├── BaseUnitTest.java                  ← Étendre cette classe
├── BaseIntegrationTest.java           ← Ou celle-ci
├── factory/
│   ├── TestDataFactory.java           ← Créer données avec ceci
│   └── MockFactory.java               ← Mocks communs
├── service/template/
│   └── TestServiceTemplate.java       ← Copier-coller ce template
└── [Vos tests ici...]

src/test/resources/
└── application-test.yml               ← Config de test (H2, etc)
```

## Checklist Avant de Committer

- [ ] Les tests s'exécutent sans erreur
- [ ] Les tests passent tous
- [ ] Pas de `@Disabled` sans raison
- [ ] Chaque test a un `@DisplayName` clair
- [ ] Les données viennent de `TestDataFactory`
- [ ] Les dépendances sont mockées avec `@Mock`
- [ ] Les assertions vérifient le résultat
- [ ] Les mocks sont vérifiés avec `verify()`
- [ ] Couverture >= 80%

---

**Version**: 1.0 - Quick Start  
**Pour plus de détails**: Voir `TESTING_GUIDE.md`
