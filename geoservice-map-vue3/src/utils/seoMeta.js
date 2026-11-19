/** Adresse publique du site, base des URL canoniques et de partage. */
export const SITE_URL = 'https://parcs-et-jardins.autmel.ovh';

/**
 * Applique le titre, la description et l'URL d'une route aux balises du document :
 * title, description, Open Graph et Twitter. Le pré-rendu capture ce résultat pour chaque page.
 * @param {Document} doc - document à mettre à jour.
 * @param {{path: string, meta?: {title?: string, description?: string, robots?: string}}} route - route résolue.
 */
export function applyRouteMeta(doc, route) {
  const set = (selector, value) => doc.querySelector(selector)?.setAttribute('content', value);
  const { title, description } = route.meta ?? {};
  if (title) {
    doc.title = title;
    set('meta[property="og:title"]', title);
    set('meta[name="twitter:title"]', title);
  }
  if (description) {
    set('meta[name="description"]', description);
    set('meta[property="og:description"]', description);
    set('meta[name="twitter:description"]', description);
  }
  set('meta[property="og:url"]', SITE_URL + route.path);
  set('meta[name="robots"]', route.meta?.robots ?? 'index, follow');

  // Adresse canonique : celle de la route, sans paramètre ni ancre.
  let canonical = doc.querySelector('link[rel="canonical"]');
  if (!canonical && doc.head) {
    canonical = doc.createElement('link');
    canonical.setAttribute('rel', 'canonical');
    doc.head.appendChild(canonical);
  }
  canonical?.setAttribute('href', SITE_URL + route.path);
}
