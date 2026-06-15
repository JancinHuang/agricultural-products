<template>
  <BasePage title="分类列表">
    <template #actions>
      <BaseButton type="primary" @click="openCreateCategory">
        <el-icon><Plus /></el-icon>
        添加分类
      </BaseButton>
    </template>

    <BaseToolbar :model="searchForm" @search="handleSearch" @reset="handleReset">
      <BaseFormItem label="关键词">
        <BaseInput v-model="searchForm.keyword" placeholder="分类名称" clearable @keyup.enter="handleSearch" />
      </BaseFormItem>
    </BaseToolbar>

    <BaseTable :data="tableData" v-loading="loading" stripe>
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
  </BasePage>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getCategoryPage, addCategory, updateCategory, deleteCategory } from '@/api/category'
import { formatTime } from '@/utils/time'
import { ENABLE_STATUS_OPTIONS } from '@/constants/status'
import { normalizeCategory } from '@/services/domainAdapters'
import { useCrudPage } from '@/composables/useCrudPage'
import BasePage from '@/components/base/BasePage.vue'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseToolbar from '@/components/base/BaseToolbar.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import BaseTableActions from '@/components/base/BaseTableActions.vue'
import BaseTable from '@/components/base/BaseTable.vue'
import BaseTableColumn from '@/components/base/BaseTableColumn.vue'
import BaseImagePreview from '@/components/base/BaseImagePreview.vue'
import BaseDialogForm from '@/components/base/BaseDialogForm.vue'
import StatusTag from '@/components/base/StatusTag.vue'
import CategoryForm from '@/components/admin/CategoryForm.vue'
import { notify } from '@/services/uiFeedback'

const dialogVisible = ref(false)
const submitLoading = ref(false)
const categoryFormRef = ref(null)
const iconPreviewUrl = ref('')
const previewVisible = ref(false)
const previewUrl = ref('')
const isEdit = ref(false)
const dialogTitle = ref('添加分类')

const defaultForm = () => ({
  id: null,
  name: '',
  description: '',
  icon: '',
  sort: 0,
  status: 1
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
  fetchPage: getCategoryPage,
  remove: deleteCategory,
  searchDefaults: { keyword: '' },
  normalize: normalizeCategory,
  deleteMessage: '确定要删除该分类吗？'
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
  isEdit.value = false
  dialogTitle.value = '添加分类'
  iconPreviewUrl.value = ''
  form.value = defaultForm()
  dialogVisible.value = true
}

const openEditCategory = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑分类'
  iconPreviewUrl.value = row.iconUrl || ''
  form.value = {
    id: row.id,
    name: row.name,
    description: row.description,
    icon: row.icon,
    sort: row.sort,
    status: row.status
  }
  dialogVisible.value = true
}

const handleIconUploaded = (result) => {
  form.value.icon = result.objectKey
  iconPreviewUrl.value = result.url
  notify.success('上传成功')
}

const removeIcon = () => {
  form.value.icon = ''
  iconPreviewUrl.value = ''
}

const handleSubmit = async () => {
  const valid = await categoryFormRef.value?.validate?.().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCategory({ ...form.value })
      notify.success('更新成功')
    } else {
      await addCategory({ ...form.value })
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
