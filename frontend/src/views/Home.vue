<template>
  <div class="home-container">
    <HomeHero :slides="heroSlides" @open="openPath" />

    <div v-if="userStore.userInfo" class="greeting-bar">
      <span>{{ greetingText }}，{{ userStore.userInfo.nickname || userStore.userInfo.username }}！今天想买点什么新鲜好物？</span>
    </div>

    <HomeCategoryGrid
      :categories="categories"
      :loading="categories.length === 0"
      @open="openCategory"
      @open-all="router.push('/shop')"
    />

    <HomeProductSection
      title="热销推荐"
      :products="hotProducts"
      :loading="hotLoading"
      scrollable
      badge-label="热销"
      @more="router.push({ path: '/shop', query: { orderBy: 'sales' } })"
      @open-product="openProduct"
      @add-cart="handleAddToCart"
    />

    <HomeProductSection
      title="新品上架"
      :products="newProducts"
      :loading="newLoading"
      badge-label="新品"
      badge-type="new"
      @more="router.push({ path: '/shop', query: { orderBy: 'create_time' } })"
      @open-product="openProduct"
      @add-cart="handleAddToCart"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import HomeHero from '@/components/home/HomeHero.vue'
import HomeCategoryGrid from '@/components/home/HomeCategoryGrid.vue'
import HomeProductSection from '@/components/home/HomeProductSection.vue'
import { getHotProducts, searchProducts } from '@/api/product'
import { getCategoryList } from '@/api/category'
import { getBannerList } from '@/api/banner'
import { useCart } from '@/composables/useCart'
import { useUserStore } from '@/store/user'
import { imageUtils } from '@/utils/imageUtils'
import { normalizeProduct, withPageDefaults } from '@/services/domainAdapters'
import { notify } from '@/services/uiFeedback'

const router = useRouter()
const userStore = useUserStore()
const { addToCart } = useCart()

const categories = ref([])
const hotProducts = ref([])
const newProducts = ref([])
const heroSlides = ref([])
const hotLoading = ref(false)
const newLoading = ref(false)

const defaultHeroSlides = [
  {
    title: '新鲜直采 品质溯源',
    subtitle: '从田间到餐桌，每一份都是自然的馈赠',
    btnText: '立即选购',
    link: '/shop',
    showTitle: 1,
    titleColor: '#ffffff',
    titleFontSize: 42,
    titleFontWeight: 700,
    subtitleColor: 'rgba(255,255,255,0.9)',
    subtitleFontSize: 18,
    showButton: 1,
    buttonColor: '#2e7d32',
    bg: 'linear-gradient(135deg, #1b5e20 0%, #2e7d32 40%, #4caf50 100%)',
    icon: '🌾'
  },
  {
    title: '助农增收 共建美好',
    subtitle: '支持乡村发展，享受绿色健康农产品',
    btnText: '了解更多',
    link: '/shop',
    showTitle: 1,
    titleColor: '#ffffff',
    titleFontSize: 42,
    titleFontWeight: 700,
    subtitleColor: 'rgba(255,255,255,0.9)',
    subtitleFontSize: 18,
    showButton: 1,
    buttonColor: '#2e7d32',
    bg: 'linear-gradient(135deg, #33691e 0%, #558b2f 40%, #7cb342 100%)',
    icon: '🌱'
  },
  {
    title: '限时优惠 爆款推荐',
    subtitle: '精选热销农产品，产地直发新鲜到家',
    btnText: '查看热销',
    link: '/shop?orderBy=sales',
    showTitle: 1,
    titleColor: '#ffffff',
    titleFontSize: 42,
    titleFontWeight: 700,
    subtitleColor: 'rgba(255,255,255,0.9)',
    subtitleFontSize: 18,
    showButton: 1,
    buttonColor: '#ff8f00',
    bg: 'linear-gradient(135deg, #e65100 0%, #ff8f00 40%, #ffb300 100%)',
    icon: '🔥'
  }
]

heroSlides.value = defaultHeroSlides

const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const openPath = path => router.push(path)
const openProduct = product => router.push(`/product/${product.id}`)
const openCategory = category => router.push({ path: '/shop', query: { categoryId: category.id } })
const getBannerImageUrl = image => imageUtils.getImageUrl(image)

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
      imageUrl: item.imageUrl || getBannerImageUrl(item.image),
      showTitle: item.showTitle ?? 1,
      titleColor: item.titleColor || '#ffffff',
      titleFontSize: Number(item.titleFontSize || 42),
      titleFontWeight: Number(item.titleFontWeight || 700),
      subtitleColor: item.subtitleColor || 'rgba(255,255,255,0.9)',
      subtitleFontSize: Number(item.subtitleFontSize || 18),
      showButton: item.showButton ?? 1,
      buttonColor: item.buttonColor || '#2e7d32'
    }))
    if (banners.length) heroSlides.value = banners
  } catch (error) {
    console.error(error)
  }
}

const loadHotProducts = async () => {
  hotLoading.value = true
  try {
    const res = await getHotProducts(8)
    hotProducts.value = (res.data || []).map(normalizeProduct)
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
    newProducts.value = withPageDefaults(res.data).list.map(normalizeProduct)
  } catch (error) {
    console.error(error)
  } finally {
    newLoading.value = false
  }
}

const handleAddToCart = async (product) => {
  if (product.stock <= 0) {
    notify.warning('商品已售罄')
    return
  }
  try {
    const success = await addToCart(product, 1)
    if (success) {
      notify.success({ message: `${product.name} 已加入购物车`, duration: 1500 })
    }
  } catch (error) {
    console.error(error)
    notify.error('加入购物车失败')
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

.greeting-bar {
  padding: 12px 24px;
  color: var(--color-primary);
  background: linear-gradient(90deg, var(--color-primary-bg), transparent);
  border-bottom: 1px solid var(--color-primary-lighter);
  text-align: center;
  font-size: var(--font-size-sm);
}
</style>
