<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>轮播图列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加轮播图
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="标题" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="150">
          <template #default="{ row }">
            <img
              v-if="row.image || row.imageUrl"
              :src="row.imageUrl || getImageUrl(row.image)"
              class="banner-thumb"
              @click="previewImage(row.imageUrl || getImageUrl(row.image))"
            />
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
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="92px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入轮播标题" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" type="textarea" :rows="2" placeholder="请输入副标题" />
        </el-form-item>
        <el-form-item label="按钮文案">
          <el-input v-model="form.buttonText" placeholder="例如：立即选购" />
        </el-form-item>
        <el-form-item label="跳转链接">
          <el-input v-model="form.linkUrl" placeholder="/shop 或 /product/1" />
        </el-form-item>
        <el-form-item label="图片" prop="image">
          <el-upload
            class="banner-uploader"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            :accept="ALLOWED_IMAGE_ACCEPT"
          >
            <div class="upload-trigger">
              <img v-if="imagePreviewUrl || form.image" :src="imagePreviewUrl || getImageUrl(form.image)" class="upload-preview" />
              <div v-else class="upload-placeholder">
                <el-icon><Plus /></el-icon>
                <span>上传轮播图</span>
              </div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="图片预览" width="720px" append-to-body>
      <img v-if="previewUrl" :src="previewUrl" class="preview-image" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getBannerPage, addBanner, updateBanner, deleteBanner } from '@/api/banner'
import { uploadFile } from '@/api/upload'
import { imageUtils } from '@/utils/imageUtils'
import { ALLOWED_IMAGE_ACCEPT, allowedImageMessage, isAllowedImageFile } from '@/utils/uploadValidation'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const imagePreviewUrl = ref('')
const formRef = ref(null)
const isEdit = ref(false)

const searchForm = reactive({
  keyword: '',
  status: null
})

const form = reactive({
  id: null,
  title: '',
  subtitle: '',
  buttonText: '立即查看',
  linkUrl: '/shop',
  image: '',
  sort: 0,
  status: 1
})

const dialogTitle = computed(() => isEdit.value ? '编辑轮播图' : '添加轮播图')
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  image: [{ required: true, message: '请上传图片', trigger: 'change' }]
}

const getImageUrl = image => imageUtils.getImageUrl(image)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getBannerPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword,
      status: searchForm.status
    })
    tableData.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    title: '',
    subtitle: '',
    buttonText: '立即查看',
    linkUrl: '/shop',
    image: '',
    sort: 0,
    status: 1
  })
  imagePreviewUrl.value = ''
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

const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    title: row.title || '',
    subtitle: row.subtitle || '',
    buttonText: row.buttonText || '立即查看',
    linkUrl: row.linkUrl || '/shop',
    image: row.image || '',
    sort: row.sort || 0,
    status: row.status ?? 1
  })
  imagePreviewUrl.value = row.imageUrl || ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const payload = { ...form }
    delete payload.imageUrl
    if (isEdit.value) {
      await updateBanner(payload)
      ElMessage.success('更新成功')
    } else {
      await addBanner(payload)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该轮播图吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteBanner(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const beforeUpload = (file) => {
  const isImage = isAllowedImageFile(file)
  const isLt5M = file.size / 1024 / 1024 <= 5
  if (!isImage) {
    ElMessage.error(allowedImageMessage)
    return false
  }
  if (!isLt5M) {
    ElMessage.error('轮播图不能超过 5MB')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  try {
    const res = await uploadFile(options.file)
    const data = res.data || {}
    form.image = data.objectKey || data.url || ''
    imagePreviewUrl.value = data.url || getImageUrl(form.image)
    options.onSuccess?.(res)
    ElMessage.success('上传成功')
  } catch (error) {
    console.error(error)
    options.onError?.(error)
  }
}

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

onMounted(loadData)
</script>

<style scoped>
.page-container {
  padding: 0;
}

.banner-thumb {
  width: 120px;
  height: 56px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e6ebdf;
  cursor: pointer;
}

.banner-uploader {
  width: 320px;
  height: 150px;
  border: 1px dashed #cfd8c8;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.banner-uploader:hover {
  border-color: var(--color-primary);
}

.banner-uploader :deep(.el-upload) {
  width: 100%;
  height: 100%;
}

.upload-trigger,
.upload-placeholder {
  width: 100%;
  height: 100%;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #7a8676;
  background: #f7faf3;
}

.upload-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.preview-image {
  width: 100%;
  max-height: 520px;
  object-fit: contain;
}
</style>
