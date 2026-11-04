# CUJ critique — Sanity : consultation carte des isochrones

## Pourquoi ce CUJ

Le cœur du produit AUT'MEL : un contributeur consulte la carte d'une commune
et voit ses couches (parcs, entrées, isochrones, carreaux INSEE). Sans ce
parcours, l'application n'a aucune valeur — tout le reste (calcul, export,
proposition manuelle, admin) en découle.

## Parcours critique (Gherkin)

```gherkin
Fonctionnalité: Sanity — consultation carte isochrones
  En tant que contributeur
  Je veux consulter la carte d'une commune
  Afin d'analyser l'accès aux espaces verts (indicateur OMS)

  Scénario: Le contributeur voit la carte et les couches de sa commune
    Étant donné l'application démarrée avec une base PostGIS saine
    Quand je vérifie que l'application répond ("home", healthcheck "awake")
    Et que je m'identifie avec un compte contributeur valide
    Et que j'affiche la page de consultation "/mvc/consult/map"
    Alors la page affiche les sélecteurs Région / EPCI / Commune
    Et les couches GeoJSON répondent en 200 avec un "FeatureCollection" valide
      | ordre | endpoint                            | authentifié |
      | 1     | /map/years                          | non         |
      | 2     | /mvc/geojson/parkPrefectureByCorner | oui         |
      | 3     | /mvc/geojson/parkGardenByCorner     | oui         |
```

## Test E2E associé

`src/test/java/com/github/cunvoas/geoserviceisochrone/e2e/CujSanityConsultMapE2EIT.java`

- Stack : Spring Boot `RANDOM_PORT` + `TestRestTemplate` + Testcontainers
  PostGIS (`postgis/postgis:16-3.4`).
- Schema créé par Hibernate (`ddl-auto=update`, Flyway désactivé dans le test :
  les migrations supposent des données de prod qui n'existent pas en base de test).
- Utilisateur de test inséré via `ContributeurRepository` avec mot de passe
  haché par le même `PasswordEncoder`.
- Activation : `mvn test -De2e=true -Dtest=CujSanityConsultMapE2EIT`
  (nécessite Docker ; désactivé sinon).

## Gardes-fou "sanity"

Ce test n'est pas un test de calcul : il vérifie que le boot se fait, que la
sécurité laisse passer le parcours authentifié et que le plan de données
(`/map/**`, `/mvc/geojson/**`) renvoie des structures valides. Si ce CUJ casse,
les isochrones ne s'affichent plus : blocage release.
