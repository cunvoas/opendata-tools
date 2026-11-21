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

## Lecture à deux niveaux

### Pour le grand public

Un satellite ne mesure pas directement la santé d'un arbre : il mesure la lumière que les feuilles renvoient. Sentinel-2 observe chaque parc dans le visible et le proche infrarouge. Une feuille active absorbe fortement le rouge et réfléchit fortement le proche infrarouge ; un sol nu, du béton, une ombre ou de l'eau ne se comportent pas de la même façon. Le projet combine quatre indices complémentaires : le NDVI (vigueur et densité de la végétation), le SAVI (la même idée, corrigée de la réflexion du sol), le NDRE (teneur en chlorophylle des canopées denses) et le GNDVI (activité chlorophyllienne, stress hydrique ou azoté). Copernicus calcule pour chaque parc la moyenne, le minimum, le maximum et l'écart-type de ces indices, sans transmettre d'image. `VegetationEvaluator` transforme ensuite ces chiffres en une lecture simple : le parc est-il minéral, clairsemé ou densément végétalisé ; quel score de 0 à 100 lui donner ; existe-t-il un signe de stress ; la végétation est-elle homogène ; et quel niveau de confiance accorder au résultat. C'est un indicateur de télédétection, pas un diagnostic phytosanitaire : il aide à repérer les parcs à examiner sur le terrain, il ne remplace pas une expertise.

### Lecture détaillée et justification

#### 1. Ce que le calcul reçoit

Le calcul ne part pas d'une image mais de statistiques zonales. `ClientCopernicusNdvi` appelle l'API Statistical de Copernicus Data Space Ecosystem sur les réflectances de surface Sentinel-2 L2A. L'evalscript calcule, pixel par pixel, les quatre indices, puis Sentinel Hub agrège les valeurs sur le polygone du parc. `NdviStatistics` transporte la moyenne, le minimum, le maximum, l'écart-type et le nombre de pixels valides (`sampleCount`). `VegetationEvaluator` ne voit donc ni la distribution complète des pixels ni les scènes individuelles : il travaille sur des résumés. C'est une limite de fond, pas un détail d'implémentation.

| Indice | Bandes | Formule | Ce qu'il apporte |
|---|---|---|---|
| NDVI | B08, B04 | `(B08 − B04) / (B08 + B04)` | vigueur et densité de la végétation |
| SAVI | B08, B04 | `((B08 − B04) / (B08 + B04 + 0,5)) × 1,5` | NDVI corrigé de la réflectance du sol (`L = 0,5`) |
| NDRE | B08, B05 | `(B08 − B05) / (B08 + B05)` | chlorophylle des couverts denses, moins saturé que le NDVI |
| GNDVI | B08, B03 | `(B08 − B03) / (B08 + B03)` | activité chlorophyllienne, stress hydrique ou azoté |

Les pixels nuageux, ombres, eau et pixels saturés sont exclus par le masque SCL (`[1, 3, 6, 8, 9, 10]`). La sortie est demandée à 10 m, sur une grille plafonnée à 512 × 512 pixels pour rester sous 1 Processing Unit par appel. Le SAVI reprend la correction du sol de Huete (1988), avec la valeur usuelle `L = 0,5` pour un couvert intermédiaire.

#### 2. Le recouvrement : minéral, clairsemé ou dense

Le SAVI sert de référence de couvert, parce qu'il est le seul des quatre indices à corriger explicitement la réflexion du sol. En son absence, le NDVI prend le relais avec des seuils relevés pour compenser le biais minéral.

| Référence | Seuil minéral | Seuil dense | Lecture |
|---|---|---|---|
| SAVI | 0,18 | 0,42 | référence prioritaire |
| NDVI (repli) | 0,22 | 0,55 | seuils relevés car le NDVI est tiré vers le bas par le sol |

Sous le seuil minéral, le parc est classé « Minéral, bâti, ombre dense ou sol nu » et le score est fixé à 0 : il n'y a pas de couvert végétal actif à évaluer. Entre les deux seuils, le parc est « Couvert faible / Pelouse ouverte ou arbustif jeune ». Au-dessus, il est « Couvert dense / Strate arborée ou parc mature ».

**Honnêteté sur les seuils :** ces valeurs (0,18 / 0,42 / 0,22 / 0,55) sont des valeurs de travail, pas des seuils extraits tels quels de la littérature. Elles sont regroupées en constantes dans `VegetationEvaluator` et demandent une calibration sur des parcs de la métropole avec vérité terrain (relevés de terrain, LAI, inspections phytosanitaires). La littérature justifie le choix des indices et leur physique, pas la valeur exacte de chaque seuil.

#### 3. La vigueur : pondération dynamique des indices

La vigueur n'est pas une simple moyenne des quatre indices. Les poids changent selon le type de couvert, parce que les indices n'ont pas la même information au même endroit :

| Couvert | Pondération | Pourquoi |
|---|---|---|
| Faible | `0,65 × SAVI + 0,35 × NDVI` | en couvert clairsemé, la structure et le sol dominent ; le SAVI est le plus fiable |
| Dense | `0,50 × NDRE + 0,30 × GNDVI + 0,20 × NDVI` | en couvert dense, le NDVI sature ; l'information migre vers la chlorophylle (red-edge et vert) |

Les poids sont renormalisés sur les indices réellement présents : si le NDRE manque, son poids est redistribué sur le GNDVI et le NDVI, et la somme des poids reste égale à 1. Si seul le SAVI est disponible en couvert dense, il porte toute la vigueur. Cette dégradation gracieuse évite de produire une valeur inventée quand une bande manque.

La physique derrière ces poids est documentée : le SAVI neutralise la contribution du sol (Huete, 1988 ; HAL hal-03041269 ; HAL hal-03888173), le NDRE reste sensible à la chlorophylle à forte biomasse (Barnes et al., 2000), et le GNDVI suit la concentration en chlorophylle (Gitelson, Kaufman & Merzlyak, 1996). La thèse HAL tel-05407531, qui modélise le transfert radiatif en milieu urbain (DART, Rennes), conclut que les indices ajustés au sol et les indices chlorophylliens sont les plus fiables pour estimer le LAI et détecter les stress en ville.

#### 4. Le diagnostic physiologique

Le diagnostic ne se déclenche que si le NDVI moyen dépasse 0,60, c'est-à-dire si la biomasse est suffisante pour que le NDVI puisse masquer un stress. Deux règles s'enchaînent :

1. si le NDRE moyen est inférieur à 0,32 : « Alerte : Déficit chlorophyllien canopée (NDRE bas) » ;
2. sinon, si le GNDVI moyen est inférieur à 0,48 : « Alerte : Stress hydrique ou azoté (GNDVI bas) ».

Si le NDVI est fort mais que le NDRE et le GNDVI manquent tous les deux, le diagnostic est explicitement dégradé en « Diagnostic limité (NDRE et GNDVI manquants pour détecter un stress précoce) ». Si le NDVI est faible, aucune alerte physiologique n'est produite : le diagnostic est volontairement réservé aux canopées dont le NDVI est assez élevé pour masquer un stress.

**Limite :** ces règles sont des heuristiques. Un NDRE bas peut signaler une maladie, une carence, un tassement de sol ou un stress hydrique ; le code ne distingue pas ces causes. Il alerte sur un symptôme, pas sur une cause. Les seuils 0,60 / 0,32 / 0,48 sont également des valeurs de travail, non calibrées sur la MEL.

#### 5. L'hétérogénéité et l'anomalie locale

L'écart-type mesure la dispersion spatiale des valeurs à l'intérieur du polygone (avec un résidu temporel possible là où plusieurs scènes sont de qualité équivalente). Il est lu d'abord sur le SAVI, puis sur le NDVI en repli :

- `std > 0,16` : « Très hétérogène » ;
- `std > 0,09` : « Modérément hétérogène » ;
- sinon : « Homogène ».

L'anomalie locale compare `max − min` : une amplitude supérieure à 0,50 déclenche `anomalieLocaleDetectee = true`. Elle est calculée sur le NDVI, puis sur le SAVI si le NDVI n'a pas de bornes.

Deux garde-fous importants : si `sampleCount` vaut 0 ou 1, l'écart-type et l'amplitude ne sont pas interprétés (`Non calculable (pixels insuffisants)`, anomalie `null`), car un polygone d'un seul pixel renvoie un écart-type nul qui ne signifie pas « homogène ». Si `sampleCount` est inconnu (`null`), l'analyse est conservée : le code ne bloque pas faute d'information. Si l'écart-type manque, l'homogénéité est `Non calculable (écart-type manquant)` et aucune pénalité n'est appliquée. Ces garde-fous sont justifiés par la fragmentation des espaces verts urbains et la sensibilité des indices à l'organisation spatiale des strates (HAL hal-05350772), ainsi que par la réponse des arbres urbains à la sécheresse et à l'imperméabilisation (HAL hal-05193142).

#### 6. Le score et la confiance

Le score synthétique est :

```
score = clamp(vigueur × 100 − 20 × écart_type_de_référence, 0, 100)
```

Il est arrondi au dixième. La pénalité d'irrégularité ne s'applique que si l'écart-type est mesurable. Le score est donc un indicateur relatif de vigueur et d'homogénéité, pas une mesure absolue de santé : il agrège des réflectances, pas des processus physiologiques.

Le niveau de confiance dépend du nombre d'indices disponibles :

| Indices disponibles | Confiance |
|---|---|
| 4 | Élevée |
| 2 ou 3 | Moyenne |
| 1 | Faible |
| aucun SAVI/NDVI | Nulle (score `null`, évaluation refusée) |

Le SAVI ou le NDVI est indispensable : sans l'un des deux, l'évaluation est refusée (`Nulle`) même si le NDRE et le GNDVI sont présents.

#### 7. La fenêtre temporelle : ce qui est réellement implémenté

Une description fréquente de ce projet annonce « une fenêtre de mai à la deuxième semaine de juin, par tranches hebdomadaires, pour repérer le pic de vigueur ». **Ce n'est pas ce que fait le code actuel.** Voici le comportement réel de `ClientCopernicusNdvi` :

- une seule requête Statistical par fenêtre ; si la fenêtre principale ne renvoie rien, une seconde requête est faite sur la fenêtre de secours ;
- un seul `aggregationInterval` couvrant toute la fenêtre, pas une boucle hebdomadaire ;
- `mosaickingOrder: leastCC` : Sentinel Hub sélectionne les scènes les moins nuageuses sur la fenêtre ;
- masque SCL `[1, 3, 6, 8, 9, 10]` : pixels saturés/défectueux, ombres de nuages, eau, nuages moyens et hauts, cirrus fins ;
- année demandée : année précédente (`LocalDate.now().getYear() - 1`), pour disposer de l'archive L2A complète ;
- fenêtre principale dans `application.yml` : **1er–15 juin** ; fenêtre de secours : **16–30 juin** ;
- les valeurs par défaut codées dans les annotations `@Value` sont différentes (1–6 juin, repli 1–6 juillet), mais le YAML les remplace. En configuration standard, ce sont donc les bornes 1–15 juin et 16–30 juin qui s'appliquent.

Il n'y a donc **ni découpage hebdomadaire, ni sélection d'une scène par semaine, ni détection d'un pic phénologique**. La sélection des scènes est déléguée au mosaïquage `leastCC`. La fréquence de repasse de Sentinel-2 (de l'ordre de quelques jours) n'est pas exploitée comme une série temporelle : elle sert au mosaïquage, pas à un suivi intra-saisonnier. Le protocole est plus simple que celui décrit dans le texte cité. Les arguments de fond — éviter la sécheresse estivale, viser une canopée déployée, tenir compte de la nébulosité — restent valables ; ce sont les mécanismes annoncés qui ne sont pas implémentés.

Le choix de juin reste défendable : à cette période, la canopée des essences feuillues est déployée et les strates herbacées n'ont pas encore subi le jaunissement des sécheresses de juillet-août. C'est un compromis raisonnable entre phénologie et nébulosité dans le Nord de la France. Mais c'est un **choix opérationnel de configuration**, pas un protocole validé par une publication. Il ne « renforce » pas à lui seul la validité scientifique : il réduit un biais saisonnier à condition que la même fenêtre soit appliquée à toutes les mesures comparées.

La période réellement stockée doit être lue avec chaque mesure. Le framing de restitution relève une période observée du 8 au 16 juin 2025, qui ne correspond pas exactement aux bornes configurées aujourd'hui (1–15 juin). La requête utilise une borne de fin exclusive (`endExclusive = to + 1 jour`), ce qui peut expliquer un `period_end` au 16 pour une fenêtre finissant le 15 ; quoi qu'il en soit, la configuration a pu changer entre campagnes. Toute comparaison doit s'appuyer sur `period_start` et `period_end` de `ndvi_result`, pas sur une hypothèse de fenêtre fixe.

#### 8. Ce que la méthode permet — et ne permet pas de conclure

**Ce qu'elle permet :** produire, pour chaque parc, un instantané reproductible de l'état du couvert à une période donnée, avec quatre indices complémentaires, une lecture de recouvrement, un score relatif, un diagnostic de stress débutant, une mesure d'hétérogénéité et un niveau de confiance. La mesure est objective et reproductible : le NDVI a été validé comme mesure de verdure de quartier contre une cotation d'experts (Rhew et al., 2011, r ≈ 0,69), et la formule, la plage de valeurs et la sensibilité aux nuages sont documentées par la NASA Earth Observatory.

**Ce qu'elle ne permet pas :** conclure à une évolution temporelle. Le backend enregistre une mesure dans `ndvi_result` et s'arrête là : `hasResult` empêche un nouvel appel tant qu'une mesure existe, sauf relance forcée. Il n'y a donc **pas de série annuelle automatique**. Les colonnes NDVI de `park_area_computed` ne sont plus alimentées (le commentaire de `NdviBatchJobService` est explicite), et la table `ndvi_observation` existe encore dans le schéma mais n'est pas alimentée par le code actuel. Le framing `ndvi-par-parc-park-area-computed` (v1.3) décrit encore une recopie sur toutes les années de `park_area_computed` ; le code actuel a évolué et c'est `ndvi_result` qui fait foi. Les millésimes 2015, 2017, 2019 et 2021 sont des années de référence INSEE utilisées par l'application, pas quatre acquisitions satellite. Comparer 2015 et 2021 dans les données exposées ne mesure donc pas l'évolution du couvert : au mieux, cela répète la même mesure ; au pire, l'année 2015 n'a aucune donnée. La période stockée dans `ndvi_result.period_start` / `period_end` permet de savoir ce qui a été mesuré, pas de reconstituer une série sans campagnes annuelles dédiées.

**Ce qu'elle ne remplace pas :** une calibration terrain. Les seuils, les poids et la pénalité d'écart-type sont des valeurs de travail. Le score n'a pas été validé contre des relevés de LAI, des inspections phytosanitaires ou des notes d'expert sur la MEL. Les pixels mixtes restent la première source d'incertitude : le masque SCL écarte les nuages, les ombres et l'eau, mais pas le bâti inclus dans un pixel de canopée. L'agrégation par le fournisseur masque la distribution des pixels et les scènes retenues. Enfin, le NDVI seul sature en couvert dense et reste sensible au sol en couvert ouvert, ce qui justifie le SAVI, le NDRE et le GNDVI, mais ne les rend pas parfaits (D'Silva, 2025 ; HAL tel-05407531).

La démarche est donc **rigoureuse sur la traçabilité et la dégradation explicite**, mais elle ne doit pas être présentée comme « quasi académique » : elle manque encore de calibration terrain, de validation croisée et de série temporelle réellement annuelle.

#### 9. Sources et ce qu'elles justifient

Les références complètes sont en section 7. Le tableau ci-dessous indique ce que chaque source soutient — et ce qu'elle ne soutient pas.

| Source | Ce qu'elle justifie dans ce document |
|---|---|
| Rhew et al. (2011), PMC3225119 | le NDVI comme mesure objective et reproductible de la verdure de quartier (r ≈ 0,69 vs cotation d'experts) ; pas un diagnostic physiologique |
| NASA Earth Observatory, *Measuring Vegetation* | la formule du NDVI, la plage −1..+1, la lecture des valeurs et la sensibilité aux nuages |
| D'Silva (2025), SSRN 5165379 | les limites du NDVI en zone urbaine dense et l'intérêt d'indices complémentaires ; l'EVI est cité comme piste, non implémenté ici |
| Huete (1988) | la correction du sol du SAVI et la valeur `L = 0,5` |
| HAL hal-03041269 ; HAL hal-03888173 | la comparaison NDVI/SAVI sur Sentinel-2 et la neutralisation de la réflectance des sols artificialisés |
| Barnes et al. (2000) | l'introduction du NDRE pour la détection précoce des variations de chlorophylle en canopée fermée |
| Gitelson, Kaufman & Merzlyak (1996) | le GNDVI et sa sensibilité à la chlorophylle |
| HAL tel-05407531 | la modélisation radiative 3D en milieu urbain et la fiabilité des indices ajustés au sol et chlorophylliens pour le LAI et les stress |
| HAL hal-05350772 ; HAL hal-05193142 | l'effet de l'organisation spatiale des strates et de l'imperméabilisation sur les indices et la réponse des arbres urbains à la sécheresse |
| PMC12864911 | la comparaison d'indices spectraux optimisés pour la végétation et l'eau ; contexte méthodologique, pas calibration des seuils |

**Ce que la littérature ne fournit pas :** les valeurs exactes 0,18 / 0,42 / 0,22 / 0,55 / 0,60 / 0,32 / 0,48 / 0,16 / 0,09 / 0,50. Ces constantes sont des choix d'ingénierie, à calibrer. Les citer comme si elles sortaient des articles serait une surinterprétation.

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

- Les **seuils** (0,18 / 0,42, 0,22 / 0,55, 0,60 / 0,32 / 0,48, 0,16 / 0,09, 0,50) sont des valeurs de
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
- La **fenêtre** est une configuration (1–15 juin, repli 16–30 juin), pas un découpage
  hebdomadaire : une seule agrégation `leastCC` par fenêtre, sans détection de pic
  phénologique.
- Pas de **série annuelle** : une mesure par parc en pratique dans `ndvi_result`, et
  `hasResult` empêche un nouvel appel tant qu'elle existe. Les millésimes
  2015/2017/2019/2021 sont des années INSEE, pas des campagnes NDVI : la comparaison
  2015/2021 n'est pas une mesure d'évolution.

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

Sources NDVI reprises des framings `recuperation-ndvi-parc` et `ndvi-parc-restitution` :

10. Rhew et al. (2011). *Validation of the Normalized Difference Vegetation Index as a Measure of Neighborhood Greenness* — https://pmc.ncbi.nlm.nih.gov/articles/PMC3225119/
11. D'Silva (2025). *Comparative NDVI and EVI Analysis of Urban Green Infrastructure in European Cities* — https://papers.ssrn.com/sol3/papers.cfm?abstract_id=5165379
12. NASA Earth Observatory. *Measuring Vegetation (NDVI & EVI)* — https://science.nasa.gov/earth/earth-observatory/measuring-vegetation-ndvi-evi/
