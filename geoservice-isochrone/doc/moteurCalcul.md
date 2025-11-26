

```mermaid
sequenceDiagram
    participant Scheduler
    participant BatchJobService
    participant ComputeJobCarreRepository
    participant ComputeCarreService
    participant ComputeJobIrisRepository
    participant ComputeIrisService

    Scheduler->>BatchJobService: processShapes()
    alt isJUnitTest() == true ou shutdownRequired == true
        BatchJobService-->>Scheduler: return (skip)
    else
        BatchJobService->>BatchJobService: launchOrFinish(true)
        alt possibleLaunch == true
            loop while possibleNext && possibleMax > 0
                BatchJobService->>ComputeJobCarreRepository: findByStatusOrderByDemand(TO_PROCESS, page)
                ComputeJobCarreRepository-->>BatchJobService: List<ComputeJob>
                loop for each ComputeJob
                    BatchJobService->>ComputeJobCarreRepository: save(IN_PROCESS)
                    BatchJobService->>ComputeCarreService: computeCarreByComputeJob(job)
                    ComputeCarreService-->>BatchJobService: processed (Boolean)
                    alt processed == true
                        BatchJobService->>ComputeJobCarreRepository: save(PROCESSED)
                    else
                        BatchJobService->>ComputeJobCarreRepository: save(IN_ERROR)
                    end
                end
                BatchJobService->>ComputeJobIrisRepository: findByStatusOrderByDemand(TO_PROCESS, page)
                ComputeJobIrisRepository-->>BatchJobService: List<ComputeIrisJob>
                loop for each ComputeIrisJob
                    BatchJobService->>ComputeJobIrisRepository: save(IN_PROCESS)
                    BatchJobService->>ComputeIrisService: computeIrisByComputeJob(irisJob)
                    ComputeIrisService-->>BatchJobService: processed (Boolean)
                    alt processed == true
                        BatchJobService->>ComputeJobIrisRepository: save(PROCESSED)
                    else
                        BatchJobService->>ComputeJobIrisRepository: save(IN_ERROR)
                    end
                end
                alt shutdownRequired == true
                    BatchJobService-->>Scheduler: break
                end
            end
            BatchJobService->>BatchJobService: launchOrFinish(false)
        end
    end
```

---

## Explication de la méthode `processShapes`

La méthode `processShapes` est une tâche planifiée (annotée avec `@Scheduled`) qui traite les entités "Carre" et "Iris" à partir de la base de données. Elle fonctionne par lots (pagination) et effectue les étapes suivantes :

1. **Vérifications initiales** :  
   - Ignore l'exécution si le contexte est un test JUnit ou si un arrêt est demandé (`shutdownRequired`).

2. **Gestion de la concurrence** :  
   - Utilise `launchOrFinish(true)` pour s'assurer qu'aucun autre traitement n'est en cours.

3. **Traitement des jobs "Carre"** :  
   - Récupère les jobs à traiter via le repository, selon l'environnement (dev ou prod).
   - Pour chaque job :
     - Marque le job comme "IN_PROCESS".
     - Appelle le service de calcul (`computeCarreService`).
     - Met à jour le statut selon le résultat (PROCESSED ou IN_ERROR).

4. **Traitement des jobs "Iris"** :  
   - Même logique que pour les "Carre", mais avec le repository et service dédiés aux "Iris".

5. **Arrêt anticipé** :  
   - Si un arrêt est demandé pendant le traitement, la boucle s'interrompt.

6. **Fin du traitement** :  
   - Appelle `launchOrFinish(false)` pour libérer le verrou de concurrence.

---

Ce diagramme et cette explication permettent de visualiser le flux d'exécution et les interactions principales de la méthode `processShapes` dans le service `BatchJobService`.




