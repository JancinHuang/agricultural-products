<template>
  <div class="cart-item">
    <el-checkbox :model-value="item.selected === 1" @change="$emit('select', item, $event)" />

    <img
      :src="imageUrl"
      class="cart-item__image"
      :alt="item.productName"
      @click="$emit('open', item)"
    />

    <div class="cart-item__info">
      <h4 class="cart-item__name" @click="$emit('open', item)">
        {{ item.productName }}
      </h4>
      <el-tag v-if="item.productStatus !== 1" type="danger" size="small">已下架</el-tag>
    </div>

    <PriceText :value="item.price" class="cart-item__price" />

    <el-input-number
      :model-value="item.quantity"
      :min="1"
      :max="item.productStock"
      size="small"
      @change="$emit('quantity-change', item, $event)"
    />

    <PriceText :value="subtotal" class="cart-item__subtotal" />

    <el-button type="danger" link @click="$emit('delete', item)">删除</el-button>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import PriceText from '@/components/base/PriceText.vue'
import { imageUtils } from '@/utils/imageUtils'

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

defineEmits(['select', 'quantity-change', 'delete', 'open'])

const imageUrl = computed(() => imageUtils.getImageUrl(props.item.productImage))
const subtotal = computed(() => Number(props.item.price || 0) * Number(props.item.quantity || 0))
</script>

<style scoped>
.cart-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  margin-bottom: var(--spacing-md);
  background: var(--color-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition-fast);
}

.cart-item:hover {
  box-shadow: var(--shadow-md);
}

.cart-item__image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  flex-shrink: 0;
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: transform var(--transition-fast);
}

.cart-item__image:hover {
  transform: scale(1.05);
}

.cart-item__info {
  flex: 1;
  min-width: 0;
}

.cart-item__name {
  margin: 0 0 4px;
  color: var(--color-text-primary);
  cursor: pointer;
  font-size: var(--font-size-md);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-item__name:hover {
  color: var(--color-primary);
}

.cart-item__price {
  width: 80px;
  flex-shrink: 0;
  text-align: center;
}

.cart-item__subtotal {
  width: 100px;
  flex-shrink: 0;
  font-size: var(--font-size-md);
  font-weight: 700;
  text-align: right;
}

@media (max-width: 768px) {
  .cart-item {
    flex-wrap: wrap;
    gap: var(--spacing-sm);
  }

  .cart-item__info {
    flex: 1 1 calc(100% - 110px);
  }

  .cart-item__price,
  .cart-item__subtotal {
    width: auto;
    text-align: left;
  }
}
</style>
