<template>
  <div class="product-detail-main">
    <ProductGallery
      class="product-detail-main__gallery"
      :images="[product.imageUrl || product.image]"
      :alt="product.name"
      :height="420"
    />

    <div class="product-detail-main__info">
      <h1 class="product-detail-main__name">{{ product.name }}</h1>
      <p class="product-detail-main__desc">{{ product.description }}</p>

      <div class="product-detail-main__rating">
        <BaseRate :model-value="avgRating" disabled show-score text-color="#ff9900" />
        <span>{{ reviewCount }} 条评价</span>
      </div>

      <div class="product-detail-main__price">
        <span class="product-detail-main__symbol">¥</span>
        <span class="product-detail-main__value">{{ product.price }}</span>
        <span class="product-detail-main__unit">/ {{ product.unit || '件' }}</span>
      </div>

      <div class="product-detail-main__meta">
        <div class="product-detail-main__meta-item">
          <span>库存</span>
          <strong>{{ product.stock }} {{ product.unit }}</strong>
        </div>
        <div class="product-detail-main__meta-item">
          <span>销量</span>
          <strong>{{ product.sales || 0 }}</strong>
        </div>
        <div class="product-detail-main__meta-item">
          <span>产地</span>
          <strong>{{ product.origin || '-' }}</strong>
        </div>
        <div class="product-detail-main__meta-item">
          <span>分类</span>
          <strong>{{ product.categoryName || '-' }}</strong>
        </div>
      </div>

      <div class="product-detail-main__actions">
        <div class="product-detail-main__quantity">
          <span>购买数量</span>
          <BaseInputNumber :model-value="quantity" :min="1" :max="product.stock" size="large" @change="$emit('update:quantity', $event)" />
        </div>
        <div class="product-detail-main__buttons">
          <BaseButton
            type="primary"
            size="large"
            :loading="adding"
            :disabled="product.stock <= 0"
            class="product-detail-main__cart"
            @click="$emit('add-cart')"
          >
            <el-icon><ShoppingCart /></el-icon>
            {{ product.stock <= 0 ? '已售罄' : '加入购物车' }}
          </BaseButton>
          <BaseButton
            :type="favorite ? 'danger' : 'default'"
            size="large"
            class="product-detail-main__favorite"
            @click="$emit('toggle-favorite')"
          >
            <el-icon><Star /></el-icon>
            {{ favorite ? '已收藏' : '收藏' }}
          </BaseButton>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ShoppingCart, Star } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseInputNumber from '@/components/base/BaseInputNumber.vue'
import BaseRate from '@/components/base/BaseRate.vue'
import ProductGallery from '@/components/shop/ProductGallery.vue'

defineProps({
  product: {
    type: Object,
    required: true
  },
  quantity: {
    type: Number,
    default: 1
  },
  favorite: {
    type: Boolean,
    default: false
  },
  adding: {
    type: Boolean,
    default: false
  },
  avgRating: {
    type: Number,
    default: 0
  },
  reviewCount: {
    type: Number,
    default: 0
  }
})

defineEmits(['update:quantity', 'add-cart', 'toggle-favorite'])
</script>

<style scoped>
.product-detail-main {
  display: flex;
  gap: 40px;
  padding: 32px;
  margin-bottom: var(--spacing-xl);
  background: var(--color-white);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.product-detail-main__gallery {
  flex: 0 0 420px;
}

.product-detail-main__info {
  flex: 1;
  min-width: 0;
}

.product-detail-main__name {
  margin: 0 0 var(--spacing-md);
  color: var(--color-text-primary);
  font-size: 24px;
  font-weight: 700;
  line-height: 1.3;
}

.product-detail-main__desc {
  margin: 0 0 var(--spacing-lg);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}

.product-detail-main__rating {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-xl);
  color: var(--color-text-placeholder);
  font-size: var(--font-size-sm);
}

.product-detail-main__price {
  display: flex;
  align-items: baseline;
  gap: 2px;
  padding: var(--spacing-md) var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
  background: linear-gradient(135deg, #fff5f5, #fff0e6);
  border-radius: var(--radius-md);
}

.product-detail-main__symbol {
  color: var(--color-price);
  font-size: 18px;
  font-weight: 700;
}

.product-detail-main__value {
  color: var(--color-price);
  font-size: 36px;
  font-weight: 800;
  line-height: 1;
}

.product-detail-main__unit {
  margin-left: 4px;
  color: var(--color-text-placeholder);
  font-size: var(--font-size-sm);
}

.product-detail-main__meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-md);
  margin-bottom: 28px;
}

.product-detail-main__meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-detail-main__meta-item span {
  color: var(--color-text-placeholder);
  font-size: var(--font-size-xs);
}

.product-detail-main__meta-item strong {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.product-detail-main__actions {
  padding-top: var(--spacing-xl);
  border-top: 1px solid var(--color-border-light);
}

.product-detail-main__quantity {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  margin-bottom: var(--spacing-md);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.product-detail-main__buttons {
  display: flex;
  gap: var(--spacing-md);
}

.product-detail-main__cart {
  flex: 1;
}

.product-detail-main__cart,
.product-detail-main__favorite {
  height: 48px;
  font-size: var(--font-size-md);
  font-weight: 600;
}

.product-detail-main__favorite {
  padding: 0 var(--spacing-xl);
}

@media (max-width: 768px) {
  .product-detail-main {
    flex-direction: column;
    padding: var(--spacing-lg);
    gap: var(--spacing-lg);
  }

  .product-detail-main__gallery {
    flex: none;
  }

  .product-detail-main__buttons {
    flex-direction: column;
  }

  .product-detail-main__cart,
  .product-detail-main__favorite {
    width: 100%;
  }
}
</style>
