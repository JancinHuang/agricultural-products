<template>
  <div class="home-container">
    <!-- Hero Banner -->
    <section class="hero-section">
      <el-carousel height="420px" :interval="5000" arrow="always">
        <el-carousel-item v-for="(slide, index) in heroSlides" :key="index">
          <div class="hero-slide" :style="getHeroStyle(slide)">
            <img v-if="slide.imageUrl" :src="slide.imageUrl" :alt="slide.title" class="hero-bg-image" />
            <div class="hero-overlay"></div>
            <div class="hero-content">
              <h2>{{ slide.title }}</h2>
              <p>{{ slide.subtitle }}</p>
              <el-button type="primary" size="large" round @click="router.push(slide.link || slide.linkUrl || '/shop')">
                {{ slide.btnText }}
                <el-icon class="el-icon--right"><ArrowRight /></el-icon>
              </el-button>
            </div>
            <div class="hero-decoration">{{ slide.icon }}</div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- 个性化问候 -->
    <div class="greeting-bar" v-if="userStore.userInfo">
      <span>{{ greetingText }}，{{ userStore.userInfo.nickname || userStore.userInfo.username }}！今天想买点什么新鲜好物？</span>
    </div>

    <!-- 分类快捷入口 -->
    <section class="section category-section">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">商品分类</h2>
          <el-button text type="primary" @click="router.push('/shop')">浏览全部</el-button>
        </div>
        <div class="category-grid" v-loading="categories.length === 0">
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="category-card"
            @click="router.push({ path: '/shop', query: { categoryId: cat.id } })"
          >
            <div class="category-icon">
              <img v-if="cat.icon || cat.iconUrl" :src="getCategoryIconSrc(cat)" :alt="cat.name" class="category-icon-img" />
              <span v-else>{{ getCategoryEmoji(cat.name) }}</span>
            </div>
            <span class="category-name">{{ cat.name }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 热销推荐 -->
    <section class="section">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">🔥 热销推荐</h2>
          <el-button text type="primary" @click="router.push({ path: '/shop', query: { orderBy: 'sales' } })">
            查看更多
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="scroll-wrapper" v-loading="hotLoading">
          <button class="scroll-arrow scroll-left" @click="scrollHot(-1)" v-if="hotProducts.length > 4">
            <el-icon :size="20"><ArrowLeft /></el-icon>
          </button>
          <div class="product-scroll" ref="hotScrollRef">
            <div
              v-for="product in hotProducts"
              :key="product.id"
              class="product-card"
              @click="router.push(`/product/${product.id}`)"
            >
              <div class="product-image-wrapper">
                <ProductImage :src="product.image" :alt="product.name" :height="200" :lazy="true" />
                <div class="product-badge" v-if="product.sales > 50">热销</div>
                <div class="product-overlay">
                  <el-button type="primary" size="small" @click.stop="handleAddToCart(product)">
                    <el-icon><ShoppingCart /></el-icon> 加入购物车
                  </el-button>
                </div>
              </div>
              <div class="product-info">
                <h3 class="product-name" :title="product.name">{{ product.name }}</h3>
                <div class="product-meta">
                  <span class="product-price">¥{{ product.price }}</span>
                  <span class="product-sales">已售{{ product.sales || 0 }}</span>
                </div>
              </div>
            </div>
          </div>
          <button class="scroll-arrow scroll-right" @click="scrollHot(1)" v-if="hotProducts.length > 4">
            <el-icon :size="20"><ArrowRight /></el-icon>
          </button>
        </div>
      </div>
    </section>

    <!-- 新品上架 -->
    <section class="section">
      <div class="section-inner">
        <div class="section-header">
          <h2 class="section-title">🌱 新品上架</h2>
          <el-button text type="primary" @click="router.push({ path: '/shop', query: { orderBy: 'create_time' } })">
            查看更多
            <el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="product-grid" v-loading="newLoading">
          <div
            v-for="product in newProducts"
            :key="product.id"
            class="product-card"
            @click="router.push(`/product/${product.id}`)"
          >
            <div class="product-image-wrapper">
              <ProductImage :src="product.image" :alt="product.name" :height="200" :lazy="true" />
              <div class="product-badge new-badge">新品</div>
              <div class="product-overlay">
                <el-button type="primary" size="small" @click.stop="handleAddToCart(product)">
                  <el-icon><ShoppingCart /></el-icon> 加入购物车
                </el-button>
              </div>
            </div>
            <div class="product-info">
              <h3 class="product-name" :title="product.name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description || '优质农产品，产地直发' }}</p>
              <div class="product-meta">
                <span class="product-price">¥{{ product.price }}</span>
                <span class="product-sales">已售{{ product.sales || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, ArrowLeft, ShoppingCart } from '@element-plus/icons-vue'
import { getHotProducts, searchProducts } from '@/api/product'
import { getCategoryList } from '@/api/category'
import { getBannerList } from '@/api/banner'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'
import ProductImage from '@/components/ProductImage.vue'
import { imageUtils } from '@/utils/imageUtils'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const categories = ref([])
const hotProducts = ref([])
const newProducts = ref([])
const heroSlides = ref([])
const hotLoading = ref(false)
const newLoading = ref(false)
const hotScrollRef = ref(null)

const scrollHot = (direction) => {
  const el = hotScrollRef.value
  if (!el) return
  const scrollAmount = 260 * 2 // scroll 2 cards at a time
  el.scrollBy({ left: direction * scrollAmount, behavior: 'smooth' })
}

const defaultHeroSlides = [
  {
    title: '新鲜直采 品质溯源',
    subtitle: '从田间到餐桌，每一份都是自然的馈赠',
    btnText: '立即选购',
    link: '/shop',
    bg: 'linear-gradient(135deg, #1b5e20 0%, #2e7d32 40%, #4caf50 100%)',
    icon: '🌾'
  },
  {
    title: '助农增收 共建美好',
    subtitle: '支持乡村发展，享受绿色健康农产品',
    btnText: '了解更多',
    link: '/shop',
    bg: 'linear-gradient(135deg, #33691e 0%, #558b2f 40%, #7cb342 100%)',
    icon: '🌱'
  },
  {
    title: '限时优惠 爆款推荐',
    subtitle: '精选热销农产品，产地直发新鲜到家',
    btnText: '查看热销',
    link: '/shop?orderBy=sales',
    bg: 'linear-gradient(135deg, #e65100 0%, #ff8f00 40%, #ffb300 100%)',
    icon: '🔥'
  }
]

heroSlides.value = defaultHeroSlides

const getHeroStyle = slide => ({
  background: slide.imageUrl ? '#1f2a1d' : slide.bg
})

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const getCategoryEmoji = (name) => {
  if (!name) return '🌿'
  const map = {
    '水果': '🍎', '蔬菜': '🥬', '粮食': '🌾', '粮油': '🌾',
    '茶叶': '🍵', '坚果': '🥜', '蜂蜜': '🍯', '肉类': '🥩',
    '禽蛋': '🥚', '水产': '🐟', '菌菇': '🍄', '干货': '🫙',
    '调味': '🧂', '饮品': '🥤', '零食': '🍬', '有机': '🌿'
  }
  for (const [key, emoji] of Object.entries(map)) {
    if (name.includes(key)) return emoji
  }
  return '🌿'
}

const getCategoryIconUrl = icon => imageUtils.getImageUrl(icon, { width: 96, height: 96, quality: 80 })
const getCategoryIconSrc = category => category.iconUrl || getCategoryIconUrl(category.icon)

const loadCategories = async () => {
  try {
    const res = await getCategoryList()
    categories.value = res.data || []
  } catch (error) {
    console.error(error)
  }
}

const loadBanners = async () => {
  try {
    const res = await getBannerList()
    const banners = (res.data || []).map(item => ({
      ...item,
      btnText: item.buttonText || '立即查看',
      link: item.linkUrl || '/shop',
      imageUrl: item.imageUrl || getCategoryIconUrl(item.image)
    }))
    if (banners.length) {
      heroSlides.value = banners
    }
  } catch (error) {
    console.error(error)
  }
}

const loadHotProducts = async () => {
  hotLoading.value = true
  try {
    const res = await getHotProducts(8)
    hotProducts.value = res.data || []
  } catch (error) {
    console.error(error)
  } finally {
    hotLoading.value = false
  }
}

const loadNewProducts = async () => {
  newLoading.value = true
  try {
    const res = await searchProducts({ orderBy: 'create_time', pageNum: 1, pageSize: 8 })
    newProducts.value = res.data.list || []
  } catch (error) {
    console.error(error)
  } finally {
    newLoading.value = false
  }
}

const handleAddToCart = async (product) => {
  if (product.stock <= 0) {
    ElMessage.warning('商品已售罄')
    return
  }
  try {
    const success = await cartStore.addToCart(product, 1)
    if (success) {
      ElMessage.success({ message: `${product.name} 已加入购物车 🛒`, duration: 1500 })
    }
  } catch (error) {
    console.error(error)
    ElMessage.error('加入购物车失败')
  }
}

onMounted(() => {
  loadBanners()
  loadCategories()
  loadHotProducts()
  loadNewProducts()
})
</script>

<style scoped>
.home-container {
  background: var(--color-bg-page);
}

/* ── Hero Banner ── */
.hero-section :deep(.el-carousel__item) {
  border-radius: 0;
}

.hero-slide {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.hero-bg-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(20, 36, 18, 0.72), rgba(28, 64, 26, 0.46));
  z-index: 1;
}

.hero-content {
  text-align: center;
  z-index: 3;
  animation: fadeInUp 0.8s ease;
}

.hero-content h2 {
  font-size: 42px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 16px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.hero-content p {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 30px;
  letter-spacing: 1px;
}

.hero-content .el-button {
  font-size: 16px;
  padding: 12px 32px;
  height: auto;
}

.hero-decoration {
  position: absolute;
  font-size: 200px;
  opacity: 0.1;
  right: 10%;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ── Greeting ── */
.greeting-bar {
  background: linear-gradient(90deg, var(--color-primary-bg), transparent);
  border-bottom: 1px solid var(--color-primary-lighter);
  text-align: center;
  padding: 12px 24px;
  font-size: 14px;
  color: var(--color-primary);
}

/* ── Sections ── */
.section {
  margin-bottom: 8px;
}

.section-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 36px 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

/* ── Category Grid ── */
.category-section {
  background: #fff;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.category-card {
  background: var(--color-bg-section);
  border-radius: var(--radius-md);
  padding: 24px 12px;
  text-align: center;
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.category-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.category-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
  line-height: 1;
  font-size: 40px;
}

.category-icon-img {
  width: 56px;
  height: 56px;
  display: block;
  object-fit: cover;
  border-radius: 12px;
}

.category-name {
  font-size: 14px;
  color: var(--color-text-primary);
  font-weight: 500;
}

/* ── Product Scroll (热销) ── */
.scroll-wrapper {
  position: relative;
}

.product-scroll {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding: 4px 4px 14px;
  scroll-behavior: smooth;
  -webkit-overflow-scrolling: touch;
}

.product-scroll::-webkit-scrollbar {
  height: 8px;
}

.product-scroll::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 4px;
}

.product-scroll::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 4px;
}

.product-scroll::-webkit-scrollbar-thumb:hover {
  background: #909399;
}

.product-scroll > .product-card {
  flex: 0 0 240px;
}

.scroll-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-60%);
  z-index: 5;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: #fff;
  box-shadow: var(--shadow-md);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-primary);
  transition: all var(--transition-fast);
}

.scroll-arrow:hover {
  background: var(--color-primary);
  color: #fff;
  box-shadow: var(--shadow-lg);
}

.scroll-left {
  left: -8px;
}

.scroll-right {
  right: -8px;
}

/* ── Product Grid (新品) ── */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

/* ── Product Card (共用) ── */
.product-card {
  background: #fff;
  border-radius: var(--radius-md);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  border: 1px solid var(--color-border-light);
}

.product-card:hover {
  transform: translateY(-5px);
  box-shadow: var(--shadow-lg);
}

.product-image-wrapper {
  position: relative;
  overflow: hidden;
}

.product-card:hover .product-image-wrapper :deep(img) {
  transform: scale(1.05);
  transition: transform 0.3s;
}

.product-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-dark));
  color: #fff;
  font-size: 12px;
  border-radius: 4px;
  z-index: 3;
}

.new-badge {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
}

.product-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
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

.product-info {
  padding: 14px;
}

.product-name {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text-primary);
}

.product-desc {
  font-size: 12px;
  color: var(--color-text-placeholder);
  margin: 0 0 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  font-size: 20px;
  font-weight: 700;
  color: var(--color-price);
}

.product-sales {
  font-size: 12px;
  color: var(--color-text-placeholder);
}

/* ── Carousel overrides ── */
.hero-section :deep(.el-carousel__indicator .el-carousel__button) {
  width: 36px;
  height: 8px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.5);
}

.hero-section :deep(.el-carousel__indicator.is-active .el-carousel__button) {
  background: #fff;
  width: 52px;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.6);
}

.hero-section :deep(.el-carousel__arrow) {
  width: 48px;
  height: 48px;
  font-size: 18px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(4px);
}

.hero-section :deep(.el-carousel__arrow:hover) {
  background: rgba(255, 255, 255, 0.5);
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .hero-content h2 {
    font-size: 28px;
  }

  .hero-content p {
    font-size: 14px;
  }

  .section-title {
    font-size: 20px;
  }

  .category-grid {
    grid-template-columns: repeat(4, 1fr);
    gap: 10px;
  }

  .category-card {
    padding: 16px 8px;
  }

  .category-icon {
    font-size: 28px;
  }

  .product-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .product-scroll > .product-card {
    flex: 0 0 180px;
  }
}
</style>
