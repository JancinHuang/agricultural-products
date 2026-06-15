<template>
  <AdminCrudShell
    title="分类列表"
    :search-model="searchForm"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #actions>
      <BaseButton type="primary" @click="openCreateCategory">
        <el-icon><Plus /></el-icon>
        添加分类
      </BaseButton>
    </template>

    <template #search>
      <BaseFormItem label="关键词">
        <BaseInput v-model="searchForm.keyword" placeholder="分类名称" clearable @keyup.enter="handleSearch" />
      </BaseFormItem>
    </template>

    <AdminDataTable
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :data="tableData"
      :loading="loading"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    >
      <BaseTableColumn prop="id" label="ID" width="80" />
      <BaseTableColumn label="图标" width="90">
        <template #default="{ row }">
          <img v-if="row.icon" :src="row.iconUrl" class="category-thumb" @click="previewImage(row.iconUrl)" />
          <span v-else class="empty-icon">{{ getCategoryEmoji(row.name) }}</span>
        </template>
      </BaseTableColumn>
      <BaseTableColumn prop="name" label="分类名称" min-width="130" />
      <BaseTableColumn prop="description" label="描述" min-width="180" show-overflow-tooltip />
      <BaseTableColumn prop="sort" label="排序" width="90" />
      <BaseTableColumn label="状态" width="100">
        <template #default="{ row }">
          <StatusTag :value="row.status" :options="ENABLE_STATUS_OPTIONS" />
        </template>
      </BaseTableColumn>
      <BaseTableColumn label="创建时间" width="170">
        <template #default="{ row }">
          <span class="time-cell">{{ formatTime(row.createTime) }}</span>
        </template>
      </BaseTableColumn>
      <BaseTableColumn label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <BaseTableActions @edit="openEditCategory(row)" @delete="deleteCategoryRow(row)" />
        </template>
      </BaseTableColumn>
    </AdminDataTable>

    <BaseDialogForm
      v-model="dialogVisible"
      :title="dialogTitle"
      width="560px"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <CategoryForm
        ref="categoryFormRef"
        :model-value="form"
        :icon-preview-url="iconPreviewUrl"
        @icon-uploaded="handleIconUploaded"
        @remove-icon="removeIcon"
      />
    </BaseDialogForm>

    <BaseImagePreview v-model="previewVisible" :src="previewUrl" title="图标预览" width="360px" />
  </AdminCrudShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getCategoryPage, addCategory, updateCategory, deleteCategory } from '@/api/category'
import { formatTime } from '@/utils/time'
import { ENABLE_STATUS_OPTIONS } from '@/constants/status'
import { normalizeCategory } from '@/services/domainAdapters'
import { useCrudPage } from '@/composables/useCrudPage'
import { useDialogForm } from '@/composables/useDialogForm'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseTableActions from '@/components/base/BaseTableActions.vue'
import BaseTableColumn from '@/components/base/BaseTableColumn.vue'
import BaseImagePreview from '@/components/base/BaseImagePreview.vue'
import BaseDialogForm from '@/components/base/BaseDialogForm.vue'
import StatusTag from '@/components/base/StatusTag.vue'
import AdminCrudShell from '@/components/admin/AdminCrudShell.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import CategoryForm from '@/components/admin/CategoryForm.vue'
import { notify } from '@/services/uiFeedback'

const iconPreviewUrl = ref('')
const previewVisible = ref(false)
const previewUrl = ref('')

const defaultForm = () => ({
  id: null,
  name: '',
  description: '',
  icon: '',
  sort: 0,
  status: 1
})

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
  fetchPage: getCategoryPage,
  remove: deleteCategory,
  searchDefaults: { keyword: '' },
  normalize: normalizeCategory,
  deleteMessage: '确定要删除该分类吗？'
})

const {
  visible: dialogVisible,
  submitLoading,
  formRef: categoryFormRef,
  form,
  dialogTitle,
  openCreate: openCreateDialog,
  openEdit: openEditDialog,
  submit: submitDialog
} = useDialogForm({
  defaults: defaultForm,
  normalize: row => ({
    id: row.id,
    name: row.name,
    description: row.description,
    icon: row.icon,
    sort: row.sort,
    status: row.status
  }),
  submitCreate: addCategory,
  submitUpdate: updateCategory,
  title: {
    create: '添加分类',
    edit: '编辑分类'
  }
})

const getCategoryEmoji = (name) => {
  if (!name) return '🌱'
  const map = {
    水果: '🍎',
    蔬菜: '🥬',
    粮食: '🌾',
    禽蛋: '🥚',
    水产: '🐟',
    肉: '🥩',
    干货: '🫙',
    调味: '🧂'
  }
  for (const [key, emoji] of Object.entries(map)) {
    if (name.includes(key)) return emoji
  }
  return '🌱'
}

const openCreateCategory = () => {
  iconPreviewUrl.value = ''
  openCreateDialog()
}

const openEditCategory = (row) => {
  iconPreviewUrl.value = row.iconUrl || ''
  openEditDialog(row)
}

const handleIconUploaded = (result) => {
  form.icon = result.objectKey
  iconPreviewUrl.value = result.url
  notify.success('上传成功')
}

const removeIcon = () => {
  form.icon = ''
  iconPreviewUrl.value = ''
}

const handleSubmit = async () => {
  try {
    await submitDialog(null, async (payload, isEditMode) => {
      notify.success(isEditMode ? '更新成功' : '添加成功')
      await loadData()
    })
  } catch (error) {
    console.error(error)
  }
}

const deleteCategoryRow = async (row) => {
  try {
    await handleDelete(row, () => notify.success('删除成功'))
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

onMounted(loadData)
</script>

<style scoped>
.empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  background: var(--color-primary-bg);
  font-size: 22px;
}
</style>
