<template>
  <el-dialog v-model="innerVisible" title="选择要评价的商品" width="560px" append-to-body>
    <div v-if="items.length" class="review-picker">
      <button
        v-for="item in items"
        :key="item.id"
        class="review-picker__item"
        type="button"
        @click="$emit('select', item)"
      >
        <img :src="getImageUrl(item.productImage)" :alt="item.productName" />
        <div class="review-picker__info">
          <span class="review-picker__name">{{ item.productName }}</span>
          <span class="review-picker__meta">¥{{ formatAmount(item.price) }} x {{ item.quantity }}</span>
        </div>
        <span class="review-picker__action">评价</span>
      </button>
    </div>
    <EmptyState v-else description="暂无可评价商品" />
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import EmptyState from '@/components/base/EmptyState.vue'
import { imageUtils } from '@/utils/imageUtils'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  items: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'select'])

const innerVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const formatAmount = value => Number(value || 0).toFixed(2)
const getImageUrl = url => imageUtils.getImageUrl(url)
</script>

<style scoped>
.review-picker {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.review-picker__item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  width: 100%;
  padding: var(--spacing-md);
  cursor: pointer;
  background: var(--color-bg-light);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  text-align: left;
  transition: border-color var(--transition-fast), background var(--transition-fast);
}

.review-picker__item:hover {
  background: var(--color-bg-section);
  border-color: var(--color-primary-light);
}

.review-picker__item img {
  width: 56px;
  height: 56px;
  object-fit: cover;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.review-picker__info {
  flex: 1;
  min-width: 0;
}

.review-picker__name {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-picker__meta {
  display: block;
  margin-top: 4px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.review-picker__action {
  color: var(--color-primary);
  font-size: var(--font-size-sm);
  font-weight: 600;
}
</style>
