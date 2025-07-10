# Calcul des densité des parcs par habitants

## Processus batch de traitement des travaux à calculer

```
        @Scheduled (4000s)
        BatchJobService.processShapes()
            préparation d'un lot de 100 éléments de calcul
            recherche d'une liste de ComputeJob (tache de traitement atomique par carré-année)
	        
	        // test si serveur principal ou poste de soutien au calcul
	        si serveur pricipal, on prend les 10 éléments ComputeJob les plus anciens
	        si poste de soutien, on prend les 10 éléments ComputeJob les plus récents
	        
	        pour chaque ComputeJob
	            déclaration de début de traitement
	            appel au service computeCarreService pour calculer l'élément
	            application du nouveau status
	            sauvegrade
	        fin ComputeJob
	        
	        // test si serveur principal ou poste de soutien au calcul
	        si serveur pricipal, on prend les 10 éléments ComputeIrisJob les plus anciens
	        si poste de soutien, on prend les 10 éléments ComputeIrisJob les plus récents
	        
	        pour chaque ComputeIrisJob
	            déclaration de début de traitement
	            appel au service computIrisService pour calculer l'élément
	            application du nouveau status
	            sauvegrade
	        fin ComputeIrisJob
```

## Calcul pour les IRIS

```
        ComputeIrisJob.computeIrisByComputeJob(ComputeIrisJob)
            récupération de l'iris
            récupération de la densité
            this.computeIrisShape(job, irisShape, isDense)
                recherche des la liste des parcs et jardin en accessibilité (ParkArea)
                préparation de l'agglomérat
                pour chaque ParkArea
                    controle des dates de création-destruction vs année à calculer
                    récupération de l'aire de parc calculé (ParkAreaComputed)
                    s'il n'est pas calculé
                        calcul du ParkAreaComputed
                    ajout de la forme d'accéssibilité à l'agglomérat
                    ajout de la surface de parc disponible
                    
                    
                fin ParkArea   
            
            
        
```

## reverse
```
sequenceDiagram : computeIrisShape
    participant Client
    participant ComputeIrisServiceIris
    participant ParkAreaRepository
    participant ParkTypeService
    participant ParkAreaComputedRepository
    participant ComputeParkAreaV2
    participant ApplicationBusinessProperties
    participant inseeCarre200mComputedV2Repository

    Client->>ComputeIrisServiceIris: computeIrisShape(job, carreShape, isDense)
    ComputeIrisServiceIris->>ParkAreaRepository: findParkInMapArea(carreShape.getContour())
    ParkAreaRepository-->>ComputeIrisServiceIris: List<ParkArea>
    ComputeIrisServiceIris->>ParkTypeService: populate(parkAreasInIris)
    loop for each ParkArea
        ComputeIrisServiceIris->>ParkAreaComputedRepository: findByIdAndAnnee(parkArea.getId(), annee)
        alt if not present
            ComputeIrisServiceIris->>ComputeParkAreaV2: computeGenericParkAreaV2(parkArea, annee)
            ComputeParkAreaV2-->>ComputeIrisServiceIris: ParkAreaComputed
        end
        ComputeIrisServiceIris->>ApplicationBusinessProperties: getRecoAtLeastParkSurface()
        Note right of ComputeIrisServiceIris: Mise à jour des surfaces, unions de polygones, noms, etc.
    end
    ComputeIrisServiceIris->>inseeCarre200mComputedV2Repository: findByAnneeAndIris(annee, carreShape.getIris())
    alt if null
        ComputeIrisServiceIris->>inseeCarre200mComputedV2Repository: new IrisDataComputed()
    end
    ComputeIrisServiceIris->>ComputeIrisServiceIris: computePopAndDensity(dto, carreShape, shapeParkOnIris)
    ComputeIrisServiceIris->>inseeCarre200mComputedV2Repository: save(irisComputed)


```



```
sequenceDiagram: computePopAndDensityDetail

    participant ComputeIrisServiceIris
    participant IrisShapeRepository
    participant IrisDataRepository

    ComputeIrisServiceIris->>IrisShapeRepository: findIrisInMapArea(geometryToAnalyse)
    IrisShapeRepository-->>ComputeIrisServiceIris: List<IrisShape> shapesWithIso

    loop for each carreWithIso in shapesWithIso
        ComputeIrisServiceIris->>IrisDataRepository: findByAnneeAndIris(dto.annee, carreWithIso.getIris())
        IrisDataRepository-->>ComputeIrisServiceIris: IrisData irisData

        alt irisData != null
            ComputeIrisServiceIris->>ComputeIrisServiceIris: getSurface(isoOnCarre)
            note right of ComputeIrisServiceIris: Calcule surfacePopulationIso
        else
            ComputeIrisServiceIris->>ComputeIrisServiceIris: log "IrisData NOT FOUND"
        end
    end

    alt surfacePopulationIso != 0
        ComputeIrisServiceIris->>ComputeIrisServiceIris: crDto.surfaceParkPerCapita = ...
    end
    ComputeIrisServiceIris->>ComputeIrisServiceIris: crDto.populationInIsochrone = ...

    ComputeIrisServiceIris->>ComputeIrisServiceIris: getSurface(parkOnCarre)
    ComputeIrisServiceIris->>ComputeIrisServiceIris: Calcule popInc et popExc

    alt dto.polygonParkAreasSustainableOms != null
        ComputeIrisServiceIris->>ComputeIrisServiceIris: getSurface(parkSustainable)
        ComputeIrisServiceIris->>ComputeIrisServiceIris: Calcule popWithSufficient
    end

    ComputeIrisServiceIris-->>ComputeIrisServiceIris: return crDto
```
