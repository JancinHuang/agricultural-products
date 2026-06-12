<template>
  <div class="favorite-page">
    <div class="favorite-wrapper">
      <!-- 页面标题 -->
      <div class="page-header">
        <h2>我的收藏 <span class="count" v-if="favorites.length">（{{ favorites.length }}）</span></h2>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="!loading && favorites.length === 0">
        <el-empty description="还没有收藏任何商品">
          <el-button type="primary" @click="router.push('/shop')">去逛逛</el-button>
        </el-empty>
      </div>

      <!-- 收藏列表 -->
      <div class="favorite-grid" v-loading="loading" v-else>
        <div v-for="item in favorites" :key="item.id" class="product-card">
          <div class="product-image-wrapper" @click="goToDetail(item.productId)">
            <img :src="getImageUrl(item.productImage)" :alt="item.productName" />
            <div class="product-status">
              <el-tag v-if="item.productStatus !== 1" type="danger" size="small" effect="dark">已下架</el-tag>
            </div>
            <div class="product-overlay">
              <el-button type="primary" size="small" @click.stop="goToDetail(item.productId)">
                查看详情
              </el-button>
            </div>
          </div>
          <div class="product-info">
            <h3 class="product-name" @click="goToDetail(item.productId)" :title="item.productName">
              {{ item.productName }}
            </h3>
            <div class="product-meta">
              <span class="product-price">¥{{ item.price }}</span>
              <el-tag v-if="item.productStatus === 1" type="success" size="small">在售</el-tag>
            </div>
            <div class="product-actions">
              <el-button
                type="primary"
                size="small"
                :loading="addingToCart[item.productId]"
                :disabled="item.productStatus !== 1"
                @click="handleAddToCart(item)"
              >
                <el-icon><ShoppingCart /></el-icon> 加入购物车
              </el-button>
              <el-button size="small" @click="removeFavorite(item)" class="remove-btn">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart, Delete } from '@element-plus/icons-vue'
import { getFavoriteList, removeFromFavorite } from '@/api/favorite'
import { useCartStore } from '@/store/cart'
import { imageUtils } from '@/utils/imageUtils'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)
const favorites = ref([])
const addingToCart = ref({})

const getImageUrl = (url) => imageUtils.getImageUrl(url)

const loadFavorites = async () => {
  loading.value = true
  try {
    const res = await getFavoriteList()
    favorites.value = res.data || []
  } catch (error) {
    console.error(error)
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (productId) => {
  router.push(`/product/${productId}`)
}

const handleAddToCart = async (item) => {
  if (item.productStatus !== 1) {
    ElMessage.warning('该商品已下架')
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
      ElMessage.success({ message: `${item.productName} 已加入购物车 🛒`, duration: 1500 })
    } else {
      ElMessage.error('加入购物车失败')
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加入购物车失败')
  } finally {
    addingToCart.value[item.productId] = false
  }
}

const removeFavorite = async (item) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeFromFavorite(item.productId)
    ElMessage.success('已取消收藏')
    loadFavorites()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadFavorites()
  cartStore.init()
})
</script>

<style scoped>
.favorite-page {
  background: var(--color-bg-page);
  min-height: 100%;
}

.favorite-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

/* ── 标题 ── */
.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.count {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-placeholder);
}

/* ── 空状态 ── */
.empty-state {
  padding: 80px 0;
}

/* ── 商品网格 ── */
.favorite-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

/* ── 商品卡片 ── */
.product-card {
  background: #fff;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--color-border-light);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.product-image-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
  cursor: pointer;
}

.product-image-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image-wrapper img {
  transform: scale(1.05);
}

.product-status {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 3;
}

.product-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-normal);
  z-index: 2;
}

.product-card:hover .product-overlay {
  opacity: 1;
}

/* ── 商品信息 ── */
.product-info {
  padding: 14px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 10px;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.product-name:hover {
  color: var(--color-primary);
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-price);
}

.product-actions {
  display: flex;
  gap: 8px;
}

.product-actions .el-button {
  flex: 1;
}

.remove-btn {
  flex: 0 0 auto !important;
}

.remove-btn:hover {
  color: var(--color-danger);
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .favorite-wrapper {
    padding: 16px;
  }

  .favorite-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}
</style>
