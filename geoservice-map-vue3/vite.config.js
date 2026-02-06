import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'
import fs from 'fs'
import path from 'path'

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
    const timestamp = new Date().toISOString().replace(/[:.]/g, '').slice(0, -5)
    const version = `${packageJson.version}-${timestamp}`
    
    const versionJson = JSON.stringify({ version }, null, 2)
    
    this.emitFile({
      type: 'asset',
      fileName: 'version.json',
      source: versionJson
    })
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    versionPlugin,
    vue(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'robots.txt'],
      manifest: {
        name: 'Analyse des Parcs et Jardins selon l\'OMS',
        short_name: 'Parcs & Jardins',
        description: 'Application d\'analyse géographique des parcs et jardins selon les standards OMS',
        theme_color: '#2d5016',
        background_color: '#ffffff',
        display: 'standalone',
        scope: '/parcs-et-jardins/',
        start_url: '/parcs-et-jardins/',
        icons: [
          {
            src: '/parcs-et-jardins/icons/icon-192x192.png',
            sizes: '192x192',
            type: 'image/png',
            purpose: 'any'
          },
          {
            src: '/parcs-et-jardins/icons/icon-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any'
          },
          {
            src: '/parcs-et-jardins/icons/icon-maskable-192x192.png',
            sizes: '192x192',
            type: 'image/png',
            purpose: 'maskable'
          },
          {
            src: '/parcs-et-jardins/icons/icon-maskable-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable'
          }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff,woff2,ttf,eot}'],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/api\..*/i,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              expiration: {
                maxEntries: 100,
                maxAgeSeconds: 60 * 60 * 24 * 7 // 7 jours
              }
            }
          },
          {
            urlPattern: /^https:\/\/tile\..*/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'tiles-cache',
              expiration: {
                maxEntries: 500,
                maxAgeSeconds: 60 * 60 * 24 * 30 // 30 jours
              }
            }
          }
        ]
      }
    })
  ],
  build: {
    target: "ES2022"
  },
  base: '/parcs-et-jardins/',
  server: {
    historyApiFallback: true,
    cors: {
      origin: false,
    },

    //middlewareMode: "html"
  },
  
  
})
