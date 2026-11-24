import { fileURLToPath, URL } from 'node:url'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    // Em dev, o front chama /api no mesmo host e o Vite repassa ao Spring Boot (sem CORS).
    proxy: {
      '/api': 'http://localhost:8080',
      '/v3/api-docs': 'http://localhost:8080',
    },
  },
})
