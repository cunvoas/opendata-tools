# Framing — Évaluation de la végétation urbaine à partir des indices Sentinel-2

Document de cadrage de l'algorithme `VegetationEvaluator`, qui transforme les statistiques
Copernicus (SAVI, NDVI, NDRE, GNDVI) en diagnostic lisible : recouvrement, score de santé,
stress physiologique, hétérogénéité spatiale et anomalie locale.

Code concerné :

| Rôle | Fichier |
|---|---|
| Calcul des 4 indices (evalscript CDSE) et parsing | `extern/ndvi/ClientCopernicusNdvi.java` |
| Statistiques par zone | `extern/ndvi/NdviStatistics.java` |
| Arbre de décision et dégradation gracieuse | `extern/ndvi/VegetationEvaluator.java` |
| Persistance des mesures | `model/isochrone/NdviResult.java`, `db/migration/v2.2.4_ndvi_savi.sql` |

---

## 1. Contexte et problème

Les parcs urbains sont des objets **spectralement hétérogènes** : un pixel Sentinel-2 de
10 m y mélange canopée, pelouse, allée, voirie et bâti. Trois défauts en découlent :

1. **Bruit minéral.** Sur un couvert ouvert, la réflectance du sol et de l'asphalte tire le
   signal rouge vers le haut et fait chuter artificiellement le NDVI.
2. **Saturation de la canopée.** Au-delà d'un LAI élevé, le NDVI plafonne : deux parcs
   denses différents produisent la même valeur, et un stress débutant devient invisible.
3. **Instabilité statistique.** Sur un square d'un seul pixel, les bornes (min/max) et
   l'écart-type n'ont pas de sens, alors qu'ils portent le diagnostic d'hétérogénéité.

L'algorithme est donc construit en quatre étages, chacun répondant à un de ces défauts, et
il **dégrade gracieusement** quand une bande ou une statistique manque.

## 2. Données utilisées

Un seul appel à l'API Statistical de Copernicus Data Space Ecosystem calcule les quatre
indices sur les mêmes pixels (sortie demandée à 10 m, bandes B03/B04/B05/B08 rééchantillonnées
par Sentinel Hub). Les pixels nuageux, ombres et eau sont exclus par le masque SCL.

| Indice | Bandes | Formule implémentée |
|---|---|---|
| NDVI | B04, B08 | `(B08 − B04) / (B08 + B04)` |
| GNDVI | B03, B08 | `(B08 − B03) / (B08 + B03)` |
| NDRE | B05, B08 | `(B08 − B05) / (B08 + B05)` |
| SAVI | B04, B08 | `((B08 − B04) / (B08 + B04 + 0.5)) × 1.5` |

Le facteur `L = 0,5` du SAVI est la valeur usuelle pour un couvert intermédiaire
(Huete, 1988). Copernicus ne renvoie que des statistiques agrégées (moyenne, min, max,
écart-type, nombre de pixels) : l'algorithme travaille donc **par zone**, pas par pixel.

### 2.1 Aucune image n'est téléchargée : le calcul est déporté dans l'evalscript

Le projet n'utilise pas d'archive d'images : il appelle l'**API Statistical** de CDSE, qui
exécute l'evalscript de `ClientCopernicusNdvi` côté Sentinel Hub, sur les réflectances
Sentinel-2 L2A, puis renvoie uniquement les statistiques zonales.

Conséquences pour la lecture de ce document :

- Les raisonnements spectraux (bruit minéral, saturation de canopée, sensibilité Red-Edge)
  restent valables **au pixel** : ils s'appliquent dans le moteur Sentinel Hub, avant
  agrégation. Rien n'est perdu par le fait de ne pas manipuler d'images.
- Le masque SCL, l'élimination des dénominateurs nuls et les quatre formules sont évalués
  pixel par pixel, exactement comme dans un traitement raster local.
- Avec `mosaickingOrder: leastCC`, Sentinel Hub compose une mosaïque sur la fenêtre
  d'acquisition ; les statistiques portent sur cette image composite, donc elles sont
  essentiellement **spatiales**. Un résidu temporel subsiste là où plusieurs scènes sont
  de qualité équivalente (la dispersion intègre alors un peu de variabilité phénologique).
  Si le mosaïquage était retiré de la requête, `std` deviendrait spatio-temporel et le
  diagnostic d'hétérogénéité devrait être réinterprété.
- La résolution est imposée par la requête : grille de 10 m, plafonnée à 512 × 512 pixels
  (`requestSize`), pour rester sous 1 Processing Unit par appel. Un polygone plus petit
  qu'un pixel ne renvoie qu'un échantillon, d'où l'absence possible de `min`/`max`/`std`.

## 3. Architecture de la décision

### Étape 1 — Recouvrement et vigueur

Le SAVI sert de **référence de couvert** : il est le seul indice insensible à la
réflectance du sol. En son absence, le NDVI prend le relais avec des seuils relevés
(0,22 / 0,55 au lieu de 0,18 / 0,42) pour compenser le biais minéral.

| Condition | Recouvrement | Vigueur |
|---|---|---|
| référence < seuil minéral | Minéral, bâti, ombre dense ou sol nu | score = 0 |
| référence < seuil dense | Couvert faible / pelouse ouverte ou arbustif jeune | `0,65·SAVI + 0,35·NDVI`, pondérations renormalisées sur les indices présents |
| référence ≥ seuil dense | Couvert dense / strate arborée ou parc mature | `0,50·NDRE + 0,30·GNDVI + 0,20·NDVI`, renormalisé |

La bascule de pondération traduit la physique des indices : en couvert clairsemé, la
structure (SAVI) porte l'information ; en couvert dense, le NDVI sature et l'information
migre vers la chlorophylle (Red‑Edge, vert).

### Étape 2 — Diagnostic physiologique

Un NDVI supérieur à 0,60 accompagné d'un NDRE inférieur à 0,32 ou d'un GNDVI inférieur à
0,48 signale un stress débutant que la biomasse seule masque. Si les deux bandes sensibles
manquent, le diagnostic est explicitement dégradé en « Diagnostic limité » plutôt que
produit au hasard.

### Étape 3 — Hétérogénéité spatiale et anomalie locale

- Écart-type de référence : SAVI, puis NDVI en repli.
  `> 0,16` très hétérogène · `> 0,09` modérément hétérogène · sinon homogène.
- Anomalie locale : amplitude `max − min` du NDVI (puis du SAVI), seuil 0,50.
- Sans écart-type ou sans bornes, l'analyse est neutralisée (`Non calculable`, `null`) :
  une micro-parcelle d'un pixel ne produit pas de faux diagnostic de fragmentation.

### Étape 4 — Score et confiance

`score = clamp(vigueur × 100 − 20 × std_référence, 0, 100)`, arrondi au dixième : la
pénalité d'irrégularité ne s'applique que si l'écart-type est mesurable.

Un niveau de confiance accompagne le résultat : 4 indices disponibles → **Élevée**,
2 ou 3 → **Moyenne**, 1 → **Faible**, aucun SAVI/NDVI → **Nulle** (score `null`).

### Exposition API

`NdviController` (`/map/ndvi/summaries`, aussi écrit dans les JSON de publication) expose
pour chaque parc les **quatre indices avec moyenne, min, max et écart-type**, ainsi que le
bloc `evaluation` produit par `VegetationEvaluator` (recouvrement, score, confiance,
diagnostic, homogénéité, anomalie). Le `sample_count` de la mesure est transmis à
l'évaluateur : une zone d'un seul pixel bascule en « pixels insuffisants » plutôt qu'en
« homogène ».

La **carte** (`GeoMapServiceV2`, vues `ParkView` / `ParkGardenView`) porte aussi les quatre
indices avec moyenne, min, max et écart-type, plus les champs `vegetation*` (recouvrement,
score, confiance, diagnostic, homogénéité, anomalie). Les valeurs sont lues dans la
**dernière mesure `ndvi_result`** de la zone de parc ; les colonnes héritées de
`park_area_computed` ne servent que de repli quand aucune mesure n'existe.

Le **batch de publication** (`ServicePublicationExporter.writeNdviJson`) écrit les mêmes
données dans `data/ndvi/<com2co>/ndvi_<année>_<com2co>.json` et dans le fichier de chaque
commune : indices et bloc `evaluation` y sont sérialisés tels quels.

Côté **front** (`geoservice-map-vue3`) : `utils/vegetation.js` normalise ces champs (indices
plats pour la carte, objet `evaluation` imbriqué pour l'écran NDVI). Le détail d'un parc sur
la carte affiche les quatre indices avec moyenne/min/max/écart-type puis l'évaluation ;
l'écran NDVI ajoute les colonnes NDRE, GNDVI, SAVI, score de santé et confiance.

## 4. Justification scientifique

### 4.1 Débruitage urbain par le SAVI

Le SAVI corrige la contribution du sol par le paramètre `L` (Huete, 1988). Sur Sentinel-2,
la comparaison NDVI/SAVI le long d'un gradient sols nus → zones boisées confirme que le
SAVI neutralise la réflectance des sols artificialisés, là où le NDVI produit des valeurs
trompeuses. C'est la justification directe du choix du SAVI comme indice de référence de
couvert, et du relèvement des seuils quand il manque.

- HAL hal-03041269 — *Sentinel-2 for High Resolution Mapping of Slope-Based Vegetation Indices* : https://hal.science/hal-03041269
- HAL hal-03888173 — *Calcul d'indices avec QGIS sur image Sentinel-2A : du NDVI au NDBI* : https://hal.science/hal-03888173
- Huete, A. R. (1988), *A soil-adjusted vegetation index (SAVI)*, Remote Sensing of Environment.

### 4.2 Complémentarité Red-Edge / vert contre la saturation du NDVI

Le NDVI sature sur les canopées multi-strates et reste sensible au bruit de fond du sol en
couvert ouvert ; le NDRE (Red‑Edge) reste sensible à la chlorophylle à forte biomasse et le
GNDVI (vert) suit la concentration en chlorophylle et le stress hydrique/azoté. La thèse
HAL tel-05407531 montre, par modélisation radiative 3D (DART) en milieu urbain (Rennes),
que les indices ajustés au sol et les indices chlorophylliens sont les plus fiables pour
estimer le LAI et détecter les stress thermiques ou hydriques en ville. C'est ce qui motive
la pondération `NDRE + GNDVI + NDVI` sur les couverts denses et la règle d'alerte
« NDVI fort + NDRE/GNDVI faible ».

- HAL tel-05407531 — *Évaluation des séries temporelles Sentinel-2 pour le suivi de l'état de santé de la végétation arborée en milieu urbain* : https://theses.hal.science/tel-05407531
- Gitelson, Kaufman & Merzlyak (1996), *Use of a green channel in remote sensing of global vegetation*, Remote Sensing of Environment.
- Barnes et al. (2000) — introduction du NDRE pour la détection précoce des variations de chlorophylle en canopée fermée.
- *Optimized spectral indices for global vegetation and water mapping* : https://pmc.ncbi.nlm.nih.gov/articles/PMC12864911/

### 4.3 Hétérogénéité des pixels et robustesse du diagnostic

Les espaces verts urbains sont fragmentés : l'organisation spatiale des strates influence
fortement la précision des indices Sentinel-2 (HAL hal-05350772), et la réponse des arbres
urbains à la sécheresse dépend aussi du taux d'imperméabilisation (HAL hal-05193142).
L'écart-type et l'amplitude max‑min sont donc traités comme des signaux de premier ordre
(foyers de stress, clairières), et non comme des indicateurs secondaires.

Ces mêmes travaux constatent des séries incomplètes (nuages, bandes 20 m non extraites sur
petites parcelles). D'où la dégradation gracieuse : renormalisation des poids, diagnostic
limité explicite, hétérogénéité neutralisée et pénalité retirée quand la dispersion n'est
pas mesurable.

- HAL hal-05193142 — *Evaluation of the effects of drought and urbanization on urban trees using Sentinel-2 time series* : https://hal.science/hal-05193142/
- HAL hal-05350772 — *Urban trees species classification using Sentinel-2 and PlanetScope* : https://hal.science/hal-05350772

## 5. Dégradation gracieuse (règles implémentées)

| Donnée manquante | Comportement | Effet sur le résultat |
|---|---|---|
| SAVI | NDVI comme référence, seuils 0,22 / 0,55 | confiance ≤ Moyenne |
| NDRE | poids redistribué sur GNDVI + NDVI (somme renormalisée) | stress détecté via GNDVI seul |
| NDRE et GNDVI | diagnostic « limité » si NDVI > 0,60 | pas d'alerte précoce inventée |
| SAVI seul en couvert dense | poids 1,0 sur SAVI | pas de division par zéro |
| `std` | hétérogénéité « Non calculable (écart-type manquant) », pénalité = 0 | score non pénalisé |
| `min` / `max` | anomalie = `null` | pas de faux positif sur 1 pixel |
| `sampleCount` < 2 | hétérogénéité « Non calculable (pixels insuffisants) », anomalie = `null` | un `std = 0` sur 1 pixel n'est pas interprété comme « homogène » |
| SAVI et NDVI | recouvrement « Données insuffisantes », score `null`, confiance `Nulle` | évaluation refusée explicitement |

## 6. Limites et calibration

- Les **seuils** (0,18 / 0,42, 0,60 / 0,32 / 0,48, 0,16 / 0,09, 0,50) sont des valeurs de
  travail regroupées en constantes dans `VegetationEvaluator`. Elles ne sont pas issues
  telles quelles de la littérature : elles doivent être calibrées sur des parcs de la MEL
  avec vérité terrain (relevés de terrain, LAI, inspections phytosanitaires).
- Le score est **relatif à une zone**, pas une mesure absolue de santé : il agrège des
  statistiques de réflectance, pas des processus physiologiques.
- Les pixels mixtes restent la source d'incertitude principale ; le masque SCL écarte
  nuages/ombres/eau mais pas le bâti inclus dans un pixel de canopée.
- L'agrégation est faite par le fournisseur : on ne voit ni la distribution des pixels, ni
  les scènes retenues. Une valeur aberrante de `min`/`max` peut venir d'un pixel isolé de
  la mosaïque, non d'un vrai défaut de la zone.
- Pas d'**imputation temporelle** à ce jour : une fenêtre sans image exploitable donne
  « aucune donnée », pas une valeur estimée depuis l'année précédente.

## 7. Références

Sources HAL (prioritaires) :

1. Thèse — *Évaluation des séries temporelles d'images Sentinel-2 pour la caractérisation et le suivi de l'état de santé de la végétation arborée en milieu urbain* — https://theses.hal.science/tel-05407531
2. Le Saint et al. — *Evaluation of the effects of drought and urbanization on urban trees using Sentinel-2 time series* — https://hal.science/hal-05193142/
3. *Urban trees species classification using Sentinel-2 and PlanetScope* — https://hal.science/hal-05350772
4. Parmentier (UMR EVS) — *Calcul d'indices avec QGIS sur image Sentinel-2A : de l'indice de végétation (NDVI) à celui de la construction (NDBI)* — https://hal.science/hal-03888173
5. *Sentinel-2 for High Resolution Mapping of Slope-Based Vegetation Indices* — https://hal.science/hal-03041269

Littérature de référence :

6. Huete, A. R. (1988). A soil-adjusted vegetation index (SAVI). *Remote Sensing of Environment*.
7. Gitelson, A. A., Kaufman, Y. J., & Merzlyak, M. N. (1996). Use of a green channel in remote sensing of global vegetation. *Remote Sensing of Environment*.
8. Barnes, E. M. et al. (2000). Coincident detection of crop water stress, nitrogen status and canopy density using ground-based multispectral data (NDRE).
9. *Optimized spectral indices for global vegetation and water mapping* — https://pmc.ncbi.nlm.nih.gov/articles/PMC12864911/
