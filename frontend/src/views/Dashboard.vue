<template>
  <div class="dashboard-container">
    <DashboardStatCards v-loading="overviewLoading" :items="statCards" />

    <div v-loading="chartsLoading">
      <el-row :gutter="20" class="dashboard-chart-row">
        <el-col :span="16">
          <DashboardChartCard ref="salesChartRef" title="近 7 日销售额" :option="salesOption" />
        </el-col>
        <el-col :span="8">
          <DashboardChartCard ref="categoryChartRef" title="分类商品分布" :option="categoryOption" />
        </el-col>
      </el-row>

      <el-row :gutter="20" class="dashboard-chart-row">
        <el-col :span="8">
          <DashboardChartCard ref="orderStatusChartRef" title="订单状态" :option="orderStatusOption" />
        </el-col>
        <el-col :span="16">
          <DashboardChartCard ref="userGrowthChartRef" title="用户增长" :option="userGrowthOption" />
        </el-col>
      </el-row>

      <el-row :gutter="20" class="dashboard-chart-row">
        <el-col :span="12">
          <DashboardChartCard ref="hotProductsChartRef" title="热销商品" :option="hotProductsOption" />
        </el-col>
        <el-col :span="12">
          <DashboardRevenuePanel :items="revenueData" />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, markRaw } from 'vue'
import { User, Goods, List, Grid } from '@element-plus/icons-vue'
import DashboardStatCards from '@/components/admin/DashboardStatCards.vue'
import DashboardChartCard from '@/components/admin/DashboardChartCard.vue'
import DashboardRevenuePanel from '@/components/admin/DashboardRevenuePanel.vue'
import {
  getOverview,
  getSalesTrend,
  getCategoryDistribution,
  getOrderStatus,
  getUserGrowth,
  getHotProducts,
  getRevenue
} from '@/api/statistics'
import {
  money,
  createSalesOption,
  createCategoryOption,
  createOrderStatusOption,
  createUserGrowthOption,
  createHotProductsOption
} from '@/services/dashboardCharts'

const overviewLoading = ref(false)
const chartsLoading = ref(false)

const statCards = ref([
  { key: 'user', label: '用户总数', value: 0, icon: markRaw(User), color: 'linear-gradient(135deg, #2e7d32 0%, #4caf50 100%)' },
  { key: 'product', label: '商品总数', value: 0, icon: markRaw(Goods), color: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' },
  { key: 'order', label: '订单总数', value: 0, icon: markRaw(List), color: 'linear-gradient(135deg, #ff8f00 0%, #ffb300 100%)' },
  { key: 'category', label: '分类总数', value: 0, icon: markRaw(Grid), color: 'linear-gradient(135deg, #795548 0%, #a1887f 100%)' }
])

const revenueData = ref([
  { label: '今日营收', value: '¥0' },
  { label: '本周营收', value: '¥0' },
  { label: '本月营收', value: '¥0' },
  { label: '本年营收', value: '¥0' }
])

const salesOption = ref(null)
const categoryOption = ref(null)
const orderStatusOption = ref(null)
const userGrowthOption = ref(null)
const hotProductsOption = ref(null)

const salesChartRef = ref(null)
const categoryChartRef = ref(null)
const orderStatusChartRef = ref(null)
const userGrowthChartRef = ref(null)
const hotProductsChartRef = ref(null)

const loadOverview = async () => {
  overviewLoading.value = true
  try {
    const res = await getOverview()
    statCards.value[0].value = res.data.userCount || 0
    statCards.value[1].value = res.data.productCount || 0
    statCards.value[2].value = res.data.orderCount || 0
    statCards.value[3].value = res.data.categoryCount || 0
  } catch (error) {
    console.error(error)
  } finally {
    overviewLoading.value = false
  }
}

const loadCharts = async () => {
  chartsLoading.value = true
  try {
    const [salesRes, categoryRes, orderRes, userRes, hotRes, revenueRes] = await Promise.all([
      getSalesTrend(),
      getCategoryDistribution(),
      getOrderStatus(),
      getUserGrowth(),
      getHotProducts(),
      getRevenue()
    ])

    salesOption.value = createSalesOption(salesRes.data || [])
    categoryOption.value = createCategoryOption(categoryRes.data || [])
    orderStatusOption.value = createOrderStatusOption(orderRes.data || [])
    userGrowthOption.value = createUserGrowthOption(userRes.data || [])
    hotProductsOption.value = createHotProductsOption(hotRes.data || [])

    revenueData.value = [
      { label: '今日营收', value: money(revenueRes.data?.todayRevenue) },
      { label: '本周营收', value: money(revenueRes.data?.weekRevenue) },
      { label: '本月营收', value: money(revenueRes.data?.monthRevenue) },
      { label: '本年营收', value: money(revenueRes.data?.yearRevenue) }
    ]
  } catch (error) {
    console.error(error)
  } finally {
    chartsLoading.value = false
  }
}

const handleResize = () => {
  salesChartRef.value?.resize()
  categoryChartRef.value?.resize()
  orderStatusChartRef.value?.resize()
  userGrowthChartRef.value?.resize()
  hotProductsChartRef.value?.resize()
}

onMounted(() => {
  loadOverview()
  loadCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard-chart-row {
  margin-bottom: var(--spacing-lg);
}
</style>
