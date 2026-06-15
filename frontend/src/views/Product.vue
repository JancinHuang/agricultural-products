<template>
  <AdminCrudShell
    title="产品列表"
    :search-model="searchForm"
    @search="handleSearch"
    @reset="handleReset"
  >
    <template #actions>
      <BaseButton type="primary" @click="openCreateProduct">
        <el-icon><Plus /></el-icon>
        添加产品
      </BaseButton>
    </template>

    <template #search>
      <BaseFormItem label="关键词">
        <BaseInput v-model="searchForm.keyword" placeholder="产品名称" clearable @keyup.enter="handleSearch" />
      </BaseFormItem>
      <BaseFormItem label="分类">
        <BaseSelect v-model="searchForm.categoryId" placeholder="全部分类" clearable style="width: 140px">
          <BaseOption v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
        </BaseSelect>
      </BaseFormItem>
      <BaseFormItem label="状态">
        <BaseSelect v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px">
          <BaseOption v-for="item in PRODUCT_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </BaseSelect>
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
      <BaseTableColumn prop="id" label="ID" width="70" />
      <BaseTableColumn label="图片" width="86">
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
      </BaseTableColumn>
      <BaseTableColumn prop="name" label="产品名称" min-width="130" />
      <BaseTableColumn prop="categoryName" label="分类" min-width="100" />
      <BaseTableColumn label="价格" width="100">
        <template #default="{ row }">
          <PriceText :value="row.price" />
        </template>
      </BaseTableColumn>
      <BaseTableColumn prop="stock" label="库存" width="90" />
      <BaseTableColumn prop="unit" label="单位" width="80" />
      <BaseTableColumn prop="origin" label="产地" min-width="110" />
      <BaseTableColumn prop="sales" label="销量" width="90" />
      <BaseTableColumn label="状态" width="90">
        <template #default="{ row }">
          <StatusTag :value="row.status" :options="PRODUCT_STATUS_OPTIONS" />
        </template>
      </BaseTableColumn>
      <BaseTableColumn label="创建时间" min-width="160">
        <template #default="{ row }">
          <span class="time-cell">{{ formatTime(row.createTime) }}</span>
        </template>
      </BaseTableColumn>
      <BaseTableColumn label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <BaseTableActions @edit="openEditProduct(row)" @delete="deleteProductRow(row)" />
        </template>
      </BaseTableColumn>
    </AdminDataTable>

    <BaseDialogForm
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :loading="submitLoading"
      @submit="handleSubmit"
    >
      <ProductForm
        ref="productFormRef"
        :model-value="form"
        :categories="categoryList"
        :image-preview-url="imagePreviewUrl"
        @image-uploaded="handleImageUploaded"
      />
    </BaseDialogForm>

    <BaseImagePreview v-model="previewVisible" :src="previewUrl" title="图片预览" />
  </AdminCrudShell>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { getProductPage, addProduct, updateProduct, deleteProduct } from '@/api/product'
import { getCategoryList } from '@/api/category'
import { formatTime } from '@/utils/time'
import { PRODUCT_STATUS_OPTIONS } from '@/constants/status'
import { normalizeProduct } from '@/services/domainAdapters'
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
import PriceText from '@/components/base/PriceText.vue'
import AdminCrudShell from '@/components/admin/AdminCrudShell.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import ProductForm from '@/components/admin/ProductForm.vue'
import { notify } from '@/services/uiFeedback'

const categoryList = ref([])
const imagePreviewUrl = ref('')
const previewVisible = ref(false)
const previewUrl = ref('')

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

const {
  visible: dialogVisible,
  submitLoading,
  formRef: productFormRef,
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
    categoryId: row.categoryId,
    description: row.description,
    price: row.price,
    stock: row.stock,
    unit: row.unit,
    origin: row.origin,
    image: row.image,
    status: row.status
  }),
  submitCreate: addProduct,
  submitUpdate: updateProduct,
  title: {
    create: '添加产品',
    edit: '编辑产品'
  }
})

const loadCategoryList = async () => {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const openCreateProduct = () => {
  imagePreviewUrl.value = ''
  openCreateDialog()
}

const openEditProduct = (row) => {
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

const deleteProductRow = async (row) => {
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

onMounted(() => {
  loadCategoryList()
  loadData()
})
</script>
