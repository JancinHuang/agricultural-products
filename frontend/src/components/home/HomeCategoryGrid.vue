<template>
  <section class="home-section home-section--white">
    <div class="home-section__inner">
      <div class="home-section__header">
        <h2>商品分类</h2>
        <BaseButton text type="primary" @click="$emit('open-all')">浏览全部</BaseButton>
      </div>
      <div class="home-category-grid" v-loading="loading">
        <button
          v-for="category in categories"
          :key="category.id"
          class="home-category-card"
          type="button"
          @click="$emit('open', category)"
        >
          <div class="home-category-card__icon">
            <img v-if="category.icon || category.iconUrl" :src="getCategoryIconSrc(category)" :alt="category.name" />
            <span v-else>{{ getCategoryEmoji(category.name) }}</span>
          </div>
          <span class="home-category-card__name">{{ category.name }}</span>
        </button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { imageUtils } from '@/utils/imageUtils'
import BaseButton from '@/components/base/BaseButton.vue'

defineProps({
  categories: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['open', 'open-all'])

const getCategoryEmoji = (name) => {
  if (!name) return '🌿'
  const map = {
    '水果': '🍎', '蔬菜': '🥬', '粮食': '🌾', '粮油': '🌾',
    '茶叶': '🍵', '坚果': '🥜', '蜂蜜': '🍯', '肉类': '🥩',
    '禽蛋': '🥚', '水产': '🐟', '菌菇': '🍄', '干货': '🫙',
    '调味': '🧂', '饮品': '🥤', '零食': '🍬', '有机': '🌿'
  }
  for (const [key, emoji] of Object.entries(map)) {
    if (name.includes(key)) return emoji
  }
  return '🌿'
}

const getCategoryIconUrl = icon => imageUtils.getImageUrl(icon, { width: 96, height: 96, quality: 80 })
const getCategoryIconSrc = category => category.iconUrl || getCategoryIconUrl(category.icon)
</script>

<style scoped>
.home-section {
  margin-bottom: 8px;
}

.home-section--white {
  background: var(--color-white);
}

.home-section__inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 36px 24px;
}

.home-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-xl);
}

.home-section__header h2 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 24px;
  font-weight: 700;
}

.home-category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: var(--spacing-md);
}

.home-category-card {
  padding: var(--spacing-xl) var(--spacing-md);
  cursor: pointer;
  background: var(--color-bg-section);
  border: 0;
  border-radius: var(--radius-md);
  text-align: center;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.home-category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.home-category-card__icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto var(--spacing-sm);
  font-size: 40px;
  line-height: 1;
}

.home-category-card__icon img {
  width: 56px;
  height: 56px;
  display: block;
  object-fit: cover;
  border-radius: 12px;
}

.home-category-card__name {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 500;
}

@media (max-width: 768px) {
  .home-section__header h2 {
    font-size: 20px;
  }

  .home-category-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
  }

  .home-category-card {
    padding: var(--spacing-md) var(--spacing-sm);
  }

  .home-category-card__icon {
    font-size: 28px;
  }
}
</style>
