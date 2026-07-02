import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import viteCompression from 'vite-plugin-compression'

export default defineConfig({
  plugins: [
    vue(),
    // Gzip 压缩：构建时生成 .gz 文件，nginx 直接返回压缩版本
    viteCompression({ algorithm: 'gzip', ext: '.gz', threshold: 1024, deleteOriginFile: false })
  ],
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    // 输出目录
    outDir: 'dist',
    // chunk 分包策略：把大依赖拆成独立文件，利用浏览器并行加载 + 独立缓存
    rollupOptions: {
      output: {
        manualChunks: {
          // Vue 核心 ~130KB，几乎不变 → 单独 chunk，浏览器永久缓存
          'vendor-vue': ['vue', 'vue-router', 'pinia'],
          // Element Plus ~1MB，升级频率低 → 独立 chunk
          'vendor-element': ['element-plus'],
          // ECharts ~800KB，仅 Dashboard/Statistics 用到，独立加载
          'vendor-echarts': ['echarts'],
        }
      }
    },
    // 资源内联阈值：小于 4KB 的静态资源内联为 base64，减少 HTTP 请求
    assetsInlineLimit: 4096,
    // CSS 代码分割
    cssCodeSplit: true,
  }
})
