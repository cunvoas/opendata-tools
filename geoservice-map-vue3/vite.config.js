import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
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
