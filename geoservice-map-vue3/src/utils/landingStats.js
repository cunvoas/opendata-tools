import { DATA_MODE, STATIC_ON_GIT, DYNAMIC_ON_LOCAL } from './config.js';
import { epciParSiren } from './territoires.js';
import { bornesTranche } from './tranches.js';

/** Dernier millésime Filosofi publié dans geoservice-data. */
export const ANNEE_DONNEES = 2021;
/** Code INSEE de Lille. */
export const COMMUNE_LILLE = '59350';
/** Numéro Siren de la Métropole Européenne de Lille (identifiant public, jamais l'id technique). */
export const SIREN_MEL = '200093201';

/** Dossier de publication de la MEL, résolu par son numéro Siren. */
export const dossierEpciMel = () => epciParSiren(SIREN_MEL)?.dossier ?? null;

/**
 * URL des statistiques d'une commune, au même format que StatsGraph.
 * @param {string} insee - code INSEE de la commune.
 * @param {number} annee - millésime.
 * @returns {string} URL du JSON.
 */
export function urlStatsCommune(insee, annee = ANNEE_DONNEES) {
  return DATA_MODE === 'dynamic'
    ? `${DYNAMIC_ON_LOCAL}/map/stats/city/${insee}?annee=${annee}`
    : `${STATIC_ON_GIT}/data/stats/${insee.substring(0, 2)}/${insee}/stats_${insee}_${annee}.json`;
}

/**
 * URL des statistiques d'un EPCI pour un type de graphique.
 * @param {number|string} id - dossier de publication de l'EPCI.
 * @param {string} type - `all`, `urbans` ou `suburbs`.
 * @param {number} annee - millésime.
 * @returns {string} URL du JSON.
 */
export function urlStatsEpciType(id, type = 'all', annee = ANNEE_DONNEES) {
  return DATA_MODE === 'dynamic'
    ? `${DYNAMIC_ON_LOCAL}/map/stats/com2co/${id}/${type}?annee=${annee}`
    : `${STATIC_ON_GIT}/data/stats/com2co/${id}/stats_c2c_${id}_${type}_${annee}.json`;
}

/**
 * URL des statistiques « densités confondues » d'un EPCI.
 * @param {number|string} id - dossier de publication de l'EPCI.
 * @param {number} annee - millésime.
 * @returns {string} URL du JSON.
 */
export const urlStatsEpci = (id, annee = ANNEE_DONNEES) => urlStatsEpciType(id, 'all', annee);

/** Ajoute le suffixe v2 à un chemin JSON. */
const suffixeV2 = (url) => url.replace(/\.json$/, '.v2.json');

/**
 * URLs à essayer pour une commune : contrat v2 d'abord, ancien format en secours.
 * En mode dynamique, l'API locale n'expose pas encore les fichiers v2.
 * @param {string} insee - code INSEE.
 * @param {number} annee - millésime.
 * @returns {string[]} URLs candidates.
 */
export function urlsStatsCommune(insee, annee = ANNEE_DONNEES) {
  const url = urlStatsCommune(insee, annee);
  return DATA_MODE === 'dynamic' ? [url] : [suffixeV2(url), url];
}

/**
 * URLs à essayer pour un EPCI : contrat v2 d'abord, ancien format en secours.
 * @param {number|string} id - dossier de publication.
 * @param {string} type - `all`, `urbans` ou `suburbs`.
 * @param {number} annee - millésime.
 * @returns {string[]} URLs candidates.
 */
export function urlsStatsEpciType(id, type = 'all', annee = ANNEE_DONNEES) {
  const url = urlStatsEpciType(id, type, annee);
  return DATA_MODE === 'dynamic' ? [url] : [suffixeV2(url), url];
}

/**
 * Télécharge le premier JSON disponible d'une liste d'URLs.
 * @param {string|string[]} urls - URLs candidates.
 * @param {Function} fetchFn - fonction fetch (injectable pour les tests).
 * @returns {Promise<object|null>} JSON, null si toutes les URLs échouent.
 */
export async function chargerStats(urls, fetchFn = fetch) {
  for (const url of [].concat(urls)) {
    try {
      const reponse = await fetchFn(url);
      if (reponse.ok) return await reponse.json();
    } catch {
      // URL suivante
    }
  }
  return null;
}

const somme = (stats) => stats.reduce((total, tranche) => total + (tranche.habitants || 0), 0);
const nombreOuNull = (valeur) => (typeof valeur === 'number' && Number.isFinite(valeur) ? valeur : null);
const pourcent = (part, total) => Math.round((1000 * part) / total) / 10;

/**
 * Résume un JSON de statistiques : part des habitants sous le minimum OMS.
 * Les communes ont cinq tranches (les trois premières sont sous le minimum), les EPCI « all » trois niveaux
 * (le premier est « non respect des minima »). Les habitants sans parc accessible sont dans la première tranche.
 * @param {object} json - JSON publié par geoservice-data.
 * Le bloc `descripteur`, calculé par le serveur, apporte les deux m²/habitant (moyenne pondérée par la population,
 * ratio global) ; absent des anciens JSON, il donne alors null.
 * @returns {{annee: string, habitants: number, sousMinimum: number, minimumM2: (number|null), premiereBorne: (number|null), premiereTranche: (number|null), m2Moyen: (number|null), m2Global: (number|null)}|null}
 *   null si le JSON est inexploitable.
 */
export function resumeStats(json) {
  const stats = json?.stats;
  if (!Array.isArray(stats) || stats.length < 3) return null;
  const habitants = somme(stats);
  if (habitants <= 0) return null;

  const parCommune = stats.length >= 5;
  const sousMinimum = parCommune ? somme(stats.slice(0, 3)) : stats[0].habitants;
  const bornes = (tranche) => bornesTranche(tranche) ?? { min: null, max: null };
  const seuilMinimum = bornes(json.seuils?.[3] ?? json.stats?.[3]);
  const premiereTranche = bornes(json.seuils?.[0] ?? json.stats?.[0]);
  return {
    annee: String(json.annee ?? ''),
    habitants,
    sousMinimum: pourcent(sousMinimum, habitants),
    minimumM2: parCommune ? seuilMinimum.min : null,
    premiereBorne: parCommune ? premiereTranche.max : null,
    premiereTranche: parCommune ? pourcent(stats[0].habitants, habitants) : null,
    m2Moyen: nombreOuNull(json.descripteur?.m2ParHabitantMoyen),
    m2Global: nombreOuNull(json.descripteur?.m2ParHabitantGlobal)
  };
}

/**
 * Télécharge et résume un JSON de statistiques.
 * @param {string|string[]} urls - URLs candidates (v2 puis ancien format).
 * @param {Function} fetchFn - fonction fetch (injectable pour les tests).
 * @returns {Promise<object|null>} résumé, null si le téléchargement ou le JSON échoue.
 */
export async function chargerResume(urls, fetchFn = fetch) {
  const json = await chargerStats(urls, fetchFn);
  return json ? resumeStats(json) : null;
}

const ID_DONNEES_INTEGREES = 'landing-stats';

/**
 * Lit les chiffres intégrés au HTML au pré-rendu : la page s'affiche avec ses chiffres dès le premier rendu,
 * sans repasser par « Chargement… ». Renvoie un objet vide si rien n'est intégré ou si le contenu est invalide.
 * @param {Document} doc - document à lire.
 * @returns {{lille?: object, mel?: object}} chiffres intégrés.
 */
export function lireDonneesIntegrees(doc = document) {
  try {
    const brut = doc.getElementById(ID_DONNEES_INTEGREES)?.textContent;
    const donnees = brut ? JSON.parse(brut) : {};
    return donnees && typeof donnees === 'object' ? donnees : {};
  } catch {
    return {};
  }
}

/**
 * Intègre les chiffres au document, pour que le pré-rendu les emporte dans le HTML.
 * @param {{lille: object, mel: object}} donnees - résumés à intégrer.
 * @param {Document} doc - document à écrire.
 */
export function integrerDonnees(donnees, doc = document) {
  let script = doc.getElementById(ID_DONNEES_INTEGREES);
  if (!script) {
    script = doc.createElement('script');
    script.type = 'application/json';
    script.id = ID_DONNEES_INTEGREES;
    doc.head.appendChild(script);
  }
  // « < » échappé : le contenu ne peut jamais refermer la balise script
  script.textContent = JSON.stringify(donnees).replace(/</g, '\\u003c');
}

