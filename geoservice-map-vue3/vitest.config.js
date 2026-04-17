import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'happy-dom',
    pool: 'vmForks',
    globals: true,
    reporters: ['default', 'junit'],
    outputFile: {
      junit: './coverage/test-report.xml'
    },
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],
      include: ['src/**/*.{js,vue}'],
      exclude: ['src/main.js']
    }
  }
})
