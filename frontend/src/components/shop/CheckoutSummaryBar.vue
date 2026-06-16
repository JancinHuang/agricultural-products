<template>
  <div class="checkout-summary">
    <BaseButton @click="$emit('back')">
      <el-icon><ArrowLeft /></el-icon>
      返回购物车
    </BaseButton>
    <div class="checkout-summary__right">
      <span class="checkout-summary__text">共 <em>{{ totalCount }}</em> 件商品</span>
      <span class="checkout-summary__price">应付：<PriceText :value="totalPrice" /></span>
      <BaseButton type="primary" size="large" :loading="submitting" class="checkout-summary__submit" @click="$emit('submit')">
        提交订单
      </BaseButton>
    </div>
  </div>
</template>

<script setup>
import { ArrowLeft } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'
import PriceText from '@/components/base/PriceText.vue'

defineProps({
  totalCount: {
    type: Number,
    default: 0
  },
  totalPrice: {
    type: Number,
    default: 0
  },
  submitting: {
    type: Boolean,
    default: false
  }
})

defineEmits(['back', 'submit'])
</script>

<style scoped>
.checkout-summary {
  position: sticky;
  bottom: 0;
  z-index: 10;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-xl);
  background: var(--color-white);
  border-radius: var(--radius-md);
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
}

.checkout-summary__right {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);
}

.checkout-summary__text,
.checkout-summary__price {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.checkout-summary__text em {
  color: var(--color-primary);
  font-style: normal;
  font-weight: 600;
}

.checkout-summary__price :deep(.price-text) {
  font-size: 24px;
  font-weight: 700;
}

.checkout-summary__submit {
  height: 48px;
  padding: 0 32px;
  font-size: var(--font-size-md);
  font-weight: 600;
}

@media (max-width: 768px) {
  .checkout-summary {
    flex-direction: column;
    gap: var(--spacing-md);
  }

  .checkout-summary__right {
    flex-wrap: wrap;
    justify-content: flex-end;
    width: 100%;
    gap: var(--spacing-md);
  }
}
</style>
