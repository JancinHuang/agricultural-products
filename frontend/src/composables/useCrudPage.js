import { reactive, ref } from 'vue'
import { withPageDefaults } from '@/services/domainAdapters'
import { confirmDanger } from '@/services/uiFeedback'

export function useCrudPage(options) {
  const {
    fetchPage,
    remove,
    searchDefaults = {},
    pageSize: defaultPageSize = 10,
    normalize = item => item,
    deleteMessage = '确定要删除该数据吗？'
  } = options

  const loading = ref(false)
  const tableData = ref([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(defaultPageSize)
  const searchForm = reactive({ ...searchDefaults })

  const buildParams = () => ({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    ...searchForm
  })

  const loadData = async () => {
    loading.value = true
    try {
      const res = await fetchPage(buildParams())
      const page = withPageDefaults(res.data)
      tableData.value = page.list.map(normalize)
      total.value = page.total
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
    Object.assign(searchForm, searchDefaults)
    pageNum.value = 1
    loadData()
  }

  const handleSizeChange = (size) => {
    pageSize.value = size
    pageNum.value = 1
    loadData()
  }

  const handleCurrentChange = (page) => {
    pageNum.value = page
    loadData()
  }

  const handleDelete = async (row, onDeleted) => {
    await confirmDanger(deleteMessage)
    await remove(row.id)
    await onDeleted?.(row)
    await loadData()
  }

  return {
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
  }
}
