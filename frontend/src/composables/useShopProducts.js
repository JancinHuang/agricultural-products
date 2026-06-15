import { onBeforeUnmount, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCategoryList } from '@/api/category'
import { searchProducts } from '@/api/product'
import { useCartStore } from '@/store/cart'
import { imageUtils, smartPreload } from '@/utils/imageUtils'
import { performanceMonitor } from '@/utils/performanceMonitor'
import { normalizeProduct, withPageDefaults } from '@/services/domainAdapters'
import { notify } from '@/services/uiFeedback'

export function useShopProducts() {
  const router = useRouter()
  const route = useRoute()
  const cartStore = useCartStore()

  const loading = ref(false)
  const products = ref([])
  const categories = ref([])
  const total = ref(0)
  const pageNum = ref(1)
  const pageSize = ref(12)
  const addingToCart = ref({})
  const previewVisible = ref(false)
  const previewImages = ref([])
  const previewIndex = ref(0)
  let performanceReportHandler = null

  const searchForm = reactive({
    keyword: '',
    categoryId: null,
    minPrice: null,
    maxPrice: null,
    orderBy: 'create_time'
  })

  const loadCategories = async () => {
    try {
      const res = await getCategoryList()
      categories.value = res.data || []
    } catch (error) {
      console.error('[Shop] Failed to load categories:', error)
    }
  }

  const preloadNearbyImages = () => {
    const urls = products.value
      .slice(0, 4)
      .map(product => product.image ? imageUtils.getImageUrl(product.image, { width: 640, quality: 80 }) : null)
      .filter(Boolean)

    if (urls.length > 0) {
      smartPreload(urls, {
        concurrency: 2,
        onProgress: (loaded, count) => {
          if (import.meta.env.DEV) {
            console.debug(`[Shop] Preloaded ${loaded}/${count} images`)
          }
        }
      })
    }
  }

  const loadProducts = async () => {
    loading.value = true
    try {
      const res = await searchProducts({
        keyword: searchForm.keyword,
        categoryId: searchForm.categoryId,
        minPrice: searchForm.minPrice,
        maxPrice: searchForm.maxPrice,
        orderBy: searchForm.orderBy,
        pageNum: pageNum.value,
        pageSize: pageSize.value
      })
      const page = withPageDefaults(res.data)
      products.value = page.list.map(normalizeProduct)
      total.value = page.total
      preloadNearbyImages()
    } catch (error) {
      console.error('[Shop] Failed to load products:', error)
      notify.error('加载商品失败')
    } finally {
      loading.value = false
    }
  }

  const applyRouteQuery = () => {
    if (route.query.keyword) searchForm.keyword = route.query.keyword
    if (route.query.categoryId) searchForm.categoryId = parseInt(route.query.categoryId)
    if (route.query.orderBy) searchForm.orderBy = route.query.orderBy
  }

  const handleSearch = () => {
    if (searchForm.minPrice && searchForm.maxPrice && searchForm.minPrice > searchForm.maxPrice) {
      notify.warning('最低价不能大于最高价')
      return
    }
    pageNum.value = 1
    loadProducts()
  }

  const handleSizeChange = (size) => {
    pageSize.value = size
    pageNum.value = 1
    loadProducts()
  }

  const handlePageChange = (page) => {
    pageNum.value = page
    loadProducts()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const goToDetail = (id) => {
    router.push(`/product/${id}`)
  }

  const handleImageClick = (product) => {
    if (product.image) {
      previewImages.value = [product.image]
      previewIndex.value = 0
      previewVisible.value = true
    }
  }

  const handleAddToCart = async (product) => {
    if (product.stock <= 0) {
      notify.warning('商品已售罄')
      return
    }

    addingToCart.value[product.id] = true
    try {
      const success = await cartStore.addToCart(product, 1)
      if (success) {
        notify.success(`${product.name} 已加入购物车`, { duration: 1500 })
      } else {
        notify.error('加入购物车失败')
      }
    } catch (error) {
      console.error('[Shop] Failed to add to cart:', error)
      notify.error('加入购物车失败')
    } finally {
      addingToCart.value[product.id] = false
    }
  }

  const startDevPerformanceMonitor = () => {
    if (!import.meta.env.DEV) return
    performanceMonitor.start()
    performanceReportHandler = () => {
      console.debug('[Shop] Performance Report:', performanceMonitor.getReport())
    }
    window.addEventListener('beforeunload', performanceReportHandler)
  }

  const stopDevPerformanceMonitor = () => {
    if (performanceReportHandler) {
      window.removeEventListener('beforeunload', performanceReportHandler)
      performanceReportHandler = null
    }
  }

  const initShop = () => {
    applyRouteQuery()
    loadCategories()
    loadProducts()
    cartStore.init()
    startDevPerformanceMonitor()
  }

  onBeforeUnmount(stopDevPerformanceMonitor)

  return {
    searchForm,
    loading,
    products,
    categories,
    total,
    pageNum,
    pageSize,
    addingToCart,
    previewVisible,
    previewImages,
    previewIndex,
    initShop,
    handleSearch,
    handleSizeChange,
    handlePageChange,
    goToDetail,
    handleImageClick,
    handleAddToCart
  }
}
