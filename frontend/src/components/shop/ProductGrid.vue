<template>
  <div
    v-if="products.length || loading"
    class="product-grid"
    v-loading="loading"
    element-loading-text="加载商品中..."
  >
    <ProductCard
      v-for="product in products"
      :key="product.id"
      :product="product"
      :loading="loadingMap[product.id]"
      :badge-label="badgeLabel"
      :badge-type="badgeType"
      @open="$emit('open', $event)"
      @add-cart="$emit('add-cart', $event)"
      @preview="$emit('preview', $event)"
    />
  </div>
  <EmptyState v-else :description="emptyText" />
</template>

<script setup>
import ProductCard from '@/components/shop/ProductCard.vue'
import EmptyState from '@/components/base/EmptyState.vue'

defineProps({
  products: {
    type: Array,
    default: () => []
  },
  emptyText: {
    type: String,
    default: '暂无商品'
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingMap: {
    type: Object,
    default: () => ({})
  },
  badgeLabel: {
    type: String,
    default: ''
  },
  badgeType: {
    type: String,
    default: ''
  }
})

defineEmits(['open', 'add-cart', 'preview'])
</script>

<style scoped>
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--spacing-lg);
  min-height: 240px;
  margin-bottom: var(--spacing-lg);
}

@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: var(--spacing-sm);
  }
}
</style>
