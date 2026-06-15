import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { cancelOrder, confirmOrder, getOrderDetail, getUserOrders, payOrder } from '@/api/order'
import { getUserReviews } from '@/api/review'
import { useCart } from '@/composables/useCart'
import { confirmAction, notify } from '@/services/uiFeedback'

const ORDER_TABS = [
  { key: 'all', label: '全部订单' },
  { key: '0', label: '待付款' },
  { key: '1', label: '待发货' },
  { key: '2', label: '待收货' },
  { key: '3', label: '已完成' }
]

export function useUserOrders() {
  const router = useRouter()
  const { addToCart, initCart } = useCart()

  const loading = ref(false)
  const orders = ref([])
  const reviewedOrderProductKeys = ref(new Set())
  const activeTab = ref('all')
  const detailVisible = ref(false)
  const orderDetail = ref(null)
  const reviewPickerVisible = ref(false)
  const reviewPickerOrder = ref(null)

  const filteredOrders = computed(() => {
    if (activeTab.value === 'all') return orders.value
    return orders.value.filter(order => order.status === Number(activeTab.value))
  })

  const tabCounts = computed(() => ORDER_TABS.reduce((counts, tab) => {
    counts[tab.key] = tab.key === 'all'
      ? orders.value.length
      : orders.value.filter(order => order.status === Number(tab.key)).length
    return counts
  }, {}))

  const reviewPickerItems = computed(() => {
    if (!reviewPickerOrder.value) return []
    return reviewableItems(reviewPickerOrder.value)
  })

  const firstOrderItem = order => (order.items || [])[0]
  const getReviewKey = (orderId, productId) => `${Number(orderId)}:${Number(productId)}`
  const isItemReviewed = (item, order = orderDetail.value?.order) => {
    if (!item?.productId || !order?.id) return false
    return reviewedOrderProductKeys.value.has(getReviewKey(order.id, item.productId))
  }
  const firstUnreviewedOrderItem = order => (order.items || []).find(item => !isItemReviewed(item, order))
  const reviewableItems = order => (order.items || []).filter(item => !isItemReviewed(item, order))
  const isOrderFullyReviewed = order => {
    const items = order.items || []
    return items.length > 0 && items.every(item => isItemReviewed(item, order))
  }

  const loadOrders = async () => {
    loading.value = true
    try {
      const [orderRes, reviewRes] = await Promise.all([
        getUserOrders(),
        getUserReviews()
      ])
      orders.value = orderRes.data || []
      reviewedOrderProductKeys.value = new Set((reviewRes.data || [])
        .filter(review => !review.parentId)
        .filter(review => review.orderId && review.productId)
        .map(review => getReviewKey(review.orderId, review.productId)))
    } catch (error) {
      console.error(error)
    } finally {
      loading.value = false
    }
  }

  const viewDetail = async (order) => {
    try {
      const res = await getOrderDetail(order.id)
      orderDetail.value = res.data
      detailVisible.value = true
    } catch (error) {
      console.error(error)
    }
  }

  const goReview = (order, item = null) => {
    if (!order) return
    if (!item) {
      const items = reviewableItems(order)
      if (items.length > 1) {
        reviewPickerOrder.value = order
        reviewPickerVisible.value = true
        return
      }
    }

    const targetItem = item || firstUnreviewedOrderItem(order) || firstOrderItem(order)
    if (!targetItem?.productId) {
      notify.warning('该订单没有可评价的商品')
      return
    }
    if (isItemReviewed(targetItem, order)) {
      notify.info('该商品已评价，不能重复评价')
      return
    }

    detailVisible.value = false
    reviewPickerVisible.value = false
    router.push({
      path: `/product/${targetItem.productId}`,
      query: {
        review: '1',
        orderId: order.id
      }
    })
  }

  const handleRebuy = async (order) => {
    const items = order.items || []
    if (!items.length) {
      notify.warning('该订单没有可再次购买的商品')
      return
    }

    let successCount = 0
    for (const item of items) {
      const success = await addToCart({
        id: item.productId,
        name: item.productName,
        image: item.productImage,
        price: item.price,
        stock: item.productStock || item.quantity,
        status: 1
      }, item.quantity || 1)
      if (success) successCount += 1
    }

    if (successCount === 0) {
      notify.error('加入购物车失败')
      return
    }

    notify.success(`已将 ${successCount} 件商品加入购物车`)
    router.push('/cart')
  }

  const runOrderAction = async ({ order, message, successMessage, action, type = 'info' }) => {
    try {
      await confirmAction(message, { type })
      await action(order.id)
      notify.success(successMessage)
      loadOrders()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') console.error(error)
    }
  }

  const handlePay = order => runOrderAction({
    order,
    message: '确定要支付该订单吗？',
    successMessage: '支付成功',
    action: payOrder
  })

  const handleCancel = order => runOrderAction({
    order,
    message: '确定要取消该订单吗？',
    successMessage: '订单已取消',
    action: cancelOrder,
    type: 'warning'
  })

  const handleConfirm = order => runOrderAction({
    order,
    message: '确定已经收到货物了吗？',
    successMessage: '确认收货成功',
    action: confirmOrder
  })

  const initOrders = () => {
    loadOrders()
    initCart()
  }

  return {
    tabs: ORDER_TABS,
    loading,
    orders,
    reviewedOrderProductKeys,
    activeTab,
    detailVisible,
    orderDetail,
    reviewPickerVisible,
    reviewPickerOrder,
    filteredOrders,
    tabCounts,
    reviewPickerItems,
    isOrderFullyReviewed,
    loadOrders,
    viewDetail,
    goReview,
    handleRebuy,
    handlePay,
    handleCancel,
    handleConfirm,
    initOrders
  }
}
