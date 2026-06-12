<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="订单编号" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 130px">
            <el-option label="待付款" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="待收货" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单编号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            ¥{{ formatAmount(row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收货人" width="110" />
        <el-table-column prop="receiverPhone" label="联系电话" width="140" />
        <el-table-column prop="receiverAddress" label="收货地址" min-width="180" show-overflow-tooltip />
        <el-table-column label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleView(row)">详情</el-button>
            <el-button
              v-if="row.status < 3"
              type="success"
              link
              @click="handleNextStatus(row)"
            >
              {{ getNextStatusText(row.status) }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="订单详情" width="820px" class="order-detail-dialog">
      <div v-if="orderDetail">
        <div class="detail-summary">
          <div class="summary-main">
            <div class="summary-title">
              <span>订单编号</span>
              <strong>{{ orderDetail.orderNo }}</strong>
            </div>
            <el-tag :type="getStatusType(orderDetail.status)" effect="plain">
              {{ getStatusText(orderDetail.status) }}
            </el-tag>
          </div>
          <div class="summary-grid">
            <div class="summary-item">
              <span>用户</span>
              <strong>{{ orderDetail.userName || '-' }}</strong>
            </div>
            <div class="summary-item">
              <span>订单金额</span>
              <strong class="price">¥{{ formatAmount(orderDetail.totalAmount) }}</strong>
            </div>
            <div class="summary-item">
              <span>收货人</span>
              <strong>{{ orderDetail.receiverName || '-' }}</strong>
            </div>
            <div class="summary-item">
              <span>联系电话</span>
              <strong>{{ orderDetail.receiverPhone || '-' }}</strong>
            </div>
            <div class="summary-item wide">
              <span>收货地址</span>
              <strong>{{ orderDetail.receiverAddress || '-' }}</strong>
            </div>
            <div class="summary-item wide">
              <span>备注</span>
              <strong>{{ orderDetail.remark || '-' }}</strong>
            </div>
            <div class="summary-item wide">
              <span>创建时间</span>
              <strong>{{ formatTime(orderDetail.createTime) }}</strong>
            </div>
          </div>
        </div>

        <section class="detail-products" v-if="orderItems.length">
          <div class="section-title">
            <h4>商品明细</h4>
            <span>共 {{ orderItems.length }} 件</span>
          </div>
          <div class="product-list">
            <div v-for="item in orderItems" :key="item.id" class="product-row">
              <img
                :src="getThumbUrl(item.productImage)"
                class="product-thumb"
                :alt="item.productName"
              />
              <div class="product-info">
                <div class="product-name">{{ item.productName }}</div>
                <div class="product-meta">商品ID：{{ item.productId }}</div>
              </div>
              <div class="product-price">¥{{ formatAmount(item.price) }}</div>
              <div class="product-quantity">x{{ item.quantity }}</div>
              <div class="product-subtotal">¥{{ formatAmount(item.subtotal) }}</div>
            </div>
          </div>
        </section>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getOrderPage, deleteOrder, updateOrderStatus, getOrderById } from '@/api/order'
import { formatTime } from '@/utils/time'
import { getStatusText, getStatusType, getNextStatusText } from '@/utils/orderStatus'
import { imageUtils } from '@/utils/imageUtils'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const orderDetailResponse = ref(null)

const searchForm = reactive({
  keyword: '',
  status: null
})

const orderDetail = computed(() => orderDetailResponse.value?.order || null)
const orderItems = computed(() => orderDetailResponse.value?.items || [])
const formatAmount = value => Number(value || 0).toFixed(2)
const getThumbUrl = url => imageUtils.getImageUrl(url, { width: 80, height: 80, quality: 75 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getOrderPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    tableData.value = res.data.list
    total.value = res.data.total
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  pageNum.value = 1
  loadData()
}

const handleView = async (row) => {
  try {
    const res = await getOrderById(row.id)
    orderDetailResponse.value = res.data
    dialogVisible.value = true
  } catch (error) {
    console.error(error)
  }
}

const handleNextStatus = (row) => {
  const nextStatus = row.status + 1
  const actionText = getNextStatusText(row.status)

  ElMessageBox.confirm(`确定要${actionText}吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await updateOrderStatus(row.id, nextStatus)
    ElMessage.success(`${actionText}成功`)
    loadData()
  }).catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该订单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteOrder(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.detail-products {
  margin-top: 18px;
}

.detail-summary {
  border: 1px solid #e8ece3;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.summary-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  background: #f5f8f1;
  border-bottom: 1px solid #e8ece3;
}

.summary-title {
  min-width: 0;
}

.summary-title span,
.summary-item span {
  display: block;
  margin-bottom: 4px;
  color: #7a8676;
  font-size: 12px;
}

.summary-title strong {
  display: block;
  color: #1f2a1d;
  font-size: 15px;
  overflow-wrap: anywhere;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.summary-item {
  min-width: 0;
  padding: 12px 16px;
  border-right: 1px solid #e8ece3;
  border-bottom: 1px solid #e8ece3;
}

.summary-item:nth-child(2n),
.summary-item.wide {
  border-right: 0;
}

.summary-item.wide {
  grid-column: 1 / -1;
}

.summary-item strong {
  display: block;
  color: #1f2a1d;
  font-size: 14px;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.section-title h4 {
  font-size: 15px;
  font-weight: 700;
  margin: 0;
  color: #1f2a1d;
}

.section-title span {
  color: #7a8676;
  font-size: 12px;
}

.product-list {
  border: 1px solid #e8ece3;
  border-radius: 8px;
  overflow: hidden;
}

.product-row {
  display: grid;
  grid-template-columns: 56px minmax(0, 1fr) 90px 64px 100px;
  gap: 12px;
  align-items: center;
  padding: 10px 12px;
  background: #fff;
  border-bottom: 1px solid #eef2ea;
}

.product-row:last-child {
  border-bottom: 0;
}

.product-thumb {
  width: 56px;
  height: 56px;
  display: block;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e8ece3;
  background: #f6f8f3;
}

.product-info {
  min-width: 0;
}

.product-name {
  color: #1f2a1d;
  font-size: 14px;
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  margin-top: 4px;
  color: #8b9587;
  font-size: 12px;
}

.product-price,
.product-quantity,
.product-subtotal {
  color: #4f5b4b;
  font-size: 13px;
  text-align: right;
  white-space: nowrap;
}

.product-subtotal {
  color: var(--color-price);
  font-weight: 700;
}

.price {
  color: var(--color-price);
  font-weight: 700;
}

:deep(.order-detail-dialog .el-dialog__body) {
  padding-top: 10px;
}

@media (max-width: 820px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .summary-item {
    border-right: 0;
  }

  .product-row {
    grid-template-columns: 56px minmax(0, 1fr);
  }

  .product-price,
  .product-quantity,
  .product-subtotal {
    text-align: left;
    grid-column: 2;
  }
}
</style>
