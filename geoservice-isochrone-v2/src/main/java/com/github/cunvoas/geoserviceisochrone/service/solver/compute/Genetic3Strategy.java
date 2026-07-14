package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ChromosomePair;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;
import org.apache.commons.math3.random.RandomGenerator;

import com.github.cunvoas.geoserviceisochrone.extern.helper.DistanceHelper;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

import lombok.extern.slf4j.Slf4j;

/**
 * Algorithme Genetique  pour uncalcule de proposition de parc avec commons-math3.
 *
 * <p><strong>Encodage du chromosome :</strong><br>
 * Chaque gène est un {@code Double} représentant la surface de parc (en m²)
 * proposée pour le carré correspondant dans la liste ordonnée des carrés.
 * La valeur est comprise entre 0 et {@link AbstractComputationtrategy#CARRE_SURFACE}.</p>
 *
 * <p><strong>Fitness :</strong><br>
 * Simulation séquentielle du décodage : les gènes sont appliqués dans l'ordre
 * des carrés (trié par déficit décroissant), avec les mêmes filtres que
 * {@code decodeChromosome} — seuil {@code minParkSurface} et vérification
 * SPC courante après propagation cumulative des ajouts précédents.
 * La valeur retournée est la réduction totale du déficit pondérée par la
 * population : {@code Σ (deficitAvant - deficitAprès) × pop}.</p>
 *
 * <p><strong>Paramètres AG :</strong></p>
 * <ul>
 *   <li>Taille de population : 100</li>
 *   <li>Taux de croisement : 0.9</li>
 *   <li>Taux de mutation : 0.05</li>
 *   <li>Élitisme : 10 %</li>
 *   <li>Générations : 200</li>
 *   <li>Sélection : tournoi de taille 3</li>
 *   <li>Croisement : uniforme</li>
 * </ul>
 */
@Slf4j
@Deprecated
public class Genetic3Strategy extends AbstractComputationtrategy {

    // ── Paramètres AG ──────────────────────────────────────────────────────────
    private static final int    POPULATION_SIZE      = 400;
    private static final double CROSSOVER_RATE       = 0.9;
    private static final double MUTATION_RATE        = 0.25;
    private static final double ELITISM_RATE         = 0.05;
    private static final int    MAX_GENERATIONS      = 300;
    private static final int    TOURNAMENT_ARITY     = 5;

    private final double minParkSurface;

    public Genetic3Strategy(double minParkSurface) {
        this.minParkSurface = minParkSurface;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Chromosome interne
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Chromosome représentant une proposition complète :
     * {@code genes.get(i)} = surface de parc proposée (m²) pour le i-ème carré
     * de la liste triée par déficit décroissant.
     *
     * <p><strong>Cohérence fitness / décodage :</strong><br>
     * La méthode {@code fitness()} simule séquentiellement le même processus que
     * {@code decodeChromosome} : les gènes sont évalués dans l'ordre de la liste,
     * avec les mêmes filtres (seuil surface minimale, vérification SPC courante)
     * et la même propagation cumulative aux voisins. Cela garantit que l'AG
     * sélectionne des chromosomes dont la fitness reflète fidèlement le résultat
     * réel du décodage.</p>
     *
     * <p>La matrice {@code neighbors} pré-calcule les relations de voisinage
     * (index dans la liste triée) pour éviter de recalculer les distances
     * géographiques à chaque évaluation de fitness.</p>
     *
     * <p>Les tableaux {@code initAccessingSurface}, {@code initPopulation} et
     * {@code initSurfacePerCapita} sont des snapshots pris à la construction
     * du chromosome, garantissant que {@code fitness()} est stateless même si
     * les objets {@link ParkProposalWork} sont mutés lors du décodage.</p>
     */
    private class ParkChromosome extends AbstractListChromosome<Double> {

        private final List<ParkProposalWork> carres;
        private final Double minSpc;
        private final Double recoSpc;
        private final List<Double> genes;
        private final List<List<Integer>> neighbors;

        /**
         * Snapshot des valeurs initiales accessingSurface et population,
         * pris à la construction du chromosome.
         * Garantit que fitness() est stateless même si les objets ParkProposalWork
         * sont mutés pendant decodeChromosome().
         */
        private final double[] initAccessingSurface;
        private final double[] initPopulation;
        private final double[] initSurfacePerCapita;

        ParkChromosome(List<Double>           representation,
                       List<ParkProposalWork>  carres,
                       Double                 minSpc,
                       Double                 recoSpc,
                       List<List<Integer>>    neighbors) {
            super(representation);
            this.genes     = new ArrayList<>(representation);
            this.carres    = carres;
            this.minSpc    = minSpc;
            this.recoSpc   = recoSpc;
            this.neighbors = neighbors;

            // Snapshot des valeurs courantes au moment de la création
            int n = carres.size();
            this.initAccessingSurface = new double[n];
            this.initPopulation       = new double[n];
            this.initSurfacePerCapita = new double[n];
            for (int i = 0; i < n; i++) {
                ParkProposalWork c = carres.get(i);
                this.initAccessingSurface[i] = c.getAccessingSurface().doubleValue();
                this.initPopulation[i]       = c.getAccessingPopulation().doubleValue();
                this.initSurfacePerCapita[i] = c.getSurfacePerCapita().doubleValue();
            }
        }

        public List<Double> getGenes() {
            return genes;
        }

        /**
         * Calcule la fitness par simulation séquentielle du décodage.
         *
         * <p>L'évaluation reproduit fidèlement la procédure de
         * {@code decodeChromosome}, dans le même ordre et avec les mêmes règles :
         * <ol>
         *   <li><strong>Encodage binaire :</strong> un gène &lt; 0.5 signifie
         *       "ne pas proposer de parc" ; un gène ≥ 0.5 signifie "proposer un parc".
         *       L'AG optimise la <em>décision de placement</em>, pas la surface.</li>
         *   <li>Un carré dont la densité simulée courante ({@code simulatedAccess[i]/pop})
         *       atteint déjà {@code minSpc} est ignoré : la propagation cumulative
         *       des parcs précédemment ajoutés peut avoir comblé son déficit.</li>
         *   <li>Quand un parc est décidé, sa taille est toujours
         *       {@code max((recoSpc − currentSpc) × pop, minParkSurface)},
         *       identique à la stratégie itérative. La surface est appliquée
         *       au carré et propagée à tous ses voisins accessibles.</li>
         * </ol>
         * Cette cohérence garantit que l'AG sélectionne des chromosomes
         * dont la fitness correspond au résultat effectif du décodage.</p>
         *
         * <p>La valeur retournée est la réduction totale du déficit pondérée
         * par la population :
         * {@code Σ (deficitBefore[i] - deficitAfter[i]) × pop[i]},
         * mesurée vers {@code recoSpc}.</p>
         */
        @Override
        public double fitness() {
            int n = carres.size();
            // Copie de travail initialisée sur les snapshots immuables :
            // simulatedAccess[i] accumule les apports au fil de la simulation.
            double[] simulatedAccess = Arrays.copyOf(initAccessingSurface, n);

            // ── Phase 1 : simulation séquentielle ────────────────────────────
            for (int i = 0; i < n; i++) {
                double pop = initPopulation[i];
                if (pop == 0) continue;

                double geneVal = genes.get(i);

                // Miroir du filtre de decodeChromosome : un parc trop petit n'est
                // jamais proposé, il ne doit donc pas contribuer à la fitness.
                if (geneVal < minParkSurface) continue;

                // Vérification sur l'état simulé courant : la propagation
                // cumulative des parcs précédents peut avoir comblé ce déficit.
                double currentSpc = simulatedAccess[i] / pop;
                if (currentSpc >= minSpc) continue;

                // Plafonnement au déficit vers recoSpc (cohérent avec decodeChromosome
                // et avec la stratégie itérative qui propose (recoSpc−spc)×pop).
                double recoDeficit   = Math.max((recoSpc - currentSpc) * pop, 0.0);
                double effectiveGene = Math.min(geneVal, Math.max(recoDeficit, minParkSurface));

                // Appliquer le parc sur ce carré
                simulatedAccess[i] += effectiveGene;

                // Propager aux voisins accessibles (identique à decodeChromosome)
                for (int j : neighbors.get(i)) {
                    simulatedAccess[j] += effectiveGene;
                }
            }

            // ── Phase 2 : calcul de la réduction totale du déficit ────────────
            // Comparaison entre le déficit initial (snapshot) et le déficit
            // simulé après application séquentielle de tous les gènes retenus.
            double totalDeficitReduction = 0.0;
            for (int i = 0; i < n; i++) {
                double pop = initPopulation[i];
                if (pop == 0) continue;

                // Mesure vers recoSpc : l'AG optimise l'atteinte du niveau recommandé,
                // pas seulement du seuil minimal — identique à la cible de l'itératif.
                double deficitBefore = Math.max(0, recoSpc - initSurfacePerCapita[i]);
                double newSpc        = simulatedAccess[i] / pop;
                double deficitAfter  = Math.max(0, recoSpc - newSpc);

                totalDeficitReduction += (deficitBefore - deficitAfter) * pop;
            }
            return totalDeficitReduction;
        }

        @Override
        protected void checkValidity(List<Double> repr) throws MathIllegalArgumentException {
            // les valeurs sont clampées à la création ; rien à vérifier
        }

        @Override
        public AbstractListChromosome<Double> newFixedLengthChromosome(List<Double> repr) {
            return new ParkChromosome(repr, carres, minSpc, recoSpc, neighbors);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Opérateurs génétiques
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Croisement uniforme : pour chaque gène, choisit aléatoirement le gène
     * du parent 1 ou du parent 2 (taux = 0.5 bit à bit).
     */
    private static final CrossoverPolicy UNIFORM_CROSSOVER = (first, second) -> {
        ParkChromosome p1 = (ParkChromosome) first;
        ParkChromosome p2 = (ParkChromosome) second;
        List<Double> g1 = new ArrayList<>(p1.getGenes());
        List<Double> g2 = new ArrayList<>(p2.getGenes());
        RandomGenerator rng = GeneticAlgorithm.getRandomGenerator();
        for (int i = 0; i < g1.size(); i++) {
            if (rng.nextBoolean()) {
                double tmp = g1.get(i);
                g1.set(i, g2.get(i));
                g2.set(i, tmp);
            }
        }
        return new ChromosomePair(
                p1.newFixedLengthChromosome(g1),
                p2.newFixedLengthChromosome(g2));
    };

    /**
     * Mutation gaussienne adaptative : σ proportionnel à la valeur courante du gène
     * (15 %), avec un minimum à 1 % de CARRE_SURFACE (≈ 400 m²) pour explorer
     * les gènes proches de zéro. Résultat clampé dans [0, CARRE_SURFACE].
     */
    private static final MutationPolicy GAUSSIAN_MUTATION = chromosome -> {
        ParkChromosome c     = (ParkChromosome) chromosome;
        List<Double>   genes = new ArrayList<>(c.getGenes());
        RandomGenerator rng  = GeneticAlgorithm.getRandomGenerator();
        int    idx         = rng.nextInt(genes.size());
        double currentGene = genes.get(idx);
        double sigma = Math.max(currentGene * 0.15, AbstractComputationtrategy.CARRE_SURFACE * 0.01);
        double mutated = currentGene + rng.nextGaussian() * sigma;
        mutated = Math.max(0, Math.min(CARRE_SURFACE, mutated));
        genes.set(idx, mutated);
        return c.newFixedLengthChromosome(genes);
    };

    // ══════════════════════════════════════════════════════════════════════════
    //  Point d'entrée
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> squaresOnTerritoryMap,
                                      Double minSquareMeterPerCapita,
                                      Double recoSquareMeterPerCapita,
                                      Integer urbanDistance) {

        // Ordre stable des carrés : on travaille sur une liste indexée
        List<String>             keys   = new ArrayList<>(squaresOnTerritoryMap.keySet());
        List<ParkProposalWork>   carres = new ArrayList<>();
        for (String k : keys) carres.add(squaresOnTerritoryMap.get(k));

        // Tri par déficit décroissant (newMissingSurface) avant toute construction
        // de chromosome. Cette étape est indispensable pour trois raisons :
        //   1. Cohérence fitness / décodage : fitness() et decodeChromosome()
        //      itèrent tous les deux dans l'ordre de cette liste ; le tri garantit
        //      qu'ils appliquent les gènes dans le même ordre prioritaire.
        //   2. Alignement avec les stratégies itératives : IterativeComputationDeficit1Strategy
        //      traite en premier le carré au plus grand déficit. L'AG doit suivre
        //      la même logique pour rester comparable.
        //   3. Qualité du seeding : les gènes initiaux sont calculés dans cet ordre ;
        //      les carrés les plus déficitaires reçoivent leurs gènes en premier,
        //      ce qui donne à l'AG un point de départ heuristiquement proche de
        //      la solution itérative.
        {
            // Capture finale nécessaire : carres est réassignée après le tri,
            // donc la lambda ne peut pas la capturer directement.
            final List<ParkProposalWork> carresAvantTri = carres;
            List<Integer> si = new ArrayList<>(keys.size());
            for (int i = 0; i < keys.size(); i++) si.add(i);
            si.sort((a, b) -> {
                BigDecimal da = carresAvantTri.get(a).getNewMissingSurface();
                BigDecimal db = carresAvantTri.get(b).getNewMissingSurface();
                double va = da != null ? da.doubleValue() : 0.0;
                double vb = db != null ? db.doubleValue() : 0.0;
                return Double.compare(vb, va); // décroissant : plus grand déficit en premier
            });
            List<String>           sk = new ArrayList<>(keys.size());
            List<ParkProposalWork> sc = new ArrayList<>(keys.size());
            for (int idx : si) { sk.add(keys.get(idx)); sc.add(carres.get(idx)); }
            keys   = sk;
            carres = sc;
        }

        int n = carres.size();
        if (n == 0) return new ArrayList<>();

        // ── Matrice d'adjacence par index ─────────────────────────────────────
        // neighborIdx.get(i) = liste des index j dont le carré j est dans le
        // rayon d'accessibilité du carré i. Précalculé une seule fois pour
        // toutes les évaluations de fitness.
        List<List<Integer>> neighborIdx = buildNeighborIndex(carres, urbanDistance);

        // ── Population initiale ───────────────────────────────────────────────
        List<Chromosome> chromosomes = new ArrayList<>(POPULATION_SIZE);
        RandomGenerator rng = GeneticAlgorithm.getRandomGenerator();

        for (int p = 0; p < POPULATION_SIZE; p++) {
            List<Double> genes = new ArrayList<>(n);
            for (ParkProposalWork carre : carres) {
                double pop = carre.getAccessingPopulation().doubleValue();
                double spc = carre.getSurfacePerCapita().doubleValue();
                if (pop > 0 && spc < recoSquareMeterPerCapita) {
                    // gène initial : surface nécessaire (± bruit ±20 %)
                    double base = Math.min(
                            Math.max(recoSquareMeterPerCapita - spc, 0) * pop,
                            CARRE_SURFACE);
                    double noise = (rng.nextDouble() * 0.4 - 0.2) * base;
                    genes.add(Math.max(0, base + noise));
                } else {
                    genes.add(0.0);
                }
            }
            chromosomes.add(new ParkChromosome(genes, carres,
                    minSquareMeterPerCapita, recoSquareMeterPerCapita, neighborIdx));
        }

        Population initialPopulation = new ElitisticListPopulation(
                chromosomes, POPULATION_SIZE, ELITISM_RATE);

        // ── Algorithme génétique ──────────────────────────────────────────────
        GeneticAlgorithm ga = new GeneticAlgorithm(
                UNIFORM_CROSSOVER,
                CROSSOVER_RATE,
                GAUSSIAN_MUTATION,
                MUTATION_RATE,
                new TournamentSelection(TOURNAMENT_ARITY));

        StoppingCondition stop = new FixedGenerationCount(MAX_GENERATIONS);
        Population finalPop    = ga.evolve(initialPopulation, stop);

        // ── Décodage du meilleur chromosome ──────────────────────────────────
        ParkChromosome best  = (ParkChromosome) finalPop.getFittestChromosome();
        List<Double>   genes = best.getGenes();

        log.info("AG terminé après {} générations. Fitness du meilleur : {}",
                MAX_GENERATIONS, best.fitness());

        List<ParkProposal> proposals = decodeChromosome(genes, keys, squaresOnTerritoryMap, carres,
                minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);

        // ── Balayage complémentaire : zones non couvertes par l'AG ──────────────────
        // Quand des gènes ont derivé sous minParkSurface, certaines zones
        // déficitaires (spc < minSpc) restent sans proposition.
        // Ce sweep identique à la stratégie itérative les couvre systématiquement.
        for (int sweep = 0; sweep < squaresOnTerritoryMap.size(); sweep++) {
            List<ParkProposalWork> sorted = sortProposalsByDeficit(squaresOnTerritoryMap);
            if (sorted.isEmpty()) break;
            ParkProposalWork toProcess = sorted.get(0);
            if (toProcess.getSurfacePerCapita().doubleValue() >= minSquareMeterPerCapita) break;
            double swPop = toProcess.getAccessingPopulation().doubleValue();
            if (swPop == 0) break;
            double swCurrentSpc = toProcess.getSurfacePerCapita().doubleValue();
            double swParkSurface = Math.min(
                    Math.max(recoSquareMeterPerCapita - swCurrentSpc, 0.0), CARRE_SURFACE) * swPop;
            if (swParkSurface < minParkSurface) break;

            ParkProposal sweepProposal = new ParkProposal();
            sweepProposal.setAnnee(toProcess.getAnnee());
            sweepProposal.setIdInspire(toProcess.getIdInspire());
            sweepProposal.setParkSurface(BigDecimal.valueOf(swParkSurface));
            sweepProposal.setCentre(toProcess.getCentre());
            sweepProposal.setIsDense(toProcess.getIsDense());
            proposals.add(sweepProposal);

            double swNewTotal = toProcess.getAccessingSurface().doubleValue() + swParkSurface;
            toProcess.setAccessingSurface(BigDecimal.valueOf(swNewTotal));
            toProcess.setNewAccessingSurface(BigDecimal.valueOf(swParkSurface));
            toProcess.setNewMissingSurface(
                    toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(swParkSurface)).max(BigDecimal.ZERO));
            toProcess.setSurfacePerCapita(BigDecimal.valueOf(swNewTotal / swPop));
            toProcess.setNewSurfacePerCapita(BigDecimal.valueOf(swNewTotal / swPop));

            List<ParkProposalWork> swNeighbors = findNeighbors(toProcess.getIdInspire(), squaresOnTerritoryMap, urbanDistance);
            for (ParkProposalWork neighbor : swNeighbors) {
                double nTotal = neighbor.getAccessingSurface().doubleValue() + swParkSurface;
                neighbor.setAccessingSurface(BigDecimal.valueOf(nTotal));
                neighbor.setNewAccessingSurface(BigDecimal.valueOf(swParkSurface));
                neighbor.setNewMissingSurface(
                        neighbor.getNewMissingSurface().subtract(BigDecimal.valueOf(swParkSurface)).max(BigDecimal.ZERO));
                double nPop = neighbor.getAccessingPopulation().doubleValue();
                if (nPop > 0) {
                    neighbor.setSurfacePerCapita(BigDecimal.valueOf(nTotal / nPop));
                    neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(nTotal / nPop));
                }
            }
            log.debug("Sweep AG : {} — {} m² ajoutés.", toProcess.getIdInspire(), swParkSurface);
        }
        log.info("AG + sweep : {} propositions totales.", proposals.size());
        return proposals;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Décodage & mise à jour de la squaresOnTerritoryMap
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Précalcule pour chaque carré (par index) la liste des index des carrés
     * dont le centroïde est dans le rayon {@code urbanDistance} (+ 100 m de marge).
     * Utilisé une seule fois avant l'AG pour ne pas recalculer les distances
     * à chaque évaluation de fitness.
     */
    private List<List<Integer>> buildNeighborIndex(List<ParkProposalWork> carres, Integer urbanDistance) {
        int n = carres.size();
        List<List<Integer>> neighborIdx = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            neighborIdx.add(new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            ParkProposalWork ci = carres.get(i);
            double yi = ci.getCentre().getCentroid().getY();
            double xi = ci.getCentre().getCentroid().getX();
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                ParkProposalWork cj = carres.get(j);
                double dist = 1_000 * DistanceHelper.crowFlyDistance(
                        yi, xi,
                        cj.getCentre().getCentroid().getY(),
                        cj.getCentre().getCentroid().getX());
                if (dist < urbanDistance + 100) {
                    neighborIdx.get(i).add(j);
                }
            }
        }
        log.debug("Matrice d'adjacence précalculée pour {} carrés.", n);
        return neighborIdx;
    }

    private List<ParkProposal> decodeChromosome(List<Double>                  genes,
                                                 List<String>                  keys,
                                                 Map<String, ParkProposalWork> squaresOnTerritoryMap,
                                                 List<ParkProposalWork>        carres,
                                                 Double                        minSpc,
                                                 Double                        recoSpc,
                                                 Integer                       urbanDistance) {
        List<ParkProposal> proposals = new ArrayList<>();

        // accessingSurface est mis à jour au fil des ajouts (propagation cumulative).
        // Pour chaque carré, la densité effective est recalculée à partir de
        // accessingSurface (qui intègre déjà les apports des voisins précédemment traités).

        for (int i = 0; i < carres.size(); i++) {
            ParkProposalWork carre = carres.get(i);
            double newParkSurface = genes.get(i);

            // On ne propose un parc que si la surface du gène est suffisante.
            if (newParkSurface < minParkSurface) {
                log.debug("Carré {} : surface proposée {} < seuil minimal {} — ignoré.",
                        carre.getIdInspire(), newParkSurface, minParkSurface);
                continue;
            }

            double pop = carre.getAccessingPopulation().doubleValue();
            if (pop == 0) continue;

            // Densité effective courante (accessingSurface déjà mis à jour par
            // les ajouts précédents dans cette boucle)
            double currentSpc = carre.getAccessingSurface().doubleValue() / pop;

            // On ne propose que pour les carrés encore déficitaires après
            // propagation des ajouts précédents.
            if (currentSpc >= minSpc) {
                log.debug("Carré {} : densité effective {}/hab déjà au-dessus du seuil après propagation — ignoré.",
                        carre.getIdInspire(), currentSpc);
                continue;
            }

            // Plafonnement à recoSpc : cohérent avec fitness() et avec la stratégie
            // itérative qui propose exactement (recoSpc − currentSpc) × pop.
            double recoDeficit = Math.max((recoSpc - currentSpc) * pop, 0.0);
            newParkSurface = Math.min(newParkSurface, Math.max(recoDeficit, minParkSurface));

            // ── Créer la proposition ──────────────────────────────────────────
            ParkProposal proposal = new ParkProposal();
            proposal.setAnnee(carre.getAnnee());
            proposal.setIdInspire(carre.getIdInspire());
            proposal.setParkSurface(BigDecimal.valueOf(newParkSurface));
            proposal.setCentre(carre.getCentre());
            proposal.setIsDense(carre.getIsDense());
            proposals.add(proposal);

            // ── Mettre à jour le carré courant ───────────────────────────────
            double newTotalSurface = carre.getAccessingSurface().doubleValue() + newParkSurface;
            carre.setAccessingSurface(BigDecimal.valueOf(newTotalSurface));
            carre.setNewAccessingSurface(BigDecimal.valueOf(newParkSurface));
            carre.setNewMissingSurface(
                    carre.getNewMissingSurface()
                         .subtract(BigDecimal.valueOf(newParkSurface))
                         .max(BigDecimal.ZERO));
            double newSpc = newTotalSurface / pop;
            carre.setSurfacePerCapita(BigDecimal.valueOf(newSpc));
            carre.setNewSurfacePerCapita(BigDecimal.valueOf(newSpc));

            log.info("Proposition carré {} : surface ajoutée = {} m², densité {} → {} m²/hab.",
                    carre.getIdInspire(), newParkSurface, currentSpc, newSpc);

            // ── Propager aux voisins dans le rayon d'accessibilité ───────────
            // accessingSurface de chaque voisin est incrémentée de newParkSurface
            // pour que les carrés traités ensuite dans cette boucle bénéficient
            // de cet ajout (propagation cumulative correcte).
            List<ParkProposalWork> neighbors = findNeighbors(carre.getIdInspire(), squaresOnTerritoryMap, urbanDistance);
            for (ParkProposalWork neighbor : neighbors) {
                double neighborNewTotalSurface =
                        neighbor.getAccessingSurface().doubleValue() + newParkSurface;
                neighbor.setAccessingSurface(BigDecimal.valueOf(neighborNewTotalSurface));
                // setAccessingNewSurface doit contenir le delta (surface du parc ajouté),
                // pas le total accessible — cohérent avec le traitement du carré principal.
                neighbor.setNewAccessingSurface(BigDecimal.valueOf(newParkSurface));
                neighbor.setNewMissingSurface(
                        neighbor.getNewMissingSurface()
                                .subtract(BigDecimal.valueOf(newParkSurface))
                                .max(BigDecimal.ZERO));

                double neighborPop = neighbor.getAccessingPopulation().doubleValue();
                if (neighborPop != 0) {
                    double neighborNewSpc = neighborNewTotalSurface / neighborPop;
                    neighbor.setSurfacePerCapita(BigDecimal.valueOf(neighborNewSpc));
                    neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborNewSpc));
                } else {
                    neighbor.setNewSurfacePerCapita(null);
                }
            }
        }

        log.info("Décodage AG : {} propositions générées.", proposals.size());
        return proposals;
    }
}