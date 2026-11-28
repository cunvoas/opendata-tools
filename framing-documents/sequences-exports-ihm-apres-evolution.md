# Séquences — Exports IHM après évolution

Document de séquences. Complète `revue-exports-ihm-admutil.md`.
Date : 2026-11-22.
Périmètre : `geoservice-isochrone-v2`, après implémentation de :
- la progression de production longue ;
- la séparation production / commit final ;
- le dashboard d'état des lieux paginé ;
- la portée `ADMINISTRATOR` / `ASSO_MANAGER` limitée à l'EPCI du gestionnaire.

Aucun diagramme ne décrit l'ancien flux « publication = push immédiat ».

## 1. Accès au dashboard et portée utilisateur

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Browser
    participant Ctl as ExportDashboardControler
    participant Access as AccessScopeService
    participant Dash as ExportDashboardService
    participant Git as GitHelper
    participant Repo as CommunauteCommuneRepository

    User->>Browser: GET /mvc/management/exports?page=0&size=10
    Browser->>Ctl: requête HTTP
    Ctl->>Ctl: @PreAuthorize ADMINISTRATOR ou ASSO_MANAGER
    alt rôle absent
        Ctl-->>Browser: 403
    else rôle présent
        Ctl->>Access: current()
        Access-->>Ctl: Contributeur
        alt contributeur non identifié
            Ctl-->>Browser: 403
        else contributeur identifié
            Ctl->>Access: scopedEpciId(contrib)
            alt ASSO_MANAGER sans idCommunauteCommune
                Ctl-->>Browser: 403 "Aucun EPCI associé"
            else périmètre valide
                Ctl->>Dash: scan(scope, page, size)
                Dash->>Git: state(root) et changedPaths(root)
                Git-->>Dash: HEAD, branche, fichiers modifiés
                alt scope non nul
                    Dash->>Repo: findById(scope)
                    Repo-->>Dash: un seul EPCI
                else scope nul (admin)
                    Dash->>Repo: findAll(PageRequest(page,size))
                    Repo-->>Dash: page d'EPCI + total
                end
                Dash->>Dash: buildEpciStates, blockers, snapshotHash
                Dash-->>Ctl: ExportDashboard
                Ctl->>Ctl: totalPages, pageSize autorisé 10/50/100
                Ctl-->>Browser: HTML paginé
            end
        end
    end
```

Points de contrôle :

- Un gestionnaire ne peut pas élargir la page : `scope` est imposé par `idCommunauteCommune`.
- Un administrateur voit la vue nationale, paginée.
- Les alertes sont rendues dans une zone scrollable limitée à 10 lignes.

## 2. Demande de calcul : EPCI, ville, parc

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Browser
    participant JobCtl as ComputeJobRestControler
    participant Token as TokenManagement
    participant Access as AccessScopeService
    participant Repo as Repositories
    participant Batch as BatchJobService

    User->>Browser: choisit EPCI ou ville ou parc + année
    Browser->>JobCtl: POST /mvc/ajax/jobs/api/request
    JobCtl->>JobCtl: @PreAuthorize ADMINISTRATOR ou ASSO_MANAGER
    JobCtl->>Token: isTokenValid(token)
    alt token invalide
        JobCtl-->>Browser: 401
    else token valide
        JobCtl->>JobCtl: compter les périmètres fournis
        alt zéro ou plusieurs périmètres
            JobCtl-->>Browser: 400
        else un seul périmètre
            alt année fournie et non configurée
                JobCtl-->>Browser: 400
            else année valide ou absente
                alt parkId
                    JobCtl->>Repo: ParkJardinRepository.findById
                    Repo-->>JobCtl: parc + commune
                    JobCtl->>Access: canAccessEpci(contrib, EPCI du parc)
                    alt hors périmètre
                        JobCtl-->>Browser: 403
                    else autorisé
                        JobCtl->>Batch: requestProcessParc(parc)
                        Batch-->>JobCtl: nombre de shapes
                        JobCtl-->>Browser: 202
                    end
                else cityId
                    JobCtl->>Repo: CityRepository.findById
                    Repo-->>JobCtl: ville + EPCI parent
                    JobCtl->>Access: canAccessEpci(contrib, EPCI parent)
                    alt hors périmètre
                        JobCtl-->>Browser: 403
                    else autorisé
                        JobCtl->>Batch: requestProcessCity(ville, année)
                        Batch-->>JobCtl: nombre de shapes
                        JobCtl-->>Browser: 202
                    end
                else com2coId
                    JobCtl->>Repo: CommunauteCommuneRepository.findById
                    Repo-->>JobCtl: EPCI
                    JobCtl->>Access: canAccessEpci(contrib, EPCI)
                    alt hors périmètre
                        JobCtl-->>Browser: 403
                    else autorisé
                        JobCtl->>Batch: requestProcessCom2Co(EPCI, année)
                        Batch-->>JobCtl: nombre de shapes
                        JobCtl-->>Browser: 202
                    end
                else parkOverpass
                    alt contributeur non ADMIN
                        JobCtl-->>Browser: 403
                    else ADMIN
                        JobCtl-->>Browser: 202
                    end
                end
            end
        end
    end
```

Règles :

- `ADMINISTRATOR` : tout EPCI.
- `ASSO_MANAGER` : uniquement l'EPCI `idCommunauteCommune`, y compris via une ville ou un parc rattaché.
- `parkOverpass` reste réservé à l'administrateur.
- Les entités sont résolues par `findById` : une entité absente renvoie `404`, pas une exception lazy.

## 3. Demande de publication d'un EPCI

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Browser
    participant PubCtl as PublishRestControler
    participant Token as TokenManagement
    participant Access as AccessScopeService
    participant Repo as CommunauteCommuneRepository
    participant Batch as BatchJobService
    participant Pub as PublishService

    User->>Browser: clic « Publier » sur un EPCI
    Browser->>PubCtl: POST /mvc/ajax/publish/api/request
    PubCtl->>PubCtl: @PreAuthorize ADMINISTRATOR ou ASSO_MANAGER
    PubCtl->>Token: isTokenValid(token)
    alt token invalide
        PubCtl-->>Browser: 401
    else token valide
        PubCtl->>PubCtl: valider requestedYear non nul et configuré
        alt année absente ou inconnue
            PubCtl-->>Browser: 400 avec message
        else année valide
            PubCtl->>Repo: findById(c2cId) via refusSiren
            alt EPCI absent
                PubCtl-->>Browser: 412 avec message
            else Siren absent
                PubCtl-->>Browser: 412 avec message
            else Siren présent
                PubCtl->>Access: canAccessEpci(contrib, c2cId)
                alt hors périmètre
                    PubCtl-->>Browser: 403
                else autorisé
                    PubCtl->>Batch: getProgressStatsEpciLevel(c2cId, null, année)
                    Batch-->>PubCtl: statistiques
                    alt aucune statistique
                        PubCtl-->>Browser: 404
                    else jobs incomplets ou en erreur
                        PubCtl-->>Browser: 412
                    else canPublish vrai
                        alt publication déjà en cours
                            PubCtl-->>Browser: 409
                        else libre
                            PubCtl->>Pub: prepare(c2cId, année)
                            Pub->>Pub: crée le statut avec 9 étapes
                            Pub-->>PubCtl: copie du statut
                            PubCtl->>Pub: publishAsync(c2cId, année)
                            Note over Pub: @Async rend la main immédiatement
                            PubCtl-->>Browser: 202
                            Browser->>Browser: ouvre le suivi de statut
                        end
                    end
                end
            end
        end
    end
```

## 4. Production asynchrone et progression

```mermaid
sequenceDiagram
    autonumber
    participant Browser
    participant PubCtl as PublishRestControler
    participant Pub as PublishService
    participant Repo as CommunauteCommuneRepository
    participant Stats as StatsSurfaceService
    participant Exp as ServicePublicationExporter
    participant FS as Fichiers JSON
    participant Status as PublicationStatus

    Note over Pub: thread @Async
    Pub->>Repo: findById(c2cId)
    Repo-->>Pub: EPCI
    alt Siren absent
        Pub->>Status: phase PRODUCTION_FAILED
    else Siren présent
        Pub->>Status: startStep(referentiel)
        Pub->>Exp: writeRegions()
        Exp->>FS: regions, com2cos, cities, densite
        Pub->>Status: endStep(referentiel)

        Pub->>Status: startStep(stats-cities)
        Pub->>Stats: writeStatsSurfaceByCom2CoIdAndAnnee(c2cId, année, callback)
        loop chaque ville
            Stats->>Status: city(index, total, insee, nom)
            Stats->>FS: stats_<insee>_<année>.json et .v2.json
        end
        Pub->>Status: endStep(stats-cities)

        Pub->>Status: startStep(stats-epci)
        Pub->>Stats: urbans + suburbs + all
        Stats->>FS: trois fichiers EPCI et .v2.json
        Pub->>Status: endStep(stats-epci)

        loop parkOutline, isochrone, carres, iris, ndvi
            Pub->>Status: startStep(code)
            Pub->>Exp: write...
            Exp->>FS: fichier GeoJSON ou NDVI
            Pub->>Status: endStep(code)
        end

        Pub->>Status: startStep(territoires)
        Pub->>Exp: writeTerritoires()
        Exp->>FS: data/territoires.json
        Pub->>Status: endStep(territoires)

        alt étape en exception
            Pub->>Status: failStep(code)
            Pub->>Status: phase PRODUCTION_FAILED
        else fichier attendu absent
            Pub->>Status: phase PRODUCTION_FAILED
        else production complète
            Pub->>Status: phase PRODUCED
            Note over Pub: aucun push Git ici
        end
    end
```

- Les étapes sont pondérées : référentiel 8 %, stats villes 27 %, stats EPCI 10 %, parkOutline 10 %, isochrone 15 %, carreaux 15 %, IRIS 5 %, NDVI 5 %, territoires 5 %.
- `x/y villes` est alimenté par le callback de `StatsSurfaceService`.
- L'ETA est recalculée à chaque mise à jour, mais reste indicative.
- `territoires.json` est écrit après les stats, car il lit les fichiers de stats, mais il est classé référentiel dans le dashboard et le commit.

## 5. Suivi de statut et timeline IHM

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant Page as editCommunauteCommune
    participant PubCtl as PublishRestControler
    participant Pub as PublishService
    participant Batch as BatchJobService

    loop tant que phase PRODUCING ou PUBLISHING
        Page->>PubCtl: POST /mvc/ajax/publish/api/status
        PubCtl->>PubCtl: token + rôle + portée EPCI
        alt token, rôle ou EPCI invalide
            PubCtl-->>Page: 401 ou 403
        else autorisé
            PubCtl->>Batch: getProgressStatsEpciLevel
            Batch-->>PubCtl: compteurs jobs
            PubCtl->>Pub: getStatus(c2cId, année)
            Pub-->>PubCtl: copie profonde du statut
            PubCtl-->>Page: phase, étapes, ville x/y, objets, ETA
            Page->>Page: met à jour la timeline
        end
        Page->>Page: attend 5 s
    end
```

La timeline affiche :

```text
✔ Référentiel            terminé
⟳ Stats par ville        12/95 villes
○ Park outline           en attente
○ Isochrones             en attente
○ Carreaux               en attente
○ IRIS                   en attente
○ NDVI                   en attente
○ Territoires            en attente
```

## 6. Commit final — administrateur

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant Browser
    participant DashCtl as ExportDashboardControler
    participant Access as AccessScopeService
    participant Dash as ExportDashboardService
    participant Git as GitHelper
    participant GH as Dépôt GitHub Pages

    Admin->>Browser: ouvre le dashboard
    Browser->>DashCtl: GET /mvc/management/exports
    DashCtl->>Access: scopedEpciId(admin)
    Access-->>DashCtl: null
    DashCtl->>Dash: scan(null, page, size)
    Dash-->>DashCtl: vue nationale + fichiers modifiés + snapshotHash
    DashCtl-->>Browser: HTML paginé
    Admin->>Browser: vérifie alertes et matrice
    Admin->>Browser: clic « Commit final »
    Browser->>DashCtl: POST /mvc/ajax/publish/api/commit
    DashCtl->>DashCtl: @PreAuthorize ADMIN ou ASSO_MANAGER
    DashCtl->>DashCtl: token + rôle + portée
    DashCtl->>Dash: commit(snapshotHash, message, null)
    Dash->>Dash: rescan(page 0, taille 1)
    alt hash différent
        Dash-->>DashCtl: IllegalStateException
        DashCtl-->>Browser: 409
    else blockers non vides
        DashCtl-->>Browser: 409
    else aucun fichier
        DashCtl-->>Browser: 409
    else état stable
        Dash->>Git: commitFiles(root, liste exacte, message)
        Git->>Git: vérifie l'index hors périmètre
        Git->>Git: add/rm uniquement les fichiers listés
        Git->>Git: commit
        alt credentials + feature activés
            Git->>GH: push
            GH-->>Git: OK
            Git-->>Dash: SHA + pushed=true
        else non configuré
            Git-->>Dash: SHA + pushed=false
        end
        Dash-->>DashCtl: CommitResult
        DashCtl-->>Browser: 200 + SHA + message
    end
```

## 7. Commit final — gestionnaire limité à son EPCI

```mermaid
sequenceDiagram
    autonumber
    actor Manager
    participant Browser
    participant DashCtl as ExportDashboardControler
    participant Access as AccessScopeService
    participant Dash as ExportDashboardService
    participant Git as GitHelper

    Manager->>Browser: GET /mvc/management/exports
    Browser->>DashCtl: requête
    DashCtl->>Access: current() + scopedEpciId
    Access-->>DashCtl: idCommunauteCommune du manager
    alt aucun EPCI associé
        DashCtl-->>Browser: 403
    else EPCI présent
        DashCtl->>Dash: scan(scope, page, size)
        Dash->>Dash: filtre changedPaths sur l'EPCI
        Note over Dash: GeoJSON / stats EPCI / NDVI / stats des villes de l'EPCI
        Note over Dash: référentiel global exclu
        Dash-->>DashCtl: dashboard limité
        DashCtl-->>Browser: HTML avec périmètre affiché
        Manager->>Browser: clic « Commit final »
        Browser->>DashCtl: POST /mvc/ajax/publish/api/commit
        DashCtl->>Dash: commit(snapshotHash, message, scope)
        Dash->>Dash: rescan filtré sur scope
        alt hash différent ou blockers
            DashCtl-->>Browser: 409
        else état stable
            Dash->>Git: commitFiles(liste des fichiers de l'EPCI)
            Git-->>Dash: SHA
            Dash-->>Browser: 200 + SHA
        end
    end
```

Un gestionnaire ne peut pas commiter :

- `data/regions.json` ;
- `data/com2cos/**` ;
- `data/cities/**` ;
- `data/territoires.json` ;
- les fichiers d'un autre EPCI.

## 8. Référentiel — administrateur uniquement

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant Browser
    participant PubCtl as PublishRestControler
    participant Token as TokenManagement
    participant Access as AccessScopeService
    participant Pub as PublishService
    participant Exp as ServicePublicationExporter
    participant Dash as Dashboard

    Admin->>Browser: clic « Publier le référentiel »
    Browser->>PubCtl: POST /mvc/ajax/publish/api/referential
    PubCtl->>PubCtl: @PreAuthorize ADMINISTRATOR
    PubCtl->>Token: isTokenValid(token)
    alt token invalide
        PubCtl-->>Browser: 401
    else token valide
        PubCtl->>Access: isAdmin(contrib)
        alt non admin
            PubCtl-->>Browser: 403
        else admin
            PubCtl->>Pub: publishReferentialAsync()
            PubCtl-->>Browser: 202
            Pub->>Exp: writeRegions()
            Pub->>Exp: writeTerritoires()
            Exp-->>Dash: fichiers référentiel en attente
            Note over Pub,Dash: aucun push ici, commit final via dashboard
        end
    end
```

## 9. Table des responsabilités

| Composant | Rôle après évolution |
|---|---|
| `ComputeJobRestControler` | Valide token, périmètre unique, année, appartenance EPCI, puis crée les jobs. |
| `BatchJobService` | File de jobs, calculs, progression. |
| `PublishRestControler` | Valide token, année, Siren, rôle, portée EPCI, `canPublish`, verrou de publication. |
| `PublishService` | Produit les fichiers, met à jour `PublicationStatus`, s'arrête à `PRODUCED`. |
| `PublicationStatus` | Étapes, ville x/y, objets, fichiers, ETA. |
| `ExportDashboardControler` | Portée admin/manager, pagination 10/50/100. |
| `ExportDashboardService` | Scan, filtrage EPCI, blockers, hash de snapshot, commit final. |
| `GitHelper` | Commit d'une liste exacte, contrôle de l'index, push conditionnel. |
| `AccessScopeService` | Source unique de la portée `ADMINISTRATOR` / `ASSO_MANAGER`. |

## 10. Codes de retour

| Code | Cas |
|---|---|
| `202` | Calcul ou production accepté. |
| `400` | Périmètre multiple/absent, année absente ou inconnue. |
| `401` | Token invalide. |
| `403` | Rôle insuffisant, EPCI hors périmètre, manager sans EPCI, fichier interdit. |
| `404` | EPCI, ville ou parc inexistant ; aucune statistique de publication. |
| `409` | Publication déjà en cours, snapshot périmé, blockers, aucun fichier. |
| `412` | Siren absent, jobs incomplets ou en erreur. |
