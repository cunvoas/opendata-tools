/**
 * Feature flags du front, fixés au build par des variables Vite VITE_FEATURE_<NOM>=true|false.
 * Sans valeur, une fonctionnalité n'est active qu'en développement : un build de production
 * ne l'expose donc que si on l'active explicitement.
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
