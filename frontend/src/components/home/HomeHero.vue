<template>
  <section class="home-hero">
    <el-carousel height="420px" :interval="5000" arrow="always">
      <el-carousel-item v-for="(slide, index) in slides" :key="slide.id || index">
        <div class="home-hero__slide" :style="{ background: slide.imageUrl ? '#1f2a1d' : slide.bg }">
          <img v-if="slide.imageUrl" :src="slide.imageUrl" :alt="slide.title" class="home-hero__image" />
          <div class="home-hero__overlay"></div>
          <div class="home-hero__content">
            <h2>{{ slide.title }}</h2>
            <p>{{ slide.subtitle }}</p>
            <BaseButton type="primary" size="large" round @click="$emit('open', slide.link || slide.linkUrl || '/shop')">
              {{ slide.btnText }}
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </BaseButton>
          </div>
          <div class="home-hero__decoration">{{ slide.icon }}</div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </section>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'

defineProps({
  slides: {
    type: Array,
    default: () => []
  }
})

defineEmits(['open'])
</script>

<style scoped>
.home-hero :deep(.el-carousel__item) {
  border-radius: 0;
}

.home-hero__slide {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.home-hero__image,
.home-hero__overlay {
  position: absolute;
  inset: 0;
}

.home-hero__image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
}

.home-hero__overlay {
  background: linear-gradient(90deg, rgba(20, 36, 18, 0.72), rgba(28, 64, 26, 0.46));
  z-index: 1;
}

.home-hero__content {
  text-align: center;
  z-index: 3;
  animation: fadeInUp 0.8s ease;
}

.home-hero__content h2 {
  margin: 0 0 var(--spacing-md);
  color: var(--color-white);
  font-size: 42px;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.home-hero__content p {
  margin: 0 0 30px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 18px;
  letter-spacing: 1px;
}

.home-hero__content .el-button {
  height: auto;
  padding: 12px 32px;
  font-size: var(--font-size-md);
}

.home-hero__decoration {
  position: absolute;
  right: 10%;
  top: 50%;
  z-index: 2;
  font-size: 200px;
  opacity: 0.1;
  transform: translateY(-50%);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.home-hero :deep(.el-carousel__indicator .el-carousel__button) {
  width: 36px;
  height: 8px;
  border-radius: var(--radius-sm);
  background: rgba(255, 255, 255, 0.5);
}

.home-hero :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  width: 52px;
  background: var(--color-white);
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.6);
}

.home-hero :deep(.el-carousel__arrow) {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(4px);
  font-size: 18px;
}

.home-hero :deep(.el-carousel__arrow:hover) {
  background: rgba(255, 255, 255, 0.5);
}

@media (max-width: 768px) {
  .home-hero__content h2 {
    font-size: 28px;
  }

  .home-hero__content p {
    font-size: var(--font-size-sm);
  }
}
</style>
