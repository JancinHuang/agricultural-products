<template>
  <BaseDialog v-model="innerVisible" title="订单详情" width="820px" class="order-detail-dialog">
    <div v-if="order" class="order-detail">
      <div class="order-detail__summary">
        <div class="order-detail__head">
          <div class="order-detail__title">
            <span>订单编号</span>
            <strong>{{ order.orderNo }}</strong>
          </div>
          <StatusTag :value="order.status" :text="getStatusText(order.status)" :type="getStatusType(order.status)" />
        </div>

        <div class="order-detail__grid">
          <div class="order-detail__item">
            <span>用户</span>
            <strong>{{ order.userName || '-' }}</strong>
          </div>
          <div class="order-detail__item">
            <span>订单金额</span>
            <PriceText :value="order.totalAmount" />
          </div>
          <div class="order-detail__item">
            <span>收货人</span>
            <strong>{{ order.receiverName || '-' }}</strong>
          </div>
          <div class="order-detail__item">
            <span>联系电话</span>
            <strong>{{ order.receiverPhone || '-' }}</strong>
          </div>
          <div class="order-detail__item order-detail__item--wide">
            <span>收货地址</span>
            <strong>{{ order.receiverAddress || '-' }}</strong>
          </div>
          <div class="order-detail__item order-detail__item--wide">
            <span>备注</span>
            <strong>{{ order.remark || '-' }}</strong>
          </div>
          <div class="order-detail__item order-detail__item--wide">
            <span>创建时间</span>
            <strong>{{ formatTime(order.createTime) }}</strong>
          </div>
        </div>
      </div>

      <section v-if="items.length" class="order-detail__products">
        <div class="order-detail__section-title">
          <h4>商品明细</h4>
          <span>共 {{ items.length }} 件</span>
        </div>
        <div class="order-detail__product-list">
          <div v-for="item in items" :key="item.id" class="order-detail__product-row">
            <img
              :src="getThumbUrl(item.productImage)"
              class="order-detail__thumb"
              :alt="item.productName"
            />
            <div class="order-detail__product-info">
              <div class="order-detail__product-name">{{ item.productName }}</div>
              <div class="order-detail__product-meta">商品ID：{{ item.productId }}</div>
            </div>
            <PriceText :value="item.price" class="order-detail__number" />
            <div class="order-detail__number">x{{ item.quantity }}</div>
            <PriceText :value="item.subtotal" class="order-detail__number order-detail__subtotal" />
          </div>
        </div>
      </section>
    </div>
  </BaseDialog>
</template>

<script setup>
import { computed } from 'vue'
import BaseDialog from '@/components/base/BaseDialog.vue'
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
  }
})

const emit = defineEmits(['update:modelValue'])

const innerVisible = computed({
  get: () => props.modelValue,
  set: value => emit('update:modelValue', value)
})

const order = computed(() => props.detail?.order || null)
const items = computed(() => props.detail?.items || [])
const getThumbUrl = url => imageUtils.getImageUrl(url, { width: 80, height: 80, quality: 75 })
</script>

<style scoped>
.order-detail__products {
  margin-top: var(--spacing-lg);
}

.order-detail__summary {
  overflow: hidden;
  background: var(--color-white);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
}

.order-detail__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  background: var(--color-bg-light);
  border-bottom: 1px solid var(--color-border-light);
}

.order-detail__title {
  min-width: 0;
}

.order-detail__title span,
.order-detail__item span {
  display: block;
  margin-bottom: 4px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.order-detail__title strong,
.order-detail__item strong {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.order-detail__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.order-detail__item {
  min-width: 0;
  padding: var(--spacing-md);
  border-right: 1px solid var(--color-border-light);
  border-bottom: 1px solid var(--color-border-light);
}

.order-detail__item:nth-child(2n),
.order-detail__item--wide {
  border-right: 0;
}

.order-detail__item--wide {
  grid-column: 1 / -1;
}

.order-detail__section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--spacing-sm);
}

.order-detail__section-title h4 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: var(--font-size-md);
  font-weight: 700;
}

.order-detail__section-title span {
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.order-detail__product-list {
  overflow: hidden;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
}

.order-detail__product-row {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr) 90px 64px 100px;
  gap: var(--spacing-md);
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  background: var(--color-white);
  border-bottom: 1px solid var(--color-border-light);
}

.order-detail__product-row:last-child {
  border-bottom: 0;
}

.order-detail__thumb {
  width: 56px;
  height: 56px;
  display: block;
  object-fit: cover;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  background: var(--color-bg-light);
}

.order-detail__product-info {
  min-width: 0;
}

.order-detail__product-name {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.order-detail__product-meta {
  margin-top: 4px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-xs);
}

.order-detail__number {
  color: var(--color-text-regular);
  font-size: var(--font-size-sm);
  text-align: right;
  white-space: nowrap;
}

.order-detail__subtotal {
  font-weight: 700;
}

:deep(.order-detail-dialog .el-dialog__body) {
  padding-top: var(--spacing-sm);
}

@media (max-width: 820px) {
  .order-detail__grid {
    grid-template-columns: 1fr;
  }

  .order-detail__item {
    border-right: 0;
  }

  .order-detail__product-row {
    grid-template-columns: 56px minmax(0, 1fr);
  }

  .order-detail__number {
    grid-column: 2;
    text-align: left;
  }
}
</style>
