<template>
  <div class="detail-page" v-loading="loading">
    <div class="detail-wrapper" v-if="product">
      <!-- 商品主信息 -->
      <div class="product-main">
        <div class="product-gallery">
          <el-image
            :src="getImageUrl(product.image)"
            :alt="product.name"
            :preview-src-list="[getImageUrl(product.image)]"
            fit="cover"
            class="product-main-image"
            preview-teleported
          />
          <div class="image-zoom-hint">
            <el-icon><ZoomIn /></el-icon>
            <span>点击查看大图</span>
          </div>
        </div>
        <div class="product-info">
          <h1 class="product-name">{{ product.name }}</h1>
          <p class="product-desc">{{ product.description }}</p>

          <div class="product-rating">
            <el-rate v-model="avgRating" disabled show-score text-color="#ff9900" />
            <span class="rating-count">{{ reviewCount }} 条评价</span>
          </div>

          <div class="price-block">
            <span class="price-symbol">¥</span>
            <span class="price-value">{{ product.price }}</span>
            <span class="price-unit">/ {{ product.unit || '件' }}</span>
          </div>

          <div class="meta-grid">
            <div class="meta-item">
              <span class="meta-label">库存</span>
              <span class="meta-value">{{ product.stock }} {{ product.unit }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">销量</span>
              <span class="meta-value">{{ product.sales || 0 }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">产地</span>
              <span class="meta-value">{{ product.origin || '—' }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">分类</span>
              <span class="meta-value">{{ product.categoryName || '—' }}</span>
            </div>
          </div>

          <div class="product-actions">
            <div class="quantity-row">
              <span class="action-label">购买数量</span>
              <el-input-number v-model="quantity" :min="1" :max="product.stock" size="large" />
            </div>
            <div class="action-buttons">
              <el-button
                type="primary"
                size="large"
                :loading="addingToCart"
                :disabled="!product || product.stock <= 0"
                @click="handleAddToCart"
                class="cart-btn"
              >
                <el-icon><ShoppingCart /></el-icon>
                {{ !product || product.stock <= 0 ? '已售罄' : '加入购物车' }}
              </el-button>
              <el-button
                :type="isFavorite ? 'danger' : 'default'"
                size="large"
                @click="handleToggleFavorite"
                class="fav-btn"
              >
                <el-icon><Star /></el-icon>
                {{ isFavorite ? '已收藏' : '收藏' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 商品评价 -->
      <div class="review-section">
        <div class="section-header">
          <h2>商品评价</h2>
          <div class="header-actions">
            <el-select v-model="reviewSort" size="small" style="width: 120px">
              <el-option label="最新" value="newest" />
              <el-option label="最热" value="hottest" />
            </el-select>
            <el-button type="primary" size="small" @click="showReviewDialog" v-if="canReview">发表评价</el-button>
          </div>
        </div>

        <div class="review-list" v-if="sortedReviews.length > 0">
          <div v-for="review in sortedReviews" :key="review.id" class="review-item">
            <div class="review-header">
              <el-avatar :src="getImageUrl(review.userAvatar)" :size="40">{{ review.userName }}</el-avatar>
              <div class="review-info">
                <div class="user-name">{{ review.userName }}</div>
                <el-rate v-model="review.rating" disabled />
              </div>
              <div class="review-time">{{ formatTime(review.createTime) }}</div>
              <div class="review-actions" v-if="isReviewOwner(review)">
                <el-button type="primary" link size="small" @click="editReview(review)">编辑</el-button>
                <el-button type="danger" link size="small" @click="deleteReviewItem(review)">删除</el-button>
              </div>
            </div>
            <div class="review-content">{{ review.content }}</div>
            <div class="review-images" v-if="getReviewImageList(review).length">
              <el-image
                v-for="(img, index) in getReviewImageList(review)"
                :key="index"
                :src="getImageUrl(img)"
                :preview-src-list="getReviewImageList(review).map(i => getImageUrl(i))"
                fit="cover"
                class="review-image"
              />
            </div>
            <div class="review-footer">
              <div class="like-btn" @click="handleToggleLike(review)">
                <el-icon :class="{ liked: review.isLiked }"><Pointer /></el-icon>
                <span>{{ review.likeCount || 0 }}</span>
              </div>
              <div class="reply-btn" @click="showReplyDialog(review)">
                <el-icon><ChatDotRound /></el-icon>
                <span>回复</span>
              </div>
            </div>

            <div class="replies" v-if="review.replies && review.replies.length > 0">
              <div v-for="reply in review.replies" :key="reply.id" class="reply-item">
                <div class="reply-header">
                  <el-avatar :src="getImageUrl(reply.userAvatar)" :size="28">{{ reply.userName }}</el-avatar>
                  <span class="reply-user">{{ reply.userName }}</span>
                  <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                  <div class="reply-actions" v-if="isReviewOwner(reply)">
                    <el-button type="danger" link size="small" @click="deleteReviewItem(reply)">删除</el-button>
                  </div>
                </div>
                <div class="reply-content">{{ reply.content }}</div>
                <div class="reply-footer">
                  <div class="like-btn small" @click="handleToggleLike(reply)">
                    <el-icon :class="{ liked: reply.isLiked }"><Pointer /></el-icon>
                    <span>{{ reply.likeCount || 0 }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无评价" />
      </div>
    </div>

    <el-dialog
      v-model="reviewDialogVisible"
      :title="isEditMode ? '编辑评价' : '发表评价'"
      width="520px"
      append-to-body
    >
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="说说商品质量、包装、口感或使用体验"
          />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            v-model:file-list="reviewImageList"
            class="review-uploader"
            action="/api/upload"
            list-type="picture-card"
            :headers="uploadHeaders"
            :before-upload="beforeReviewImageUpload"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :on-remove="handleUploadRemove"
            :on-preview="handlePreview"
            :accept="ALLOWED_IMAGE_ACCEPT"
            :limit="5"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">最多上传 5 张图片</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeReviewDialog">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="replyDialogVisible" title="回复评价" width="460px" append-to-body>
      <el-input
        v-model="replyForm.content"
        type="textarea"
        :rows="4"
        maxlength="500"
        show-word-limit
        placeholder="请输入回复内容"
      />
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitReply">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Star, Pointer, ChatDotRound, Plus, ZoomIn } from '@element-plus/icons-vue'
import { getProductById } from '@/api/product'
import { addToFavorite, removeFromFavorite, checkFavorite } from '@/api/favorite'
import { getProductReviews, addReview, replyReview, updateReview, deleteReview as deleteReviewApi, toggleReviewLike, getReviewStats, canReviewProduct } from '@/api/review'
import { formatTime } from '@/utils/time'
import { imageUtils } from '@/utils/imageUtils'
import { ALLOWED_IMAGE_ACCEPT, allowedImageMessage, isAllowedImageFile } from '@/utils/uploadValidation'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const loading = ref(false)
const product = ref(null)
const quantity = ref(1)
const isFavorite = ref(false)
const reviews = ref([])
const avgRating = ref(0)
const reviewCount = ref(0)
const reviewDialogVisible = ref(false)
const replyDialogVisible = ref(false)
const isEditMode = ref(false)
const editingReviewId = ref(null)
const addingToCart = ref(false)
const submitting = ref(false)
const reviewSort = ref('newest')
const reviewImageList = ref([])
const hasCompletedPurchase = ref(false)
const hasReviewed = ref(false)
const reviewOrderId = ref(null)

const uploadHeaders = computed(() => ({
  Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
}))

const reviewForm = reactive({
  rating: 5,
  content: '',
  images: ''
})

const replyForm = reactive({
  parentId: null,
  content: ''
})

const canReview = computed(() => {
  return userStore.userInfo && userStore.userInfo.username !== 'admin' && hasCompletedPurchase.value
})

const isReviewOwner = (review) => {
  return userStore.userInfo && review.userId === userStore.userInfo.id
}

const getImageUrl = (url) => imageUtils.getImageUrl(url)

const splitImages = (images) => {
  return images ? images.split(',').map(item => item.trim()).filter(Boolean) : []
}

const getReviewImageList = (review) => {
  return splitImages(review.imageUrls || review.images)
}

const sortedReviews = computed(() => {
  const list = [...reviews.value]
  if (reviewSort.value === 'hottest') {
    return list.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
  }
  return list
})

const loadProduct = async () => {
  loading.value = true
  try {
    const res = await getProductById(route.params.id)
    product.value = res.data
  } catch (error) {
    console.error(error)
    ElMessage.error('加载商品失败')
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

const loadReviews = async () => {
  try {
    const res = await getProductReviews(route.params.id)
    reviews.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const loadReviewStats = async () => {
  try {
    const res = await getReviewStats(route.params.id)
    avgRating.value = res.data.avgRating
    reviewCount.value = res.data.count
  } catch (error) {
    console.error(error)
  }
}

const loadCanReview = async () => {
  if (!userStore.userInfo || userStore.userInfo.username === 'admin') {
    hasCompletedPurchase.value = false
    hasReviewed.value = false
    reviewOrderId.value = null
    return
  }
  try {
    const queryOrderId = route.query.orderId ? Number(route.query.orderId) : null
    const res = await canReviewProduct(route.params.id, queryOrderId)
    hasCompletedPurchase.value = !!res.data?.hasCompletedPurchase
    hasReviewed.value = !!res.data?.hasReviewed
    reviewOrderId.value = res.data?.orderId || null
  } catch (error) {
    console.error(error)
    hasCompletedPurchase.value = false
    hasReviewed.value = false
    reviewOrderId.value = null
  }
}

const getReviewBlockedMessage = () => {
  if (hasReviewed.value) return '该商品已评价，不能重复评价'
  return '购买并完成订单后才能发表评价'
}

const handleAddToCart = async () => {
  if (!product.value) return
  if (product.value.stock <= 0) {
    ElMessage.warning('商品已售罄')
    return
  }
  if (quantity.value > product.value.stock) {
    ElMessage.warning(`库存不足，最多可购买${product.value.stock}件`)
    return
  }
  addingToCart.value = true
  try {
    const success = await cartStore.addToCart(product.value, quantity.value)
    if (success) {
      ElMessage.success({ message: `${product.value.name} ×${quantity.value} 已加入购物车 🛒`, duration: 1500 })
    } else {
      ElMessage.error('加入购物车失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加入购物车失败')
  } finally {
    addingToCart.value = false
  }
}

const handleToggleFavorite = async () => {
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    if (isFavorite.value) {
      await removeFromFavorite(product.value.id)
      ElMessage.success('已取消收藏')
    } else {
      await addToFavorite(product.value.id)
      ElMessage.success('收藏成功')
    }
    isFavorite.value = !isFavorite.value
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

const showReviewDialog = () => {
  if (!canReview.value) {
    ElMessage.warning(getReviewBlockedMessage())
    return
  }
  isEditMode.value = false
  editingReviewId.value = null
  reviewForm.rating = 5
  reviewForm.content = ''
  reviewForm.images = ''
  reviewImageList.value = []
  reviewDialogVisible.value = true
}

const openReviewFromOrder = async () => {
  if (route.query.review !== '1') return
  await nextTick()
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录后再评价')
    router.push('/login')
    return
  }
  if (!canReview.value) {
    ElMessage.warning(getReviewBlockedMessage())
    return
  }
  showReviewDialog()
}

const clearReviewQuery = () => {
  if (route.query.review !== '1') return
  const query = { ...route.query }
  delete query.review
  delete query.orderId
  router.replace({ path: route.path, query })
}

const closeReviewDialog = () => {
  reviewDialogVisible.value = false
  clearReviewQuery()
}

const editReview = (review) => {
  isEditMode.value = true
  editingReviewId.value = review.id
  reviewForm.rating = review.rating
  reviewForm.content = review.content
  reviewForm.images = review.images || ''
  const rawImages = splitImages(review.images)
  const previewImages = splitImages(review.imageUrls || review.images)
  if (rawImages.length) {
    reviewImageList.value = rawImages.map((objectKey, index) => ({
      name: `image-${index}`,
      url: getImageUrl(previewImages[index] || objectKey),
      objectKey
    }))
  } else {
    reviewImageList.value = []
  }
  reviewDialogVisible.value = true
}

const showReplyDialog = (review) => {
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  replyForm.parentId = review.id
  replyForm.content = ''
  replyDialogVisible.value = true
}

const beforeReviewImageUpload = (file) => {
  const isImage = isAllowedImageFile(file)
  const isLt5M = file.size / 1024 / 1024 <= 5

  if (!isImage) {
    ElMessage.error(allowedImageMessage)
    return false
  }
  if (!isLt5M) {
    ElMessage.error('评价图片不能超过 5MB')
    return false
  }
  return true
}

const handleUploadSuccess = (response, file) => {
  if (response.code === 200 && response.data && response.data.url) {
    const objectKey = response.data.objectKey || response.data.url
    const images = reviewForm.images ? reviewForm.images.split(',').filter(Boolean) : []
    images.push(objectKey)
    reviewForm.images = images.join(',')
    file.objectKey = objectKey
    file.url = response.data.url
  } else {
    ElMessage.error(response.message || '上传失败')
    const index = reviewImageList.value.findIndex(f => f.uid === file.uid)
    if (index > -1) {
      reviewImageList.value.splice(index, 1)
    }
  }
}

const getUploadErrorMessage = (error) => {
  const rawMessage = error?.message || error?.toString?.() || ''
  try {
    const responseText = error?.target?.response || error?.request?.responseText
    if (responseText) {
      const data = JSON.parse(responseText)
      return data.message || rawMessage
    }
  } catch (e) {
    // Ignore parse failures and fall through to generic message handling.
  }
  if (rawMessage.includes('413')) return '图片文件过大，请压缩后再上传'
  return rawMessage || '图片上传失败，请重试'
}

const handleUploadError = (error, file) => {
  ElMessage.error(getUploadErrorMessage(error))
  const index = reviewImageList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    reviewImageList.value.splice(index, 1)
  }
}

const handlePreview = (file) => {
  if (file.url) {
    window.open(getImageUrl(file.url), '_blank')
  }
}

const handleUploadRemove = (file) => {
  let imageToRemove = ''
  if (file.response && file.response.data) {
    imageToRemove = file.response.data.objectKey || file.response.data.url
  } else if (file.objectKey) {
    imageToRemove = file.objectKey
  } else if (file.url) {
    imageToRemove = file.url.replace('http://localhost:8080', '')
  }
  if (imageToRemove) {
    const images = reviewForm.images ? reviewForm.images.split(',').filter(Boolean) : []
    const index = images.findIndex(img => img === imageToRemove)
    if (index > -1) {
      images.splice(index, 1)
      reviewForm.images = images.join(',')
    }
  }
}

const deleteReviewItem = async (review) => {
  try {
    await ElMessageBox.confirm('确定要删除该评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteReviewApi(review.id)
    ElMessage.success('删除成功')
    loadReviews()
    loadReviewStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleToggleLike = async (review) => {
  if (!userStore.userInfo) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    const res = await toggleReviewLike(review.id)
    review.isLiked = res.data.isLiked
    review.likeCount = res.data.likeCount
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  }
}

const submitReview = async () => {
  if (!reviewForm.content || reviewForm.content.trim().length === 0) {
    ElMessage.warning('请输入评价内容')
    return
  }
  if (reviewForm.content.length > 500) {
    ElMessage.warning('评价内容不能超过500字')
    return
  }

  submitting.value = true
  try {
    if (isEditMode.value) {
      await updateReview(editingReviewId.value, {
        rating: reviewForm.rating,
        content: reviewForm.content,
        images: reviewForm.images
      })
      ElMessage.success('修改成功')
    } else {
      await addReview({
        productId: product.value.id,
        orderId: route.query.orderId ? Number(route.query.orderId) : reviewOrderId.value,
        rating: reviewForm.rating,
        content: reviewForm.content,
        images: reviewForm.images
      })
      ElMessage.success('评价成功')
    }
    closeReviewDialog()
    loadReviews()
    loadReviewStats()
    loadCanReview()
  } catch (error) {
    console.error(error)
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const submitReply = async () => {
  if (!replyForm.content || replyForm.content.trim().length === 0) {
    ElMessage.warning('请输入回复内容')
    return
  }

  submitting.value = true
  try {
    await replyReview({
      parentId: replyForm.parentId,
      content: replyForm.content
    })
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    loadReviews()
  } catch (error) {
    console.error(error)
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
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

/* ── 商品主信息 ── */
.product-main {
  display: flex;
  gap: 40px;
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-sm);
  margin-bottom: 24px;
}

.product-gallery {
  flex: 0 0 420px;
  position: relative;
  overflow: hidden;
  border-radius: var(--radius-md);
}

.product-main-image {
  width: 100%;
  height: 420px;
  display: block;
  background: var(--color-bg-section);
  cursor: zoom-in;
}

.product-main-image :deep(img) {
  transition: transform var(--transition-normal);
}

.product-gallery:hover .product-main-image :deep(img) {
  transform: scale(1.035);
}

.image-zoom-hint {
  position: absolute;
  right: 14px;
  bottom: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 10px;
  border-radius: 6px;
  background: rgba(20, 32, 18, 0.72);
  color: #fff;
  font-size: 12px;
  pointer-events: none;
  opacity: 0;
  transform: translateY(6px);
  transition: opacity var(--transition-fast), transform var(--transition-fast);
}

.product-gallery:hover .image-zoom-hint {
  opacity: 1;
  transform: translateY(0);
}

.product-info {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 12px;
  line-height: 1.3;
}

.product-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin: 0 0 20px;
  line-height: 1.6;
}

/* ── 评分 ── */
.product-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 24px;
}

.rating-count {
  font-size: 13px;
  color: var(--color-text-placeholder);
}

/* ── 价格区 ── */
.price-block {
  background: linear-gradient(135deg, #fff5f5, #fff0e6);
  border-radius: var(--radius-md);
  padding: 16px 20px;
  margin-bottom: 24px;
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.price-symbol {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-price);
}

.price-value {
  font-size: 36px;
  font-weight: 800;
  color: var(--color-price);
  line-height: 1;
}

.price-unit {
  font-size: 13px;
  color: var(--color-text-placeholder);
  margin-left: 4px;
}

/* ── 信息网格 ── */
.meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 28px;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-label {
  font-size: 12px;
  color: var(--color-text-placeholder);
}

.meta-value {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}

/* ── 购买操作 ── */
.product-actions {
  border-top: 1px solid var(--color-border-light);
  padding-top: 24px;
}

.quantity-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.action-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.cart-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.fav-btn {
  height: 48px;
  padding: 0 24px;
}

/* ── 评价区 ── */
.review-section {
  background: #fff;
  border-radius: var(--radius-lg);
  padding: 28px 32px;
  box-shadow: var(--shadow-sm);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-light);
}

.section-header h2 {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: var(--color-text-primary);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.review-info {
  flex: 1;
  margin-left: 10px;
}

.user-name {
  font-weight: 600;
  margin-bottom: 2px;
  font-size: 14px;
}

.review-time {
  color: var(--color-text-placeholder);
  font-size: 12px;
}

.review-actions {
  margin-left: 12px;
}

.review-content {
  margin-bottom: 10px;
  line-height: 1.7;
  font-size: 14px;
  color: var(--color-text-primary);
}

.review-images {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.review-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.review-footer {
  display: flex;
  gap: 24px;
  margin-top: 10px;
}

.like-btn, .reply-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  color: var(--color-text-secondary);
  font-size: 13px;
  transition: color var(--transition-fast);
}

.like-btn:hover, .reply-btn:hover {
  color: var(--color-primary);
}

.like-btn .liked {
  color: var(--color-danger);
}

.like-btn.small {
  font-size: 12px;
}

.replies {
  margin-top: 12px;
  margin-left: 50px;
  padding-left: 16px;
  border-left: 2px solid var(--color-border-light);
}

.reply-item {
  padding: 10px 0;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.reply-user {
  font-weight: 500;
  font-size: 13px;
}

.reply-time {
  color: var(--color-text-placeholder);
  font-size: 11px;
}

.reply-actions {
  margin-left: auto;
}

.reply-content {
  font-size: 13px;
  color: var(--color-text-primary);
  line-height: 1.6;
}

.reply-footer {
  margin-top: 6px;
}

.upload-tip {
  font-size: 12px;
  color: var(--color-text-placeholder);
  margin-top: 5px;
}

.review-uploader :deep(.el-upload--picture-card),
.review-uploader :deep(.el-upload-list__item) {
  width: 96px;
  height: 96px;
}

.review-uploader :deep(.el-upload--picture-card) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .detail-wrapper {
    padding: 16px;
  }

  .product-main {
    flex-direction: column;
    padding: 20px;
    gap: 20px;
  }

  .product-gallery {
    flex: none;
  }

  .product-main-image {
    height: 280px;
  }

  .meta-grid {
    grid-template-columns: 1fr 1fr;
  }

  .action-buttons {
    flex-direction: column;
  }

  .cart-btn, .fav-btn {
    width: 100%;
  }
}
</style>
