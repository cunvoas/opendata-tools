import fs from 'node:fs';
import path from 'node:path';

const distDir = path.resolve('dist');
const baseUrl = 'https://parcs-et-jardins.autmel.ovh';
const today = new Date().toISOString().split('T')[0];

// Les pages HTML sont pré-rendues par vite-plugin-prerender (vite.config.js)
const routes = [
  { path: 'carte', changefreq: 'monthly', priority: '1.0' },
  { path: 'stats', changefreq: 'monthly', priority: '0.9' },
  { path: 'information', changefreq: 'yearly', priority: '0.8' },
  { path: 'aideoVideo', changefreq: 'yearly', priority: '0.5' },
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
