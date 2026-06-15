<template>
  <AdminCrudPage title="订单列表">
    <AdminSearchForm :model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="订单编号" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option
            v-for="option in ORDER_STATUS_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
      </el-form-item>
    </AdminSearchForm>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="订单编号" min-width="180" show-overflow-tooltip />
      <el-table-column prop="userName" label="用户" width="120" />
      <el-table-column prop="totalAmount" label="订单金额" width="120">
        <template #default="{ row }">
          <PriceText :value="row.totalAmount" />
        </template>
      </el-table-column>
      <el-table-column label="订单状态" width="110">
        <template #default="{ row }">
          <StatusTag :value="row.status" :text="getStatusText(row.status)" :type="getStatusType(row.status)" />
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
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <BaseTableActions :show-edit="false" @delete="deleteOrderRow(row)">
            <el-button type="primary" link @click="viewOrder(row)">详情</el-button>
            <el-button
              v-if="row.status < 3"
              type="success"
              link
              @click="advanceOrder(row)"
            >
              {{ getNextStatusText(row.status) }}
            </el-button>
          </BaseTableActions>
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      @change="loadData"
    />

    <OrderDetailDialog v-model="dialogVisible" :detail="orderDetailResponse" />
  </AdminCrudPage>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminCrudPage from '@/components/admin/AdminCrudPage.vue'
import AdminSearchForm from '@/components/admin/AdminSearchForm.vue'
import OrderDetailDialog from '@/components/admin/OrderDetailDialog.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import BaseTableActions from '@/components/base/BaseTableActions.vue'
import StatusTag from '@/components/base/StatusTag.vue'
import PriceText from '@/components/base/PriceText.vue'
import { getOrderPage, deleteOrder, updateOrderStatus, getOrderById } from '@/api/order'
import { ORDER_STATUS_OPTIONS } from '@/constants/status'
import { withPageDefaults, normalizeOrder } from '@/services/domainAdapters'
import { useCrudPage } from '@/composables/useCrudPage'
import { formatTime } from '@/utils/time'
import { getStatusText, getStatusType, getNextStatusText } from '@/utils/orderStatus'

const dialogVisible = ref(false)
const orderDetailResponse = ref(null)

const {
  loading,
  tableData,
  total,
  pageNum,
  pageSize,
  searchForm,
  loadData,
  handleSearch,
  handleReset,
  handleDelete: confirmDelete
} = useCrudPage({
  searchDefaults: {
    keyword: '',
    status: null
  },
  fetcher: async ({ pageNum, pageSize, searchForm }) => {
    const res = await getOrderPage({
      pageNum,
      pageSize,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    const page = withPageDefaults(res.data)
    return {
      list: page.list.map(normalizeOrder),
      total: page.total
    }
  },
  deleteFn: row => deleteOrder(row.id),
  deleteMessage: '确定要删除该订单吗？'
})

const viewOrder = async (row) => {
  const res = await getOrderById(row.id)
  orderDetailResponse.value = res.data
  dialogVisible.value = true
}

const advanceOrder = async (row) => {
  const nextStatus = row.status + 1
  const actionText = getNextStatusText(row.status)

  try {
    await ElMessageBox.confirm(`确定要${actionText}吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateOrderStatus(row.id, nextStatus)
    ElMessage.success(`${actionText}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error(error)
    }
  }
}

const deleteOrderRow = async (row) => {
  await confirmDelete(row, () => ElMessage.success('删除成功'))
}
</script>
