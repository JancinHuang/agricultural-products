<template>
  <div class="admin-data-table">
    <BaseTable :data="data" v-loading="loading" stripe>
      <slot />
    </BaseTable>

    <BasePagination
      :current-page="currentPage"
      :page-size="pageSize"
      :total="total"
      :page-sizes="pageSizes"
      @update:current-page="$emit('update:currentPage', $event)"
      @update:page-size="$emit('update:pageSize', $event)"
      @size-change="$emit('size-change', $event)"
      @current-change="$emit('current-change', $event)"
    />
  </div>
</template>

<script setup>
import { DEFAULT_PAGE_SIZES } from '@/constants/status'
import BasePagination from '@/components/base/BasePagination.vue'
import BaseTable from '@/components/base/BaseTable.vue'

defineProps({
  data: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  currentPage: {
    type: Number,
    required: true
  },
  pageSize: {
    type: Number,
    required: true
  },
  total: {
    type: Number,
    required: true
  },
  pageSizes: {
    type: Array,
    default: () => DEFAULT_PAGE_SIZES
  }
})

defineEmits([
  'update:currentPage',
  'update:pageSize',
  'size-change',
  'current-change'
])
</script>

<style scoped>
.admin-data-table {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}
</style>
