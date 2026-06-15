<template>
  <div class="favorite-page">
    <div class="favorite-wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>我的收藏 <span class="count" v-if="favorites.length">（{{ favorites.length }}）</span></h2>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && favorites.length === 0">
        <el-empty description="还没有收藏任何商品">
          <el-button type="primary" @click="router.push('/shop')">去逛逛</el-button>
        </el-empty>
      </div>

      <!-- 收藏列表 -->
      <div class="favorite-grid" v-loading="loading" v-else>
        <FavoriteProductCard
          v-for="item in favorites"
          :key="item.id"
          :item="item"
          :adding="Boolean(addingToCart[item.productId])"
          @open="goToDetail"
          @add-cart="handleAddToCart"
          @remove="removeFavorite"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import FavoriteProductCard from '@/components/shop/FavoriteProductCard.vue'
import { useFavorites } from '@/composables/useFavorites'

const {
  router,
  loading,
  favorites,
  addingToCart,
  goToDetail,
  handleAddToCart,
  removeFavorite,
  initFavorites
} = useFavorites()

onMounted(() => {
  initFavorites()
})
</script>

<style scoped>
.favorite-page {
  background: var(--color-bg-page);
  min-height: 100%;
}

.favorite-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* ── 标题 ── */
.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.count {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-placeholder);
}

/* ── 空状态 ── */
.empty-state {
  padding: 80px 0;
}

/* ── 商品网格 ── */
.favorite-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .favorite-wrapper {
    padding: 16px;
  }

  .favorite-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
