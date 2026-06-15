<template>
  <div class="checkout-panel">
    <div class="checkout-panel__title">
      <el-icon><Goods /></el-icon>
      <span>商品清单（{{ items.length }} 件）</span>
    </div>
    <div class="checkout-panel__items">
      <div v-for="item in items" :key="item.id" class="checkout-item">
        <img :src="getImageUrl(item.productImage)" :alt="item.productName" class="checkout-item__image" />
        <div class="checkout-item__info">
          <span class="checkout-item__name">{{ item.productName }}</span>
          <span class="checkout-item__price">¥{{ item.price }} x {{ item.quantity }}</span>
        </div>
        <PriceText :value="Number(item.price || 0) * Number(item.quantity || 0)" class="checkout-item__subtotal" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { Goods } from '@element-plus/icons-vue'
import PriceText from '@/components/base/PriceText.vue'
import { imageUtils } from '@/utils/imageUtils'

defineProps({
  items: {
    type: Array,
    default: () => []
  }
})

const getImageUrl = url => imageUtils.getImageUrl(url)
</script>

<style scoped>
.checkout-panel {
  overflow: hidden;
  margin-bottom: var(--spacing-lg);
  background: var(--color-white);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.checkout-panel__title {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-lg);
  color: var(--color-text-primary);
  background: var(--color-bg-section);
  border-bottom: 1px solid var(--color-border-light);
  font-size: var(--font-size-md);
  font-weight: 600;
}

.checkout-panel__title .el-icon {
  color: var(--color-primary);
}

.checkout-panel__items {
  padding: 8px 0;
}

.checkout-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: 14px var(--spacing-lg);
  transition: background var(--transition-fast);
}

.checkout-item:hover {
  background: var(--color-bg-section);
}

.checkout-item__image {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.checkout-item__info {
  flex: 1;
  min-width: 0;
}

.checkout-item__name {
  display: block;
  margin-bottom: 4px;
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.checkout-item__price {
  color: var(--color-text-placeholder);
  font-size: var(--font-size-sm);
}

.checkout-item__subtotal {
  min-width: 80px;
  flex-shrink: 0;
  font-size: var(--font-size-md);
  font-weight: 700;
  text-align: right;
}
</style>
