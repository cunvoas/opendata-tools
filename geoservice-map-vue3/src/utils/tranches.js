/**
 * Libellés et bornes des tranches de surface.
 * Les JSON v2 portent borneMin/borneMax numériques ; les JSON actuels portent un texte libre.
 * Le front lit les nombres quand ils existent, sinon il traduit le texte (« > 0<=3 », « >= 12 m²/hab. »).
 */

const nombre = (texte) => Number(String(texte).replace(',', '.'));

/**
 * Bornes d'une tranche en m² par habitant.
 * @param {{borneMin?: number, borneMax?: number, surface?: string}} tranche - tranche publiée.
 * @returns {{min: (number|null), max: (number|null)}|null} bornes, null si le libellé n'en porte pas.
 */
export function bornesTranche(tranche = {}) {
  if (typeof tranche.borneMin === 'number' || typeof tranche.borneMax === 'number') {
    return {
      min: typeof tranche.borneMin === 'number' ? tranche.borneMin : null,
      max: typeof tranche.borneMax === 'number' ? tranche.borneMax : null
    };
  }
  const texte = String(tranche.surface ?? '');
  const nombres = [...texte.matchAll(/\d+(?:[.,]\d+)?/g)].map((trouve) => nombre(trouve[0]));
  if (!nombres.length) return null;
  if (nombres.length >= 2) return { min: nombres[0], max: nombres[1] };
  // Une seule borne : « >= 12 » ouvre vers le haut, « < 3 » ferme vers le bas.
  return /^\s*</.test(texte) ? { min: null, max: nombres[0] } : { min: nombres[0], max: null };
}

const LIBELLES_CONNUS = [
  [/non\s*respect/i, 'Sous les minima de l’OMS'],
  [/minimum\s*conseill/i, 'Minimum conseillé par l’OMS'],
  [/pr[ée]conis/i, 'Niveau préconisé par l’OMS']
];

/** Traduit un libellé brut publié, sans notation mathématique. */
function humaniser(texte) {
  const propre = String(texte ?? '').trim();
  const connu = LIBELLES_CONNUS.find(([motif]) => motif.test(propre));
  if (connu) return connu[1];
  return propre ? propre[0].toLocaleUpperCase('fr') + propre.slice(1) : '';
}

/**
 * Libellé d'affichage d'une tranche, en français courant.
 * @param {{borneMin?: number, borneMax?: number, surface?: string}} tranche - tranche publiée.
 * @returns {string} libellé, sans « <= » ni « < ».
 */
export function libelleTranche(tranche = {}) {
  const bornes = bornesTranche(tranche);
  if (!bornes) return humaniser(tranche.surface);
  const { min, max } = bornes;
  if (min == null && max != null) return `Moins de ${max} m² par habitant`;
  if (min != null && max == null) return `${min} m² ou plus par habitant`;
  if (!min) return `Moins de ${max} m² par habitant`;
  return `De ${min} à ${max} m² par habitant`;
}

const POURCENT = new Intl.NumberFormat('fr-FR', { maximumFractionDigits: 1 });

/** Part d'une tranche dans le total, en pourcentage arrondi à 0,1. */
export function pourcentage(part, total) {
  if (!total) return 0;
  return Math.round((1000 * part) / total) / 10;
}

/** Pourcentage formaté en français : « 44,4 % ». */
export function formaterPourcentage(part, total) {
  return `${POURCENT.format(pourcentage(part, total))} %`;
}
