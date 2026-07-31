# Documentation Fonctionnelle et Technique : Proposition Manuelle de Parcs

## 1. Présentation Générale
La fonctionnalité de **Proposition Manuelle de Parcs** permet aux utilisateurs (aménageurs, urbanistes, contributeurs) de dessiner manuellement de futurs projets de parcs et jardins sur une carte interactive, puis d'**évaluer en temps réel leur impact** sur la couverture en espaces verts de la commune (calculé au niveau de carreaux de 200m × 200m).

Contrairement à l'algorithme automatique qui propose lui-même les meilleurs emplacements, ce module laisse l'utilisateur libre de son tracé (mode cercle rapide ou mode polygone précis) tout en réutilisant le même moteur d'évaluation basé sur les recommandations de l'OMS (Organisation Mondiale de la Santé).

---

## 2. Architecture des Données & Schéma BDD

Les propositions manuelles et leurs évaluations s'appuient sur 3 tables principales :

```
+----------------------------+         1:N         +----------------------+
| manual_park_proposal_meta  | <------------------ | manual_park_proposal |
+----------------------------+                     +----------------------+
| id (PK)                    |                     | id (PK)              |
| insee (VARCHAR)            |                     | id_meta (FK)         |
| annee (INT)                |                     | name (VARCHAR)       |
| type_algo = 'MANUAL'       |                     | mode ('CIRCLE'/'POLY')|
| number_of_parks (INT)      |                     | surface (NUMERIC)    |
| total_surface_of_parks     |                     | centre (POINT)       |
+----------------------------+                     | contour (GEOMETRY)   |
              | 1:N                                +----------------------+
              v
+----------------------------+
|      manual_eval_work      |
+----------------------------+
| id (PK)                    |
| id_meta (FK)               |
| id_inspire (VARCHAR 30)    |
| is_dense (BOOLEAN)         |
| local_pop (NUMERIC)        |
| accessing_pop (NUMERIC)    |
| accessing_surf (NUMERIC)   |
| surf_per_capita (NUMERIC)  |
| miss_surf (NUMERIC)        |
| new_accessing_surf (NUM)   |
| new_surf_per_capita (NUM)  |
| new_miss_surf (NUMERIC)    |
+----------------------------+
```

1. **`manual_park_proposal_meta`** : Métadonnée globale pour une commune et une année donnée. Regroupe les statistiques cumulées (nombre de parcs, surface totale).
2. **`manual_park_proposal`** : Chaque parc dessiné par l'utilisateur.
   - Mode `CIRCLE` : défini par un centre (lat/lng), un diamètre ou une surface.
   - Mode `POLYGON` : tracé via l'outil Leaflet-Geoman (GeoJSON), dont le centroïde et la surface réelle PostGIS (`ST_Area`) sont calculés.
3. **`manual_eval_work`** : Résultats du calcul d'impact sur chaque carreau INSEE 200m de la commune (comparaison des métriques *Avant* vs *Après* l'ajout des parcs manuels).

---

## 3. Moteur d'Évaluation de la Desserte (Algorithme)

### A. Paramètres de l'OMS selon la Densité Urbaine
L'algorithme adapte ses seuils en fonction du caractère dense ou périurbain de la commune (`ServiceOpenData.isDistanceDense(insee)`) :
- **Zone Dense** : Rayon de marche OMS = `300m`, Cible = `10 m²/habitant` (Seuil minimal = `3 m²/hab`).
- **Zone Périurbaine / Rurale** : Rayon de marche OMS = `500m`, Cible = `25 m²/habitant` (Seuil minimal = `8 m²/hab`).

### B. Formule de Proximité Géographique
Pour déterminer si un parc manuel bénéficie à un carreau $C$ :
1. Calcul de la distance à vol d'oiseau ($d$) entre le centroïde du carreau $C$ et le centre de la proposition manuelle $P$.
2. Le parc est considéré accessible depuis le carreau si :
$$\text{Distance}(C, P) < \text{Rayon\_OMS} + \text{SQUARE\_DISTANCE}$$
*(où $\text{SQUARE\_DISTANCE} \approx 283\text{m}$ correspond à la moitié de la diagonale d'un carreau de 200m pour inclure les bordures).*

### C. Indicateurs Avant / Après
Pour chaque carreau $C$ :
- $\text{Surface\_Accessible\_Avant}$ : Surface cumulée des parcs existants accessibles dans le rayon OMS.
- $\text{Surface\_Accessible\_Après} = \text{Surface\_Accessible\_Avant} + \sum \text{Surface}(P_{\text{accessibles}})$.
- $\text{Nouveau\_Ratio\_m2\_par\_hab} = \frac{\text{Surface\_Accessible\_Après}}{\text{Population\_Accessible}}$.
- $\text{Nouveau\_Déficit\_m2} = \max\left(0, (\text{Cible\_OMS} \times \text{Pop\_Local}) - \text{Surface\_Accessible\_Après}\right)$.

---

## 4. API & Contrôleurs

| Méthode | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/mvc/proposal/manual` | Page principale Spring MVC (formulaire + carte) |
| `POST` | `/mvc/proposal/manual/save` | Enregistrement/mise à jour d'un parc manuel |
| `POST` | `/mvc/proposal/manual/delete` | Suppression d'un parc manuel (et nettoyage de la meta si vide) |
| `GET` | `/mvc/geojson/manualProposal/{insee}?annee=YYYY` | Récupère le GeoJSON des parcs manuels dessinés |
| `GET` | `/mvc/geojson/manualProposal/{insee}/evaluate?annee=YYYY` | Charge les carreaux évalués pré-enregistrés en BDD (lecture seule) |
| `POST` | `/mvc/geojson/manualProposal/{insee}/evaluate?annee=YYYY` | **Déclenche le re-calcul d'impact**, sauvegarde les résultats dans `manual_eval_work` et retourne le GeoJSON |

---

## 5. Interface Utilisateur & Ergonomie (Front-End)

La page `manualProposal.html` intègre :
- **Bandeau BÊTA** dans le titre de la page.
- **Sélecteurs de Territoire** (Région, EPCI, Commune) et **Sélecteur d'Année** (synchro avec les données INSEE).
- **Barre d'outils Carte** :
  - Outils de dessin Leaflet-Geoman (Cercle / Polygone).
  - Bouton **"Évaluer"** (icône calculatrice) : envoie une requête `POST` vers l'endpoint d'évaluation pour calculer l'impact et afficher les carreaux colorés.
  - Case à cocher **"Mode daltonien"** : bascule dynamiquement la palette de couleurs des carreaux et des légendes pour l'accessibilité (Palette ColorBrewer2 : rouge/orange/jaune/bleu).
  - **Couches superposées (Overlays)** : Cadastre, Découpage IRIS, Végétation IGN, Propositions manuelles et Couche d'évaluation.
- **Popups interactives** sur les carreaux évalués indiquant :
  - Identifiant INSPIRE du carreau.
  - Situation *Avant* (m²/hab et manque en m²).
  - Situation *Après* (m²/hab et manque restant en m²).
  - Population locale.

---

## 6. Structure des Classes Principales (Back-End)

1. `ManualProposalController` : Contrôleur Spring MVC pour l'affichage de la page et la soumission du formulaire HTML.
2. `ManualProposalGeoJsonController` : Contrôleur REST fournissant les flux GeoJSON pour Leaflet.
3. `ManualProposalService` : Service métier de création, mise à jour, suppression et comptage des parcs manuels. Utilise des Streams Java pour agréger les surfaces totales.
4. `ManualProposalEvaluationService` : Cœur du moteur d'évaluation. Calcule l'impact spatial de chaque parc manuel sur chaque carreau INSEE et enregistre les entités `ManualEvalWork`.
5. `GeoMapServiceV2` : Génère les représentations géométriques GeoJSON (conversion des cercles en polygones 64 côtés pour le rendu cartographique).
