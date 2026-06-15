<template>
  <AdminCrudPage :title="title">
    <template v-if="$slots.actions" #actions>
      <slot name="actions" />
    </template>

    <AdminSearchForm
      v-if="$slots.search"
      :model="searchModel"
      @search="$emit('search')"
      @reset="$emit('reset')"
    >
      <slot name="search" />
    </AdminSearchForm>

    <slot />
  </AdminCrudPage>
</template>

<script setup>
import AdminCrudPage from '@/components/admin/AdminCrudPage.vue'
import AdminSearchForm from '@/components/admin/AdminSearchForm.vue'

defineProps({
  title: {
    type: String,
    required: true
  },
  searchModel: {
    type: Object,
    default: () => ({})
  }
})

defineEmits(['search', 'reset'])
</script>
