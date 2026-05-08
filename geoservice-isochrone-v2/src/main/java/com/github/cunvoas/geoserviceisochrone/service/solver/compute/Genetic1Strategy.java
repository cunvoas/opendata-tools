package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.math.BigDecimal;
import java.util.ArrayList;
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
 * Genetic Algorithm strategy for park proposal computation using commons-math3.
 *
 * <p><strong>Encodage du chromosome :</strong><br>
 * Chaque gène est un {@code Double} représentant la surface de parc (en m²)
 * proposée pour le carré correspondant dans la liste ordonnée des carrés.
 * La valeur est comprise entre 0 et {@link AbstractComputationtrategy#CARRE_SURFACE}.</p>
 *
 * <p><strong>Fitness :</strong><br>
 * Maximise la réduction du déficit total pondéré par la population :
 * {@code Σ max(0, minSpc - (accessSurf + geneSurf) / pop) × pop}</p>
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
public class Genetic1Strategy extends AbstractComputationtrategy {

    // ── Paramètres AG ──────────────────────────────────────────────────────────
    private static final int    POPULATION_SIZE      = 100;
    private static final double CROSSOVER_RATE       = 0.9;
    private static final double MUTATION_RATE        = 0.05;
    private static final double ELITISM_RATE         = 0.10;
    private static final int    MAX_GENERATIONS      = 200;
    private static final int    TOURNAMENT_ARITY     = 3;

    private final double minParkSurface;

    public Genetic1Strategy(double minParkSurface) {
        this.minParkSurface = minParkSurface;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Chromosome interne
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Chromosome représentant une proposition complète :
     * genes.get(i) = surface proposée pour le i-ème carré (m²).
     *
     * <p>La fitness tient compte des effets de voisinage : un parc ajouté en i
     * bénéficie à tous les carrés j dont le centroïde est dans le rayon
     * d'accessibilité de i (et réciproquement). La matrice {@code neighbors}
     * pré-calcule ces relations pour éviter de recalculer les distances à
     * chaque évaluation de fitness.</p>
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
         * Fitness = réduction du déficit pondérée par la population,
         * <strong>en tenant compte des effets de voisinage</strong>.
         *
         * <p>Pour chaque carré i, la surface totale simulée est :
         * <pre>
         *   totalSurface[i] = accessingSurface[i]
         *                   + genes[i]                    // parc posé en i
         *                   + Σ genes[j] pour j ∈ neighbors[i]  // parcs voisins accessibles
         * </pre>
         * Cela reflète exactement la même logique que {@code decodeChromosome} :
         * un parc ajouté en j améliore aussi la densité du carré i si j est
         * dans le rayon d'accessibilité.</p>
         */
        @Override
        public double fitness() {
            int size = carres.size();
            double totalDeficitReduction = 0.0;

            for (int i = 0; i < size; i++) {
                double pop = initPopulation[i];
                if (pop == 0) continue;

                // Surface de base initiale (snapshot BDD, jamais mutée)
                double simulatedSurface = initAccessingSurface[i];

                // Surface du parc proposé sur ce carré
                simulatedSurface += genes.get(i);

                // Surface des parcs proposés dans les carrés voisins
                for (int j : neighbors.get(i)) {
                    simulatedSurface += genes.get(j);
                }

                double newSpc = simulatedSurface / pop;

                // déficit calculé sur la valeur initiale (snapshot)
                double deficitBefore = Math.max(0, minSpc - initSurfacePerCapita[i]);
                double deficitAfter  = Math.max(0, minSpc - newSpc);

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
     * Mutation gaussienne : perturbe un gène aléatoire par un bruit N(0, σ)
     * avec σ = 10 % de CARRE_SURFACE, puis clampe dans [0, CARRE_SURFACE].
     */
    private static final MutationPolicy GAUSSIAN_MUTATION = chromosome -> {
        ParkChromosome c    = (ParkChromosome) chromosome;
        List<Double>   genes = new ArrayList<>(c.getGenes());
        RandomGenerator rng   = GeneticAlgorithm.getRandomGenerator();
        int    idx   = rng.nextInt(genes.size());
        double sigma = AbstractComputationtrategy.CARRE_SURFACE * 0.10;
        double mutated = genes.get(idx) + rng.nextGaussian() * sigma;
        mutated = Math.max(0, Math.min(CARRE_SURFACE, mutated));
        genes.set(idx, mutated);
        return c.newFixedLengthChromosome(genes);
    };

    // ══════════════════════════════════════════════════════════════════════════
    //  Point d'entrée
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                                      Double minSquareMeterPerCapita,
                                      Double recoSquareMeterPerCapita,
                                      Integer urbanDistance) {

        // Ordre stable des carrés : on travaille sur une liste indexée
        List<String>             keys   = new ArrayList<>(carreMap.keySet());
        List<ParkProposalWork>   carres = new ArrayList<>();
        for (String k : keys) carres.add(carreMap.get(k));

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
                    // gène initial : surface nécessaire (± bruit)
                    double base = Math.min(
                            Math.max(recoSquareMeterPerCapita - spc, 0) * pop,
                            CARRE_SURFACE);
                    double noise = (rng.nextDouble() * 0.4 - 0.2) * base; // ±20 %
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

        return decodeChromosome(genes, keys, carreMap, carres,
                minSquareMeterPerCapita, urbanDistance);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Décodage & mise à jour de la carreMap
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
                                                 Map<String, ParkProposalWork> carreMap,
                                                 List<ParkProposalWork>        carres,
                                                 Double                        minSpc,
                                                 Integer                       urbanDistance) {
        List<ParkProposal> proposals = new ArrayList<>();

        // accessingSurface est mis à jour au fil des ajouts (propagation cumulative).
        // Pour chaque carré, la densité effective est recalculée à partir de
        // accessingSurface (qui intègre déjà les apports des voisins précédemment traités).

        for (int i = 0; i < carres.size(); i++) {
            ParkProposalWork carre = carres.get(i);
            double newParkSurface  = genes.get(i);

            // On ne propose un parc que si la surface du gène est suffisante
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
            // propagation des ajouts précédents
            if (currentSpc >= minSpc) {
                log.debug("Carré {} : densité effective {}/hab déjà au-dessus du seuil après propagation — ignoré.",
                        carre.getIdInspire(), currentSpc);
                continue;
            }

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
            carre.setNewSurface(BigDecimal.valueOf(newParkSurface));
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
            List<ParkProposalWork> neighbors = findNeighbors(carre.getIdInspire(), carreMap, urbanDistance);
            for (ParkProposalWork neighbor : neighbors) {
                double neighborNewTotalSurface =
                        neighbor.getAccessingSurface().doubleValue() + newParkSurface;
                neighbor.setAccessingSurface(BigDecimal.valueOf(neighborNewTotalSurface));
                neighbor.setNewSurface(BigDecimal.valueOf(neighborNewTotalSurface));
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