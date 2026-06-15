<template>
  <div class="review-editor">
    <slot />
  </div>
</template>

<style scoped>
.review-editor {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}
</style>
