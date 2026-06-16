<template>
  <div class="cart-footer">
    <div class="footer-left">
      <BaseCheckbox :model-value="selectAll" @change="$emit('select-all', $event)">全选</BaseCheckbox>
    </div>
    <div class="footer-right">
      <span class="selected-info">已选择 <em>{{ selectedCount }}</em> 件商品</span>
      <span class="total-price">合计：<em>¥{{ totalPrice.toFixed(2) }}</em></span>
      <BaseButton type="primary" size="large" :disabled="selectedCount === 0" @click="$emit('checkout')">
        去结算
      </BaseButton>
    </div>
  </div>
</template>

<script setup>
import BaseButton from '@/components/base/BaseButton.vue'
import BaseCheckbox from '@/components/base/BaseCheckbox.vue'

defineProps({
  selectAll: {
    type: Boolean,
    default: false
  },
  selectedCount: {
    type: Number,
    default: 0
  },
  totalPrice: {
    type: Number,
    default: 0
  }
})

defineEmits(['select-all', 'checkout'])
</script>

<style scoped>
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px 24px;
  border: 1px solid var(--color-border-light);
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.selected-info {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.selected-info em {
  color: var(--color-primary);
  font-style: normal;
  font-weight: bold;
}

.total-price {
  font-size: 14px;
  color: var(--color-text-primary);
}

.total-price em {
  font-size: 24px;
  color: var(--color-price);
  font-style: normal;
  font-weight: bold;
}

@media (max-width: 768px) {
  .cart-footer {
    flex-direction: column;
    gap: 12px;
  }

  .footer-right {
    flex-wrap: wrap;
    justify-content: flex-end;
    width: 100%;
  }
}
</style>
