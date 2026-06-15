import { storeToRefs } from 'pinia'
import { useCartStore } from '@/store/cart'

export const useCart = () => {
  const cartStore = useCartStore()
  const { cartList, loading, selectedCount, totalPrice, hasSelected, selectAll } = storeToRefs(cartStore)

  return {
    cartList,
    loading,
    selectedCount,
    totalPrice,
    hasSelected,
    selectAll,
    initCart: cartStore.init,
    loadServerCart: cartStore.loadServerCart,
    addToCart: cartStore.addToCart,
    updateQuantity: cartStore.updateQuantity,
    updateSelected: cartStore.updateSelected,
    updateAllSelected: cartStore.updateAllSelected,
    deleteItem: cartStore.deleteItem,
    deleteSelected: cartStore.deleteSelected
  }
}
