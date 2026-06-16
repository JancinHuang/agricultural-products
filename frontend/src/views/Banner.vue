<template>
  <AdminCrudShell
    title="轮播图列表"
    :search-model="searchForm"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #actions>
      <BaseButton type="primary" @click="openCreateBanner">
        <el-icon><Plus /></el-icon>
        添加轮播图
      </BaseButton>
    </template>

    <template #search>
      <BaseFormItem label="关键词">
        <BaseInput v-model="searchForm.keyword" placeholder="标题" clearable @keyup.enter="handleSearch" />
      </BaseFormItem>
      <BaseFormItem label="状态">
        <BaseSelect v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
          <BaseOption v-for="item in ENABLE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </BaseSelect>
      </BaseFormItem>
    </template>

    <AdminDataTable
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :data="tableData"
      :loading="loading"
      :total="total"
      :page-sizes="[10, 20, 50]"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    >
      <BaseTableColumn prop="id" label="ID" width="70" />
      <BaseTableColumn label="图片" width="150">
        <template #default="{ row }">
          <img v-if="row.image" :src="row.imageUrl" class="banner-thumb" @click="previewImage(row.imageUrl)" />
          <span v-else>-</span>
        </template>
      </BaseTableColumn>
      <BaseTableColumn prop="title" label="标题" min-width="140" />
      <BaseTableColumn label="标题显示" width="100">
        <template #default="{ row }">
          <StatusTag
            :value="row.showTitle"
            :options="[
              { label: '显示', value: 1, type: 'success' },
              { label: '隐藏', value: 0, type: 'info' }
            ]"
          />
        </template>
      </BaseTableColumn>
      <BaseTableColumn prop="subtitle" label="副标题" min-width="220" show-overflow-tooltip />
      <BaseTableColumn prop="buttonText" label="按钮" width="100" />
      <BaseTableColumn prop="linkUrl" label="跳转链接" min-width="140" show-overflow-tooltip />
      <BaseTableColumn prop="sort" label="排序" width="80" />
      <BaseTableColumn label="状态" width="90">
        <template #default="{ row }">
          <StatusTag :value="row.status" :options="ENABLE_STATUS_OPTIONS" />
        </template>
      </BaseTableColumn>
      <BaseTableColumn label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <BaseTableActions @edit="openEditBanner(row)" @delete="deleteBannerRow(row)" />
        </template>
      </BaseTableColumn>
    </AdminDataTable>

    <BaseDialogForm
      v-model="dialogVisible"
      :title="dialogTitle"
      width="620px"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <BannerForm
        ref="bannerFormRef"
        :model-value="form"
        :image-preview-url="imagePreviewUrl"
        @image-uploaded="handleImageUploaded"
      />
    </BaseDialogForm>

    <BaseImagePreview v-model="previewVisible" :src="previewUrl" title="图片预览" width="720px" />
  </AdminCrudShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getBannerPage, addBanner, updateBanner, deleteBanner } from '@/api/banner'
import { ENABLE_STATUS_OPTIONS } from '@/constants/status'
import { normalizeBanner } from '@/services/domainAdapters'
import { useCrudPage } from '@/composables/useCrudPage'
import { useDialogForm } from '@/composables/useDialogForm'
import BaseButton from '@/components/base/BaseButton.vue'
import BaseFormItem from '@/components/base/BaseFormItem.vue'
import BaseInput from '@/components/base/BaseInput.vue'
import BaseOption from '@/components/base/BaseOption.vue'
import BaseSelect from '@/components/base/BaseSelect.vue'
import BaseTableActions from '@/components/base/BaseTableActions.vue'
import BaseTableColumn from '@/components/base/BaseTableColumn.vue'
import BaseImagePreview from '@/components/base/BaseImagePreview.vue'
import BaseDialogForm from '@/components/base/BaseDialogForm.vue'
import StatusTag from '@/components/base/StatusTag.vue'
import AdminCrudShell from '@/components/admin/AdminCrudShell.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import BannerForm from '@/components/admin/BannerForm.vue'
import { notify } from '@/services/uiFeedback'

const imagePreviewUrl = ref('')
const previewVisible = ref(false)
const previewUrl = ref('')

const defaultForm = () => ({
  id: null,
  title: '',
  subtitle: '',
  buttonText: '立即查看',
  linkUrl: '/shop',
  image: '',
  showTitle: 1,
  titleColor: '#ffffff',
  titleFontSize: 42,
  titleFontWeight: 700,
  subtitleColor: 'rgba(255,255,255,0.9)',
  subtitleFontSize: 18,
  showButton: 1,
  buttonColor: '#2e7d32',
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
  fetchPage: getBannerPage,
  remove: deleteBanner,
  searchDefaults: {
    keyword: '',
    status: null
  },
  normalize: normalizeBanner,
  deleteMessage: '确定要删除该轮播图吗？'
})

const {
  visible: dialogVisible,
  submitLoading,
  formRef: bannerFormRef,
  form,
  dialogTitle,
  openCreate: openCreateDialog,
  openEdit: openEditDialog,
  submit: submitDialog
} = useDialogForm({
  defaults: defaultForm,
  normalize: row => ({
    id: row.id,
    title: row.title,
    subtitle: row.subtitle,
    buttonText: row.buttonText,
    linkUrl: row.linkUrl,
    image: row.image,
    showTitle: row.showTitle ?? 1,
    titleColor: row.titleColor || '#ffffff',
    titleFontSize: row.titleFontSize || 42,
    titleFontWeight: row.titleFontWeight || 700,
    subtitleColor: row.subtitleColor || 'rgba(255,255,255,0.9)',
    subtitleFontSize: row.subtitleFontSize || 18,
    showButton: row.showButton ?? 1,
    buttonColor: row.buttonColor || '#2e7d32',
    sort: row.sort,
    status: row.status
  }),
  submitCreate: addBanner,
  submitUpdate: updateBanner,
  title: {
    create: '添加轮播图',
    edit: '编辑轮播图'
  }
})

const openCreateBanner = () => {
  imagePreviewUrl.value = ''
  openCreateDialog()
}

const openEditBanner = (row) => {
  imagePreviewUrl.value = row.imageUrl || ''
  openEditDialog(row)
}

const handleImageUploaded = (result) => {
  form.image = result.objectKey
  imagePreviewUrl.value = result.url
  notify.success('上传成功')
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

const deleteBannerRow = async (row) => {
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
