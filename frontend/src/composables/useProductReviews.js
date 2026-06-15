import { computed, reactive, ref, nextTick } from 'vue'
import {
  addReview,
  canReviewProduct,
  deleteReview as deleteReviewApi,
  getProductReviews,
  getReviewStats,
  replyReview,
  toggleReviewLike,
  updateReview
} from '@/api/review'
import { imageUtils } from '@/utils/imageUtils'
import { ALLOWED_IMAGE_ACCEPT, allowedImageMessage, isAllowedImageFile } from '@/utils/uploadValidation'
import { isAdminUser } from '@/utils/auth'
import { confirmDanger, notify } from '@/services/uiFeedback'

const splitImages = images => images ? images.split(',').map(item => item.trim()).filter(Boolean) : []

export const useProductReviews = ({ route, router, userStore, product }) => {
  const reviews = ref([])
  const avgRating = ref(0)
  const reviewCount = ref(0)
  const reviewDialogVisible = ref(false)
  const replyDialogVisible = ref(false)
  const isEditMode = ref(false)
  const editingReviewId = ref(null)
  const submitting = ref(false)
  const reviewSort = ref('newest')
  const reviewImageList = ref([])
  const hasCompletedPurchase = ref(false)
  const hasReviewed = ref(false)
  const reviewOrderId = ref(null)

  const reviewForm = reactive({
    rating: 5,
    content: '',
    images: ''
  })

  const replyForm = reactive({
    parentId: null,
    content: ''
  })

  const uploadHeaders = computed(() => ({
    Authorization: userStore.token ? `Bearer ${userStore.token}` : ''
  }))

  const canReview = computed(() => {
    return userStore.userInfo && !isAdminUser(userStore.userInfo) && hasCompletedPurchase.value
  })

  const sortedReviews = computed(() => {
    const list = [...reviews.value]
    if (reviewSort.value === 'hottest') {
      return list.sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
    }
    return list
  })

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
    if (!userStore.userInfo || isAdminUser(userStore.userInfo)) {
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

  const resetReviewForm = () => {
    isEditMode.value = false
    editingReviewId.value = null
    reviewForm.rating = 5
    reviewForm.content = ''
    reviewForm.images = ''
    reviewImageList.value = []
  }

  const showReviewDialog = () => {
    if (!canReview.value) {
      notify.warning(getReviewBlockedMessage())
      return
    }
    resetReviewForm()
    reviewDialogVisible.value = true
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

  const openReviewFromOrder = async () => {
    if (route.query.review !== '1') return
    await nextTick()
    if (!userStore.userInfo) {
      notify.warning('请先登录后再评价')
      router.push('/login')
      return
    }
    if (!canReview.value) {
      notify.warning(getReviewBlockedMessage())
      return
    }
    showReviewDialog()
  }

  const editReview = (review) => {
    isEditMode.value = true
    editingReviewId.value = review.id
    reviewForm.rating = review.rating
    reviewForm.content = review.content
    reviewForm.images = review.images || ''

    const rawImages = splitImages(review.images)
    const previewImages = splitImages(review.imageUrls || review.images)
    reviewImageList.value = rawImages.map((objectKey, index) => ({
      name: `image-${index}`,
      url: imageUtils.getImageUrl(previewImages[index] || objectKey),
      objectKey
    }))
    reviewDialogVisible.value = true
  }

  const showReplyDialog = (review) => {
    if (!userStore.userInfo) {
      notify.warning('请先登录')
      router.push('/login')
      return
    }
    replyForm.parentId = review.id
    replyForm.content = ''
    replyDialogVisible.value = true
  }

  const beforeReviewImageUpload = (file) => {
    if (!isAllowedImageFile(file)) {
      notify.error(allowedImageMessage)
      return false
    }
    if (file.size / 1024 / 1024 > 5) {
      notify.error('评价图片不能超过 5MB')
      return false
    }
    return true
  }

  const handleUploadSuccess = (response, file) => {
    if (response.code === 200 && response.data?.url) {
      const objectKey = response.data.objectKey || response.data.url
      const images = reviewForm.images ? reviewForm.images.split(',').filter(Boolean) : []
      images.push(objectKey)
      reviewForm.images = images.join(',')
      file.objectKey = objectKey
      file.url = response.data.url
      return
    }

    notify.error(response.message || '上传失败')
    const index = reviewImageList.value.findIndex(item => item.uid === file.uid)
    if (index > -1) reviewImageList.value.splice(index, 1)
  }

  const getUploadErrorMessage = (error) => {
    const rawMessage = error?.message || error?.toString?.() || ''
    try {
      const responseText = error?.target?.response || error?.request?.responseText
      if (responseText) {
        const data = JSON.parse(responseText)
        return data.message || rawMessage
      }
    } catch (parseError) {
      console.error('[ReviewUpload] Failed to parse upload error:', parseError)
    }
    if (rawMessage.includes('413')) return '图片文件过大，请压缩后再上传'
    return rawMessage || '图片上传失败，请重试'
  }

  const handleUploadError = (error, file) => {
    notify.error(getUploadErrorMessage(error))
    const index = reviewImageList.value.findIndex(item => item.uid === file.uid)
    if (index > -1) reviewImageList.value.splice(index, 1)
  }

  const handlePreview = (file) => {
    if (file.url) {
      window.open(imageUtils.getImageUrl(file.url), '_blank')
    }
  }

  const handleUploadRemove = (file) => {
    const imageToRemove = file.response?.data
      ? file.response.data.objectKey || file.response.data.url
      : file.objectKey || file.url || ''

    if (!imageToRemove) return
    const images = reviewForm.images ? reviewForm.images.split(',').filter(Boolean) : []
    const index = images.findIndex(image => image === imageToRemove)
    if (index > -1) {
      images.splice(index, 1)
      reviewForm.images = images.join(',')
    }
  }

  const deleteReviewItem = async (review) => {
    try {
      await confirmDanger('确定要删除该评价吗？')
      await deleteReviewApi(review.id)
      notify.success('删除成功')
      loadReviews()
      loadReviewStats()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        console.error(error)
      }
    }
  }

  const handleToggleLike = async (review) => {
    if (!userStore.userInfo) {
      notify.warning('请先登录')
      return
    }
    try {
      const res = await toggleReviewLike(review.id)
      review.isLiked = res.data.isLiked
      review.likeCount = res.data.likeCount
    } catch (error) {
      console.error(error)
      notify.error('操作失败')
    }
  }

  const submitReview = async () => {
    if (!reviewForm.content || reviewForm.content.trim().length === 0) {
      notify.warning('请输入评价内容')
      return
    }
    if (reviewForm.content.length > 500) {
      notify.warning('评价内容不能超过500字')
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
        notify.success('修改成功')
      } else {
        await addReview({
          productId: product.value.id,
          orderId: route.query.orderId ? Number(route.query.orderId) : reviewOrderId.value,
          rating: reviewForm.rating,
          content: reviewForm.content,
          images: reviewForm.images
        })
        notify.success('评价成功')
      }
      closeReviewDialog()
      loadReviews()
      loadReviewStats()
      loadCanReview()
    } catch (error) {
      console.error(error)
      notify.error('操作失败')
    } finally {
      submitting.value = false
    }
  }

  const submitReply = async () => {
    if (!replyForm.content || replyForm.content.trim().length === 0) {
      notify.warning('请输入回复内容')
      return
    }

    submitting.value = true
    try {
      await replyReview({
        parentId: replyForm.parentId,
        content: replyForm.content
      })
      notify.success('回复成功')
      replyDialogVisible.value = false
      loadReviews()
    } catch (error) {
      console.error(error)
      notify.error('回复失败')
    } finally {
      submitting.value = false
    }
  }

  return {
    ALLOWED_IMAGE_ACCEPT,
    reviews,
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
  }
}
