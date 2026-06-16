<template>
  <section class="home-section">
    <div class="home-section__inner">
      <div class="home-section__header">
        <h2>{{ title }}</h2>
        <BaseButton text type="primary" @click="$emit('more')">
          查看更多
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </BaseButton>
      </div>

      <div v-if="scrollable" class="home-products-rail" v-loading="loading">
        <button v-if="products.length > 4" class="home-products-rail__arrow home-products-rail__arrow--left" @click="scroll(-1)">
          <el-icon :size="20"><ArrowLeft /></el-icon>
        </button>
        <div ref="scrollRef" class="home-products-rail__scroll">
          <ProductCard
            v-for="product in products"
            :key="product.id"
            :product="product"
            :badge-label="badgeLabel"
            @open="$emit('open-product', $event)"
            @add-cart="$emit('add-cart', $event)"
          />
        </div>
        <button v-if="products.length > 4" class="home-products-rail__arrow home-products-rail__arrow--right" @click="scroll(1)">
          <el-icon :size="20"><ArrowRight /></el-icon>
        </button>
      </div>

      <ProductGrid
        v-else
        :products="products"
        :loading="loading"
        :badge-label="badgeLabel"
        :badge-type="badgeType"
        :empty-text="emptyText"
        @open="$emit('open-product', $event)"
        @add-cart="$emit('add-cart', $event)"
      />
    </div>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'
import ProductCard from '@/components/shop/ProductCard.vue'
import ProductGrid from '@/components/shop/ProductGrid.vue'

defineProps({
  title: {
    type: String,
    required: true
  },
  products: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  scrollable: {
    type: Boolean,
    default: false
  },
  badgeLabel: {
    type: String,
    default: ''
  },
  badgeType: {
    type: String,
    default: ''
  },
  emptyText: {
    type: String,
    default: '暂无商品'
  }
})

defineEmits(['more', 'open-product', 'add-cart'])

const scrollRef = ref(null)

const scroll = (direction) => {
  if (!scrollRef.value) return
  scrollRef.value.scrollBy({ left: direction * 520, behavior: 'smooth' })
}
</script>

<style scoped>
.home-section {
  margin-bottom: 8px;
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

.home-products-rail {
  position: relative;
}

.home-products-rail__scroll {
  display: flex;
  gap: var(--spacing-lg);
  overflow-x: auto;
  padding: 4px 4px 14px;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
}

.home-products-rail__scroll :deep(.product-card) {
  flex: 0 0 240px;
}

.home-products-rail__scroll::-webkit-scrollbar {
  height: 8px;
}

.home-products-rail__scroll::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: var(--radius-sm);
}

.home-products-rail__scroll::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: var(--radius-sm);
}

.home-products-rail__arrow {
  position: absolute;
  top: 50%;
  z-index: 5;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--color-text-primary);
  background: var(--color-white);
  border: 0;
  border-radius: 50%;
  box-shadow: var(--shadow-md);
  transform: translateY(-60%);
  transition: color var(--transition-fast), background var(--transition-fast), box-shadow var(--transition-fast);
}

.home-products-rail__arrow:hover {
  color: var(--color-white);
  background: var(--color-primary);
  box-shadow: var(--shadow-lg);
}

.home-products-rail__arrow--left {
  left: -8px;
}

.home-products-rail__arrow--right {
  right: -8px;
}

@media (max-width: 768px) {
  .home-section__header h2 {
    font-size: 20px;
  }

  .home-products-rail__scroll :deep(.product-card) {
    flex-basis: 180px;
  }
}
</style>
