import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Vite config for the Smart Solar Microgrid Trading System web application.
export default defineConfig({
  base: './',
  plugins: [react()],
  server: {
    port: 5173
  }
})
