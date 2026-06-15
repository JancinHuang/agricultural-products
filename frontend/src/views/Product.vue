<template>
  <BasePage title="产品列表">
    <template #actions>
      <el-button type="primary" @click="openCreateProduct">
        <el-icon><Plus /></el-icon>
        添加产品
      </el-button>
    </template>

    <BaseToolbar :model="searchForm" @search="handleSearch" @reset="handleReset">
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
          <el-option v-for="item in PRODUCT_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
    </BaseToolbar>

    <el-table :data="tableData" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="图片" width="86">
        <template #default="{ row }">
          <img
            v-if="row.image"
            :src="row.imageUrl"
            class="table-image"
            style="cursor: pointer;"
            @click="previewImage(row.imageUrl)"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="产品名称" min-width="130" />
      <el-table-column prop="categoryName" label="分类" min-width="100" />
      <el-table-column label="价格" width="100">
        <template #default="{ row }">
          <PriceText :value="row.price" />
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="90" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="origin" label="产地" min-width="110" />
      <el-table-column prop="sales" label="销量" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <StatusTag :value="row.status" :options="PRODUCT_STATUS_OPTIONS" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="160">
        <template #default="{ row }">
          <span class="time-cell">{{ formatTime(row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <BaseTableActions @edit="openEditProduct(row)" @delete="deleteProductRow(row)" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <ProductForm
        ref="productFormRef"
        :model-value="form"
        :categories="categoryList"
        :image-preview-url="imagePreviewUrl"
        @image-uploaded="handleImageUploaded"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <BaseImagePreview v-model="previewVisible" :src="previewUrl" title="图片预览" />
  </BasePage>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getProductPage, addProduct, updateProduct, deleteProduct } from '@/api/product'
import { getCategoryList } from '@/api/category'
import { formatTime } from '@/utils/time'
import { PRODUCT_STATUS_OPTIONS } from '@/constants/status'
import { normalizeProduct } from '@/services/domainAdapters'
import { useCrudPage } from '@/composables/useCrudPage'
import BasePage from '@/components/base/BasePage.vue'
import BaseToolbar from '@/components/base/BaseToolbar.vue'
import BasePagination from '@/components/base/BasePagination.vue'
import BaseTableActions from '@/components/base/BaseTableActions.vue'
import BaseImagePreview from '@/components/base/BaseImagePreview.vue'
import StatusTag from '@/components/base/StatusTag.vue'
import PriceText from '@/components/base/PriceText.vue'
import ProductForm from '@/components/admin/ProductForm.vue'

const categoryList = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const productFormRef = ref(null)
const imagePreviewUrl = ref('')
const previewVisible = ref(false)
const previewUrl = ref('')
const isEdit = ref(false)

const defaultForm = () => ({
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
  fetchPage: getProductPage,
  remove: deleteProduct,
  searchDefaults: {
    keyword: '',
    categoryId: null,
    status: null
  },
  normalize: normalizeProduct,
  deleteMessage: '确定要删除该产品吗？'
})

const dialogTitle = ref('添加产品')

const loadCategoryList = async () => {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const openCreateProduct = () => {
  isEdit.value = false
  dialogTitle.value = '添加产品'
  imagePreviewUrl.value = ''
  form.value = defaultForm()
  dialogVisible.value = true
}

const openEditProduct = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑产品'
  imagePreviewUrl.value = row.imageUrl || ''
  form.value = {
    id: row.id,
    name: row.name,
    categoryId: row.categoryId,
    description: row.description,
    price: row.price,
    stock: row.stock,
    unit: row.unit,
    origin: row.origin,
    image: row.image,
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
  const valid = await productFormRef.value?.validate?.().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const payload = { ...form.value }
    if (isEdit.value) {
      await updateProduct(payload)
      ElMessage.success('更新成功')
    } else {
      await addProduct(payload)
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

const deleteProductRow = async (row) => {
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

onMounted(() => {
  loadCategoryList()
  loadData()
})
</script>
