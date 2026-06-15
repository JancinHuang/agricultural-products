<template>
  <div class="shop-container">
    <ShopToolbar
      :form="searchForm"
      :categories="categories"
      :total="total"
      @search="handleSearch"
    />

    <ProductGrid
      :products="products"
      :loading="loading"
      :loading-map="addingToCart"
      @open="goToDetail($event.id)"
      @preview="handleImageClick"
      @add-cart="handleAddToCart"
    />

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 48]"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <ImagePreview
      v-model="previewVisible"
      :image-list="previewImages"
      :initial-index="previewIndex"
    />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import ProductGrid from '@/components/shop/ProductGrid.vue'
import ShopToolbar from '@/components/shop/ShopToolbar.vue'
import ImagePreview from '@/components/ImagePreview.vue'
import { useShopProducts } from '@/composables/useShopProducts'

const {
  searchForm,
  loading,
  products,
  categories,
  total,
  pageNum,
  pageSize,
  addingToCart,
  previewVisible,
  previewImages,
  previewIndex,
  initShop,
  handleSearch,
  handleSizeChange,
  handlePageChange,
  goToDetail,
  handleImageClick,
  handleAddToCart
} = useShopProducts()

onMounted(() => {
  initShop()
})
</script>

<style scoped>
.shop-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 24px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}
</style>
