<template>
  <BasePage :title="title">
    <template v-if="$slots.actions" #actions>
      <slot name="actions" />
    </template>
    <slot />
  </BasePage>
</template>

<script setup>
import BasePage from '@/components/base/BasePage.vue'

defineProps({
  title: {
    type: String,
    required: true
  }
})
</script>
