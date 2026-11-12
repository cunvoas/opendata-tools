import fs from 'node:fs';
import path from 'node:path';

const distDir = path.resolve('dist');
const template = fs.readFileSync(path.join(distDir, 'index.html'), 'utf-8');
const baseUrl = 'https://autmel.ovh';
const today = new Date().toISOString().split('T')[0];

const routes = [
  {
    path: 'carte',
    title: 'Carte des parcs et jardins – MEL | Parcs & Jardins',
    description: 'Explorez la carte interactive des parcs, jardins et espaces verts de la Métropole Européenne de Lille. Analyse de l\'accessibilité selon les critères OMS.',
    changefreq: 'monthly',
    priority: '1.0',
  },
  {
    path: 'stats',
    title: 'Statistiques des espaces verts – MEL | Parcs & Jardins',
    description: 'Statistiques détaillées sur la superficie et la répartition des espaces verts par habitant dans la MEL. Comparaison avec les standards OMS (10 m² par habitant).',
    changefreq: 'monthly',
    priority: '0.9',
  },
  {
    path: 'information',
    title: 'Informations – Méthodologie et sources | Parcs & Jardins',
    description: 'Méthodologie, sources de données et informations sur l\'étude géographique des parcs et jardins de la MEL réalisée par Autmel, Deûl\'Air, Entrelianne et LM Oxygène.',
    changefreq: 'yearly',
    priority: '0.8',
  },
  {
    path: 'aideoVideo',
    title: 'Aide vidéo | Parcs & Jardins',
    description: 'Tutoriels vidéo pour utiliser l\'application cartographique des parcs et jardins de la MEL.',
    changefreq: 'yearly',
    priority: '0.5',
  },
];

// Ajoute automatiquement /ndvi au HTML et au sitemap si le flag est actif
if (process.env.VITE_FEATURE_NDVI === 'true') {
  routes.push({
    path: 'ndvi',
    title: 'NDVI des parcs et jardins – MEL | Parcs & Jardins',
    description: 'Indice de végétation NDVI mesuré par satellite sur les parcs et jardins de la Métropole Européenne de Lille : moyenne, minimum, maximum et écart-type par parc.',
    changefreq: 'monthly',
    priority: '0.8',
  });
}

// 1. Génération des pages HTML statiques par route
for (const route of routes) {
  const canonicalUrl = `${baseUrl}/${route.path}`;
  let html = template
    .replace(/<title>.*?<\/title>/, `<title>${route.title}</title>`)
    .replace(
      /<meta name="description" content=".*?"\s*\/?>/,
      `<meta name="description" content="${route.description}" />`
    )
    .replace(
      '</head>',
      `  <link rel="canonical" href="${canonicalUrl}" />\n</head>`
    );

  const routeDir = path.join(distDir, route.path);
  fs.mkdirSync(routeDir, { recursive: true });
  fs.writeFileSync(path.join(routeDir, 'index.html'), html, 'utf-8');
  console.log(`[HTML] Généré : /${route.path}/index.html`);
}

// 2. Génération dynamique du sitemap.xml dans dist/
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
<urlset xmlns="http://sitemaps.org">
${sitemapUrls}
</urlset>
`;

fs.writeFileSync(path.join(distDir, 'sitemap.xml'), sitemapXml, 'utf-8');
console.log(`[SEO] Généré : /sitemap.xml (${routes.length} URLs, lastmod: ${today})`);
