package com.github.cunvoas.geoserviceisochrone.service.solver.compute;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategie hybride chi2 + deficit complet, iterative carreau par carreau.
 *
 * <p><strong>Role du chi2 (moindres carres ponderes) :</strong></p>
 * <ul>
 *   <li>Le chi2 est utilise comme <em>filtre de convergence</em> : quand plus aucun carreau
 *       deficitaire ne fournit d'observations, la boucle s'arrete.</li>
 *   <li>Le chi2 n'est <strong>pas</strong> utilise pour plafonner le montant propose :
 *       la moyenne ponderee est systematiquement inferieure au deficit du pire carreau,
 *       ce qui sous-evaluerait chaque preconisation.</li>
 * </ul>
 *
 * <p><strong>Montant propose par iteration :</strong></p>
 * <ul>
 *   <li>Pour le carreau selectionne, on applique son <strong>deficit complet</strong> :
 *       {@code surface = (reco - surfaceParHab) * population}, borne a {@code CARRE_SURFACE}.</li>
 *   <li>Ce comportement est aligne sur {@link IterativeComputationDeficit1Strategy} (D1),
 *       ce qui elimine l'ecart de ~38 % observe avec l'ancienne formule.</li>
 * </ul>
 *
 * <p><strong>Algorithme (par iteration) :</strong></p>
 * <ol>
 *   <li>Construire les observations chi2 : carreaux avec population &gt; 0
 *       et {@code surfaceParHab &lt;= minSquareMeterPerCapita}.</li>
 *   <li>Si aucune observation, arret (plus de deficit).</li>
 *   <li>Ajuster un modele constant (degre 0) — resultat loggue, non utilise comme montant.</li>
 *   <li>Selectionner le carreau avec le plus grand deficit ({@code newMissingSurface} decroissant).</li>
 *   <li>Calculer {@code newParkSurface = deficitParHab * population}, borne a {@code CARRE_SURFACE}.
 *       Si {@code < MIN_PARK_SURFACE}, arret.</li>
 *   <li>Creer la {@link ParkProposal} et mettre a jour le carreau central
 *       ({@code newSurface}, {@code accessingSurface}, {@code surfacePerCapita},
 *       {@code newMissingSurface}).</li>
 *   <li>Propager {@code newParkSurface} aux voisins dans le rayon {@code urbanDistance}
 *       (memes mises a jour ; {@code newMissingSurface} borne a 0).</li>
 *   <li>Repeter jusqu'a epuisement des deficits ou atteinte de {@code maxIterations}.</li>
 * </ol>
 *
 * <p><strong>Invariant cle :</strong> {@code accessingSurface} est le seul accumulateur de
 * surface totale (originale + tous les parcs ajoutes comme carreau central ou voisin).
 * Il est mute a chaque etape 6 ET etape 7 pour garantir la coherence des iterations futures.</p>
 *
 * <p><strong>Effets de bord :</strong></p>
 * <ul>
 *   <li>Modifie in-place les instances {@link ParkProposalWork}
 *       ({@code newSurface}, {@code accessingSurface}, {@code surfacePerCapita},
 *       {@code newMissingSurface}).</li>
 *   <li>Retourne la liste des {@link ParkProposal} retenues (&ge; {@code MIN_PARK_SURFACE} m²).</li>
 * </ul>
 *
 * @see IterativeComputationDeficit1Strategy pour l'approche iterative de reference (D1)
 */
@Slf4j
public class LeastSquaresNeighbour2Strategy extends AbstractComputationtrategy {

    /**
     * Calcule les propositions de parcs de facon iterative, carreau par carreau.
     *
     * <p>Chaque iteration traite le carreau le plus deficitaire et lui propose un parc
     * couvrant son <strong>deficit complet</strong> ({@code (reco - surfaceParHab) * population}).
     * Le chi2 sert uniquement a detecter la convergence (etape 1-2).</p>
     *
     * <ol>
     *   <li><strong>Observations chi2</strong> : carreaux avec population &gt; 0
     *       et {@code surfacePerCapita &le; minSquareMeterPerCapita} ;  
     *       si vide → arret (convergence).</li>
     *   <li><strong>Fit chi2</strong> (degre 0) : calcule la moyenne ponderee des deficits
     *       — loggue uniquement, non utilise comme montant.</li>
     *   <li><strong>Selection</strong> : carreau avec le plus grand {@code newMissingSurface}.</li>
     *   <li><strong>Garde deficitaire</strong> : si {@code surfacePerCapita > minSquareMeterPerCapita},
     *       arret.</li>
     *   <li><strong>Montant</strong> : {@code newParkSurface = deficitParHab * population},
     *       borne a {@code CARRE_SURFACE} ; si {@code < MIN_PARK_SURFACE}, arret.</li>
     *   <li><strong>Carreau central</strong> : cree la {@link ParkProposal} ; met a jour
     *       {@code newSurface}, {@code accessingSurface} (accumulateur), {@code surfacePerCapita},
     *       {@code newMissingSurface}.</li>
     *   <li><strong>Voisins</strong> : propage {@code newParkSurface} a tous les carreaux
     *       dans le rayon {@code urbanDistance} (memes mises a jour ;
     *       {@code newMissingSurface} borne a 0).</li>
     * </ol>
     *
     * @param squaresOnTerritoryMap                carte des carreaux indexee par idInspire (modifiee in-place)
     * @param minSquareMeterPerCapita  seuil en dessous duquel un carreau est deficitaire (m²/hab)
     * @param recoSquareMeterPerCapita objectif OMS de surface par habitant (m²/hab, ex : 12)
     * @param urbanDistance            rayon d'accessibilite pour la propagation aux voisins (metres)
     * @return liste des {@link ParkProposal} retenues (&ge; {@code MIN_PARK_SURFACE} m²)
     */
    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> squaresOnTerritoryMap,
            Double minSquareMeterPerCapita,
            Double recoSquareMeterPerCapita,
            Integer urbanDistance) {

        List<ParkProposal> proposals = new ArrayList<>();
        if (squaresOnTerritoryMap == null || squaresOnTerritoryMap.isEmpty()) {
            log.warn("Carte vide, aucune proposition par moindres carres.");
            return proposals;
        }

        // Boucle iterative : le pas chi2 est inferieur au deficit reel (addition partielle par iteration),
        // il faut donc beaucoup plus d'iterations que squaresOnTerritoryMap.size() pour converger.
        // Le facteur 10 offre une marge suffisante sans risquer une boucle infinie.
        int maxIterations = squaresOnTerritoryMap.size() * 100;
        for (int step = 0; step < maxIterations; step++) {

            // --- Etape 1 : Construire les observations chi2 sur les carreaux deficitaires ---
            WeightedObservedPoints observedPoints = new WeightedObservedPoints();
            squaresOnTerritoryMap.values().forEach(work -> {
                double population = work.getAccessingPopulation() != null ? work.getAccessingPopulation().doubleValue() : 0d;
                double surfacePerCapita = work.getNewSurfacePerCapita() != null ? work.getNewSurfacePerCapita().doubleValue() : 0d;
                if (population <= 0) {
                    return;
                }
                double deficitPerCapita = Math.max(0d, recoSquareMeterPerCapita - surfacePerCapita);
                // N'inclure que les carreaux vraiment deficitaires (sous le seuil minimum)
                if (deficitPerCapita > 0 && surfacePerCapita <= minSquareMeterPerCapita) {
                    observedPoints.add(population, 0d, deficitPerCapita);
                }
            });

            if (observedPoints.toList().isEmpty()) {
                log.info("Iteration {} : aucun deficit restant, arret.", step);
                break;
            }

            // --- Etape 2 : Fit chi2 (modele constant = deficit moyen pondere par la population) ---
            PolynomialCurveFitter fitter = PolynomialCurveFitter.create(0);
            double[] coeffs = fitter.fit(observedPoints.toList());
            double additionPerCapita = Math.max(0d, coeffs[0]);
            // Borner pour ne pas depasser la recommandation
            additionPerCapita = Math.min(additionPerCapita, recoSquareMeterPerCapita);
            log.info("Iteration {} : addition estimee par chi2 = {} m2/hab", step, additionPerCapita);

            // --- Etape 3 : Selectionner le carreau avec le plus grand deficit ---
            List<ParkProposalWork> sorted = sortProposalsByDeficit(squaresOnTerritoryMap);
            if (sorted.isEmpty()) {
                break;
            }
            ParkProposalWork toProcess = sorted.get(0);

            // --- Etape 4 : Verifier que le carreau est encore deficitaire ---
            double surfacePerCapita = toProcess.getNewSurfacePerCapita() != null ? toProcess.getNewSurfacePerCapita().doubleValue() : 0d;
            if (surfacePerCapita > minSquareMeterPerCapita) {
                log.info("Iteration {} : tous les carreaux sont traites (meilleur = {} m2/hab).", step, surfacePerCapita);
                break;
            }

            double population = toProcess.getAccessingPopulation() != null ? toProcess.getAccessingPopulation().doubleValue() : 0d;
            if (population <= 0) {
                log.info("Iteration {} : carreau {} sans population, arret.", step, toProcess.getIdInspire());
                break;
            }

            // --- Etape 5 : Calculer la surface a ajouter ---
            // On applique le deficit complet du carreau selectionne (aligne avec D1).
            // additionPerCapita (chi2) est la moyenne ponderee de tous les deficits : elle est
            // toujours inferieure au deficit du pire carreau, ce qui sous-evalue chaque preconisation.
            // Le chi2 garde son role de filtre (etape 1) mais n'est plus utilise comme plafond du montant.
            double deficitPerCapita = Math.max(0d, recoSquareMeterPerCapita - surfacePerCapita);
            double newParkSurface = Math.min(deficitPerCapita * population, CARRE_SURFACE);

            if (newParkSurface < MIN_PARK_SURFACE) {
                log.info("Iteration {} : surface proposee {} m2 < MIN_PARK_SURFACE pour le carreau {}, arret.",
                        step, newParkSurface, toProcess.getIdInspire());
                break;
            }

            // --- Etape 6 : Appliquer la proposition sur le carreau central ---
            ParkProposal proposal = new ParkProposal();
            proposal.setAnnee(toProcess.getAnnee());
            proposal.setIdInspire(toProcess.getIdInspire());
            proposal.setParkSurface(BigDecimal.valueOf(newParkSurface));
            proposal.setCentre(toProcess.getCentre());
            proposal.setIsDense(toProcess.getIsDense());
            proposals.add(proposal);

            // newMissingSurface diminue du parc ajoute (pas de max(0) : conforme a la reference)
            toProcess.setNewMissingSurface(toProcess.getNewMissingSurface()
                    .subtract(BigDecimal.valueOf(newParkSurface))
                    .max(BigDecimal.ZERO));

            // Cumul : newAccessingSurface += newParkSurface (preserve les propagations des iterations precedentes)
            double newTotalSurface = toProcess.getNewAccessingSurface().doubleValue() + newParkSurface;
            toProcess.setNewAccessingSurface(BigDecimal.valueOf(newTotalSurface));
            double newSurfacePerCapita = newTotalSurface / population;
            toProcess.setNewSurfacePerCapita(BigDecimal.valueOf(newSurfacePerCapita));

            // --- Etape 7 : Propager la surface ajoutee aux voisins dans le rayon d'accessibilite ---
            // La nouvelle surface est positionnee sur le centre et recalculee vis-a-vis de la population de chaque voisin
            List<ParkProposalWork> neighbors = findNeighbors(toProcess.getIdInspire(), squaresOnTerritoryMap, urbanDistance);
            for (ParkProposalWork neighbor : neighbors) {
                // newAccessingSurface cumule la surface totale accessible (original + toutes
                // les contributions voisines). L'ancien code écrasait ce cumul avec le seul
                // delta (ligne suivante), ce qui faisait perdre les contributions quand le
                // voisin devenait centre → sous-estimation systematique.
                double neighborTotalSurface = neighbor.getNewAccessingSurface().doubleValue() + newParkSurface;
                neighbor.setNewAccessingSurface(BigDecimal.valueOf(neighborTotalSurface));

                // null-check sur accessingPopulation
                double neighborPopulation = neighbor.getAccessingPopulation() != null
                    ? neighbor.getAccessingPopulation().doubleValue() : 0d;
                if (neighborPopulation != 0) {
                    double neighborSurfacePerCapita = neighborTotalSurface / neighborPopulation;
                    neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborSurfacePerCapita));
                } else {
                    neighbor.setNewSurfacePerCapita(null);
                }

                // FIX (C): Baser la mise a jour sur le newMissingSurface propre au voisin
                // L'ancien code utilisait toProcess.getNewMissingSurface() (valeur du carreau CENTRAL
                // apres soustraction a l'etape 6) puis soustrayait newParkSurface une 2e fois.
                BigDecimal neighborFallbackMissing = neighbor.getNewMissingSurface() != null
                    ? neighbor.getNewMissingSurface() : BigDecimal.ZERO;
                BigDecimal neighborCurrentMissing = neighbor.getNewMissingSurface() != null
                    ? neighbor.getNewMissingSurface() : neighborFallbackMissing;
                neighbor.setNewMissingSurface(
                    neighborCurrentMissing.subtract(BigDecimal.valueOf(newParkSurface)).max(BigDecimal.ZERO)
                );
            }

            log.info("Iteration {} : carreau {} traite, ajout {} m2, {} voisins mis a jour.",
                    step, toProcess.getIdInspire(), newParkSurface, neighbors.size());
        }

        return proposals;
    }

}