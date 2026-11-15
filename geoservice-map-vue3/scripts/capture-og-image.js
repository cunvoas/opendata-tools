/**
 * Capture la carte de Lille en image de partage 1200x630 (Open Graph, Twitter).
 *
 * Prérequis : le site servi en local, par exemple `npm run build && npx vite preview --port 4173`,
 * et PUPPETEER_EXECUTABLE_PATH vers un Chrome récent (voir deployGhP.sh).
 * Usage : node scripts/capture-og-image.js
 * Variables facultatives : BASE_URL (http://localhost:4173), OUT, ATTENTE_MS (8000).
 *
 * Le fichier produit reprend les données publiées au moment de la capture : à refaire après chaque recalcul.
 */
import { createRequire } from 'node:module';

const puppeteer = createRequire(import.meta.url)('puppeteer');

const BASE_URL = process.env.BASE_URL || 'http://localhost:4173';
const OUT = process.env.OUT || 'public/og-carte-lille-1200x630.jpg';
const ATTENTE_MS = Number(process.env.ATTENTE_MS || 8000);
const LARGEUR = 1200;
const HAUTEUR = 630;

const browser = await puppeteer.launch({
  headless: true,
  executablePath: process.env.PUPPETEER_EXECUTABLE_PATH,
  args: ['--no-sandbox', '--disable-setuid-sandbox'],
});
try {
  const page = await browser.newPage();
  await page.setViewport({ width: LARGEUR + 300, height: HAUTEUR + 800 });
  await page.goto(`${BASE_URL}/carte`, { waitUntil: 'networkidle0', timeout: 90000 });
  await page.waitForSelector('.leaflet-container', { timeout: 30000 });
  await page.addStyleTag({ content: `.leaflet-container { width: ${LARGEUR}px !important; height: ${HAUTEUR}px !important; }` });
  await page.evaluate(() => window.dispatchEvent(new Event('resize')));
  // Les tuiles OpenStreetMap et les carreaux calculés arrivent après le chargement de la page.
  await new Promise((resolve) => setTimeout(resolve, ATTENTE_MS));
  const carte = await page.$('.leaflet-container');
  await carte.screenshot({ path: OUT, type: 'jpeg', quality: 85 });
  console.log(`[OG] Image écrite : ${OUT} (${LARGEUR}x${HAUTEUR})`);
} finally {
  await browser.close();
}
