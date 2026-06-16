<template>
  <div class="product-card">
    <div class="product-image-wrapper" @click="$emit('open', item.productId)">
      <img :src="getImageUrl(item.productImage)" :alt="item.productName" />
      <div class="product-status">
        <BaseTag v-if="item.productStatus !== 1" type="danger" size="small" effect="dark">已下架</BaseTag>
      </div>
      <div class="product-overlay">
        <BaseButton type="primary" size="small" @click.stop="$emit('open', item.productId)">
          查看详情
        </BaseButton>
      </div>
    </div>
    <div class="product-info">
      <h3 class="product-name" :title="item.productName" @click="$emit('open', item.productId)">
        {{ item.productName }}
      </h3>
      <div class="product-meta">
        <span class="product-price">¥{{ item.price }}</span>
        <BaseTag v-if="item.productStatus === 1" type="success" size="small">在售</BaseTag>
      </div>
      <div class="product-actions">
        <BaseButton
          type="primary"
          size="small"
          :loading="adding"
          :disabled="item.productStatus !== 1"
          @click="$emit('add-cart', item)"
        >
          <el-icon><ShoppingCart /></el-icon> 加入购物车
        </BaseButton>
        <BaseButton size="small" class="remove-btn" @click="$emit('remove', item)">
          <el-icon><Delete /></el-icon>
        </BaseButton>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Delete, ShoppingCart } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseTag from '@/components/base/BaseTag.vue'
import { imageUtils } from '@/utils/imageUtils'

defineProps({
  item: {
    type: Object,
    required: true
  },
  adding: {
    type: Boolean,
    default: false
  }
})

defineEmits(['open', 'add-cart', 'remove'])

const getImageUrl = url => imageUtils.getImageUrl(url)
</script>

<style scoped>
.product-card {
  background: #fff;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.product-image-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
  cursor: pointer;
}

.product-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image-wrapper img {
  transform: scale(1.05);
}

.product-status {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 3;
}

.product-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-normal);
  z-index: 2;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

.product-info {
  padding: 14px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 10px;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.product-name:hover {
  color: var(--color-primary);
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-price);
}

.product-actions {
  display: flex;
  gap: 8px;
}

.product-actions .el-button {
  flex: 1;
}

.remove-btn {
  flex: 0 0 auto !important;
}

.remove-btn:hover {
  color: var(--color-danger);
}
</style>
