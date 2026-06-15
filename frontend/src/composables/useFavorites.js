import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getFavoriteList, removeFromFavorite } from '@/api/favorite'
import { useCartStore } from '@/store/cart'
import { confirmDanger, notify } from '@/services/uiFeedback'

export function useFavorites() {
  const router = useRouter()
  const cartStore = useCartStore()
  const loading = ref(false)
  const favorites = ref([])
  const addingToCart = ref({})

  const loadFavorites = async () => {
    loading.value = true
    try {
      const res = await getFavoriteList()
      favorites.value = res.data || []
    } catch (error) {
      console.error(error)
      notify.error('加载收藏列表失败')
    } finally {
      loading.value = false
    }
  }

  const goToDetail = (productId) => {
    router.push(`/product/${productId}`)
  }

  const handleAddToCart = async (item) => {
    if (item.productStatus !== 1) {
      notify.warning('该商品已下架')
      return
    }

    addingToCart.value[item.productId] = true
    try {
      const success = await cartStore.addToCart({
        id: item.productId,
        name: item.productName,
        image: item.productImage,
        price: item.price,
        status: item.productStatus,
        stock: 999
      }, 1)
      if (success) {
        notify.success(`${item.productName} 已加入购物车`, { duration: 1500 })
      } else {
        notify.error('加入购物车失败')
      }
    } catch (error) {
      console.error(error)
      notify.error('加入购物车失败')
    } finally {
      addingToCart.value[item.productId] = false
    }
  }

  const removeFavorite = async (item) => {
    try {
      await confirmDanger('确定要取消收藏该商品吗？')
      await removeFromFavorite(item.productId)
      notify.success('已取消收藏')
      loadFavorites()
    } catch (error) {
      if (error !== 'cancel') {
        console.error(error)
        notify.error('操作失败')
      }
    }
  }

  const initFavorites = () => {
    loadFavorites()
    cartStore.init()
  }

  return {
    router,
    loading,
    favorites,
    addingToCart,
    loadFavorites,
    goToDetail,
    handleAddToCart,
    removeFavorite,
    initFavorites
  }
}
