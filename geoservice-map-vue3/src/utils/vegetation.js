/**
 * Indices de végétation exposés par l'API (carte et écran NDVI).
 * Chaque entrée décrit le préfixe des propriétés plates : `<prefix>Mean`, `Min`, `Max`, `Stddev`.
 */
export const VEGETATION_INDICES = [
  { key: 'ndvi', label: 'NDVI', prefix: 'ndvi' },
  { key: 'ndre', label: 'NDRE', prefix: 'ndre' },
  { key: 'gndvi', label: 'GNDVI', prefix: 'gndvi' },
  { key: 'savi', label: 'SAVI', prefix: 'savi' }
];

function toNumber(value) {
  if (value === null || value === undefined || value === '') return null;
  const numeric = Number.parseFloat(String(value).replace(',', '.'));
  return Number.isFinite(numeric) ? numeric : null;
}

/**
 * Statistiques d'un indice pour un parc, ou null si la moyenne est absente.
 * @param {object} properties - propriétés d'un parc (carte ou résumé NDVI).
 * @param {object} index - entrée de VEGETATION_INDICES.
 * @returns {{label: string, mean: number, min: number|null, max: number|null, std: number|null}|null}
 */
export function indexStats(properties, index) {
  const mean = toNumber(properties?.[`${index.prefix}Mean`]);
  if (mean === null) return null;
  return {
    label: index.label,
    mean,
    min: toNumber(properties?.[`${index.prefix}Min`]),
    max: toNumber(properties?.[`${index.prefix}Max`]),
    std: toNumber(properties?.[`${index.prefix}Stddev`])
  };
}

/** Statistiques disponibles pour un parc, dans l'ordre NDVI, NDRE, GNDVI, SAVI. */
export function availableIndices(properties) {
  return VEGETATION_INDICES
    .map((index) => indexStats(properties, index))
    .filter((stats) => stats !== null);
}

/**
 * Évaluation de végétation, qu'elle vienne de la carte (propriétés `vegetation*`)
 * ou du résumé NDVI (objet imbriqué `evaluation`).
 * @param {object} properties - propriétés ou résumé d'un parc.
 * @returns {{recouvrement: string|null, score: number|null, confiance: string|null,
 *            diagnostic: string|null, homogeneite: string|null, anomalie: boolean|null}|null}
 */
export function vegetationEvaluation(properties) {
  const nested = properties?.evaluation;
  if (nested) {
    return {
      recouvrement: nested.recouvrement ?? null,
      score: toNumber(nested.scoreSante),
      confiance: nested.confiance ?? null,
      diagnostic: nested.diagnosticPhysiologique ?? null,
      homogeneite: nested.homogeneite ?? null,
      anomalie: typeof nested.anomalieLocaleDetectee === 'boolean' ? nested.anomalieLocaleDetectee : null
    };
  }
  const score = toNumber(properties?.vegetationScore);
  const recouvrement = properties?.vegetationRecouvrement ?? null;
  if (score === null && recouvrement === null) return null;
  return {
    recouvrement,
    score,
    confiance: properties?.vegetationConfiance ?? null,
    diagnostic: properties?.vegetationDiagnostic ?? null,
    homogeneite: properties?.vegetationHomogeneite ?? null,
    anomalie: typeof properties?.vegetationAnomalie === 'boolean' ? properties.vegetationAnomalie : null
  };
}

/** Libellé d'affichage de l'anomalie locale (null = non calculable). */
export function anomalieLabel(anomalie) {
  if (anomalie === null || anomalie === undefined) return 'non calculable';
  return anomalie ? 'oui' : 'non';
}
