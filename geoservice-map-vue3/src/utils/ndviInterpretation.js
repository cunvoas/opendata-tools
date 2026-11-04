import { arrondiNdvi } from './ndvi.js';

/**
 * Diagnostic déterministe des 4 statistiques NDVI d'un parc (moyenne, minimum, maximum, écart-type).
 * Seuils fixes ; valeurs arrondies à 3 décimales comme affichées.
 * @param {{moyenne: number|string|null, minimum: number|string|null, maximum: number|string|null, ecartType: number|string|null}} stats
 * @returns {string} deux lignes (vigueur, structure), ou '' si une statistique est absente.
 */
export function interpreterStatistiquesNdvi({ moyenne, minimum, maximum, ecartType }) {
  const [moy, min, max, ecart] = [moyenne, minimum, maximum, ecartType].map(arrondiNdvi);
  // Un diagnostic partiel serait trompeur, et null < 0,1 vaut vrai en JavaScript.
  if ([moy, min, max, ecart].includes(null)) return '';

  let vigueur;
  if (moy < 0.1) {
    vigueur = "Absence de végétation (Surfaces minérales, eau, sols nus)";
  } else if (moy < 0.3) {
    vigueur = "Végétation très éparse ou en grand stress (zones arides, début d'émergence)";
  } else if (moy < 0.5) {
    vigueur = "Végétation modérée (pelouses, arbustes)";
  } else if (moy < 0.7) {
    vigueur = "Végétation dense et active (arbres, massifs et pelouses bien développés, bois)";
  } else {
    vigueur = "Végétation exceptionnellement dense (canopée forestière, biomasse maximale)";
  }

  let heterogeneite;
  if (ecart < 0.08) {
    heterogeneite = "Zone parfaitement homogène (pelouse uniforme ou bois continu)";
  } else if (ecart < 0.15) {
    heterogeneite = "Zone relativement uniforme avec de légères variations";
  } else {
    heterogeneite = "Forte mosaïque paysagère (mélange de sols nus, pelouses, massifs et/ou arbres)";
  }

  const remarquables = [];
  if (min < 0.1) remarquables.push("des surfaces non-végétalisées (sols nus, bâtis, routes)");
  if (max > 0.75) remarquables.push("des zones de végétation au maximum de leur potentiel chlorophyllien");
  let extremes = '.';
  if (remarquables.length === 1) extremes = ` contenant ${remarquables[0]}.`;
  if (remarquables.length === 2) extremes = ` contenant à la fois ${remarquables.join(' et ')}.`;

  return `Diagnostic : ${vigueur}.\nStructure spatiale : ${heterogeneite}${extremes}`;
}
