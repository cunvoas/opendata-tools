package com.github.cunvoas.geoserviceisochrone.service.solver.compute;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

import lombok.extern.slf4j.Slf4j;

/**
 * Iterative computation using calculeEtapeProposition over all squares.
 * <p>Priorite au carreau avec le plus grand impact humain : <strong>manque de surface × population</strong>.
 * Contrairement a {@link IterativeComputationDeficit2Strategy} qui priorise le deficit surfacique seul,
 * cette strategie favorise les carreaux ou beaucoup d'habitants subissent un manque important.</p>
 */
@Slf4j
public class IterativeComputationPopulation1Strategy extends AbstractComputationtrategy  {

    private final double minParkSurface;

    public IterativeComputationPopulation1Strategy(double minParkSurface) {
        this.minParkSurface = minParkSurface;
    }

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                                      Double minSquareMeterPerCapita,
                                      Double recoSquareMeterPerCapita,
                                      Integer urbanDistance) {
    	
        List<ParkProposal> proposals = new ArrayList<>();
        for (int i = 0; i < carreMap.size(); i++) {
            ParkProposal proposal = calculeEtapeProposition(minParkSurface, carreMap,
                    minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
            if (proposal != null) {
                proposals.add(proposal);
            }
        }
        return proposals;
    }
    

	
	/**
	 * Calcule une étape de proposition d'augmentation de parc en traitant le carré avec le plus grand impact humain.
	 *
	 * <p>Le critere de priorite est : <strong>manque de surface × population</strong>
	 * ({@code newMissingSurface × accessingPopulation}).
	 * Cela privilegie les carreaux ou le deficit est a la fois important en surface
	 * et concerne beaucoup d'habitants.</p>
	 *
	 * <p>Cette méthode implémente un algorithme itératif qui :</p>
	 * <ol>
	 *   <li>Identifie le carré avec le plus grand impact humain (manque × population, décroissant)</li>
	 *   <li>Calcule la surface de parc nécessaire pour atteindre la densité recommandée</li>
	 *   <li>Applique la proposition si elle respecte la surface minimale</li>
	 *   <li>Met à jour les données du carré traité et de ses voisins dans le rayon d'accessibilité</li>
	 * </ol>
	 *
	 * <p><strong>Algorithme :</strong></p>
	 * <pre>
	 * 1. Trier les carrés par (newMissingSurface × population) décroissant
	 * 2. Sélectionner le carré avec le plus grand impact humain
	 * 3. Si déficit &lt; seuil minimum → arrêt (tous les carrés traités)
	 * 4. Calculer : surfaceÀAjouter = max((densitéReco - densitéActuelle) × population, 0)
	 * 5. Si surfaceÀAjouter ≥ minParkSurface :
	 *    - Appliquer la proposition au carré
	 *    - Mettre à jour sa densité
	 *    - Identifier les voisins dans le rayon d'accessibilité
	 *    - Mettre à jour la densité de chaque voisin
	 * 6. Sinon : aucun parc proposé pour ce carreau
	 * </pre>
	 *
	 * @param carreMap la carte des propositions de parc indexée par idInspire (modifiée par la méthode)
	 * @param minSquareMeterPerCapita seuil minimal de surface de parc par habitant (m²/hab)
	 * @param recoSquareMeterPerCapita densité recommandée de surface de parc par habitant (m²/hab)
	 * @param urbanDistance distance d'accessibilité en mètres définissant les voisins
	 *
	 * @see #sortProposalsByMissingPopulation(Map)
	 * @see #findNeighbors(String, Map, Integer)
	 */
	public ParkProposal calculeEtapeProposition(Double minParkSurface, Map<String, ParkProposalWork> carreMap, Double minSquareMeterPerCapita, Double recoSquareMeterPerCapita, Integer urbanDistance) {
		// Priorite au carreau avec le plus grand impact humain : manque de surface × population
		List<ParkProposalWork> sorted = sortProposalsByMissingPopulation(carreMap);
		
		ParkProposal proposalResult = null;
		if (!sorted.isEmpty()) {
			ParkProposalWork toProcess = sorted.get(0);
			if (toProcess.getSurfacePerCapita().doubleValue() > minSquareMeterPerCapita) {
				log.info("Toutes les propositions de la commune sont traitées.");
				return proposalResult;
			}
			List<ParkProposalWork> neighbors = findNeighbors(toProcess.getIdInspire(), carreMap, urbanDistance);

			// calcul de la surface de parc à ajouter pour atteindre la densité recommandée
			// comprise entre 0 et 40 000 m² (surface max d'un carré de 200m x 200m)
			Double newParkSurface = 
					Math.min(
							Math.max(
									recoSquareMeterPerCapita-toProcess.getSurfacePerCapita().doubleValue(),
									0), 
						AbstractComputationtrategy.CARRE_SURFACE
						) * toProcess.getAccessingPopulation().doubleValue();
			
			if (newParkSurface>=minParkSurface) {
				
				proposalResult = new ParkProposal();
				proposalResult.setAnnee(toProcess.getAnnee());
				proposalResult.setIdInspire(toProcess.getIdInspire());
				proposalResult.setParkSurface(BigDecimal.valueOf(newParkSurface));
				proposalResult.setCentre(toProcess.getCentre());
				proposalResult.setIsDense(toProcess.getIsDense());
				
				// appliquer la proposition
				toProcess.setNewSurface(BigDecimal.valueOf(newParkSurface));
				toProcess.setNewMissingSurface(toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(newParkSurface)));
				
				// mettre à jour la surface par habitant
				Double newTotalSurface = toProcess.getAccessingSurface().doubleValue() + newParkSurface;
				Double newSurfacePerCapita = newTotalSurface / toProcess.getAccessingPopulation().doubleValue();
				toProcess.setSurfacePerCapita(BigDecimal.valueOf(newSurfacePerCapita));
				
				// mettre à jour les voisins
				for (ParkProposalWork neighbor : neighbors) {
					Double neighborNewTotalSurface = neighbor.getAccessingSurface().doubleValue() + newParkSurface;
					
					Double neighborNewSurfacePerCapita = null;
					if ( neighbor.getAccessingPopulation().doubleValue()!=0)	{
						//log.error("neighbor {} accessingPopulation={}", neighbor.getIdInspire(), neighbor.getAccessingPopulation().doubleValue());
						
						neighborNewSurfacePerCapita = neighborNewTotalSurface / neighbor.getAccessingPopulation().doubleValue();
						neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborNewSurfacePerCapita));
					
					} else {
						neighbor.setNewSurfacePerCapita(null);
					}
					neighbor.setNewSurface(new BigDecimal(String.valueOf(neighborNewTotalSurface)));
					neighbor.setNewMissingSurface( toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(newParkSurface)).max(BigDecimal.ZERO));
				}
				
				log.error("Proposition pour le carré {} : ajout de parc (surface proposée: {}).", 
						toProcess.getIdInspire(), newParkSurface);
				
			} else {
				log.info("Proposition pour le carré {} : pas d'ajout de parc (surface proposée: {}).", 
						toProcess.getIdInspire(), AbstractComputationtrategy.MIN_PARK_SURFACE);
				//toProcess.setNewSurface(null);
			}
		}
		return proposalResult;
	}
	
}
