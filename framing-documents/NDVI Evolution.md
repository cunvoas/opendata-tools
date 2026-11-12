NDVI Evolution

La réponse NDVI, GNDVI, NDRE de copernicus est par mois/jour, il n'y a que la préiode de mesure mas pas l'années.

les réponses sont actuellement stockée dans l'objet ParkAreaComputed.
Je veux modifier en gardant les datas déjà récupérée, prévoir le script de migration puis le script de drop column que je jouerai moi même.

le nouvel objet aura les données et des IDs de ParkAreaComputed, de ParcsEtJardin, iris, cadastre, carre200m. plus le type d'objet ( ParkAreaComputed et ParcsEtJardin sont une déclinaison de la même motion de parc avec un état différent quand aux calculs).

seul les service  ParkAreaComputed, rest  ParkAreaComputed doivent évoluer. le reste sera réalisé ultérieurment

## Réalisé

- Objet `NdviResult` / table `ndvi_result` : `objectType`, `parkAreaComputedId`, `parcEtJardinId`, `irisId`, `cadastreId`, `carre200mId`, période et statistiques NDVI/NDRE/GNDVI.
- `NdviResultObjectType` : `PARK_AREA_COMPUTED`, `PARC_ET_JARDIN`, `IRIS`, `CADASTRE`, `CARRE_200M`.
- `NdviBatchJobService` écrit uniquement dans `ndvi_result`.
- `ComputeParkAreaV2` utilise `ndvi_result` et le dernier job traité.
- `NdviController` lit les résumés depuis `ndvi_result`.
- Migration `v2.2.0_ndvi_result.sql` + script de drop manuel.
- Présélection utilisateur sur `/mvc/ndvi`.
- Synthèse NDVI `/mvc/management/jobs/ndvi/progress` + template `ndviProgress.html`, sans année.
- Lien depuis la pastille NDVI du dashboard.
- Distinction par type : filtre et colonne `Type` (PARC, IRIS, CADASTRE, CARRE) dans la synthèse, dérivée de `ndvi_result.object_type`.
- Tests OK : `NdviBatchJobServiceTest` 11/11, `MvcNdviControlerTest` 4/4.



Indice  Nom complet Ce qu'il mesure spécifiquement  Utilité pour notre étude
NDVI    Normalized Difference Vegetation Index  La densité globale de la végétation et la présence de chlorophylle active (via le spectre Rouge).   Détecter la fracture verte : Distinguer instantanément les zones purement minérales/bétonnées des zones végétalisées dans les quartiers.
GNDVI   Green Normalized Difference Vegetation Index    La concentration en chlorophylle et le stress hydrique (via le spectre Vert). Plus sensible que le NDVI sur la végétation dense.    Évaluer la fraîcheur : Repérer si les arbres des parcs souffrent de la sécheresse ou s'ils sont assez vigoureux pour créer de vrais îlots de fraîcheur urbains.
NDRE  Normalized Difference Red Edge Index    La structure de la canopée et l'azote foliaire (via la bande de transition Red Edge). Pénètre plus profondément le feuillage.   Mesurer la qualité de la strate arborée : Différencier une simple pelouse pelée (faible volume de fraîcheur) d'une forêt urbaine dense ou de grands arbres capables de filtrer l'air.
