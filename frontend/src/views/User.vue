<template>
  <BasePage title="用户列表">
    <template #actions>
      <BaseButton v-if="isAdmin" type="primary" @click="openCreateUser">
        <el-icon><Plus /></el-icon>
        添加用户
      </BaseButton>
    </template>

    <BaseToolbar :model="searchForm" @search="handleSearch" @reset="handleReset">
      <BaseFormItem label="关键词">
        <BaseInput v-model="searchForm.keyword" placeholder="用户名/昵称" clearable @keyup.enter="handleSearch" />
      </BaseFormItem>
    </BaseToolbar>

    <BaseTable :data="sortedUsers" v-loading="loading" stripe>
      <BaseTableColumn prop="id" label="ID" width="70" />
      <BaseTableColumn prop="username" label="用户名" min-width="120" />
      <BaseTableColumn prop="nickname" label="昵称" min-width="120" />
      <BaseTableColumn prop="phone" label="手机号" min-width="130" />
      <BaseTableColumn prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <BaseTableColumn label="角色" width="100">
        <template #default="{ row }">
          <BaseTag :type="row.role === 1 ? 'danger' : 'info'">{{ row.role === 1 ? '管理员' : '普通用户' }}</BaseTag>
        </template>
      </BaseTableColumn>
      <BaseTableColumn label="状态" width="100">
        <template #default="{ row }">
          <BaseSwitch
            v-if="isAdmin"
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
          <StatusTag v-else :value="row.status" :options="ENABLE_STATUS_OPTIONS" />
        </template>
      </BaseTableColumn>
      <BaseTableColumn label="创建时间" min-width="160">
        <template #default="{ row }">
          <span class="time-cell">{{ formatTime(row.createTime) }}</span>
        </template>
      </BaseTableColumn>
      <BaseTableColumn label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <BaseTableActions
            v-if="isAdmin"
            :delete-disabled="row.username === 'admin'"
            @edit="openEditUser(row)"
            @delete="deleteUserRow(row)"
          />
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

    <BaseDialogForm
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <UserForm ref="userFormRef" :model-value="form" :is-edit="isEdit" />
    </BaseDialogForm>
  </BasePage>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, updateUserStatus } from '@/api/user'
import { formatTime } from '@/utils/time'
import { ENABLE_STATUS_OPTIONS } from '@/constants/status'
import { useCrudPage } from '@/composables/useCrudPage'
import { usePermission } from '@/composables/usePermission'
import BasePage from '@/components/base/BasePage.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseSwitch from '@/components/base/BaseSwitch.vue'
import BaseTag from '@/components/base/BaseTag.vue'
import BaseToolbar from '@/components/base/BaseToolbar.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import BaseTableActions from '@/components/base/BaseTableActions.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseTableColumn from '@/components/base/BaseTableColumn.vue'
import BaseDialogForm from '@/components/base/BaseDialogForm.vue'
import StatusTag from '@/components/base/StatusTag.vue'
import UserForm from '@/components/admin/UserForm.vue'
import { notify } from '@/services/uiFeedback'

const { isAdmin } = usePermission()
const dialogVisible = ref(false)
const submitLoading = ref(false)
const userFormRef = ref(null)
const isEdit = ref(false)
const dialogTitle = ref('添加用户')

const defaultForm = () => ({
  id: null,
  username: '',
  nickname: '',
  password: '',
  phone: '',
  email: '',
  role: 0
})

const form = ref(defaultForm())

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
  handleDelete
} = useCrudPage({
  fetchPage: getUserList,
  remove: deleteUser,
  searchDefaults: { keyword: '' },
  deleteMessage: '确定要删除该用户吗？'
})

const sortedUsers = computed(() => [...tableData.value].sort((a, b) => {
  if (a.username === 'admin') return -1
  if (b.username === 'admin') return 1
  return 0
}))

const openCreateUser = () => {
  if (!isAdmin.value) {
    notify.warning('没有权限执行此操作')
    return
  }
  isEdit.value = false
  dialogTitle.value = '添加用户'
  form.value = defaultForm()
  dialogVisible.value = true
}

const openEditUser = (row) => {
  if (!isAdmin.value) {
    notify.warning('没有权限执行此操作')
    return
  }
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  form.value = {
    id: row.id,
    username: row.username,
    nickname: row.nickname,
    password: '',
    phone: row.phone,
    email: row.email,
    role: row.role
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await userFormRef.value?.validate?.().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = { ...form.value }
    if (isEdit.value && !payload.password) {
      delete payload.password
    }
    if (isEdit.value) {
      await updateUser(payload)
      notify.success('更新成功')
    } else {
      await addUser(payload)
      notify.success('添加成功')
    }
    dialogVisible.value = false
    await loadData()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const deleteUserRow = async (row) => {
  if (!isAdmin.value) {
    notify.warning('没有权限执行此操作')
    return
  }
  if (row.username === 'admin') {
    notify.warning('不能删除管理员账号')
    return
  }
  try {
    await handleDelete(row, () => notify.success('删除成功'))
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleStatusChange = async (row) => {
  if (row.username === 'admin') {
    notify.warning('不能禁用管理员账号')
    row.status = 1
    return
  }
  try {
    await updateUserStatus(row.id, row.status)
    notify.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
  }
}

onMounted(loadData)
</script>
