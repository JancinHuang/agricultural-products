<template>
  <div class="order-page">
    <div class="order-wrapper">
      <div class="page-header">
        <div>
          <p class="eyebrow">Order Center</p>
          <h2>我的订单</h2>
        </div>
        <el-button type="primary" plain @click="router.push('/shop')">继续选购</el-button>
      </div>

      <div class="status-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="['tab-item', { active: activeTab === tab.key }]"
          type="button"
          @click="activeTab = tab.key"
        >
          <span>{{ tab.label }}</span>
          <em>{{ getTabCount(tab.key) }}</em>
        </button>
      </div>

      <div class="order-list" v-loading="loading">
        <article v-for="order in filteredOrders" :key="order.id" class="order-card">
          <header class="order-head">
            <div class="head-left">
              <span class="order-date">{{ formatTime(order.createTime) }}</span>
              <span class="order-no">订单号：{{ order.orderNo }}</span>
            </div>
            <div class="head-right">
              <el-tag :type="getStatusType(order.status)" effect="plain">
                {{ getStatusText(order.status) }}
              </el-tag>
              <button class="detail-link" type="button" @click="viewDetail(order.id)">订单详情</button>
            </div>
          </header>

          <div class="order-main">
            <div class="product-area">
              <div v-for="item in visibleItems(order)" :key="item.id" class="product-row">
                <img :src="item.productImage || '/placeholder.jpg'" :alt="item.productName" class="product-img" />
                <div class="product-info">
                  <div class="product-name">{{ item.productName }}</div>
                  <div class="product-meta">
                    <span>单价 ¥{{ formatAmount(item.price) }}</span>
                    <span>x{{ item.quantity }}</span>
                    <span>小计 ¥{{ formatAmount(item.subtotal) }}</span>
                  </div>
                </div>
              </div>
              <button
                v-if="order.items?.length > 2"
                class="more-products"
                type="button"
                @click="viewDetail(order.id)"
              >
                查看全部 {{ order.items.length }} 件商品
              </button>
            </div>

            <div class="amount-area">
              <span class="amount-label">实付款</span>
              <strong>¥{{ formatAmount(order.totalAmount) }}</strong>
              <span class="freight">含运费 ¥0.00</span>
            </div>

            <div class="receive-area">
              <div class="receiver">
                <span>{{ order.receiverName }}</span>
                <span>{{ order.receiverPhone }}</span>
              </div>
              <p>{{ order.receiverAddress }}</p>
            </div>

            <div class="action-area">
              <el-button v-if="order.status === 0" type="primary" @click="handlePay(order)">立即支付</el-button>
              <el-button v-if="order.status === 2" type="success" @click="handleConfirm(order)">确认收货</el-button>
              <el-button
                v-if="order.status === 3"
                :type="isOrderFullyReviewed(order) ? 'success' : 'primary'"
                :plain="isOrderFullyReviewed(order)"
                @click="goReview(order)"
              >
                {{ isOrderFullyReviewed(order) ? '已评价' : '去评价' }}
              </el-button>
              <el-button v-if="order.status === 0" plain @click="handleCancel(order)">取消订单</el-button>
              <el-button plain @click="viewDetail(order.id)">查看详情</el-button>
              <el-button v-if="[2, 3].includes(order.status)" text @click="handleRebuy(order)">再买一单</el-button>
            </div>
          </div>

          <footer v-if="order.status === 2" class="order-note">
            <span>派送中</span>
            预计近期送达，收到农产品后请及时确认收货。
          </footer>
          <footer v-else-if="order.status === 3" class="order-note signed">
            <span>已签收</span>
            {{ isOrderFullyReviewed(order) ? '该订单商品已评价完成。' : '商品已完成交易，可前往商品详情页发表评价。' }}
            <button
              v-if="!isOrderFullyReviewed(order)"
              class="note-action"
              type="button"
              @click="goReview(order)"
            >
              去评价
            </button>
          </footer>
        </article>

        <el-empty v-if="!loading && filteredOrders.length === 0" description="暂无订单">
          <el-button type="primary" @click="router.push('/shop')">去购物</el-button>
        </el-empty>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="订单详情" width="720px" top="5vh">
      <div v-if="orderDetail" class="order-detail">
        <section class="detail-section">
          <h4>订单信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">订单编号</span>
              <span class="value">{{ orderDetail.order.orderNo }}</span>
            </div>
            <div class="detail-item">
              <span class="label">订单状态</span>
              <el-tag :type="getStatusType(orderDetail.order.status)" size="small">
                {{ getStatusText(orderDetail.order.status) }}
              </el-tag>
            </div>
            <div class="detail-item">
              <span class="label">创建时间</span>
              <span class="value">{{ formatTime(orderDetail.order.createTime) }}</span>
            </div>
          </div>
        </section>

        <section class="detail-section">
          <h4>收货信息</h4>
          <div class="detail-grid">
            <div class="detail-item">
              <span class="label">收货人</span>
              <span class="value">{{ orderDetail.order.receiverName }}</span>
            </div>
            <div class="detail-item">
              <span class="label">联系电话</span>
              <span class="value">{{ orderDetail.order.receiverPhone }}</span>
            </div>
            <div class="detail-item detail-full">
              <span class="label">收货地址</span>
              <span class="value">{{ orderDetail.order.receiverAddress }}</span>
            </div>
          </div>
        </section>

        <section class="detail-section">
          <h4>商品信息</h4>
          <div class="detail-products">
            <div v-for="item in orderDetail.items" :key="item.id" class="detail-product-item">
              <img :src="item.productImage || '/placeholder.jpg'" class="detail-product-img" />
              <div class="detail-product-info">
                <span class="detail-product-name">{{ item.productName }}</span>
                <span class="detail-product-price">¥{{ formatAmount(item.price) }} x {{ item.quantity }}</span>
              </div>
              <span class="detail-product-subtotal">¥{{ formatAmount(item.subtotal) }}</span>
              <el-button
                v-if="orderDetail.order.status === 3"
                :type="isItemReviewed(item) ? 'success' : 'primary'"
                link
                @click="goReview(orderDetail.order, item)"
              >
                {{ isItemReviewed(item) ? '已评价' : '评价' }}
              </el-button>
            </div>
          </div>
        </section>

        <div class="detail-total">
          订单总额：<em>¥{{ formatAmount(orderDetail.order.totalAmount) }}</em>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="reviewPickerVisible" title="选择要评价的商品" width="560px" append-to-body>
      <div v-if="reviewPickerOrder" class="review-picker">
        <div
          v-for="item in reviewableItems(reviewPickerOrder)"
          :key="item.id"
          class="review-picker-item"
          @click="goReview(reviewPickerOrder, item)"
        >
          <img :src="item.productImage || '/placeholder.jpg'" class="review-picker-img" />
          <div class="review-picker-info">
            <span class="review-picker-name">{{ item.productName }}</span>
            <span class="review-picker-meta">¥{{ formatAmount(item.price) }} x {{ item.quantity }}</span>
          </div>
          <el-button type="primary" link>评价</el-button>
        </div>
      </div>
      <el-empty v-else description="暂无可评价商品" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserOrders, getOrderDetail, payOrder, cancelOrder, confirmOrder } from '@/api/order'
import { getUserReviews } from '@/api/review'
import { formatTime } from '@/utils/time'
import { getStatusText, getStatusType } from '@/utils/orderStatus'
import { useCartStore } from '@/store/cart'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)
const orders = ref([])
const reviewedOrderProductKeys = ref(new Set())
const activeTab = ref('all')
const detailVisible = ref(false)
const orderDetail = ref(null)
const reviewPickerVisible = ref(false)
const reviewPickerOrder = ref(null)

const tabs = [
  { key: 'all', label: '全部订单' },
  { key: '0', label: '待付款' },
  { key: '1', label: '待发货' },
  { key: '2', label: '待收货' },
  { key: '3', label: '已完成' }
]

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(order => order.status === Number(activeTab.value))
})

const formatAmount = value => Number(value || 0).toFixed(2)
const visibleItems = order => (order.items || []).slice(0, 2)
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

const getTabCount = (key) => {
  if (key === 'all') return orders.value.length
  return orders.value.filter(order => order.status === Number(key)).length
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

const viewDetail = async (id) => {
  try {
    const res = await getOrderDetail(id)
    orderDetail.value = res.data
    detailVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const goReview = (order, item = null) => {
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
    ElMessage.warning('该订单没有可评价的商品')
    return
  }
  if (isItemReviewed(targetItem, order)) {
    ElMessage.info('该商品已评价，不能重复评价')
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
    ElMessage.warning('该订单没有可再次购买的商品')
    return
  }

  let successCount = 0
  for (const item of items) {
    const success = await cartStore.addToCart({
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
    ElMessage.error('加入购物车失败')
    return
  }

  ElMessage.success(`已将 ${successCount} 件商品加入购物车`)
  router.push('/cart')
}

const handlePay = async (order) => {
  try {
    await ElMessageBox.confirm('确定要支付该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await payOrder(order.id)
    ElMessage.success('支付成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleConfirm = async (order) => {
  try {
    await ElMessageBox.confirm('确定已经收到货物了吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await confirmOrder(order.id)
    ElMessage.success('确认收货成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-page {
  background: #f5f7f2;
  min-height: 100%;
}

.order-wrapper {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 18px;
}

.eyebrow {
  margin: 0 0 4px;
  color: #6f8268;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1f2a1d;
  margin: 0;
}

.status-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 18px;
  background: #fff;
  border: 1px solid #e2eadc;
  border-radius: 8px;
  padding: 8px;
  box-shadow: 0 8px 22px rgba(44, 70, 38, 0.06);
}

.tab-item {
  height: 36px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 18px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #52614f;
  font-size: 14px;
  cursor: pointer;
}

.tab-item em {
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  border-radius: 999px;
  background: #eef4ea;
  color: #47733b;
  font-style: normal;
  font-size: 12px;
}

.tab-item.active {
  background: #2f7d32;
  color: #fff;
}

.tab-item.active em {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.order-card {
  background: #fff;
  border: 1px solid #e2eadc;
  border-radius: 8px;
  margin-bottom: 14px;
  overflow: hidden;
  box-shadow: 0 8px 24px rgba(44, 70, 38, 0.05);
}

.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 16px;
  background: #eef1eb;
}

.head-left,
.head-right {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.order-date {
  font-weight: 700;
  color: #1f2a1d;
}

.order-no {
  color: #5f6b5d;
  font-size: 13px;
}

.detail-link {
  border: 0;
  background: transparent;
  color: #f06423;
  cursor: pointer;
  font-size: 13px;
}

.order-main {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) 140px 220px 140px;
  gap: 16px;
  padding: 16px;
  align-items: start;
}

.product-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-row {
  display: flex;
  gap: 12px;
  min-width: 0;
}

.product-img {
  width: 76px;
  height: 76px;
  border-radius: 6px;
  object-fit: cover;
  border: 1px solid #edf1e9;
  flex-shrink: 0;
}

.product-info {
  min-width: 0;
}

.product-name {
  color: #1f2a1d;
  font-size: 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
  color: #7a8676;
  font-size: 12px;
}

.more-products {
  width: fit-content;
  border: 0;
  background: #f4f8f1;
  color: #2f7d32;
  border-radius: 5px;
  padding: 6px 10px;
  cursor: pointer;
}

.amount-area,
.receive-area,
.action-area {
  border-left: 1px solid #edf1e9;
  padding-left: 16px;
}

.amount-area {
  text-align: right;
}

.amount-label,
.freight {
  display: block;
  color: #7a8676;
  font-size: 12px;
}

.amount-area strong {
  display: block;
  margin: 6px 0;
  color: #111;
  font-size: 20px;
}

.receiver {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #1f2a1d;
  font-weight: 600;
  font-size: 13px;
}

.receive-area p {
  margin: 8px 0 0;
  color: #687364;
  font-size: 12px;
  line-height: 1.5;
}

.action-area {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

.action-area :deep(.el-button + .el-button) {
  margin-left: 0;
}

.order-note {
  margin: 0 16px 16px 104px;
  padding: 9px 12px;
  background: #f4f7f0;
  border-radius: 6px;
  color: #52614f;
  font-size: 13px;
}

.order-note span {
  color: #f06423;
  margin-right: 8px;
  font-weight: 700;
}

.order-note.signed span {
  color: #2f7d32;
}

.note-action {
  margin-left: 10px;
  border: 0;
  background: transparent;
  color: #f06423;
  font-weight: 700;
  cursor: pointer;
}

.note-action:hover {
  color: #d94e14;
}

.order-detail {
  padding: 4px 0;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1f2a1d;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #edf1e9;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.detail-full {
  grid-column: 1 / -1;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-item .label {
  font-size: 13px;
  color: #7a8676;
  white-space: nowrap;
}

.detail-item .value {
  font-size: 13px;
  color: #1f2a1d;
}

.detail-products {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f6f8f3;
  border-radius: 6px;
}

.detail-product-img {
  width: 54px;
  height: 54px;
  object-fit: cover;
  border-radius: 4px;
  flex-shrink: 0;
}

.detail-product-info {
  flex: 1;
  min-width: 0;
}

.detail-product-name {
  display: block;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-product-price {
  font-size: 12px;
  color: #7a8676;
}

.detail-product-subtotal {
  font-size: 15px;
  font-weight: 700;
  color: #f06423;
  flex-shrink: 0;
}

.review-picker {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.review-picker-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #edf1e9;
  border-radius: 6px;
  background: #f8faf5;
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease;
}

.review-picker-item:hover {
  border-color: #b8d5ad;
  background: #f1f7ed;
}

.review-picker-img {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
  border: 1px solid #e4eadf;
}

.review-picker-info {
  flex: 1;
  min-width: 0;
}

.review-picker-name {
  display: block;
  color: #1f2a1d;
  font-size: 14px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-picker-meta {
  display: block;
  margin-top: 4px;
  color: #7a8676;
  font-size: 12px;
}

.detail-total {
  text-align: right;
  font-size: 15px;
  padding-top: 16px;
  border-top: 1px solid #edf1e9;
}

.detail-total em {
  font-size: 22px;
  color: #f06423;
  font-style: normal;
  font-weight: 700;
}

@media (max-width: 900px) {
  .order-wrapper {
    padding: 16px;
  }

  .status-tabs {
    overflow-x: auto;
  }

  .order-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .order-main {
    grid-template-columns: 1fr;
  }

  .amount-area,
  .receive-area,
  .action-area {
    border-left: 0;
    border-top: 1px solid #edf1e9;
    padding: 12px 0 0;
    text-align: left;
  }

  .order-note {
    margin-left: 16px;
  }
}
</style>
