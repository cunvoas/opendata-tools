import fs from 'node:fs';
import path from 'node:path';

const distDir = path.resolve('dist');
const baseUrl = 'https://parcs-et-jardins.autmel.ovh';
const today = new Date().toISOString().split('T')[0];
const { communes, epcis } = JSON.parse(fs.readFileSync('./src/data/territoires.json', 'utf-8'));
// Même plafond que vite.config.js : le sitemap ne liste que les pages réellement pré-rendues.
const maxTerritoires = Number(process.env.VITE_PRERENDER_MAX) || communes.length;

// Les pages HTML sont pré-rendues par vite-plugin-prerender (vite.config.js)

// La landing lit ses chiffres dans les JSON publiés : sans eux, la page pré-rendue serait vide. On refuse le build.
const home = fs.readFileSync(path.join(distDir, 'index.html'), 'utf-8');
if (!home.includes('data-landing-ready="true"')) {
  console.error("[SEO] La landing a été pré-rendue sans ses chiffres (JSON de statistiques injoignables ?). Build refusé.");
  process.exit(1);
}

// La page étude INSEE doit être pré-rendue avec son contenu : une page vide n'a rien à indexer.
const etudeInsee = path.join(distDir, 'etude-insee', 'index.html');
if (!fs.existsSync(etudeInsee) || !fs.readFileSync(etudeInsee, 'utf-8').includes('pourquoi les chiffres diffèrent')) {
  console.error("[SEO] La page /etude-insee est absente ou vide. Build refusé.");
  process.exit(1);
}

/**
 * Vérifie qu'une page territoriale pré-rendue porte le nom de son territoire.
 * Une page vide passerait sinon en production sans contenu indexable.
 * @param {string} route - chemin de la page, ex. /stats/59350.
 * @param {string} nom - nom attendu dans le H1.
 */
function verifierTerritoire(route, nom) {
  const fichier = path.join(distDir, route, 'index.html');
  if (!fs.existsSync(fichier)) {
    console.error(`[SEO] Page pré-rendue absente : ${route}. Build refusé.`);
    process.exit(1);
  }
  if (!fs.readFileSync(fichier, 'utf-8').includes(nom)) {
    console.error(`[SEO] La page pré-rendue ${route} ne contient pas « ${nom} ». Build refusé.`);
    process.exit(1);
  }
}

const territoires = [
  ...communes.slice(0, maxTerritoires).map((commune) => ({ route: `stats/${commune.insee}`, nom: commune.nom })),
  ...epcis.map((epci) => ({ route: `stats/epci/${epci.siren}`, nom: epci.nom }))
];
territoires.forEach(({ route, nom }) => verifierTerritoire(route, nom));

const routes = [
  { path: '', changefreq: 'monthly', priority: '1.0' },
  { path: 'carte', changefreq: 'monthly', priority: '1.0' },
  { path: 'stats', changefreq: 'monthly', priority: '0.9' },
  ...territoires.map(({ route }) => ({ path: route, changefreq: 'monthly', priority: '0.7' })),
  { path: 'information', changefreq: 'yearly', priority: '0.8' },
  { path: 'etude-insee', changefreq: 'yearly', priority: '0.8' },
  { path: 'aideVideo', changefreq: 'yearly', priority: '0.5' },
];

if (process.env.VITE_FEATURE_NDVI === 'true') {
  routes.push({ path: 'ndvi', changefreq: 'monthly', priority: '0.8' });
}

const sitemapUrls = routes
  .map(
    (route) => `  <url>
    <loc>${baseUrl}/${route.path}</loc>
    <lastmod>${today}</lastmod>
    <changefreq>${route.changefreq}</changefreq>
    <priority>${route.priority}</priority>
  </url>`
  )
  .join('\n');

const sitemapXml = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${sitemapUrls}
</urlset>
`;

fs.writeFileSync(path.join(distDir, 'sitemap.xml'), sitemapXml, 'utf-8');
console.log(`[SEO] Généré : /sitemap.xml (${routes.length} URLs, lastmod: ${today})`);
