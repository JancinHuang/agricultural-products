<template>
  <el-dialog v-model="innerVisible" title="订单详情" width="720px" top="5vh">
    <div v-if="detail" class="user-order-detail">
      <section class="user-order-detail__section">
        <h4>订单信息</h4>
        <div class="user-order-detail__grid">
          <div class="user-order-detail__item">
            <span>订单编号</span>
            <strong>{{ detail.order.orderNo }}</strong>
          </div>
          <div class="user-order-detail__item">
            <span>订单状态</span>
            <StatusTag
              :value="detail.order.status"
              :text="getStatusText(detail.order.status)"
              :type="getStatusType(detail.order.status)"
            />
          </div>
          <div class="user-order-detail__item">
            <span>创建时间</span>
            <strong>{{ formatTime(detail.order.createTime) }}</strong>
          </div>
        </div>
      </section>

      <section class="user-order-detail__section">
        <h4>收货信息</h4>
        <div class="user-order-detail__grid">
          <div class="user-order-detail__item">
            <span>收货人</span>
            <strong>{{ detail.order.receiverName }}</strong>
          </div>
          <div class="user-order-detail__item">
            <span>联系电话</span>
            <strong>{{ detail.order.receiverPhone }}</strong>
          </div>
          <div class="user-order-detail__item user-order-detail__item--full">
            <span>收货地址</span>
            <strong>{{ detail.order.receiverAddress }}</strong>
          </div>
        </div>
      </section>

      <section class="user-order-detail__section">
        <h4>商品信息</h4>
        <div class="user-order-detail__products">
          <div v-for="item in detail.items" :key="item.id" class="user-order-detail__product">
            <img :src="getImageUrl(item.productImage)" :alt="item.productName" />
            <div class="user-order-detail__product-info">
              <span class="user-order-detail__product-name">{{ item.productName }}</span>
              <span class="user-order-detail__product-price">¥{{ formatAmount(item.price) }} x {{ item.quantity }}</span>
            </div>
            <PriceText :value="item.subtotal" class="user-order-detail__subtotal" />
            <el-button
              v-if="detail.order.status === 3"
              :type="isItemReviewed(item) ? 'success' : 'primary'"
              link
              @click="$emit('review', detail.order, item)"
            >
              {{ isItemReviewed(item) ? '已评价' : '评价' }}
            </el-button>
          </div>
        </div>
      </section>

      <div class="user-order-detail__total">
        订单总额：<PriceText :value="detail.order.totalAmount" />
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import StatusTag from '@/components/base/StatusTag.vue'
import PriceText from '@/components/base/PriceText.vue'
import { formatTime } from '@/utils/time'
import { getStatusText, getStatusType } from '@/utils/orderStatus'
import { imageUtils } from '@/utils/imageUtils'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  detail: {
    type: Object,
    default: null
  },
  reviewedKeys: {
    type: Object,
    default: () => new Set()
  }
})

const emit = defineEmits(['update:modelValue', 'review'])

const innerVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const formatAmount = value => Number(value || 0).toFixed(2)
const getImageUrl = url => imageUtils.getImageUrl(url)
const getReviewKey = (orderId, productId) => `${Number(orderId)}:${Number(productId)}`
const isItemReviewed = item => props.reviewedKeys.has(getReviewKey(props.detail?.order?.id, item.productId))
</script>

<style scoped>
.user-order-detail {
  padding: 4px 0;
}

.user-order-detail__section {
  margin-bottom: var(--spacing-lg);
}

.user-order-detail__section h4 {
  margin: 0 0 var(--spacing-md);
  padding-bottom: var(--spacing-sm);
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border-light);
  font-size: var(--font-size-md);
  font-weight: 600;
}

.user-order-detail__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-sm);
}

.user-order-detail__item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.user-order-detail__item--full {
  grid-column: 1 / -1;
}

.user-order-detail__item span {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  white-space: nowrap;
}

.user-order-detail__item strong {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.user-order-detail__products {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.user-order-detail__product {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm);
  background: var(--color-bg-light);
  border-radius: var(--radius-sm);
}

.user-order-detail__product img {
  width: 54px;
  height: 54px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.user-order-detail__product-info {
  flex: 1;
  min-width: 0;
}

.user-order-detail__product-name {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-order-detail__product-price {
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.user-order-detail__subtotal {
  flex-shrink: 0;
  font-size: var(--font-size-md);
  font-weight: 700;
}

.user-order-detail__total {
  display: flex;
  justify-content: flex-end;
  align-items: baseline;
  gap: 4px;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--color-border-light);
  font-size: var(--font-size-md);
}
</style>
