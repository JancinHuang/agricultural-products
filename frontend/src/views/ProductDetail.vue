<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-wrapper" v-if="product">
      <ProductDetailMain
        v-model:quantity="quantity"
        :product="product"
        :favorite="isFavorite"
        :adding="addingToCart"
        :avg-rating="avgRating"
        :review-count="reviewCount"
        @add-cart="handleAddToCart"
        @toggle-favorite="handleToggleFavorite"
      />

      <ProductReviewSection
        v-model:sort="reviewSort"
        :reviews="sortedReviews"
        :can-review="canReview"
        :current-user-id="userStore.userInfo?.id"
        @show-review="showReviewDialog"
        @edit="editReview"
        @delete="deleteReviewItem"
        @toggle-like="handleToggleLike"
        @reply="showReplyDialog"
      />
    </div>

    <ProductReviewDialog
      v-model="reviewDialogVisible"
      v-model:image-list="reviewImageList"
      :is-edit-mode="isEditMode"
      :form="reviewForm"
      :upload-headers="uploadHeaders"
      :accept="ALLOWED_IMAGE_ACCEPT"
      :submitting="submitting"
      :before-upload="beforeReviewImageUpload"
      :on-upload-success="handleUploadSuccess"
      :on-upload-error="handleUploadError"
      :on-upload-remove="handleUploadRemove"
      :on-preview="handlePreview"
      @cancel="closeReviewDialog"
      @submit="submitReview"
    />

    <ProductReplyDialog
      v-model="replyDialogVisible"
      :form="replyForm"
      :submitting="submitting"
      @submit="submitReply"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ProductDetailMain from '@/components/shop/ProductDetailMain.vue'
import ProductReviewSection from '@/components/shop/ProductReviewSection.vue'
import ProductReviewDialog from '@/components/shop/ProductReviewDialog.vue'
import ProductReplyDialog from '@/components/shop/ProductReplyDialog.vue'
import { getProductById } from '@/api/product'
import { addToFavorite, removeFromFavorite, checkFavorite } from '@/api/favorite'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { useProductReviews } from '@/composables/useProductReviews'
import { notify } from '@/services/uiFeedback'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const isFavorite = ref(false)
const addingToCart = ref(false)

const {
  ALLOWED_IMAGE_ACCEPT,
  avgRating,
  reviewCount,
  reviewDialogVisible,
  replyDialogVisible,
  isEditMode,
  submitting,
  reviewSort,
  reviewImageList,
  reviewForm,
  replyForm,
  uploadHeaders,
  canReview,
  sortedReviews,
  loadReviews,
  loadReviewStats,
  loadCanReview,
  showReviewDialog,
  closeReviewDialog,
  openReviewFromOrder,
  editReview,
  showReplyDialog,
  beforeReviewImageUpload,
  handleUploadSuccess,
  handleUploadError,
  handlePreview,
  handleUploadRemove,
  deleteReviewItem,
  handleToggleLike,
  submitReview,
  submitReply
} = useProductReviews({ route, router, userStore, product })

const loadProduct = async () => {
  loading.value = true
  try {
    const res = await getProductById(route.params.id)
    product.value = res.data
  } catch (error) {
    console.error(error)
    notify.error('加载商品失败')
  } finally {
    loading.value = false
  }
}

const loadFavoriteStatus = async () => {
  if (!userStore.userInfo) return
  try {
    const res = await checkFavorite(route.params.id)
    isFavorite.value = res.data.isFavorite
  } catch (error) {
    console.error(error)
  }
}

const handleAddToCart = async () => {
  if (!product.value) return
  if (product.value.stock <= 0) {
    notify.warning('商品已售罄')
    return
  }
  if (quantity.value > product.value.stock) {
    notify.warning(`库存不足，最多可购买${product.value.stock}件`)
    return
  }
  addingToCart.value = true
  try {
    const success = await cartStore.addToCart(product.value, quantity.value)
    if (success) {
      notify.success({ message: `${product.value.name} ×${quantity.value} 已加入购物车 🛒`, duration: 1500 })
    } else {
      notify.error('加入购物车失败')
    }
  } catch (error) {
    console.error(error)
    notify.error('加入购物车失败')
  } finally {
    addingToCart.value = false
  }
}

const handleToggleFavorite = async () => {
  if (!userStore.userInfo) {
    notify.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (isFavorite.value) {
      await removeFromFavorite(product.value.id)
      notify.success('已取消收藏')
    } else {
      await addToFavorite(product.value.id)
      notify.success('收藏成功')
    }
    isFavorite.value = !isFavorite.value
  } catch (error) {
    console.error(error)
    notify.error('操作失败')
  }
}

onMounted(async () => {
  await Promise.all([
    loadProduct(),
    loadFavoriteStatus(),
    loadReviews(),
    loadReviewStats(),
    loadCanReview()
  ])
  openReviewFromOrder()
  cartStore.init()
})
</script>

<style scoped>
.detail-page {
  background: var(--color-bg-page);
  min-height: 100%;
}

.detail-wrapper {
  max-width: 1100px;
  margin: 0 auto;
  padding: 24px;
}

@media (max-width: 768px) {
  .detail-wrapper {
    padding: 16px;
  }
}
</style>
