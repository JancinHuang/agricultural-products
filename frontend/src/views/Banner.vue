<template>
  <BasePage title="轮播图列表">
    <template #actions>
      <el-button type="primary" @click="openCreateBanner">
        <el-icon><Plus /></el-icon>
        添加轮播图
      </el-button>
    </template>

    <BaseToolbar :model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="标题" clearable @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
          <el-option v-for="item in ENABLE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </BaseToolbar>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="图片" width="150">
        <template #default="{ row }">
          <img v-if="row.image" :src="row.imageUrl" class="banner-thumb" @click="previewImage(row.imageUrl)" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="140" />
      <el-table-column prop="subtitle" label="副标题" min-width="220" show-overflow-tooltip />
      <el-table-column prop="buttonText" label="按钮" width="100" />
      <el-table-column prop="linkUrl" label="跳转链接" min-width="140" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <StatusTag :value="row.status" :options="ENABLE_STATUS_OPTIONS" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <BaseTableActions @edit="openEditBanner(row)" @delete="deleteBannerRow(row)" />
        </template>
      </el-table-column>
    </el-table>

    <BasePagination
      v-model:current-page="pageNum"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" destroy-on-close>
      <BannerForm
        ref="bannerFormRef"
        :model-value="form"
        :image-preview-url="imagePreviewUrl"
        @image-uploaded="handleImageUploaded"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <BaseImagePreview v-model="previewVisible" :src="previewUrl" title="图片预览" width="720px" />
  </BasePage>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getBannerPage, addBanner, updateBanner, deleteBanner } from '@/api/banner'
import { ENABLE_STATUS_OPTIONS } from '@/constants/status'
import { normalizeBanner } from '@/services/domainAdapters'
import { useCrudPage } from '@/composables/useCrudPage'
import BasePage from '@/components/base/BasePage.vue'
import BaseToolbar from '@/components/base/BaseToolbar.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import BaseTableActions from '@/components/base/BaseTableActions.vue'
import BaseImagePreview from '@/components/base/BaseImagePreview.vue'
import StatusTag from '@/components/base/StatusTag.vue'
import BannerForm from '@/components/admin/BannerForm.vue'

const dialogVisible = ref(false)
const submitLoading = ref(false)
const bannerFormRef = ref(null)
const imagePreviewUrl = ref('')
const previewVisible = ref(false)
const previewUrl = ref('')
const isEdit = ref(false)
const dialogTitle = ref('添加轮播图')

const defaultForm = () => ({
  id: null,
  title: '',
  subtitle: '',
  buttonText: '立即查看',
  linkUrl: '/shop',
  image: '',
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
  fetchPage: getBannerPage,
  remove: deleteBanner,
  searchDefaults: {
    keyword: '',
    status: null
  },
  normalize: normalizeBanner,
  deleteMessage: '确定要删除该轮播图吗？'
})

const openCreateBanner = () => {
  isEdit.value = false
  dialogTitle.value = '添加轮播图'
  imagePreviewUrl.value = ''
  form.value = defaultForm()
  dialogVisible.value = true
}

const openEditBanner = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑轮播图'
  imagePreviewUrl.value = row.imageUrl || ''
  form.value = {
    id: row.id,
    title: row.title,
    subtitle: row.subtitle,
    buttonText: row.buttonText,
    linkUrl: row.linkUrl,
    image: row.image,
    sort: row.sort,
    status: row.status
  }
  dialogVisible.value = true
}

const handleImageUploaded = (result) => {
  form.value.image = result.objectKey
  imagePreviewUrl.value = result.url
  ElMessage.success('上传成功')
}

const handleSubmit = async () => {
  const valid = await bannerFormRef.value?.validate?.().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateBanner({ ...form.value })
      ElMessage.success('更新成功')
    } else {
      await addBanner({ ...form.value })
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

const deleteBannerRow = async (row) => {
  try {
    await handleDelete(row, () => ElMessage.success('删除成功'))
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
