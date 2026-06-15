<template>
  <div class="cart-container">
    <div class="cart-empty" v-if="cartList.length === 0">
      <EmptyState description="购物车是空的，快去选购商品吧">
        <el-button type="primary" @click="router.push('/shop')">去购物</el-button>
      </EmptyState>
    </div>

    <template v-else>
      <div class="cart-header">
        <h2>我的购物车 <span class="cart-count">（{{ cartList.length }} 件商品）</span></h2>
        <el-button type="danger" text @click="handleDeleteSelected" :disabled="!hasSelected">
          <el-icon><Delete /></el-icon> 删除选中
        </el-button>
      </div>

      <div class="cart-list" v-loading="loading">
        <CartItem
          v-for="item in cartList"
          :key="item.id"
          :item="item"
          @select="handleSelectItem"
          @quantity-change="handleUpdateQuantity"
          @delete="handleDeleteItem"
          @open="openProduct"
        />
      </div>

      <CartSummaryBar
        :select-all="selectAll"
        :selected-count="selectedCount"
        :total-price="totalPrice"
        @select-all="handleSelectAll"
        @checkout="goToCheckout"
      />
    </template>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import CartItem from '@/components/shop/CartItem.vue'
import CartSummaryBar from '@/components/shop/CartSummaryBar.vue'
import EmptyState from '@/components/base/EmptyState.vue'
import { useCartPage } from '@/composables/useCartPage'

const {
  router,
  cartList,
  loading,
  selectedCount,
  totalPrice,
  hasSelected,
  selectAll,
  initCart,
  handleSelectItem,
  handleSelectAll,
  handleUpdateQuantity,
  handleDeleteItem,
  handleDeleteSelected,
  goToCheckout,
  openProduct
} = useCartPage()

onMounted(() => {
  initCart()
})
</script>

<style scoped>
.cart-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 24px;
}

.cart-empty {
  padding: 80px 0;
}

/* ── Header ── */
.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.cart-header h2 {
  font-size: 22px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.cart-count {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-placeholder);
}

</style>
