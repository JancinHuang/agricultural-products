import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5174,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules/zrender')) {
            return 'zrender'
          }

          if (id.includes('node_modules/echarts')) {
            return 'echarts'
          }

          if (id.includes('node_modules/vue') || id.includes('node_modules/vue-router') || id.includes('node_modules/pinia')) {
            return 'vue'
          }

          if (id.includes('node_modules/element-plus') || id.includes('node_modules/@element-plus/icons-vue')) {
            return 'element'
          }

          if (id.includes('node_modules/marked')) {
            return 'markdown'
          }
        }
      }
    }
  }
})
