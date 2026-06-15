<template>
  <el-pagination
    :current-page="currentPage"
    :page-size="pageSize"
    :total="total"
    :page-sizes="pageSizes"
    layout="total, sizes, prev, pager, next, jumper"
    @update:current-page="$emit('update:currentPage', $event)"
    @update:page-size="$emit('update:pageSize', $event)"
    @size-change="$emit('size-change', $event)"
    @current-change="$emit('current-change', $event)"
  />
</template>

<script setup>
import { DEFAULT_PAGE_SIZES } from '@/constants/status'

defineProps({
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

defineEmits(['update:currentPage', 'update:pageSize', 'size-change', 'current-change'])
</script>
