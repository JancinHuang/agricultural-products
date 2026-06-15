<template>
  <article class="order-card">
    <header class="order-card__head">
      <div class="order-card__head-left">
        <span class="order-card__date">{{ formatTime(order.createTime) }}</span>
        <span class="order-card__no">订单号：{{ order.orderNo }}</span>
      </div>
      <div class="order-card__head-right">
        <StatusTag :value="order.status" :text="getStatusText(order.status)" :type="getStatusType(order.status)" />
        <button class="order-card__detail-link" type="button" @click="$emit('detail', order)">订单详情</button>
      </div>
    </header>

    <div class="order-card__main">
      <div class="order-card__products">
        <div v-for="item in visibleItems" :key="item.id" class="order-card__product-row">
          <img :src="getImageUrl(item.productImage)" :alt="item.productName" class="order-card__product-img" />
          <div class="order-card__product-info">
            <div class="order-card__product-name">{{ item.productName }}</div>
            <div class="order-card__product-meta">
              <span>单价 ¥{{ formatAmount(item.price) }}</span>
              <span>x{{ item.quantity }}</span>
              <span>小计 ¥{{ formatAmount(item.subtotal) }}</span>
            </div>
          </div>
        </div>
        <button
          v-if="order.items?.length > 2"
          class="order-card__more-products"
          type="button"
          @click="$emit('detail', order)"
        >
          查看全部 {{ order.items.length }} 件商品
        </button>
      </div>

      <div class="order-card__amount">
        <span class="order-card__amount-label">实付款</span>
        <PriceText :value="order.totalAmount" class="order-card__amount-value" />
        <span class="order-card__freight">含运费 ¥0.00</span>
      </div>

      <div class="order-card__receiver">
        <div class="order-card__receiver-name">
          <span>{{ order.receiverName }}</span>
          <span>{{ order.receiverPhone }}</span>
        </div>
        <p>{{ order.receiverAddress }}</p>
      </div>

      <div class="order-card__actions">
        <el-button v-if="order.status === 0" type="primary" @click="$emit('pay', order)">立即支付</el-button>
        <el-button v-if="order.status === 2" type="success" @click="$emit('confirm', order)">确认收货</el-button>
        <el-button
          v-if="order.status === 3"
          :type="fullyReviewed ? 'success' : 'primary'"
          :plain="fullyReviewed"
          @click="$emit('review', order)"
        >
          {{ fullyReviewed ? '已评价' : '去评价' }}
        </el-button>
        <el-button v-if="order.status === 0" plain @click="$emit('cancel', order)">取消订单</el-button>
        <el-button plain @click="$emit('detail', order)">查看详情</el-button>
        <el-button v-if="[2, 3].includes(order.status)" text @click="$emit('rebuy', order)">再买一单</el-button>
      </div>
    </div>

    <footer v-if="order.status === 2" class="order-card__note">
      <span>派送中</span>
      预计近期送达，收到农产品后请及时确认收货。
    </footer>
    <footer v-else-if="order.status === 3" class="order-card__note order-card__note--signed">
      <span>已签收</span>
      {{ fullyReviewed ? '该订单商品已评价完成。' : '商品已完成交易，可前往商品详情页发表评价。' }}
      <button
        v-if="!fullyReviewed"
        class="order-card__note-action"
        type="button"
        @click="$emit('review', order)"
      >
        去评价
      </button>
    </footer>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import StatusTag from '@/components/base/StatusTag.vue'
import PriceText from '@/components/base/PriceText.vue'
import { formatTime } from '@/utils/time'
import { getStatusText, getStatusType } from '@/utils/orderStatus'
import { imageUtils } from '@/utils/imageUtils'

const props = defineProps({
  order: {
    type: Object,
    required: true
  },
  fullyReviewed: {
    type: Boolean,
    default: false
  }
})

defineEmits(['detail', 'pay', 'confirm', 'review', 'cancel', 'rebuy'])

const visibleItems = computed(() => (props.order.items || []).slice(0, 2))
const formatAmount = value => Number(value || 0).toFixed(2)
const getImageUrl = url => imageUtils.getImageUrl(url)
</script>

<style scoped>
.order-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  margin-bottom: var(--spacing-md);
  overflow: hidden;
}

.order-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-md);
  padding: 12px var(--spacing-md);
  background: var(--color-bg-section);
}

.order-card__head-left,
.order-card__head-right {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.order-card__date {
  color: var(--color-text-primary);
  font-weight: 700;
}

.order-card__no {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.order-card__detail-link,
.order-card__note-action {
  border: 0;
  background: transparent;
  color: var(--color-accent);
  cursor: pointer;
  font-size: var(--font-size-sm);
}

.order-card__main {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) 140px 220px 140px;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  align-items: start;
}

.order-card__products {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.order-card__product-row {
  display: flex;
  gap: var(--spacing-md);
  min-width: 0;
}

.order-card__product-img {
  width: 76px;
  height: 76px;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  object-fit: cover;
  flex-shrink: 0;
}

.order-card__product-info {
  min-width: 0;
}

.order-card__product-name {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.order-card__product-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.order-card__more-products {
  width: fit-content;
  border: 0;
  background: var(--color-bg-light);
  color: var(--color-primary);
  border-radius: var(--radius-sm);
  padding: 6px 10px;
  cursor: pointer;
}

.order-card__amount,
.order-card__receiver,
.order-card__actions {
  border-left: 1px solid var(--color-border-light);
  padding-left: var(--spacing-md);
}

.order-card__amount {
  text-align: right;
}

.order-card__amount-label,
.order-card__freight {
  display: block;
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.order-card__amount-value {
  display: block;
  margin: 6px 0;
  font-size: 20px;
}

.order-card__receiver-name {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.order-card__receiver p {
  margin: 8px 0 0;
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
  line-height: 1.5;
}

.order-card__actions {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

.order-card__actions :deep(.el-button + .el-button) {
  margin-left: 0;
}

.order-card__note {
  margin: 0 var(--spacing-md) var(--spacing-md) 104px;
  padding: 9px 12px;
  background: var(--color-bg-light);
  border-radius: var(--radius-sm);
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.order-card__note span {
  color: var(--color-accent);
  margin-right: 8px;
  font-weight: 700;
}

.order-card__note--signed span {
  color: var(--color-primary);
}

.order-card__note-action {
  margin-left: 10px;
  font-weight: 700;
}

@media (max-width: 900px) {
  .order-card__head {
    align-items: flex-start;
    flex-direction: column;
  }

  .order-card__main {
    grid-template-columns: 1fr;
  }

  .order-card__amount,
  .order-card__receiver,
  .order-card__actions {
    border-left: 0;
    border-top: 1px solid var(--color-border-light);
    padding: 12px 0 0;
    text-align: left;
  }

  .order-card__note {
    margin-left: var(--spacing-md);
  }
}
</style>
