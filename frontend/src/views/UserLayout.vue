<template>
  <div class="user-layout">
    <UserSiteHeader
      v-model:keyword="searchKeyword"
      :route-path="route.path"
      :user="userStore.userInfo"
      :cart-count="cartStore.selectedCount"
      @home="router.push('/home')"
      @cart="router.push('/cart')"
      @search="handleSearch"
      @command="handleCommand"
    />

    <main class="user-main">
      <router-view />
    </main>

    <UserSiteFooter />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import UserSiteFooter from '@/components/shop/UserSiteFooter.vue'
import UserSiteHeader from '@/components/shop/UserSiteHeader.vue'
import { confirmDanger } from '@/services/uiFeedback'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()
const searchKeyword = ref('')

const handleSearch = () => {
  const keyword = searchKeyword.value.trim()
  if (keyword) {
    router.push({ path: '/shop', query: { keyword } })
  } else {
    router.push('/shop')
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    confirmDanger('确定要退出登录吗？').then(() => {
      userStore.logout()
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'orders') {
    router.push('/my-order')
  } else if (command === 'favorites') {
    router.push('/favorite')
  }
}

onMounted(() => {
  cartStore.init()
})
</script>

<style scoped>
.user-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--color-bg-page);
}

/* ── Main ── */
.user-main {
  flex: 1;
  overflow-y: auto;
}
</style>
