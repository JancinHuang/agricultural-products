<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>产品列表</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加产品
          </el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="产品名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable style="width: 140px">
            <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
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
        <el-table-column prop="id" label="ID" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <img
              v-if="row.image"
              :src="getImageUrl(row.image)"
              class="table-image"
              style="cursor: pointer;"
              @click="previewImage(getImageUrl(row.image))"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="产品名称" />
        <el-table-column prop="categoryName" label="分类" />
        <el-table-column prop="price" label="价格">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" />
        <el-table-column prop="unit" label="单位" />
        <el-table-column prop="origin" label="产地" />
        <el-table-column prop="sales" label="销量" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">
            <span class="time-cell">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" class="dialog-full">
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" class="dialog-full" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" class="dialog-full" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：斤、个、盒" />
        </el-form-item>
        <el-form-item label="产地" prop="origin">
          <el-input v-model="form.origin" placeholder="请输入产地" />
        </el-form-item>
        <el-form-item label="图片" prop="image">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="beforeUpload"
            :http-request="handleUpload"
            accept="image/*"
          >
            <div class="upload-trigger">
              <el-image
                v-if="imagePreviewUrl || form.image"
                :src="imagePreviewUrl || imageUtils.getImageUrl(form.image)"
                fit="cover"
                class="upload-preview"
              />
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Plus /></el-icon>
              </div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入产品描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="图片预览" width="500px" append-to-body>
      <img :src="previewUrl" style="width: 100%;" v-if="previewUrl" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getProductPage, addProduct, updateProduct, deleteProduct } from '@/api/product'
import { getCategoryList } from '@/api/category'
import { uploadFile } from '@/api/upload'
import { formatTime } from '@/utils/time'
import { imageUtils } from '@/utils/imageUtils'

const getImageUrl = (url) => imageUtils.getImageUrl(url)

const previewImage = (url) => {
  previewUrl.value = url
  previewVisible.value = true
}

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)
const categoryList = ref([])
const imagePreviewUrl = ref('')
const previewVisible = ref(false)
const previewUrl = ref('')

const searchForm = reactive({
  keyword: '',
  categoryId: null,
  status: null
})

const form = reactive({
  id: null,
  name: '',
  categoryId: null,
  description: '',
  price: 0,
  stock: 0,
  unit: '',
  origin: '',
  image: '',
  status: 1
})

const isEdit = ref(false)
const dialogTitle = computed(() => isEdit.value ? '编辑产品' : '添加产品')

const rules = {
  name: [
    { required: true, message: '请输入产品名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ]
}

const loadCategoryList = async () => {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getProductPage({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchForm.keyword,
      categoryId: searchForm.categoryId,
      status: searchForm.status
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
  searchForm.categoryId = null
  searchForm.status = null
  pageNum.value = 1
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  imagePreviewUrl.value = ''
  Object.assign(form, {
    id: null,
    name: '',
    categoryId: null,
    description: '',
    price: 0,
    stock: 0,
    unit: '',
    origin: '',
    image: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  imagePreviewUrl.value = row.image || ''
  Object.assign(form, {
    id: row.id,
    name: row.name,
    categoryId: row.categoryId,
    description: row.description,
    price: row.price,
    stock: row.stock,
    unit: row.unit,
    origin: row.origin,
    image: null,
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
    if (isEdit.value && payload.image == null) {
      delete payload.image
    }
    if (isEdit.value) {
      await updateProduct(payload)
      ElMessage.success('更新成功')
    } else {
      await addProduct(payload)
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
  ElMessageBox.confirm('确定要删除该产品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    loadData()
  }).catch(() => {})
}

const beforeUpload = (file) => {
  const isImage = file.type ? file.type.startsWith('image/') : /\.(png|jpe?g|gif|webp|bmp)$/i.test(file.name)
  const isLt5M = file.size / 1024 / 1024 < 5
  
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  try {
    const res = await uploadFile(options.file)
    const data = res.data || {}
    form.image = data.objectKey || data.url || ''
    imagePreviewUrl.value = data.url || imageUtils.getImageUrl(form.image)
    options.onSuccess?.(res)
    ElMessage.success('上传成功')
  } catch (error) {
    console.error(error)
    options.onError?.(error)
  }
}

onMounted(() => {
  loadCategoryList()
  loadData()
})
</script>

<style scoped>
.page-container {
  padding: 0;
}

.table-image {
  width: 50px;
  height: 50px;
  border-radius: 4px;
}

.time-cell {
  white-space: nowrap;
}

.dialog-full {
  width: 100%;
}

.upload-preview {
  width: 120px;
  height: 120px;
}

.image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  width: 120px;
  height: 120px;
  overflow: hidden;
}

.image-uploader :deep(.el-upload) {
  width: 100%;
  height: 100%;
  display: block;
}

.upload-trigger,
.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-trigger {
  position: relative;
}

.upload-preview {
  display: block;
}

.image-uploader:hover {
  border-color: var(--color-primary);
}

.upload-icon {
  font-size: 28px;
  color: #8c939d;
}
</style>
