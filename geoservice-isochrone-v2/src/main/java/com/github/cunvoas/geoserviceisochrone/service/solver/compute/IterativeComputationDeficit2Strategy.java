package com.github.cunvoas.geoserviceisochrone.service.solver.compute;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategie de calcul iterative : parcourt tous les carreaux et propose
 * un parc pour chaque carreau presentant un deficit, en une seule etape.
 * 
 * <p>Contrairement a {@link IterativeComputationDeficitStrategy} qui traite
 * un seul carreau par appel, cette variante boucle sur l'integralite de la map
 * et accumule toutes les propositions en une fois.</p>
 * 
 * @see AbstractComputationtrategy#calculeEtapeProposition(Double, Map, Double, Double, Integer)
 */
@Slf4j
public class IterativeComputationDeficit2Strategy extends AbstractComputationtrategy  {

    private final double minParkSurface;

    public IterativeComputationDeficit2Strategy(double minParkSurface) {
        this.minParkSurface = minParkSurface;
    }

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                                      Double minSquareMeterPerCapita,
                                      Double recoSquareMeterPerCapita,
                                      Integer urbanDistance) {
    	// Itere sur tous les carreaux : a chaque iteration on traite le carreau
    	// ayant le plus grand deficit. Le nombre max d'iterations = nb carreaux.
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
	 * Calcule une etape de proposition d'augmentation de parc en traitant le carre avec le plus grand deficit.
	 * 
	 * <p>Cette methode implemente un algorithme iteratif qui :</p>
	 * <ol>
	 *   <li>Identifie le carre avec le plus grand deficit en surface de parc par habitant</li>
	 *   <li>Calcule la surface de parc necessaire pour atteindre la densite recommandee</li>
	 *   <li>Applique la proposition si elle respecte la surface minimale de 1000 m²</li>
	 *   <li>Met a jour les donnees du carre traite et de ses voisins dans le rayon d'accessibilite</li>
	 * </ol>
	 * 
	 * <p><strong>Contraintes appliquees :</strong></p>
	 * <ul>
	 *   <li>Surface minimale d'un parc : 1000 m²</li>
	 *   <li>Si la surface calculee est inferieure a 1000 m², aucun parc n'est propose</li>
	 *   <li>Seuls les carres avec un deficit superieur au seuil minimum sont traites</li>
	 * </ul>
	 * 
	 * <p><strong>Algorithme :</strong></p>
	 * <pre>
	 * 1. Trier les carres par deficit decroissant
	 * 2. Selectionner le carre avec le plus grand deficit
	 * 3. Si deficit &lt; seuil minimum → arret (tous les carres traites)
	 * 4. Calculer : surfaceAAjouter = max((densiteReco - densiteActuelle) × population, 0)
	 * 5. Si surfaceAAjouter ≥ 1000 m² :
	 *    - Appliquer la proposition au carre
	 *    - Mettre a jour sa densite
	 *    - Identifier les voisins dans le rayon d'accessibilite
	 *    - Mettre a jour la densite de chaque voisin
	 * 6. Sinon : marquer le carre comme non traite (localSurface = null)
	 * </pre>
	 * 
	 * <p><strong>Impact sur les donnees :</strong></p>
	 * <ul>
	 *   <li>{@code localSurface} : surface de parc proposee pour le carre (null si &lt; 1000 m²)</li>
	 *   <li>{@code surfacePerCapita} : recalculee pour le carre et ses voisins</li>
	 *   <li>{@code accessingSurface} : surface totale accessible mise a jour</li>
	 * </ul>
	 * 
	 * @param squaresOnTerritoryMap la carte des propositions de parc indexee par idInspire.
	 *                 Chaque {@link ParkProposalWork} contient les donnees demographiques et surfaciques.
	 *                 Cette map est modifiee par la methode.
	 * @param minSquareMeterPerCapita seuil minimal de surface de parc par habitant (m²/hab).
	 *                                En dessous de ce seuil, le carre est considere comme traite.
	 *                                Valeur typique : 8 m²/hab (zone urbaine) ou 10 m²/hab (zone peri-urbaine).
	 * @param recoSquareMeterPerCapita densite recommandee de surface de parc par habitant (m²/hab).
	 *                                 Objectif a atteindre pour chaque carre.
	 *                                 Valeur typique : 12 m²/hab (recommandation OMS).
	 * @param urbanDistance distance d'accessibilite en metres definissant les voisins.
	 *                      Les carres dans ce rayon sont consideres comme accessibles.
	 *                      Valeur typique : 300m (zone urbaine) ou 1000m (zone peri-urbaine).
	 * 
	 * @see #sortProposalsByDeficit(Map)
	 * @see #findNeighbors(String, Map, Integer)
	 * @see ParkProposalWork
	 * 
	 * @author github.com/cunvoas
	 */
	public ParkProposal calculeEtapeProposition(Double minParkSurface, Map<String, ParkProposalWork> squaresOnTerritoryMap,
			Double minSquareMeterPerCapita, Double recoSquareMeterPerCapita, Integer urbanDistance) {
		// Recupere la liste des carres tries par deficit de surface de parc decroissant
		List<ParkProposalWork> sorted = sortProposalsByDeficit(squaresOnTerritoryMap);
		// Variante alternative : tri par persona (priorite aux populations sensibles)
		//List<ParkProposalWork> sorted = sortProposalsByPersona(carreMap);
		
		ParkProposal proposalResult = null;
		if (!sorted.isEmpty()) {
			// Selectionne le carre avec le plus grand deficit (tete de liste)
			ParkProposalWork toProcess = sorted.get(0);

			// Si le carre le plus deficitaire atteint la recommandation OMS,
			// tous les carres sont considers comme satisfaisants → arret
			if (toProcess.getNewSurfacePerCapita() == null
					|| toProcess.getNewSurfacePerCapita().doubleValue() >= recoSquareMeterPerCapita) {
				log.info("Toutes les propositions de la commune sont traitees (≥ reco {}).", recoSquareMeterPerCapita);
				return proposalResult;
			}

			// Calcule la surface de parc a ajouter :
			// = ecart de densite (reco - actuelle) × population accessible
			// Bornee entre 0 et 40 000 m² (surface max d'un carreau 200m × 200m)
			double deficitPerCapita = Math.max(0d, recoSquareMeterPerCapita - toProcess.getNewSurfacePerCapita().doubleValue());
			double proposedParkSurface = Math.min(
					deficitPerCapita * toProcess.getAccessingPopulation().doubleValue(),
					AbstractComputationtrategy.CARRE_SURFACE);

			// Applique la proposition seulement si la surface calculee depasse le seuil minimal
			if (proposedParkSurface >= minParkSurface) {

				// Identifie les carres voisins accessibles dans le rayon urbain
				List<ParkProposalWork> neighbors = findNeighbors(toProcess.getIdInspire(), squaresOnTerritoryMap, urbanDistance);

				// Construit la proposition de parc
				proposalResult = new ParkProposal();
				proposalResult.setAnnee(toProcess.getAnnee());
				proposalResult.setIdInspire(toProcess.getIdInspire());
				proposalResult.setParkSurface(BigDecimal.valueOf(proposedParkSurface));
				proposalResult.setCentre(toProcess.getCentre());
				proposalResult.setIsDense(toProcess.getIsDense());

				// Met a jour le carre traite : cumul, pas ecrasement par l'ideal
				double newTotalSurface = toProcess.getNewAccessingSurface().doubleValue() + proposedParkSurface;
				toProcess.setNewAccessingSurface(BigDecimal.valueOf(newTotalSurface));
				toProcess.setNewMissingSurface(toProcess.getNewMissingSurface()
						.subtract(BigDecimal.valueOf(proposedParkSurface))
						.max(BigDecimal.ZERO));

				// Recalcule la densite du carre apres ajout du parc
				Double newSurfacePerCapita = newTotalSurface / toProcess.getAccessingPopulation().doubleValue();
				toProcess.setNewSurfacePerCapita(BigDecimal.valueOf(newSurfacePerCapita));

				// Propague la mise a jour aux voisins :
				// le nouveau parc est accessible depuis chaque carre voisin,
				// donc leur surface accessible et leur densite augmentent aussi
				// Chaque voisin utilise son propre newMissingSurface, pas celui du centre
				for (ParkProposalWork neighbor : neighbors) {
					double neighborNewTotalSurface = neighbor.getNewAccessingSurface().doubleValue() + proposedParkSurface;
					neighbor.setNewAccessingSurface(BigDecimal.valueOf(neighborNewTotalSurface));
					neighbor.setNewMissingSurface(
							neighbor.getNewMissingSurface()
									.subtract(BigDecimal.valueOf(proposedParkSurface))
									.max(BigDecimal.ZERO));

					if (neighbor.getAccessingPopulation() != null
							&& neighbor.getAccessingPopulation().doubleValue() != 0) {
						Double neighborNewSurfacePerCapita = neighborNewTotalSurface
								/ neighbor.getAccessingPopulation().doubleValue();
						neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborNewSurfacePerCapita));
					}
				}

				log.error("Proposition pour le carre {} : ajout de parc (surface proposee: {}).", 
						toProcess.getIdInspire(), proposedParkSurface);
				
			} else {
				// Surface insuffisante : le deficit existe mais est trop faible
				// pour justifier la creation d'un nouveau parc
				log.warn("Proposition pour le carre {} : pas d'ajout de parc (surface proposee: {}).", 
						toProcess.getIdInspire(), AbstractComputationtrategy.MIN_PARK_SURFACE);
				//toProcess.setNewSurface(null);
			}
		}
		return proposalResult;
	}
}
