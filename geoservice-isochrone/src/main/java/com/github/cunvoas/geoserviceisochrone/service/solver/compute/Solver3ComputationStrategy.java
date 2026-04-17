package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

import lombok.extern.slf4j.Slf4j;

/**
 * Global computation using Choco solver.
 *
 * <p><strong>Formulation PPC :</strong></p>
 * <ul>
 *   <li>Variables : une IntVar par carre, en unites de {@value #UNIT_M2} m²
 *       (domaine [0, {@value #MAX_UNITS}]) pour reduire l'espace de recherche.</li>
 *   <li>Borne haute = besoinZone = max des besoins du carreau et de ses voisins :
 *       un carreau non deficitaire peut accueillir un parc utile a un voisin.</li>
 *   <li>Contrainte : 0 OU >= MIN_UNITS (parc d'au moins AT_LEAST_PARK_SURFACE m²).</li>
 *   <li>Objectif : minimiser la somme des ecarts residuels (deficit non comble) en unites.</li>
 * </ul>
 */
@Slf4j
public class Solver3ComputationStrategy extends AbstractComputationtrategy {

    /** Unite de discretisation en m² : reduit l'espace de recherche de 40000 a 80 valeurs par variable.
     *  Precision de ±500 m² sur un carre de 40 000 m² = ±1,25%, acceptable pour le metier. */
    private static final int UNIT_M2 = 500;
    /** Borne haute en unites : 40 000 m² / 500 = 80 unites. */
    private static final int MAX_UNITS = (int) (CARRE_SURFACE / UNIT_M2);
    /** Seuil minimal en unites arrondi au superieur : AT_LEAST_PARK_SURFACE (1000 m²) / 500 = 2 unites = 1000 m². */
    private static final int MIN_UNITS = (int) Math.ceil(AT_LEAST_PARK_SURFACE / UNIT_M2);

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap, Double minSquareMeterPerCapita,
            Double recoSquareMeterPerCapita, Integer urbanDistance) {

        List<ParkProposal> proposals = new ArrayList<>();
        if (carreMap.isEmpty()) {
            log.warn("Carte des carrés vide, aucune proposition à calculer");
            return proposals;
        }

        log.info("Démarrage du calcul global avec Choco Solver pour {} carrés (unite={}m²)", carreMap.size(), UNIT_M2);

        List<String> carreIds = new ArrayList<>(carreMap.keySet());
        Map<String, List<ParkProposalWork>> voisinages = new HashMap<>();
        for (String idInspire : carreIds) {
            voisinages.put(idInspire, findNeighbors(idInspire, carreMap, urbanDistance));
        }

        Model model = new Model("Park Area Optimization");
        Map<String, IntVar> additionVars = new HashMap<>();

        // --- Variables : surface ajoutee par carre, en unites de UNIT_M2 m² ---
        for (String id : carreIds) {
            ParkProposalWork carre = carreMap.get(id);
            int population   = carre.getAccessingPopulation() != null ? carre.getAccessingPopulation().intValue() : 0;
            int surfaceExist = carre.getAccessingSurface()    != null ? carre.getAccessingSurface().intValue()    : 0;

            // Besoin propre du carreau pour atteindre la recommandation
            int besoinPropre = population > 0
                    ? (int) Math.min(
                            Math.max(0, Math.ceil(((population * recoSquareMeterPerCapita) - surfaceExist) / UNIT_M2)),
                            MAX_UNITS)
                    : 0;

            // Besoin de la zone = max des besoins du carreau et de ses voisins :
            // un carreau non deficitaire peut accueillir un parc utile a un voisin deficitaire.
            int besoinZone = besoinPropre;
            for (ParkProposalWork voisin : voisinages.get(id)) {
                int popV  = voisin.getAccessingPopulation() != null ? voisin.getAccessingPopulation().intValue() : 0;
                int surfV = voisin.getAccessingSurface()   != null ? voisin.getAccessingSurface().intValue()   : 0;
                int besoinV = popV > 0
                        ? (int) Math.min(
                                Math.max(0, Math.ceil(((popV * recoSquareMeterPerCapita) - surfV) / UNIT_M2)),
                                MAX_UNITS)
                        : 0;
                besoinZone = Math.max(besoinZone, besoinV);
            }

            // Domaine [0, besoinZone] : explore tout le besoin local sans depasser la capacite physique
            IntVar addVar = model.intVar("add_" + id, 0, besoinZone);

            // Contrainte : 0 (pas de parc) OU >= MIN_UNITS (parc d'au moins AT_LEAST_PARK_SURFACE m²)
            if (besoinZone >= MIN_UNITS) {
                model.or(
                    model.arithm(addVar, "=", 0),
                    model.arithm(addVar, ">=", MIN_UNITS)
                ).post();
            }

            additionVars.put(id, addVar);
        }

        // --- Ecarts et objectif : minimiser sum(max(0, target - totalSurface)) en unites ---
        List<IntVar> deviationVars = new ArrayList<>();
        int maxObjectif = 0;

        for (String idInspire : carreIds) {
            ParkProposalWork carre = carreMap.get(idInspire);
            int population   = carre.getAccessingPopulation() != null ? carre.getAccessingPopulation().intValue() : 0;
            int surfaceExist = carre.getAccessingSurface()    != null ? carre.getAccessingSurface().intValue()    : 0;

            if (population == 0) {
                deviationVars.add(model.intVar("dev_" + idInspire, 0));
                continue;
            }

            List<IntVar> contributions = new ArrayList<>();
            contributions.add(additionVars.get(idInspire));
            for (ParkProposalWork voisin : voisinages.get(idInspire)) {
                IntVar v = additionVars.get(voisin.getIdInspire());
                if (v != null) contributions.add(v);
            }

            // Borne max reelle = somme des UB effectifs (besoinZone de chaque contributeur)
            int maxContribUnits = contributions.stream().mapToInt(IntVar::getUB).sum();
            IntVar sumAddUnits = model.intVar("sumU_" + idInspire, 0, maxContribUnits);
            model.sum(contributions.toArray(new IntVar[0]), "=", sumAddUnits).post();

            int surfaceExistUnits = (int) Math.round((double) surfaceExist / UNIT_M2);
            int targetUnits       = (int) Math.ceil((population * recoSquareMeterPerCapita) / UNIT_M2);

            IntVar totalUnits = model.intVar("tot_" + idInspire, surfaceExistUnits, surfaceExistUnits + maxContribUnits);
            model.arithm(totalUnits, "=", sumAddUnits, "+", surfaceExistUnits).post();

            int gapLo = targetUnits - (surfaceExistUnits + maxContribUnits);
            int gapHi = targetUnits - surfaceExistUnits;
            IntVar gap = model.intVar("gap_" + idInspire, Math.min(gapLo, 0), Math.max(gapHi, 0));
            model.arithm(gap, "+", totalUnits, "=", targetUnits).post();

            IntVar dev = model.intVar("dev_" + idInspire, 0, Math.max(gapHi, 0));
            model.max(dev, gap, model.intVar(0)).post();

            deviationVars.add(dev);
            maxObjectif += Math.max(gapHi, 0);
        }

        // Objectif : borne calculee (pas IntVar.MAX_INT_BOUND qui rend le modele infaisable)
        IntVar objectif = model.intVar("objective", 0, Math.max(maxObjectif, 1));
        model.sum(deviationVars.toArray(new IntVar[0]), "=", objectif).post();
        model.setObjective(Model.MINIMIZE, objectif);

        log.info("Modele construit : {} variables, objectif borne a {} unites, resolution...", carreIds.size(), maxObjectif);
        boolean solved = model.getSolver().solve();

        log.info("Solver stats : solutions={}, noeuds={}, echecs={}, temps={}ms",
                model.getSolver().getSolutionCount(),
                model.getSolver().getNodeCount(),
                model.getSolver().getFailCount(),
                model.getSolver().getTimeCount());

        if (!solved) {
            log.warn("Aucune solution trouvée par le solver — verifier les bornes et contraintes.");
            return proposals;
        }

        // --- Application des resultats : reconvertir en m² ---
        Map<String, Integer> additionsM2 = new HashMap<>();
        for (String id : carreIds) {
            additionsM2.put(id, additionVars.get(id).getValue() * UNIT_M2);
        }

        for (String idInspire : carreIds) {
            ParkProposalWork carre = carreMap.get(idInspire);
            int addedM2 = additionsM2.get(idInspire);

            if (addedM2 > 0) {
                ParkProposal proposal = new ParkProposal();
                proposal.setAnnee(carre.getAnnee());
                proposal.setIdInspire(carre.getIdInspire());
                proposal.setIsDense(carre.getIsDense());
                proposal.setCentre(carre.getCentre());
                proposal.setParkSurface(BigDecimal.valueOf(addedM2));
                proposals.add(proposal);
            }

            double totalAddedM2 = addedM2;
            for (ParkProposalWork voisin : voisinages.get(idInspire)) {
                totalAddedM2 += additionsM2.getOrDefault(voisin.getIdInspire(), 0);
            }

            double surfaceBaseM2  = carre.getAccessingSurface()    != null ? carre.getAccessingSurface().doubleValue()    : 0d;
            double population     = carre.getAccessingPopulation() != null ? carre.getAccessingPopulation().doubleValue() : 0d;
            double totalSurfaceM2 = surfaceBaseM2 + totalAddedM2;

            if (population > 0) {
                carre.setNewSurfacePerCapita(BigDecimal.valueOf(totalSurfaceM2 / population));
                carre.setSurfacePerCapita(BigDecimal.valueOf(totalSurfaceM2 / population));
            } else {
                carre.setNewSurfacePerCapita(null);
            }

            BigDecimal missing        = carre.getMissingSurface() != null ? carre.getMissingSurface() : BigDecimal.ZERO;
            BigDecimal updatedMissing = missing.subtract(BigDecimal.valueOf(totalAddedM2)).max(BigDecimal.ZERO);
            carre.setNewMissingSurface(updatedMissing);
            carre.setNewSurface(BigDecimal.valueOf(addedM2));
        }

        log.info("Résolution terminée : {} propositions retenues.", proposals.size());
        return proposals;
    }
}
