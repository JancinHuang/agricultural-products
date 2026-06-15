<template>
  <span class="price-text">¥{{ normalized }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: {
    type: [Number, String],
    default: 0
  }
})

const normalized = computed(() => Number(props.value || 0).toFixed(2))
</script>

<style scoped>
.price-text {
  color: var(--color-price);
  font-weight: 600;
}
</style>
