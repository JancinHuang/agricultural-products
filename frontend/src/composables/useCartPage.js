import { useRouter } from 'vue-router'
import { useCart } from '@/composables/useCart'
import { confirmDanger, notify } from '@/services/uiFeedback'

export function useCartPage() {
  const router = useRouter()
  const cart = useCart()

  const handleSelectItem = async (row, selected) => {
    await cart.updateSelected(row.id, selected ? 1 : 0)
  }

  const handleSelectAll = async (selected) => {
    await cart.updateAllSelected(selected ? 1 : 0)
  }

  const handleUpdateQuantity = async (row, quantity) => {
    if (quantity < 1 || quantity > row.productStock) {
      notify.warning(`数量必须在1-${row.productStock}之间`)
      return
    }
    await cart.updateQuantity(row.id, quantity)
  }

  const confirmDelete = async (message) => {
    await confirmDanger(message)
  }

  const handleDeleteItem = async (row) => {
    try {
      await confirmDelete('确定要删除该商品吗？')
      await cart.deleteItem(row.id)
      notify.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') console.error(error)
    }
  }

  const handleDeleteSelected = async () => {
    try {
      await confirmDelete('确定要删除选中的商品吗？')
      await cart.deleteSelected()
      notify.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') console.error(error)
    }
  }

  const goToCheckout = () => {
    router.push('/checkout')
  }

  const openProduct = (item) => {
    router.push(`/product/${item.productId}`)
  }

  return {
    router,
    ...cart,
    handleSelectItem,
    handleSelectAll,
    handleUpdateQuantity,
    handleDeleteItem,
    handleDeleteSelected,
    goToCheckout,
    openProduct
  }
}
