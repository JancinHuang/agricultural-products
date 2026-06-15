<template>
  <footer class="user-footer">
    <div class="footer-inner">
      <div class="footer-links">
        <router-link v-for="link in links" :key="link.path" :to="link.path">{{ link.label }}</router-link>
      </div>
      <p class="footer-copy">助农商城 &copy; 2026 | 助力乡村，品质溯源</p>
    </div>
  </footer>
</template>

<script setup>
import { USER_FOOTER_LINKS } from '@/constants/navigation'

defineProps({
  links: {
    type: Array,
    default: () => USER_FOOTER_LINKS
  }
})
</script>

<style scoped>
.user-footer {
  background: var(--color-sidebar-bg);
  color: rgba(255, 255, 255, 0.6);
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 28px 24px;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 24px;
  margin-bottom: 16px;
}

.footer-links a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 13px;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: #fff;
}

.footer-copy {
  font-size: 13px;
  margin: 0;
}

@media (max-width: 768px) {
  .footer-links {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
