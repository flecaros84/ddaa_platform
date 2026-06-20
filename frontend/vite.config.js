import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],

  server: {
    port: 5173,
    proxy: {
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/bff': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/oauth2': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/login': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/logout': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },

  test: {
    // jsdom permite probar componentes React como si existiera un DOM de navegador.
    environment: 'jsdom',

    // Archivo común para matchers de Testing Library.
    setupFiles: './src/test/setup.js',

    coverage: {
      provider: 'v8',
      reporter: ['text', 'html', 'lcov'],

      // Se mide código de aplicación.
      include: ['src/**/*.{js,jsx}'],

      // Se excluye bootstrap, assets y setup de tests porque no contienen lógica de negocio.
      exclude: [
        'src/main.jsx',
        'src/test/**',
        'src/assets/**'
      ]
    }
  }
});