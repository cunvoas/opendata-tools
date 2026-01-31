import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
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
