<template>
  <div class="order-page">
    <div class="order-wrapper">
      <OrderPageHeader @shop="router.push('/shop')" />
      <OrderStatusTabs v-model="activeTab" :tabs="tabs" :counts="tabCounts" />

      <div class="order-list" v-loading="loading">
        <OrderCard
          v-for="order in filteredOrders"
          :key="order.id"
          :order="order"
          :fully-reviewed="isOrderFullyReviewed(order)"
          @detail="viewDetail"
          @pay="handlePay"
          @confirm="handleConfirm"
          @review="goReview"
          @cancel="handleCancel"
          @rebuy="handleRebuy"
        />

        <EmptyState v-if="!loading && filteredOrders.length === 0" description="暂无订单">
          <el-button type="primary" @click="router.push('/shop')">去购物</el-button>
        </EmptyState>
      </div>
    </div>

    <UserOrderDetailDialog
      v-model="detailVisible"
      :detail="orderDetail"
      :reviewed-keys="reviewedOrderProductKeys"
      @review="goReview"
    />

    <ReviewPickerDialog
      v-model="reviewPickerVisible"
      :items="reviewPickerItems"
      @select="item => goReview(reviewPickerOrder, item)"
    />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import OrderCard from '@/components/shop/OrderCard.vue'
import OrderPageHeader from '@/components/shop/OrderPageHeader.vue'
import OrderStatusTabs from '@/components/shop/OrderStatusTabs.vue'
import UserOrderDetailDialog from '@/components/shop/UserOrderDetailDialog.vue'
import ReviewPickerDialog from '@/components/shop/ReviewPickerDialog.vue'
import EmptyState from '@/components/base/EmptyState.vue'
import { useUserOrders } from '@/composables/useUserOrders'

const router = useRouter()
const {
  tabs,
  loading,
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
  viewDetail,
  goReview,
  handleRebuy,
  handlePay,
  handleCancel,
  handleConfirm,
  initOrders
} = useUserOrders()

onMounted(() => {
  initOrders()
})
</script>

<style scoped>
.order-page {
  background: var(--color-bg-page);
  min-height: 100%;
}

.order-wrapper {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px;
}

@media (max-width: 900px) {
  .order-wrapper {
    padding: var(--spacing-md);
  }
}
</style>
