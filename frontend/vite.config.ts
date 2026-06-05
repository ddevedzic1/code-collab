import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          react: ['react', 'react-dom', 'react-router-dom'],
          chakra: ['@chakra-ui/react', '@emotion/react', '@emotion/styled', 'framer-motion'],
          codemirror: [
            '@uiw/react-codemirror',
            '@codemirror/state',
            '@codemirror/view',
            '@codemirror/theme-one-dark',
            '@codemirror/lang-python',
            '@codemirror/lang-javascript',
            '@codemirror/lang-java',
            '@codemirror/lang-cpp',
            '@codemirror/lang-rust',
            '@codemirror/lang-go',
            '@codemirror/lang-php',
            '@codemirror/lang-sql',
          ],
        },
      },
    },
  },
});
