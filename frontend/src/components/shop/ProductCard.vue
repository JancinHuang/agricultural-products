<template>
  <article class="product-card" @click="$emit('open', product)">
    <div class="product-card__media">
      <ProductImage
        :src="product.imageUrl || product.image"
        :alt="product.name"
        :height="imageHeight"
        :lazy="true"
        :widths="[320, 640, 960]"
        :quality="80"
        sizes="(max-width: 768px) 50vw, (max-width: 1400px) 33vw, 260px"
        @click.stop="$emit('preview', product)"
      />
      <span v-if="displayBadge" class="product-card__badge" :class="{ 'product-card__badge--new': badgeType === 'new' }">
        {{ displayBadge }}
      </span>
      <div class="product-card__overlay">
        <el-button type="primary" size="small" @click.stop="$emit('preview', product)">快速查看</el-button>
      </div>
    </div>
    <div class="product-card__body">
      <h3 class="product-card__title" :title="product.name">{{ product.name }}</h3>
      <p class="product-card__desc">{{ product.description || product.origin || '品质农产品' }}</p>
      <div class="product-card__meta">
        <div>
          <PriceText :value="product.price" />
          <span class="product-card__unit">/ {{ product.unit || '件' }}</span>
        </div>
        <span class="product-card__sales">已售{{ product.sales || 0 }}</span>
      </div>
      <div class="product-card__footer">
        <el-tag v-if="product.stock <= 0" type="danger" size="small">已售罄</el-tag>
        <el-tag v-else-if="product.stock < 10" type="warning" size="small">仅剩{{ product.stock }}</el-tag>
        <span v-else>库存 {{ product.stock ?? 0 }}</span>
        <el-button
          v-if="product.status === 1 && product.stock > 0"
          type="primary"
          size="small"
          :loading="loading"
          @click.stop="$emit('add-cart', product)"
        >
          加入购物车
        </el-button>
        <el-button v-else-if="product.status !== 1" type="info" size="small" disabled>商品已下架</el-button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import ProductImage from '@/components/ProductImage.vue'
import PriceText from '@/components/base/PriceText.vue'

const props = defineProps({
  product: {
    type: Object,
    required: true
  },
  imageHeight: {
    type: Number,
    default: 180
  },
  loading: {
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
  }
})

defineEmits(['open', 'add-cart', 'preview'])

const displayBadge = computed(() => props.badgeLabel || (props.product.sales > 100 ? '热销' : ''))
</script>

<style scoped>
.product-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-normal), box-shadow var(--transition-normal);
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.product-card__media {
  position: relative;
  overflow: hidden;
}

.product-card__badge {
  position: absolute;
  top: var(--spacing-sm);
  left: var(--spacing-sm);
  z-index: 2;
  padding: 4px 10px;
  color: var(--color-white);
  background: var(--color-accent);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-xs);
}

.product-card__badge--new {
  background: var(--color-primary);
}

.product-card__overlay {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity var(--transition-normal);
}

.product-card:hover .product-card__overlay {
  opacity: 1;
}

.product-card__body {
  padding: var(--spacing-md);
}

.product-card__title {
  margin: 0 0 6px;
  font-size: var(--font-size-md);
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-card__desc {
  height: 38px;
  margin: 0 0 var(--spacing-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  overflow: hidden;
}

.product-card__meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 4px;
}

.product-card__unit {
  color: var(--color-text-placeholder);
  font-size: var(--font-size-xs);
}

.product-card__sales {
  color: var(--color-text-placeholder);
  font-size: var(--font-size-xs);
}

.product-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--spacing-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}
</style>
