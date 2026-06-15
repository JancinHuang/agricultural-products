<template>
  <BasePage title="用户列表">
    <template #actions>
      <el-button v-if="isAdmin" type="primary" @click="openCreateUser">
        <el-icon><Plus /></el-icon>
        添加用户
      </el-button>
    </template>

    <BaseToolbar :model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="用户名/昵称" clearable @keyup.enter="handleSearch" />
      </el-form-item>
    </BaseToolbar>

    <el-table :data="sortedUsers" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="130" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : 'info'">{{ row.role === 1 ? '管理员' : '普通用户' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            v-if="isAdmin"
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
          />
          <StatusTag v-else :value="row.status" :options="ENABLE_STATUS_OPTIONS" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="160">
        <template #default="{ row }">
          <span class="time-cell">{{ formatTime(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <BaseTableActions
            v-if="isAdmin"
            :delete-disabled="row.username === 'admin'"
            @edit="openEditUser(row)"
            @delete="deleteUserRow(row)"
          />
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <UserForm ref="userFormRef" :model-value="form" :is-edit="isEdit" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </BasePage>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, updateUserStatus } from '@/api/user'
import { formatTime } from '@/utils/time'
import { ENABLE_STATUS_OPTIONS } from '@/constants/status'
import { useCrudPage } from '@/composables/useCrudPage'
import { usePermission } from '@/composables/usePermission'
import BasePage from '@/components/base/BasePage.vue'
import BaseToolbar from '@/components/base/BaseToolbar.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import BaseTableActions from '@/components/base/BaseTableActions.vue'
import StatusTag from '@/components/base/StatusTag.vue'
import UserForm from '@/components/admin/UserForm.vue'

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
    ElMessage.warning('没有权限执行此操作')
    return
  }
  isEdit.value = false
  dialogTitle.value = '添加用户'
  form.value = defaultForm()
  dialogVisible.value = true
}

const openEditUser = (row) => {
  if (!isAdmin.value) {
    ElMessage.warning('没有权限执行此操作')
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
      ElMessage.success('更新成功')
    } else {
      await addUser(payload)
      ElMessage.success('添加成功')
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
    ElMessage.warning('没有权限执行此操作')
    return
  }
  if (row.username === 'admin') {
    ElMessage.warning('不能删除管理员账号')
    return
  }
  try {
    await handleDelete(row, () => ElMessage.success('删除成功'))
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const handleStatusChange = async (row) => {
  if (row.username === 'admin') {
    ElMessage.warning('不能禁用管理员账号')
    row.status = 1
    return
  }
  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
  }
}

onMounted(loadData)
</script>
