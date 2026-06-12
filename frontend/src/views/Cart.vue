<template>
  <div class="cart-container">
    <!-- 空购物车 -->
    <div class="cart-empty" v-if="cartStore.cartList.length === 0">
      <el-empty description="购物车是空的，快去选购商品吧">
        <el-button type="primary" @click="router.push('/shop')">去购物</el-button>
      </el-empty>
    </div>

    <!-- 购物车列表 -->
    <template v-else>
      <div class="cart-header">
        <h2>我的购物车 <span class="cart-count">（{{ cartStore.cartList.length }} 件商品）</span></h2>
        <el-button type="danger" text @click="handleDeleteSelected" :disabled="!cartStore.hasSelected">
          <el-icon><Delete /></el-icon> 删除选中
        </el-button>
      </div>

      <div class="cart-list" v-loading="cartStore.loading">
        <div v-for="item in cartStore.cartList" :key="item.id" class="cart-item">
          <el-checkbox :model-value="item.selected === 1" @change="(val) => handleSelectItem(item, val)" />

          <img
            :src="getImageUrl(item.productImage)"
            class="cart-item-image"
            @click="router.push(`/product/${item.productId}`)"
          />

          <div class="cart-item-info">
            <h4 class="cart-item-name" @click="router.push(`/product/${item.productId}`)">
              {{ item.productName }}
            </h4>
            <el-tag v-if="item.productStatus !== 1" type="danger" size="small">已下架</el-tag>
          </div>

          <div class="cart-item-price">¥{{ item.price }}</div>

          <el-input-number
            :model-value="item.quantity"
            :min="1"
            :max="item.productStock"
            size="small"
            @change="(val) => handleUpdateQuantity(item, val)"
          />

          <div class="cart-item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>

          <el-button type="danger" link @click="handleDeleteItem(item)">
            <el-icon :size="18"><Delete /></el-icon>
          </el-button>
        </div>
      </div>

      <div class="cart-footer">
        <div class="footer-left">
          <el-checkbox :model-value="cartStore.selectAll" @change="handleSelectAll">全选</el-checkbox>
        </div>
        <div class="footer-right">
          <span class="selected-info">已选择 <em>{{ cartStore.selectedCount }}</em> 件商品</span>
          <span class="total-price">合计：<em>¥{{ cartStore.totalPrice.toFixed(2) }}</em></span>
          <el-button type="primary" size="large" @click="goToCheckout" :disabled="cartStore.selectedCount === 0">
            去结算
          </el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import { useCartStore } from '@/store/cart'
import { imageUtils } from '@/utils/imageUtils'

const router = useRouter()
const cartStore = useCartStore()

const getImageUrl = (url) => imageUtils.getImageUrl(url)

const handleSelectItem = async (row, selected) => {
  await cartStore.updateSelected(row.id, selected ? 1 : 0)
}

const handleSelectAll = async (selected) => {
  await cartStore.updateAllSelected(selected ? 1 : 0)
}

const handleUpdateQuantity = async (row, quantity) => {
  if (quantity < 1 || quantity > row.productStock) {
    ElMessage.warning(`数量必须在1-${row.productStock}之间`)
    return
  }
  await cartStore.updateQuantity(row.id, quantity)
}

const handleDeleteItem = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.deleteItem(row.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const handleDeleteSelected = async () => {
  try {
    await ElMessageBox.confirm('确定要删除选中的商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.deleteSelected()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

const goToCheckout = () => {
  router.push('/checkout')
}

onMounted(() => {
  cartStore.init()
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

/* ── Cart Items ── */
.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px 20px;
  margin-bottom: 12px;
  border: 1px solid var(--color-border-light);
  transition: box-shadow var(--transition-fast);
}

.cart-item:hover {
  box-shadow: var(--shadow-md);
}

.cart-item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  cursor: pointer;
  flex-shrink: 0;
  transition: transform var(--transition-fast);
}

.cart-item-image:hover {
  transform: scale(1.05);
}

.cart-item-info {
  flex: 1;
  min-width: 0;
}

.cart-item-name {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
  margin: 0 0 4px;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-item-name:hover {
  color: var(--color-primary);
}

.cart-item-price {
  font-size: 14px;
  color: var(--color-text-secondary);
  width: 80px;
  text-align: center;
  flex-shrink: 0;
}

.cart-item-subtotal {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-price);
  width: 100px;
  text-align: right;
  flex-shrink: 0;
}

/* ── Footer ── */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: var(--radius-md);
  padding: 20px 24px;
  border: 1px solid var(--color-border-light);
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.selected-info {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.selected-info em {
  color: var(--color-primary);
  font-style: normal;
  font-weight: bold;
}

.total-price {
  font-size: 14px;
  color: var(--color-text-primary);
}

.total-price em {
  font-size: 24px;
  color: var(--color-price);
  font-style: normal;
  font-weight: bold;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .cart-item {
    flex-wrap: wrap;
    gap: 10px;
  }

  .cart-item-info {
    flex: 1 1 calc(100% - 110px);
  }

  .cart-item-price {
    width: auto;
  }

  .cart-item-subtotal {
    width: auto;
    text-align: left;
  }

  .cart-footer {
    flex-direction: column;
    gap: 12px;
  }

  .footer-right {
    flex-wrap: wrap;
    justify-content: flex-end;
    width: 100%;
  }
}
</style>
