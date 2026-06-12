<template>
  <div class="checkout-page">
    <div class="checkout-wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>确认订单</h2>
      </div>

      <div class="checkout-body" v-loading="loading">
        <!-- 收货信息 -->
        <div class="form-panel">
          <div class="panel-title">
            <el-icon><Location /></el-icon>
            <span>收货信息</span>
          </div>
          <el-form :model="orderForm" :rules="rules" ref="formRef" label-width="90px" class="checkout-form">
            <el-form-item label="收货人" prop="receiverName">
              <el-input v-model="orderForm.receiverName" placeholder="请输入收货人姓名" />
            </el-form-item>
            <el-form-item label="联系电话" prop="receiverPhone">
              <el-input v-model="orderForm.receiverPhone" placeholder="请输入联系电话" />
            </el-form-item>
            <el-form-item label="收货地址" prop="receiverAddress">
              <el-input v-model="orderForm.receiverAddress" type="textarea" :rows="2" placeholder="请输入详细地址" />
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="orderForm.remark" placeholder="选填，可以告诉卖家您的特殊需求" />
            </el-form-item>
          </el-form>
        </div>

        <!-- 商品清单 -->
        <div class="items-panel">
          <div class="panel-title">
            <el-icon><Goods /></el-icon>
            <span>商品清单（{{ selectedItems.length }} 件）</span>
          </div>
          <div class="item-list">
            <div v-for="item in selectedItems" :key="item.id" class="order-item">
              <img :src="getImageUrl(item.productImage)" class="item-image" />
              <div class="item-info">
                <span class="item-name">{{ item.productName }}</span>
                <span class="item-price">¥{{ item.price }} × {{ item.quantity }}</span>
              </div>
              <span class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部结算栏 -->
      <div class="checkout-footer">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon> 返回购物车
        </el-button>
        <div class="footer-right">
          <span class="summary-text">共 <em>{{ totalCount }}</em> 件商品</span>
          <span class="summary-price">应付：<em>¥{{ totalPrice.toFixed(2) }}</em></span>
          <el-button type="primary" size="large" @click="submitOrder" :loading="submitting" class="submit-btn">
            提交订单
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, Goods, ArrowLeft } from '@element-plus/icons-vue'
import { getCartList } from '@/api/cart'
import { createOrder } from '@/api/order'
import { imageUtils } from '@/utils/imageUtils'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const selectedItems = ref([])

const getImageUrl = (url) => imageUtils.getImageUrl(url)

const orderForm = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  remark: ''
})

const rules = {
  receiverName: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  receiverPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  receiverAddress: [
    { required: true, message: '请输入收货地址', trigger: 'blur' }
  ]
}

const totalCount = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const totalPrice = computed(() => {
  return selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const loadSelectedItems = async () => {
  loading.value = true
  try {
    const res = await getCartList()
    const allItems = res.data || []
    selectedItems.value = allItems.filter(item => item.selected === 1)

    if (selectedItems.value.length === 0) {
      ElMessage.warning('请先选择要购买的商品')
      router.push('/cart')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/cart')
}

const submitOrder = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createOrder(orderForm)
    ElMessage.success('订单创建成功')
    router.push('/my-order')
  } catch (error) {
    console.error(error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadSelectedItems()
})
</script>

<style scoped>
.checkout-page {
  background: var(--color-bg-page);
  min-height: 100%;
}

.checkout-wrapper {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px;
  padding-bottom: 100px;
}

/* ── 标题 ── */
.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

/* ── 面板公共 ── */
.form-panel,
.items-panel {
  background: #fff;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  margin-bottom: 20px;
  overflow: hidden;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  border-bottom: 1px solid var(--color-border-light);
  background: var(--color-bg-section);
}

.panel-title .el-icon {
  color: var(--color-primary);
}

/* ── 表单 ── */
.checkout-form {
  padding: 20px 20px 4px;
}

.checkout-form :deep(.el-form-item__label) {
  font-weight: 500;
}

/* ── 商品列表 ── */
.item-list {
  padding: 8px 0;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  transition: background var(--transition-fast);
}

.order-item:hover {
  background: var(--color-bg-section);
}

.item-image {
  width: 64px;
  height: 64px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.item-price {
  font-size: 13px;
  color: var(--color-text-placeholder);
}

.item-subtotal {
  font-size: 16px;
  font-weight: 700;
  color: var(--color-price);
  flex-shrink: 0;
  min-width: 80px;
  text-align: right;
}

/* ── 底部结算栏 ── */
.checkout-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px 24px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.summary-text {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.summary-text em {
  font-style: normal;
  font-weight: 600;
  color: var(--color-primary);
}

.summary-price {
  font-size: 14px;
  color: var(--color-text-primary);
}

.summary-price em {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-price);
  font-style: normal;
}

.submit-btn {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .checkout-wrapper {
    padding: 16px;
    padding-bottom: 100px;
  }

  .checkout-footer {
    flex-direction: column;
    gap: 12px;
  }

  .footer-right {
    flex-wrap: wrap;
    justify-content: flex-end;
    width: 100%;
    gap: 12px;
  }
}
</style>
