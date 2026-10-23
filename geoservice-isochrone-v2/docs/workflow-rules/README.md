# Architecture Rules Index

| Composant | Type | Résumé | Fichier |
|---|---|---|---|
| BatchJobService | Scheduled | Batch carre/IRIS : recycle IN_PROCESS/IN_ERROR, processShapes 300s, CPU overheat gate | [batch-job-service.md](./batch-job-service.md) |
| CityAssociationControler | HTTP | Admin associate/dissociate commune↔EPCI + uploadCsv batch, role ADMIN gate | [city-association-controler.md](./city-association-controler.md) |
| ComputeJobRestControler | HTTP | File de calcul par lots : requestProcessParc/City/Com2Co, token + role ADMIN | [compute-job-rest-controler.md](./compute-job-rest-controler.md) |
| ContributeurControler | HTTP | CRUD contributeur : password policy Argon2, reset mail, role scope | [contributeur-controler.md](./contributeur-controler.md) |
| ConsultMapController | HTTP | Consultation carte : cascade region/EPCI/commune, autoLocate preset | [consult-map-controller.md](./consult-map-controller.md) |
| EntranceControler | HTTP | Gestion entrées de parc : merge isochrone polygons, refresh IGN isochrone | [entrance-controler.md](./entrance-controler.md) |
| FileExporterControler | HTTP | Export GeoJSON carreaux : key + IP allowlist (buggy), write files | [file-exporter-controler.md](./file-exporter-controler.md) |
| ManualProposalController | HTTP | Proposition manuelle de parcs : save/delete, mode CIRCLE/POLY, meta MANUAL | [manual-proposal-controller.md](./manual-proposal-controller.md) |
| ManualProposalGeoJsonController | HTTP | Évaluation impact propositions manuelles : seuils OMS, save ManualEvalWork | [manual-proposal-geojson-controller.md](./manual-proposal-geojson-controller.md) |
| ParkEditControler | HTTP | @Deprecated edit parc prefecture : update processed flag | [park-edit-controler.md](./park-edit-controler.md) |
| ParkNewControler | HTTP | Création parc : cadastre relocate, OMS default, photo EXIF, token | [park-new-controler.md](./park-new-controler.md) |
| ParksPropositionController | HTTP | Calcul propositions auto : 6 algorithmes (ITERATIVE/PPC/CHI2/GENETIC), seuils OMS dense/peri | [parks-proposition-controller.md](./parks-proposition-controller.md) |
| ProjectSimulatorController | HTTP | Simulation projet : IGN isochrone influence, impact carreaux, mode GEOMETRY_REDUCER_ISOCHRONE | [project-simulator-controller.md](./project-simulator-controller.md) |
| PublishRestControler | HTTP | Publication EPCI : canPublish gate (jobs done), GeoJSON export + stats OMS bands | [publish-rest-controler.md](./publish-rest-controler.md) |
