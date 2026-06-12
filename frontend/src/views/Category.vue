<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>分类列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加分类
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="分类名称" clearable @keyup.enter="handleSearch" />
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
        <el-table-column label="图标" width="90">
          <template #default="{ row }">
            <img
              v-if="row.icon"
              :src="getDisplayIconUrl(row)"
              class="category-thumb"
              @click="previewImage(getDisplayIconUrl(row))"
            />
            <span v-else class="empty-icon">{{ getCategoryEmoji(row.name) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="分类名称" min-width="130" />
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
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
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="86px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <div class="icon-editor">
            <el-upload
              class="icon-uploader"
              :show-file-list="false"
              :before-upload="beforeUpload"
              :http-request="handleUpload"
              accept="image/*"
            >
              <div class="upload-trigger">
                <img v-if="form.icon || iconPreviewUrl" :src="iconPreviewUrl || getIconUrl(form.icon)" class="upload-preview" />
                <div v-else class="upload-placeholder">
                  <el-icon><Plus /></el-icon>
                  <span>上传图标</span>
                </div>
              </div>
            </el-upload>
            <el-button v-if="form.icon || iconPreviewUrl" text type="danger" @click="removeIcon">移除图标</el-button>
          </div>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
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

    <el-dialog v-model="previewVisible" title="图标预览" width="360px" append-to-body>
      <img v-if="previewUrl" :src="previewUrl" class="preview-image" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getCategoryPage, addCategory, updateCategory, deleteCategory } from '@/api/category'
import { uploadFile } from '@/api/upload'
import { formatTime } from '@/utils/time'
import { imageUtils } from '@/utils/imageUtils'

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const previewVisible = ref(false)
const previewUrl = ref('')
const iconPreviewUrl = ref('')
let localIconPreviewUrl = ''
const formRef = ref(null)

const searchForm = reactive({
  keyword: ''
})

const form = reactive({
  id: null,
  name: '',
  description: '',
  icon: '',
  sort: 0,
  status: 1
})

const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑分类' : '添加分类')

const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' }
  ]
}

const getIconUrl = icon => imageUtils.getImageUrl(icon, { width: 96, height: 96, quality: 80 })
const getDisplayIconUrl = row => row.iconUrl || getIconUrl(row.icon)

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

const loadData = async () => {
  loading.value = true
  try {
    const res = await getCategoryPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword
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
  pageNum.value = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  revokeLocalIconPreview()
  iconPreviewUrl.value = ''
  Object.assign(form, {
    id: null,
    name: '',
    description: '',
    icon: '',
    sort: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  revokeLocalIconPreview()
  iconPreviewUrl.value = row.iconUrl || ''
  Object.assign(form, {
    id: row.id,
    name: row.name,
    description: row.description || '',
    icon: row.icon || '',
    sort: row.sort,
    status: row.status
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = { ...form }
    delete payload.iconUrl
    if (isEdit.value) {
      await updateCategory(payload)
      ElMessage.success('更新成功')
    } else {
      await addCategory(payload)
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
  ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const removeIcon = () => {
  revokeLocalIconPreview()
  form.icon = ''
  iconPreviewUrl.value = ''
}

const revokeLocalIconPreview = () => {
  if (localIconPreviewUrl) {
    URL.revokeObjectURL(localIconPreviewUrl)
    localIconPreviewUrl = ''
  }
}

const beforeUpload = (file) => {
  const isImage = file.type ? file.type.startsWith('image/') : /\.(png|jpe?g|gif|webp|bmp)$/i.test(file.name)
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图标大小不能超过 2MB')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  revokeLocalIconPreview()
  localIconPreviewUrl = URL.createObjectURL(options.file)
  iconPreviewUrl.value = localIconPreviewUrl
  try {
    const res = await uploadFile(options.file)
    const data = res.data || {}
    form.icon = data.objectKey || data.url || ''
    if (data.url) {
      revokeLocalIconPreview()
      iconPreviewUrl.value = data.url
    }
    options.onSuccess?.(res)
    ElMessage.success('上传成功')
  } catch (error) {
    console.error(error)
    revokeLocalIconPreview()
    iconPreviewUrl.value = form.icon ? getIconUrl(form.icon) : ''
    options.onError?.(error)
  }
}

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

onMounted(() => {
  loadData()
})

onUnmounted(() => {
  revokeLocalIconPreview()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.category-thumb,
.empty-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
}

.category-thumb {
  object-fit: cover;
  cursor: pointer;
  border: 1px solid #e7ece2;
  background: #f6f8f3;
}

.empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f1f5ec;
  font-size: 24px;
}

.icon-editor {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-uploader {
  width: 96px;
  height: 96px;
  border: 1px dashed #cfd8c8;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.icon-uploader:hover {
  border-color: var(--color-primary);
}

.icon-uploader :deep(.el-upload) {
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
  gap: 6px;
  color: #7a8676;
  font-size: 12px;
  background: #f7faf3;
}

.upload-preview {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.preview-image {
  width: 100%;
  max-height: 320px;
  object-fit: contain;
}
</style>
