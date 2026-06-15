<template>
  <div class="auth-brand-panel">
    <div class="auth-brand-content">
      <div class="brand">
        <div class="brand-icon">
          <el-icon :size="48"><Shop /></el-icon>
        </div>
        <h1>助农商城</h1>
      </div>
      <div class="features">
        <div v-for="feature in features" :key="feature" class="feature-item">
          <el-icon :size="24"><CircleCheck /></el-icon>
          <span>{{ feature }}</span>
        </div>
      </div>
      <div class="decoration">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { CircleCheck, Shop } from '@element-plus/icons-vue'

defineProps({
  features: {
    type: Array,
    default: () => ['源头直采，品质保障', '绿色有机，健康生活', '助力农乡，共赢未来']
  }
})
</script>

<style scoped>
.auth-brand-panel {
  flex: 1;
  background: linear-gradient(135deg, #1b3a26 0%, #2e7d32 50%, #4caf50 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.auth-brand-content {
  text-align: center;
  color: #fff;
  z-index: 1;
  padding: 40px;
}

.brand {
  margin-bottom: 60px;
}

.brand-icon {
  width: 80px;
  height: 80px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  backdrop-filter: blur(10px);
}

.brand h1 {
  font-size: 28px;
  font-weight: 600;
  letter-spacing: 2px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: flex-start;
  padding-left: 60px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  opacity: 0.9;
}

.decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}

.circle-1 {
  width: 400px;
  height: 400px;
  top: -100px;
  left: -100px;
}

.circle-2 {
  width: 300px;
  height: 300px;
  bottom: -50px;
  right: -50px;
}

.circle-3 {
  width: 200px;
  height: 200px;
  top: 50%;
  right: 20%;
}
</style>
