<template>
  <section class="review-list">
    <slot />
  </section>
</template>

<style scoped>
.review-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}
</style>
