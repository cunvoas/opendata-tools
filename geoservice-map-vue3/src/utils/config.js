/**
 * Configuration unique du front : source des données et feature flags.
 * Chaque valeur se surcharge au build par une variable Vite VITE_* (deployGhP.sh, .env.local).
 */

/** Source des données : static = JSON publiés (dépôt GitHub), dynamic = API locale. Par défaut : static. */
export const DATA_MODE = import.meta.env.VITE_DATA_MODE || 'static';
/** Racine des JSON publiés (dépôt GitHub). */
export const STATIC_ON_GIT = import.meta.env.VITE_STATIC_ON_GIT || 'https://raw.githubusercontent.com/autmel/geoservice-data/refs/heads/main';
/** Racine de l'API locale (mode dynamic), sans barre finale. */
export const DYNAMIC_ON_LOCAL = (import.meta.env.VITE_DYNAMIC_API || 'http://localhost:8980/isochrone').replace(/\/$/, '');

/**
 * Feature flags, fixés au build par des variables Vite VITE_FEATURE_<NOM>=true|false.
 * Sans valeur, une fonctionnalité n'est active qu'en développement.
 * @param {string|undefined} value - valeur brute de la variable d'environnement.
 * @param {boolean} isDev - true en développement (et dans les tests).
 * @returns {boolean} true si la fonctionnalité est active.
 */
export function isFeatureEnabled(value, isDev) {
  if (value === undefined || value === '') return isDev;
  return String(value).trim().toLowerCase() === 'true';
}

export const features = {
  ndvi: isFeatureEnabled(import.meta.env.VITE_FEATURE_NDVI, import.meta.env.DEV)
};
