import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  build: {
    target: "ES2022"
  },
  base: '/geolocation/',
  server: {
    historyApiFallback: true
  }
  
})
