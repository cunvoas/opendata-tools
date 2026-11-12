/**
 * Grille NDVI unique du front (écran NDVI, carte et légende) : seuils universels par type de surface.
 * Classée du NDVI le plus faible au plus élevé ; `min` est la borne basse incluse.
 * `label` est la forme courte (cellules, graphique, détail d'un parc), `description` le texte complet.
 * À tenir alignée avec NDVI_CATEGORIES de geoservice-isochrone-v2/src/main/resources/templates/consultMap.html.
 */
export const NDVI_CATEGORIES = [
  { min: -Infinity, range: '-1,0 à 0,1', label: 'Eau, neige, roche, bâti',
    description: 'Eau, neige, roche, routes et bâtiments.', color: '#d9f0a3', colorDaltonien: '#fee838' },
  { min: 0.1, range: '0,1 à 0,2', label: 'Sol nu ou végétation morte',
    description: 'Sol nu, sable ou végétation morte/sèche.', color: '#addd8e', colorDaltonien: '#008afc' },
  { min: 0.2, range: '0,2 à 0,4', label: 'Végétation très clairsemée',
    description: 'Végétation très clairsemée, début de levée, ou pelouse tondue/brûlée.', color: '#78c679', colorDaltonien: '#7d7c78' },
  { min: 0.4, range: '0,4 à 0,7', label: 'Végétation en croissance',
    description: 'Végétation en croissance active, couverture du sol modérée.', color: '#31a354', colorDaltonien: '#434e6c' },
  { min: 0.7, range: '0,7 à 1,0', label: 'Zone de saturation',
    description: 'Zone de saturation. Canopée dense, forêt, culture en pleine maturité. Attention : une plante malade peut encore afficher 0,85.', color: '#006837', colorDaltonien: '#00224e' }
];

/** Couleur des parcs dont le NDVI moyen est absent ou non numérique. */
export const NDVI_NEUTRAL_COLOR = '#959595';
/**
 * Mode daltonien : jaune → bleu foncé (cividis inversé, sauf « sols nus » choisi pour se distinguer
 * des couleurs de la légende), lisible en deutéranopie sur le fond de carte à 50 %.
 * Le contour sombre garde les parcs délimités sur le fond de carte (contraste 3:1 minimum, RGAA 3.3).
 */
export const NDVI_NEUTRAL_COLOR_DALTONIEN = '#c8c8c8';
export const NDVI_CONTOUR_DALTONIEN = '#1f2937';

const DESCENDING = [...NDVI_CATEGORIES].reverse();
const roundedFormat = new Intl.NumberFormat('en-US', { minimumFractionDigits: 3, maximumFractionDigits: 3, useGrouping: false });

/**
 * Valeur NDVI arrondie à 3 décimales, comme affichée (« 0,500 » ne doit pas rester sous le seuil 0,5).
 * @param {number|string|null} ndviValue - NDVI (nombre ou texte, virgule décimale acceptée).
 * @returns {number|null} valeur arrondie, ou null si la valeur est absente ou non numérique.
 */
export function arrondiNdvi(ndviValue) {
  const numeric = Number.parseFloat(String(ndviValue ?? '').replace(',', '.'));
  if (!Number.isFinite(numeric)) return null;
  return Number(roundedFormat.format(numeric));
}

/**
 * Catégorie d'un NDVI moyen, classée sur la valeur arrondie comme affichée.
 * @param {number|string|null} ndviMean - NDVI moyen (nombre ou texte, virgule décimale acceptée).
 * @returns {object|null} catégorie de NDVI_CATEGORIES, ou null si la valeur est absente ou non numérique.
 */
export function getNdviCategory(ndviMean) {
  const rounded = arrondiNdvi(ndviMean);
  if (rounded === null) return null;
  return DESCENDING.find((category) => rounded >= category.min);
}

/**
 * Couleur d'une catégorie selon le mode (normal ou daltonien).
 * @param {object|null} category - entrée de NDVI_CATEGORIES.
 * @param {boolean} [daltonien=false] - mode daltonien actif.
 * @returns {string} couleur #rrggbb, ou la couleur neutre si la catégorie est absente.
 */
export function couleurCategorie(category, daltonien = false) {
  if (!category) return daltonien ? NDVI_NEUTRAL_COLOR_DALTONIEN : NDVI_NEUTRAL_COLOR;
  return daltonien ? category.colorDaltonien : category.color;
}

/**
 * Couleur NDVI d'un parc (celle de sa catégorie), neutre si la valeur est absente.
 * @param {number|string|null} ndviMean - NDVI moyen du parc.
 * @param {boolean} [daltonien=false] - mode daltonien actif.
 * @returns {string} couleur #rrggbb.
 */
export function getNdviColor(ndviMean, daltonien = false) {
  return couleurCategorie(getNdviCategory(ndviMean), daltonien);
}
