<template>
  <div class="checkout-page">
    <div class="checkout-wrapper">
      <div class="page-header">
        <h2>确认订单</h2>
      </div>

      <div class="checkout-body" v-loading="loading">
        <CheckoutAddressForm ref="addressFormRef" :model-value="orderForm" />
        <CheckoutItemsPanel :items="selectedItems" />
      </div>

      <CheckoutSummaryBar
        :total-count="totalCount"
        :total-price="totalPrice"
        :submitting="submitting"
        @back="goBack"
        @submit="submitOrder"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import CheckoutAddressForm from '@/components/shop/CheckoutAddressForm.vue'
import CheckoutItemsPanel from '@/components/shop/CheckoutItemsPanel.vue'
import CheckoutSummaryBar from '@/components/shop/CheckoutSummaryBar.vue'
import { getCartList } from '@/api/cart'
import { createOrder } from '@/api/order'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const addressFormRef = ref(null)
const selectedItems = ref([])

const orderForm = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  remark: ''
})

const totalCount = computed(() => selectedItems.value.reduce((sum, item) => sum + item.quantity, 0))
const totalPrice = computed(() => selectedItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0))

const loadSelectedItems = async () => {
  loading.value = true
  try {
    const res = await getCartList()
    selectedItems.value = (res.data || []).filter(item => item.selected === 1)

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
  const valid = await addressFormRef.value?.validate?.().catch(() => false)
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
  padding: 24px 24px 100px;
}

.page-header {
  margin-bottom: var(--spacing-xl);
}

.page-header h2 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 24px;
  font-weight: 700;
}

@media (max-width: 768px) {
  .checkout-wrapper {
    padding: var(--spacing-md);
    padding-bottom: 100px;
  }
}
</style>
