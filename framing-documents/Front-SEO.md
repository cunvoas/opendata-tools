# Cadrage — Amélioration du SEO de l'application Vue 3 « Parcs & Jardins »

Document de cadrage. Remplace la note initiale « Objectif amélioration du SEO ».
Périmètre applicatif : `geoservice-map-vue3` (site `https://parcs-et-jardins.autmel.ovh`).

Termes officiels de l'Insee employés dans ce document :

- **code officiel géographique** : code à 5 caractères attribué par l'Insee à chaque commune (ex. `59350` pour Lille). Le terme courant est « code Insee ». Le COG rassemble aussi les codes des départements, des régions et des cantons.
- **numéro Siren** : identifiant à 9 chiffres attribué par l'Insee à toute personne physique ou morale inscrite au répertoire Sirene (ex. `245900410` pour la MEL). C'est l'identifiant officiel d'une intercommunalité : un EPCI n'a pas de code officiel géographique. Il sert de segment d'URL `/stats/epci/<siren>`.
- **répertoire Sirene** : répertoire tenu par l'Insee, qui recense les entreprises et leurs établissements. Il porte le numéro Siren et la catégorie juridique.
- **COG** : le Code officiel géographique, répertoire de l'Insee dont provient le code officiel géographique des communes.

## 1. SCOPE

### Dans le périmètre

- Index (`/`) : contenu, structure Hn, section sur l'étude de l'Insee, liens internes sortants.
- `/stats` : URL dédiées par commune et par intercommunalité, contenu indexable.
- Titres et métadonnées (`title`, `description`, `canonical`) par page et par territoire.
- Pré-rendu (`vite-plugin-prerender`) et `sitemap.xml` étendus aux pages territoriales.
- Tableaux statistiques : libellés lisibles, colonne de pourcentage.
- Page `/etude-insee` : fusion ou enrichissement (décision D05).
- Cadrage du prérequis backoffice : numéro Siren de l'EPCI dans `CommunauteCommune`, en ajout de colonne seule (D08, D09, contraintes C01 à C03).
- Contrat de données publiées (`autmel/geoservice-data`) : numéro Siren des EPCI, code officiel géographique des communes, bornes numériques, versionnement (D10 à D12).

### Contraintes

- **C01** Aucune modification des clés primaires ni des clés étrangères du schéma backend. Raison : limiter l'impact de migration ; `adm_com2commune.id` reste la PK et les jointures existantes (`city.id_comm2co`, `id_region`) ne bougent pas.
- **C02** Les évolutions du schéma backend sont additives uniquement : ajout de colonne, index secondaire. Aucun renommage, aucun changement de type, aucune reprise de données obligatoire.
- **C03** Le numéro Siren reste un attribut : il n'entre dans aucune PK, aucune FK, aucune contrainte de jointure.

### Hors périmètre

- Refonte graphique et charte. Raison : aucun effet mesurable sur l'indexation.
- Carte interactive (`/carte`) : les données restent rendues côté client par Leaflet. Raison : rendu canvas, non indexable sans travail disproportionné.
- Backend isochrone (`geoservice-isochrone-v2`) et calcul des statistiques. Raison : hors SEO, données déjà publiées.
- Contenu en langue étrangère. Raison : public francophone uniquement.
- Achat de liens, campagnes payantes. Raison : hors démarche associative.
- Implémentation Java, migration SQL et ré-ingestion des données dans `geoservice-isochrone-v2`. Raison : ce document cadre le prérequis (D08, D09, DEP07), il ne le réalise pas.
- Suppression rétroactive des fichiers publiés au format actuel. Raison : GitHub Raw ne sait pas rediriger, les consommateurs tiers liraient des 404 (D11).

## 2. IMPACTS

- **IM01** Les champs de recherche auto-complétés (Région, Intercommunalité, Commune) sont infranchissables pour les robots. Seule la commune par défaut, Lille, est indexable sur `/stats`. Toutes les autres communes de la MEL sont invisibles. → requires CA01, CA02 ; raises IS01
- **IM02** Aucune URL dédiée par commune ou intercommunalité, et aucun lien `<a href>` généré par la sélection. Google ne peut pas indexer une page par ville. → requires CA01 ; raises IS02
- **IM03** L'index (`/`) promet plus qu'il ne livre : le H1 est une question sans réponse immédiate, et la section « l'étude de l'INSEE et la nôtre, pourquoi les chiffres diffèrent » n'énonce pas la réponse. → requires CA04 ; raises IS03
- **IM04** La page `/etude-insee` est pré-rendue, presque vide, `noindex`, et orpheline (aucun lien interne). Elle duplique l'intention de la section sur l'étude de l'Insee dans l'index. → requires CA05 ; raises IS04
- **IM05** `/stats` porte un H1 masqué générique (« Statistiques des espaces verts ») et un H2 alimenté par les données brutes. Aucune information géographique dans les titres. → requires CA03 ; raises IS05
- **IM06** Les libellés du tableau utilisent des notations mathématiques (`0 <= surface/habitant < 3 m²/hab.`) et des capitales issues des données. Lisibilité faible, aucune colonne de pourcentage. → requires CA06 ; raises IS06
- **IM07** L'index ne redistribue aucune autorité vers les pages territoriales : aucun bloc de liens vers les communes. → requires CA07 ; raises IS02
- **IM08** L'EPCI n'a pas d'identifiant public stable. `CommunauteCommune` ne porte ni numéro Siren ni code officiel géographique : l'id est une séquence de base. Toute la chaîne (API, publication `stats_c2c_<id>`, front `EPCI_MEL = 1`) utilise cet id technique. Un ré-import change les URL. Le numéro Siren n'existe que dans un littéral du front (`245900410`), incohérent avec `EPCI_MEL = 1`. → requires CA08, CA09 ; raises IS07
- **IM09** Les JSON publiés ne portent pas l'identité officielle de l'EPCI (pas de numéro Siren, donc aucun rapprochement possible avec le COG) : `stats_c2c_1_all_2021.json` contient `"insee": "C2C_all_2021_1"`, un code synthétique. `cities_1.json` expose l'`id` technique et le code officiel géographique de chaque commune, mais pas le numéro Siren de l'EPCI. Aucun consommateur ne peut joindre ces fichiers aux référentiels officiels sans table de correspondance locale. → requires CA10 ; raises IS08
- **IM10** Les JSON publiés ne sont pas un contrat stable : pas de champ de version, pas de schéma, bornes de tranches en texte libre et non numériques (`"> 0<=3"`, `"0 <=  surface/habitant < 3 m²/hab."`, libellés d'EPCI en prose, `ratio` en chaîne), couleur de graphique dans les données, nom de commune en capitales, aucune licence ni date de mise à jour. Toute correction est une rupture silencieuse pour le front, le pré-rendu et les réutilisateurs tiers. → requires CA10, CA11 ; raises IS08

## 3. ISSUES

- **IS01** Les moteurs ne peuvent pas remplir un menu déroulant : seule Lille est explorée. → requires D01, D02 ; stems from IM01
- **IS02** Pas de route par territoire, pas de lien HTML classique, pas de maillage interne depuis l'index. → requires D01, D02, D07 ; stems from IM02, IM07
- **IS03** L'index ne répond pas à sa propre promesse éditoriale. La section sur l'étude de l'Insee pose une question (« pourquoi les chiffres diffèrent ») sans phrase de réponse, sans chiffre de l'étude, et avec un chiffre de l'Insee (51,9 %) dont la portée n'est précisée que deux paragraphes plus loin. Voir Annexe A. → requires D04 ; stems from IM03
- **IS04** `/etude-insee` : page pré-rendue de deux liens, `noindex, follow`, sans lien entrant. Budget d'exploration gaspillé et intention dupliquée avec l'index. → requires D05 ; stems from IM04
- **IS05** H1 et H2 non géographiques sur `/stats` ; titre du document figé sur la seule page. → requires D03 ; stems from IM05
- **IS06** Tableau et graphiques : tranches illisibles pour le grand public, capitales brutes, pas de pourcentage. → requires D06 ; stems from IM06
- **IS07** `/stats/epci/<sirene>` n'est pas réalisable en l'état : le numéro Siren est absent du modèle, absent de l'API et absent du chemin de publication. La clé publique est l'id technique, et deux valeurs différentes cohabitent côté front. → requires D08, D09 ; stems from IM08
- **IS08** Le format de publication empêche l'usage des référentiels Sirene et COG et n'offre aucune garantie de compatibilité : identifiants absents ou synthétiques, tranches non numériques, pas de version ni de schéma. Modifier ce format casse le front (`landingStats.js` lit les bornes par expression régulière sur `seuil.surface`, `StatsGraph.vue` lit `barColor`), le pré-rendu et les réutilisateurs. → requires D10, D11, D12 ; stems from IM09, IM10

## 4. DECISIONS

- **D01** Créer une route par territoire : `/stats/<insee>` pour une commune, `/stats/epci/<sirene>` pour une intercommunalité. Adresse stable, indépendante du nom. La branche EPCI dépend de DEP07. → addresses IS01, IS02
  - **D01.1** Le paramètre d'URL existant (`?city=…&c2c=…`) reste accepté et redirige (301) vers la route canonique.
  - **D01.2** Les territoires sans données publiées ne reçoivent pas de route : `/stats` renvoie la liste des territoires couverts.
- **D02** Pré-rendre et inscrire au `sitemap.xml` uniquement les routes territoriales disposant de données. → addresses IS01, IS02
- **D03** Dériver `title`, `description`, `canonical`, H1 et H2 du territoire résolu par la route. → addresses IS05
- **D04** Réécrire la section sur l'étude de l'Insee dans l'index en format réponse d'abord : une phrase de synthèse en tête, puis le tableau, puis les réserves. Chaque ligne du tableau porte sa source et sa date. → addresses IS03
- **D05** Supprimer `/etude-insee` du pré-rendu, du routeur et du `sitemap.xml`, et rediriger (301) vers l'ancre `#insee` de l'index. → addresses IS04
- **D06** Humaniser les libellés de tranches et ajouter une colonne de pourcentage. Conserver la borne numérique en attribut ou en note de bas de tableau. → addresses IS06
- **D07** Ajouter sur l'index un bloc de liens `<a href>` vers les communes couvertes (tri par population, plafonné). → addresses IS02
- **D08** Ajouter à `CommunauteCommune` le numéro Siren de l'EPCI : colonne `siren`, 9 chiffres, unique, non nulle pour tout EPCI publié. L'`id` reste la PK ; le numéro Siren sert de clé publique d'URL sans devenir une clé de base. → addresses IS07
  - **D08.1** Migration `adm_com2commune` : ajout de la seule colonne `siren` (nullable), plus une contrainte d'unicité matérialisée en index secondaire. Aucune PK ni FK touchée. Alimentation depuis le répertoire Sirene complété par le COG.
  - **D08.2** Seul le numéro Siren est requis pour l'URL. Les autres données de l'Insee (dénomination, catégorie juridique, population) sont instruites séparément (point ouvert 6).
  - **D08.3** Aucune table de correspondance entre le numéro Siren et l'`id` n'est créée en base : la correspondance vit dans l'export `data/epci/index.json` (D09). Raison : éviter une table supplémentaire et une FK.
- **D09** Publier un index EPCI `data/epci/index.json` (numéro Siren, nom, dossier de statistiques) et le consommer au build et dans le front. Aucun dossier existant n'est renommé, l'id technique reste le nom de dossier. → addresses IS07
- **D10** Normaliser le contenu des JSON publiés, en ajout seul : bloc d'identité (`codeInsee`, `siren`, `typeTerritoire`), bornes numériques de tranche (`borneMin`, `borneMax`, `unite`), `population`, `version`. Les champs actuels restent présents et inchangés jusqu'à la bascule. → addresses IS08
  - **D10.1** `siren` n'est présent que sur les fichiers d'EPCI et vaut le numéro Siren de l'intercommunalité (9 chiffres). `codeInsee` vaut le code officiel géographique de la commune (5 caractères).
  - **D10.2** Les libellés d'affichage quittent les données : `barColor` et libellés de tranche passent côté front.
- **D11** Double publication : les nouveaux JSON sont écrits à côté des anciens, jamais à leur place. Les anciens chemins restent servis, figés, avec un champ `deprecated`. Aucun renommage de dossier `com2co/<id>`. → addresses IS08
- **D12** Documenter le contrat dans `geoservice-data` : schéma JSON par type de fichier, champ `version`, licence des données, date de dernière mise à jour, exemple de lecture pour un tiers. → addresses IS08

## 5. CAPABILITIES

- **CA01** Routes territoriales par commune et intercommunalité, avec redirection des paramètres historiques. → implements IM01, IM02 ; requires D01 ; is realized by AV01
- **CA02** Pré-rendu et `sitemap.xml` générés depuis la liste des territoires publiés. → implements IM01 ; requires D02 ; is realized by AV01
- **CA03** Métadonnées et Hn calculés à partir du territoire. → implements IM05 ; requires D03 ; is realized by AV01
- **CA04** Section sur l'étude de l'Insee dans l'index, en réponse d'abord, sourcée et datée. → implements IM03 ; requires D04 ; is realized by AV02, AV03
- **CA05** Suppression de la page orpheline `/etude-insee` et redirection. → implements IM04 ; requires D05 ; is realized by AV04
- **CA06** Libellés de tranches lisibles et colonne de pourcentage. → implements IM06 ; requires D06 ; is realized by AV05
- **CA07** Bloc de liens internes vers les communes sur l'index. → implements IM07 ; requires D07 ; is realized by AV01
- **CA08** Numéro Siren porté par `CommunauteCommune`, exposé par l'API et les exports. → implements IM08 ; requires D08 ; is realized by AV06
- **CA09** Index EPCI publié (numéro Siren vers dossier de données), consommé par le front et le pré-rendu. → implements IM08 ; requires D09 ; is realized by AV06
- **CA10** JSON publiés conformes aux référentiels Sirene (numéro Siren des EPCI) et COG (code officiel géographique des communes), avec bornes numériques et champ de version. → implements IM09, IM10 ; requires D10 ; is realized by AV07
- **CA11** Double publication et contrat documenté, sans rupture pour les consommateurs existants. → implements IM10 ; requires D11, D12 ; is realized by AV07

## 6. ADDED VALUES

- **AV01** Chaque commune et intercommunalité couverte dispose d'une page indexable et partageable. → realizes CA01, CA02, CA03, CA07 ; is implemented by US01, US02, US03
- **AV02** Un lecteur qui arrive depuis un moteur comprend en une phrase pourquoi nos chiffres diffèrent de ceux de l'Insee. → realizes CA04 ; is implemented by US04
- **AV03** Les moteurs et les agents conversationnels peuvent citer l'étude sur la requête « pourquoi les chiffres diffèrent ». → realizes CA04 ; is implemented by US04
- **AV04** Aucun visiteur n'atterrit sur une page vide ou une URL obsolète. → realizes CA05 ; is implemented by US05
- **AV05** Les tableaux se lisent sans connaissance des notations mathématiques. → realizes CA06 ; is implemented by US06
- **AV06** Les URL d'intercommunalité sont stables : elles survivent à un ré-import et ne dépendent pas d'un identifiant technique. → realizes CA08, CA09 ; is implemented by US07
- **AV07** Un réutilisateur joint nos données aux référentiels Sirene et COG sans table de correspondance : numéro Siren pour les EPCI, code officiel géographique pour les communes. Il lit les tranches sans analyser de texte. → realizes CA10, CA11 ; is implemented by US08, US09

Chaque Capability est réalisée par au moins un Added Value. Chaque Added Value est implémentée par au moins une User Story.

## 7. USER STORIES

### US01 — Route par commune et par intercommunalité

- En tant que visiteur venu d'un moteur de recherche
- Je veux une adresse propre pour ma commune
- Afin de consulter et partager les statistiques de mon territoire

Complexité: Medium

Liens : implements AV01

```gherkin
Scénario 01.1 — Accès direct à une commune publiée
  Soit une commune couverte par l'étude
  Quand j'ouvre l'adresse /stats/59350
  Alors la page affiche le nom de la commune
  Et l'adresse canonique est https://parcs-et-jardins.autmel.ovh/stats/59350

Scénario 01.2 — Redirection d'un ancien lien à paramètres
  Soit un ancien lien /stats?city=… contenant une commune publiée
  Quand j'ouvre ce lien
  Alors je suis redirigé en 301 vers /stats/<insee>
  Et le résultat affiché est identique

Scénario 01.3 — Commune sans données
  Soit un code officiel géographique absent des données publiées
  Quand j'ouvre l'adresse /stats/99999
  Alors la page répond 404 ou renvoie la liste des territoires couverts
  Mais aucune page vide n'est indexable

Scénario 01.4 — Code officiel géographique invalide
  Soit une adresse /stats/abc
  Quand j'ouvre cette adresse
  Alors l'application renvoie la liste des territoires couverts
  Et aucune erreur JavaScript ne bloque l'affichage

Scénario 01.5 — Intercommunalité identifiée par son numéro Siren
  Soit un EPCI dont le numéro Siren est publié
  Quand j'ouvre l'adresse /stats/epci/245900410
  Alors la page affiche le nom de l'intercommunalité
  Et elle lit les statistiques du dossier déclaré par l'index EPCI
  Mais aucune requête n'utilise l'id technique de la base
```

### US02 — Pré-rendu et sitemap limités aux territoires publiés

- En tant que moteur de recherche
- Je veux découvrir toutes les pages territoriales en HTML
- Afin de les indexer sans exécuter de JavaScript

Complexité: Medium

Liens : implements AV01

```gherkin
Scénario 02.1 — Build avec territoires publiés
  Soit la liste des territoires disposant de données
  Quand la build est lancée
  Alors chaque territoire disposant de données produit un fichier HTML pré-rendu
  Et ce fichier contient le nom du territoire dans le H1

Scénario 02.2 — Sitemap cohérent
  Soit la build terminée
  Quand j'ouvre /sitemap.xml
  Alors chaque URL listée répond 200
  Et chaque URL pré-rendue au titre de CA01 y figure

Scénario 02.3 — Pré-rendu incomplet
  Soit un territoire publié dont le HTML pré-rendu ne contient pas son nom
  Quand la build se termine
  Alors la build échoue avec un message explicite
```

### US03 — Métadonnées et titres géolocalisés

- En tant que moteur de recherche
- Je veux un titre et un H1 propres à chaque territoire
- Afin de classer la bonne page sur la bonne requête

Complexité: Low

Liens : implements AV01

```gherkin
Scénario 03.1 — Balise title d'une commune
  Soit la page /stats/59350
  Quand le HTML est pré-rendu
  Alors la balise title contient « Lille » et « espaces verts »
  Et elle est différente de celle de l'index

Scénario 03.2 — Titre visible
  Soit la page /stats/59350
  Quand je l'ouvre
  Alors le H1 contient le nom de la commune en casse normale
  Et le H2 décrit la répartition, sans nom de territoire brut en capitales

Scénario 03.3 — Intercommunalité
  Soit la page d'une intercommunalité
  Quand je l'ouvre
  Alors le H1 contient « Métropole Européenne de Lille »
  Et la balise title ne contient pas le numéro Siren seul
```

### US04 — Section sur l'étude de l'Insee, en réponse d'abord

- En tant que lecteur ou moteur de recherche
- Je veux lire en une phrase pourquoi nos chiffres diffèrent de ceux de l'Insee
- Afin de comparer sans conclure à une erreur

Complexité: Medium

Liens : implements AV02, AV03

```gherkin
Scénario 04.1 — Réponse immédiate
  Soit la section « Pourquoi les chiffres diffèrent »
  Quand je lis le premier paragraphe
  Alors il énonce la cause principale en une phrase
  Et il ne dépasse pas deux phrases

Scénario 04.2 — Chiffre de l'Insee borné sur place
  Soit la phrase qui cite le chiffre de l'Insee
  Quand je la lis
  Alors le périmètre du chiffre (grand centre urbain, millésime) est indiqué dans la même phrase
  Mais aucun chiffre de l'Insee n'apparaît sans son périmètre

Scénario 04.3 — Tableau sourcé
  Soit le tableau de comparaison
  Quand je lis une ligne
  Alors la ligne porte la source et la date des données comparées

Scénario 04.4 — Ancre citable
  Soit l'adresse /#insee
  Quand je l'ouvre
  Alors la section concernée est visible en haut de fenêtre
  Et la réponse figure dans le HTML pré-rendu
```

### US05 — Suppression de `/etude-insee`

- En tant que visiteur ou robot
- Je veux ne plus trouver de page vide
- Afin de ne pas perdre de temps sur une URL sans contenu

Complexité: Low

Liens : implements AV04

```gherkin
Scénario 05.1 — Redirection
  Soit un ancien lien vers /etude-insee
  Quand je l'ouvre
  Alors je suis redirigé en 301 vers /#insee

Scénario 05.2 — Absence du sitemap et du pré-rendu
  Soit la build terminée
  Quand j'inspecte le sitemap et les fichiers pré-rendus
  Alors /etude-insee n'y figure pas
```

### US06 — Libellés de tranches et pourcentages

- En tant que lecteur
- Je veux lire les tranches en français courant avec leur poids
- Afin de comprendre la répartition sans conversion mentale

Complexité: Low

Liens : implements AV05

```gherkin
Scénario 06.1 — Tranche basse
  Soit la tranche dont la borne haute est 3 m²
  Quand elle s'affiche
  Alors le libellé est « Moins de 3 m² par habitant »
  Et il ne contient ni « <= » ni « < »

Scénario 06.2 — Pourcentage
  Soit des données pour une commune
  Quand le tableau s'affiche
  Alors chaque tranche affiche le nombre d'habitants et son pourcentage
  Et la somme des pourcentages affichés est comprise entre 99,9 % et 100,1 %

Scénario 06.3 — Casse des noms
  Soit un nom de commune fourni en capitales par les données
  Quand il s'affiche dans un titre ou dans le tableau
  Alors il est rendu en casse normale
```

### US07 — Numéro Siren de l'EPCI exposé par le backoffice

- En tant que responsable du backoffice
- Je veux que chaque intercommunalité porte son numéro Siren et le publie
- Afin que les URL d'intercommunalité soient stables et construites sans identifiant technique

Complexité: High

Liens : implements AV06

```gherkin
Scénario 07.1 — Numéro Siren exposé
  Soit une intercommunalité enregistrée
  Quand j'appelle l'API qui liste les intercommunalités
  Alors la réponse contient son numéro Siren à 9 chiffres
  Et ce numéro est identique à celui du répertoire Sirene

Scénario 07.2 — Unicité
  Soit deux intercommunalités enregistrées
  Quand la seconde reçoit le numéro Siren de la première
  Alors l'enregistrement est refusé avec un message explicite

Scénario 07.3 — Publication sans numéro Siren
  Soit une intercommunalité dépourvue de numéro Siren
  Quand la publication de ses statistiques est demandée
  Alors la publication est refusée
  Et l'erreur nomme l'intercommunalité concernée

Scénario 07.4 — Index EPCI publié
  Soit des intercommunalités publiées
  Quand l'export s'exécute
  Alors data/epci/index.json contient pour chacune le numéro Siren, le nom et le dossier de statistiques
  Et le build du front produit l'URL /stats/epci/<sirene> correspondante

Scénario 07.5 — Ré-import sans changement d'URL
  Soit une base ré-importée où l'id technique de l'intercommunalité a changé
  Quand le site est reconstruit
  Alors l'URL /stats/epci/<sirene> est inchangée
  Et la page affiche les mêmes statistiques
```

### US08 — JSON publiés conformes aux référentiels Sirene et COG

- En tant que réutilisateur des données ouvertes
- Je veux des identifiants officiels et des bornes numériques dans les JSON
- Afin de joindre les fichiers aux référentiels Sirene et COG sans retraitement de texte

Complexité: High

Liens : implements AV07

```gherkin
Scénario 08.1 — Identité d'un fichier EPCI
  Soit un fichier de statistiques d'intercommunalité
  Quand je lis son bloc d'identité
  Alors il contient un champ siren de 9 chiffres
  Et ce numéro Siren est identique à celui attribué par l'Insee à cette intercommunalité
  Mais il ne contient aucun identifiant technique de base de données

Scénario 08.2 — Identité d'un fichier commune
  Soit un fichier de statistiques de commune
  Quand je lis son bloc d'identité
  Alors il contient le code officiel géographique de la commune, sur 5 caractères
  Et il ne contient pas de champ siren

Scénario 08.3 — Bornes numériques
  Soit une tranche de surface
  Quand je la lis
  Alors elle porte une borne basse et une borne haute numériques, en m² par habitant
  Et la tranche supérieure est ouverte, sans borne haute
  Mais aucun libellé de tranche n'est nécessaire pour calculer le classement

Scénario 08.4 — Version et absence de présentation
  Soit un fichier publié
  Quand je le lis
  Alors il contient un champ version
  Et il ne contient ni couleur ni libellé d'affichage
```

### US09 — Double publication et contrat documenté

- En tant que consommateur existant des données
- Je veux que les anciens fichiers restent disponibles
- Afin de migrer sans interruption

Complexité: Medium

Liens : implements AV07

```gherkin
Scénario 09.1 — Ancien chemin toujours servi
  Soit un chemin publié avant la normalisation
  Quand je l'appelle après publication
  Alors il répond 200
  Et son contenu est identique à celui d'avant la normalisation

Scénario 09.2 — Nouveau chemin en parallèle
  Soit un territoire publié après normalisation
  Quand je liste les fichiers du territoire
  Alors le fichier normalisé et l'ancien fichier coexistent
  Et l'ancien porte une mention de dépréciation

Scénario 09.3 — Contrat documenté
  Soit le dépôt de données
  Quand je cherche la description du format
  Alors je trouve un schéma par type de fichier, la licence des données et un exemple de lecture
  Et la version du schéma correspond au champ version des fichiers

Scénario 09.4 — Non-régression d'affichage
  Soit le front à jour
  Quand il affiche une commune et une intercommunalité
  Alors les chiffres affichés sont identiques à ceux du format précédent
  Et aucun libellé de tranche n'est vide
```

## 8. DEPENDENCIES

### Dépendances dures

- **DEP07** Prérequis backoffice : `geoservice-isochrone-v2` doit ajouter le numéro Siren à `CommunauteCommune` par ajout de colonne seule, sans toucher aux PK ni aux FK (C01 à C03), puis le propager à l'API et aux exports et publier `data/epci/index.json`. Aucun numéro Siren n'existe aujourd'hui en base ; l'id est une séquence et les dossiers publiés portent cet id (`data/stats/com2co/1`). Bloque la branche EPCI de US01, ainsi que US02, US03 et US07.
- **DEP01** Les statistiques par commune et par intercommunalité publiées dans `autmel/geoservice-data` doivent exister et couvrir la liste utilisée pour le pré-rendu. Bloque US01, US02, US03. Dépend de DEP07 pour la partie intercommunalités.
- **DEP02** Le pré-rendu (`vite-plugin-prerender` + Puppeteer) doit tourner au build et joindre les JSON de statistiques. Bloque US02 ; déjà contrôlé par `scripts/postbuild.js` pour l'index.
- **DEP03** GitHub Pages doit servir `dist/<route>/index.html` en 200. Bloque DEP02.
- **DEP04** Le nom et le millésime exacts du chiffre de l'Insee cité doivent être validés par le PO. Bloque US04.
- **DEP10** Le front et le pré-rendu doivent lire le bloc d'identité et les bornes numériques avant toute bascule : `landingStats.js` (bornes déduites de `seuil.surface` par expression régulière), `StatsGraph.vue` (`surface`, `barColor`), `Isochrone.vue` (`cadastre_c2c_<id>.json`). Bloque l'affichage de US08 et US09.

### Dépendances souples

- **DEP05** Le lien vers Insee Première n° 2049 doit rester stable. Impact limité à la section sur l'étude de l'Insee.
- **DEP06** La liste des communes affichées en liens internes doit pouvoir être régénérée sans rédaction manuelle. Impact limité à D07.
- **DEP08** Le COG de l'Insee doit être disponible pour alimenter le numéro Siren des EPCI lors de la migration, en une passe ou manuellement. Impact limité à l'alimentation initiale de D08.
- **DEP09** Le littéral `com2coId: '245900410'` de `AppStatistics.vue` et la constante `EPCI_MEL = 1` doivent être remplacés par l'index EPCI. Impact limité au défaut de `/stats`.
- **DEP11** Les réutilisateurs externes des données doivent être informés de la dépréciation. Impact limité à D11 et D12.

## 9. APPENDIX

### Annexe A — Critique de l'index (`/`)

Constat, vérifié dans `src/views/AppLanding.vue`.

- **Promesse non tenue.** Le H1 (« Combien de m² de parcs et jardins par habitant dans la Métropole Européenne de Lille ? ») pose une question. La réponse n'arrive qu'après deux sections, sous forme de cartes. Aucun paragraphe de synthèse ne donne le chiffre.
- **H1 sans ancrage géographique explicite pour le résultat.** Le H1 est valable pour la MEL, mais la page affiche surtout Lille. Le nom de Lille n'apparaît que dans les cartes de chiffres, en H3.
- **Section sur l'étude de l'Insee : la question est posée, la réponse est diluée.** Le H2 promet « pourquoi les chiffres diffèrent ». Le texte répond en trois paragraphes successifs (seuils, mesure de l'accès, comparabilité) sans phrase de synthèse. Un lecteur pressé ne repart pas avec la raison principale.
- **Section sur l'étude de l'Insee : aucun chiffre de l'étude.** La section cite un chiffre de l'Insee (51,9 % de la population à moins de 300 m) mais aucun résultat de l'étude. La comparaison est donc asymétrique.
- **Section sur l'étude de l'Insee : chiffre mal borné.** La portée du 51,9 % (grand centre urbain, données 2019, pas la commune seule) est précisée deux paragraphes plus loin. Repris hors contexte (extrait enrichi, agent conversationnel), il devient faux.
- **Section sur l'étude de l'Insee : tableau non sourcé.** Sept lignes comparées, aucune source par ligne, aucune date pour les données de l'étude.
- **Section sur l'étude de l'Insee : pas de cible d'extrait enrichi.** Aucun H3, aucune formulation question/réponse courte. La requête « pourquoi les chiffres diffèrent » ne trouve pas de bloc citable.
- **Doublon orphelin.** `/etude-insee` est pré-rendue, contient deux liens, est en `noindex, follow`, et ne reçoit aucun lien interne. Elle duplique l'intention de la section sur l'étude de l'Insee dans l'index.
- **Aucun maillage territorial.** L'index ne contient aucun lien vers une commune. Il n'existe d'ailleurs aucune page à lier (IS02).
- **Métadonnées secondaires.** La balise `keywords` contient des expressions hors sujet (« événements nature Lille », « ateliers biodiversité »). Google l'ignore ; elle n'aide aucun moteur, mais brouille la lecture du document.
- **Cohérence titre/page.** Le `title` de la route (index) et le `title` statique d'`index.html` portent deux promesses différentes. Le pré-rendu écrase le second ; le premier ne mentionne pas l'Insee alors que la page traite la comparaison.

### Annexe B — Tableau de correspondance

| Impact | Issue | Décision | Capability | Added Value | User Story |
| --- | --- | --- | --- | --- | --- |
| IM01 | IS01 | D01, D02 | CA01, CA02 | AV01 | US01, US02 |
| IM02, IM07 | IS02 | D01, D02, D07 | CA01, CA07 | AV01 | US01 |
| IM03 | IS03 | D04 | CA04 | AV02, AV03 | US04 |
| IM04 | IS04 | D05 | CA05 | AV04 | US05 |
| IM05 | IS05 | D03 | CA03 | AV01 | US03 |
| IM06 | IS06 | D06 | CA06 | AV05 | US06 |
| IM08 | IS07 | D08, D09 | CA08, CA09 | AV06 | US07 |
| IM09, IM10 | IS08 | D10, D11, D12 | CA10, CA11 | AV07 | US08, US09 |

### Annexe C — Planning de livraison 2.1.x

Séquence proposée. Les durées ne sont pas estimées ici : le PO fixe la capacité et le trimestre. Ce sont les enchaînements et les critères de sortie qui sont engageants.

Principe : la donnée et son contrat passent avant les URL, les URL avant le contenu. Aucune version ne dépend d'une version ultérieure.

| Version | Contenu | Dépend de | Critère de sortie |
| --- | --- | --- | --- |
| **2.1.0** — socle données | US07 (numéro Siren en base et API), D08, D08.1, D08.3, C01 à C03. Export `data/epci/index.json` (D09). US08 et US09 (JSON v2 en double publication, schéma et licence, D10 à D12). | DEP07, DEP08, DEP11 | Le numéro Siren est en base sans toucher aux PK ni aux FK ; l'index EPCI et les JSON v2 sont publiés ; les anciens fichiers répondent toujours 200 et à l'identique. |
| **2.1.1** — lecture du contrat | Lecture du bloc d'identité et des bornes numériques côté front (`landingStats.js`, `StatsGraph.vue`, `Isochrone.vue`), DEP10. Suppression des clés codées en dur (`EPCI_MEL = 1`, `245900410`), DEP09. | 2.1.0 | Les chiffres affichés sont identiques à l'avant-publication v2 ; aucun libellé de tranche vide ; plus aucun identifiant technique dans le front. |
| **2.1.2** — URL territoriales | US01 (routes `/stats/<insee>` et `/stats/epci/<siren>`, redirections), US02 (pré-rendu et sitemap), US03 (titres et Hn géolocalisés), US05 (suppression de `/etude-insee`), D07 (bloc de liens internes). | 2.1.1, DEP01, DEP02, DEP03 | Chaque territoire publié répond 200 en HTML pré-rendu, figure au sitemap, et chaque URL ancienne redirige. Build refusé si le HTML pré-rendu est incomplet. |
| **2.1.3** — contenu éditorial | US04 (section sur l'étude de l'Insee en réponse d'abord), US06 (tranches lisibles et pourcentage). D04, D06. | 2.1.2, DEP04 (validation du chiffre de l'Insee) | La réponse tient dans le premier paragraphe, chaque chiffre porte son périmètre, les tableaux affichent le pourcentage. |

Ordre imposé par les dépendances :

1. Le numéro Siren ne peut pas arriver après les URL, sinon `/stats/epci/<siren>` ne résout rien.
2. La lecture du contrat v2 précède toute bascule de chemin, sinon le front lit un format qu'il ne connaît pas.
3. La section sur l'étude de l'Insee en dernier : elle dépend d'un arbitrage PO (DEP04) et n'a aucun impact technique bloquant.

Risques de calendrier :

- Si DEP04 (chiffre de l'Insee) glisse, 2.1.3 sort sans US04 : les trois autres versions ne sont pas affectées.
- Si le pré-rendu des pages territoriales dépasse le budget de build (Puppeteer par route), 2.1.2 se limite aux territoires au-dessus d'un seuil de population (point ouvert 5), sans changer les URL.
- La double publication de 2.1.0 double la volumétrie jusqu'au retrait des anciens fichiers (point ouvert 9). Aucun nettoyage n'est planifié.

## 10. DOCUMENT METADATA

| Champ | Valeur |
| --- | --- |
| Document | Cadrage SEO — `geoservice-map-vue3` |
| Chemin | `framing-documents/Front-SEO.md` |
| Version | 2.4 |
| Date | 2026-11-15 |
| Product Owner | à confirmer |
| Trimestre cible | à confirmer |
| Cible de livraison | 2.1.0 à 2.1.3 (Annexe C) |
| Source initiale | note « Objectif amélioration du SEO » (version 0.1, 16 lignes) |
| Code de référence (front) | `geoservice-map-vue3` : `src/views/AppLanding.vue`, `src/views/AppStatistics.vue`, `src/utils/landingStats.js`, `src/router/index.js`, `vite.config.js`, `scripts/postbuild.js`, `src/views/AppEtudeInsee.vue` |
| Code de référence (prérequis) | `geoservice-isochrone-v2` : `model/opendata/CommunauteCommune.java`, `service/opendata/ServiceOpenData.java`, `service/analytics/StatsSurfaceService.java`, `service/export/PublishService.java`, `src/main/resources/db/migration/` |
| Statut | brouillon à valider par le PO |
| Révision 2.1 | Prérequis backoffice « numéro Siren » cadré : D08, D09, CA08, CA09, AV06, US07, DEP07, DEP08, DEP09 |
| Révision 2.2 | Contrat JSON Sirene/COG cadré : IM09, IM10, IS08, D10 à D12, CA10, CA11, AV07, US08, US09, DEP10, DEP11 |
| Révision 2.3 | Contrainte de migration figée : aucune modification de PK ni de FK (C01 à C03), schéma backend en ajout seul, correspondance du numéro Siren portée par l'export (D08.1, D08.3) |
| Révision 2.4 | Planning 2.1.x ajouté (Annexe C) : quatre livraisons séquencées, critères de sortie et risques de calendrier |

### Points ouverts à trancher par le PO

1. Format d'URL retenu : `/stats/<insee>` (proposé) ou `/stats/<slug-de-commune>` (lisible, mais collision de noms à gérer).
2. Plafond du bloc de liens internes sur l'index, et critère de tri (population proposée).
3. Sort de `/etude-insee` : suppression avec redirection (proposé) ou enrichissement en page complète.
4. Validation du chiffre de l'Insee cité et de son périmètre exact (DEP04).
5. Périmètre du pré-rendu : toutes les communes couvertes, ou seulement celles au-dessus d'un seuil de population.
6. Quelles données de l'Insee ajouter à `CommunauteCommune` au-delà du numéro Siren : dénomination, catégorie juridique (métropole, CA, CC), population. Le présent cadrage n'en retient aucune (D08.2).
7. Publication des statistiques EPCI : conserver `data/stats/com2co/<id>` avec l'index par numéro Siren (proposé), ou renommer les dossiers par numéro Siren au prix d'une republication complète.
8. Nommage des champs normalisés : `codeInsee` (code officiel géographique de la commune) et `siren` (numéro Siren de l'EPCI) proposés, ou reprise du nom actuel `insee` avec un type explicite.
9. Échéance de retrait des anciens champs, et niveau d'engagement vis-à-vis des réutilisateurs tiers.
