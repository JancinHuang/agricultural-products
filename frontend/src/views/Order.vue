<template>
  <AdminCrudPage title="订单列表">
    <AdminSearchForm :model="searchForm" @search="handleSearch" @reset="handleReset">
      <BaseFormItem label="关键词">
        <BaseInput v-model="searchForm.keyword" placeholder="订单编号" clearable @keyup.enter="handleSearch" />
      </BaseFormItem>
      <BaseFormItem label="状态">
        <BaseSelect v-model="searchForm.status" placeholder="全部状态" clearable style="width: 130px">
          <BaseOption
            v-for="option in ORDER_STATUS_OPTIONS"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </BaseSelect>
      </BaseFormItem>
    </AdminSearchForm>

    <BaseTable :data="tableData" v-loading="loading" stripe>
      <BaseTableColumn prop="id" label="ID" width="80" />
      <BaseTableColumn prop="orderNo" label="订单编号" min-width="180" show-overflow-tooltip />
      <BaseTableColumn prop="userName" label="用户" width="120" />
      <BaseTableColumn prop="totalAmount" label="订单金额" width="120">
        <template #default="{ row }">
          <PriceText :value="row.totalAmount" />
        </template>
      </BaseTableColumn>
      <BaseTableColumn label="订单状态" width="110">
        <template #default="{ row }">
          <StatusTag :value="row.status" :text="getStatusText(row.status)" :type="getStatusType(row.status)" />
        </template>
      </BaseTableColumn>
      <BaseTableColumn prop="receiverName" label="收货人" width="110" />
      <BaseTableColumn prop="receiverPhone" label="联系电话" width="140" />
      <BaseTableColumn prop="receiverAddress" label="收货地址" min-width="180" show-overflow-tooltip />
      <BaseTableColumn label="创建时间" width="160">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </BaseTableColumn>
      <BaseTableColumn label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <BaseTableActions :show-edit="false" @delete="deleteOrderRow(row)">
            <BaseButton type="primary" link @click="viewOrder(row)">详情</BaseButton>
            <BaseButton
              v-if="row.status < 3"
              type="success"
              link
              @click="advanceOrder(row)"
            >
              {{ getNextStatusText(row.status) }}
            </BaseButton>
          </BaseTableActions>
        </template>
      </BaseTableColumn>
    </BaseTable>

    <BasePagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <OrderDetailDialog v-model="dialogVisible" :detail="orderDetailResponse" />
  </AdminCrudPage>
</template>

<script setup>
import { ref } from 'vue'
import AdminCrudPage from '@/components/admin/AdminCrudPage.vue'
import AdminSearchForm from '@/components/admin/AdminSearchForm.vue'
import OrderDetailDialog from '@/components/admin/OrderDetailDialog.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseOption from '@/components/base/BaseOption.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseTableActions from '@/components/base/BaseTableActions.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseTableColumn from '@/components/base/BaseTableColumn.vue'
import StatusTag from '@/components/base/StatusTag.vue'
import PriceText from '@/components/base/PriceText.vue'
import { getOrderPage, deleteOrder, updateOrderStatus, getOrderById } from '@/api/order'
import { ORDER_STATUS_OPTIONS } from '@/constants/status'
import { normalizeOrder } from '@/services/domainAdapters'
import { useCrudPage } from '@/composables/useCrudPage'
import { formatTime } from '@/utils/time'
import { getStatusText, getStatusType, getNextStatusText } from '@/utils/orderStatus'
import { confirmDanger, notify } from '@/services/uiFeedback'

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
  handleSizeChange,
  handleCurrentChange,
  handleDelete: confirmDelete
} = useCrudPage({
  searchDefaults: {
    keyword: '',
    status: null
  },
  fetchPage: getOrderPage,
  remove: deleteOrder,
  normalize: normalizeOrder,
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
    await confirmDanger(`确定要${actionText}吗？`)
    await updateOrderStatus(row.id, nextStatus)
    notify.success(`${actionText}成功`)
    loadData()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      console.error(error)
    }
  }
}

const deleteOrderRow = async (row) => {
  await confirmDelete(row, () => notify.success('删除成功'))
}
</script>
