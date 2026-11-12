import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'
import fs from 'fs'
import path from 'path'
import { createRequire } from 'module'

// vite-plugin-prerender : la build ESM (dist/index.mjs) appelle require() sans createRequire, on charge la version CJS
const require = createRequire(import.meta.url)
const VitePrerender = require('vite-plugin-prerender')
const PuppeteerRenderer = require('@prerenderer/renderer-puppeteer')

// Copy index.html to 404.html so GitHub Pages serves the SPA for unknown routes
const spaFallbackPlugin = {
  name: 'spa-fallback',
  apply: 'build',
  closeBundle() {
    const outDir = 'dist'
    const index = `${outDir}/index.html`
    const fallback = `${outDir}/404.html`
    if (fs.existsSync(index)) fs.copyFileSync(index, fallback)
  }
}

// Plugin to generate version.json from package.json
const versionPlugin = {
  name: 'version-plugin',
  apply: 'build',
  enforce: 'pre',
  resolveId(id) {
    if (id.includes('virtual-version')) {
      return id
    }
  },
  async generateBundle() {
    const packageJson = JSON.parse(fs.readFileSync('./package.json', 'utf-8'))
    const timestamp = new Date().toISOString().replace(/[:.-]/g, '').slice(0, -4).replace(/[T]/g, '-')
    const version = `${packageJson.version}_${timestamp}`
    
    const versionJson = JSON.stringify({ version }, null, 2)
    
    this.emitFile({
      type: 'asset',
      fileName: 'version.json',
      source: versionJson
    })
  }
}

// Root path: '/' for custom domain (CNAME), '/parcs-et-jardins/' for GitHub Pages repo URL
const ROOT_PATH = '/'
// const ROOT_PATH = '/parcs-et-jardins/'

// Routes pré-rendues en HTML réel (GitHub Pages sert dist/<route>/index.html en 200 au lieu du 404)
const prerenderRoutes = ['/carte', '/stats', '/information', '/aideoVideo']
if (process.env.VITE_FEATURE_NDVI === 'true') prerenderRoutes.push('/ndvi')


// https://vite.dev/config/
export default defineConfig({
  plugins: [
    versionPlugin,
    spaFallbackPlugin,
    vue(),
    VitePWA({
      registerType: 'autoUpdate',
      strategies: 'injectManifest',
      srcDir: 'src',
      filename: 'sw.js',
      includeAssets: ['favicon.ico', 'robots.txt'],
      manifest: {
        name: 'Analyse des Parcs et Jardins selon l\'OMS',
        short_name: 'Parcs & Jardins',
        description: 'Application d\'analyse géographique des parcs et jardins selon les standards OMS',
        theme_color: '#2d5016',
        background_color: '#ffffff',
        display: 'standalone',
        scope: ROOT_PATH,
        start_url: ROOT_PATH,
        icons: [
          {
            src: `${ROOT_PATH}icons/icon-192x192.png`,
            sizes: '192x192',
            type: 'image/png',
            purpose: 'any'
          },
          {
            src: `${ROOT_PATH}icons/icon-512x512.png`,
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any'
          },
          {
            src: `${ROOT_PATH}icons/icon-maskable-192x192.png`,
            sizes: '192x192',
            type: 'image/png',
            purpose: 'maskable'
          },
          {
            src: `${ROOT_PATH}icons/icon-maskable-512x512.png`,
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable'
          }
        ]
      },
    }),
    VitePrerender({
      staticDir: path.resolve('dist'),
      routes: prerenderRoutes,
      renderer: new PuppeteerRenderer({
        headless: true,
        renderAfterTime: 3000,
        args: ['--no-sandbox', '--disable-setuid-sandbox'],
      }),
      postProcess(renderedRoute) {
        renderedRoute.html = renderedRoute.html.replace(
          /<link rel="canonical" href="[^"]*"\s*\/?>/,
          `<link rel="canonical" href="https://parcs-et-jardins.autmel.ovh${renderedRoute.route}" />`
        )
        return renderedRoute
      },
    }),
  ],
  build: {
    target: "es2021"
  },
  base: ROOT_PATH,
  server: {
    historyApiFallback: true,
    cors: {
      origin: false,
    },
    // VITE_DEV_PROXY=true (test.sh) : /isochrone est redirigé vers le backend local, sans CORS
    proxy: process.env.VITE_DEV_PROXY === 'true' ? { '/isochrone': 'http://localhost:8980' } : undefined,

    //middlewareMode: "html"
  },
  
  
})
