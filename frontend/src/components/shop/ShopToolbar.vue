<template>
  <div class="shop-toolbar">
    <div class="toolbar-left">
      <BaseInput
        v-model="form.keyword"
        placeholder="搜索商品..."
        :prefix-icon="Search"
        clearable
        class="toolbar-search"
        @keyup.enter="$emit('search')"
      />
      <BaseSelect v-model="form.categoryId" placeholder="全部分类" clearable class="toolbar-select">
        <BaseOption v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </BaseSelect>
      <BaseSelect v-model="form.orderBy" placeholder="默认排序" class="toolbar-select">
        <BaseOption v-for="option in sortOptions" :key="option.value" :label="option.label" :value="option.value" />
      </BaseSelect>
    </div>
    <div class="toolbar-right">
      <span v-if="total > 0" class="product-count">共 <em>{{ total }}</em> 件商品</span>
      <BaseButton type="primary" :icon="Search" @click="$emit('search')">搜索</BaseButton>
    </div>
  </div>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseOption from '@/components/base/BaseOption.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'

defineProps({
  form: {
    type: Object,
    required: true
  },
  categories: {
    type: Array,
    default: () => []
  },
  total: {
    type: Number,
    default: 0
  },
  sortOptions: {
    type: Array,
    default: () => [
      { label: '最新上架', value: 'create_time' },
      { label: '销量优先', value: 'sales' },
      { label: '价格升序', value: 'price_asc' },
      { label: '价格降序', value: 'price_desc' }
    ]
  }
})

defineEmits(['search'])
</script>

<style scoped>
.shop-toolbar {
  background: #fff;
  padding: 16px 20px;
  border-radius: var(--radius-md);
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: var(--shadow-sm);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-search {
  width: 220px;
}

.toolbar-select {
  width: 140px;
}

.product-count {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.product-count em {
  color: var(--color-primary);
  font-style: normal;
  font-weight: bold;
}

@media (max-width: 768px) {
  .shop-toolbar,
  .toolbar-left,
  .toolbar-right {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
