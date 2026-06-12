<template>
  <div class="dashboard-container">
    <el-row :gutter="20" class="stat-row" v-loading="overviewLoading">
      <el-col :span="6" v-for="item in statCards" :key="item.key">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: item.color }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div v-loading="chartsLoading">
      <el-row :gutter="20" class="chart-row">
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>近 7 日销售额</span>
              </div>
            </template>
            <div ref="salesChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>分类商品分布</span>
              </div>
            </template>
            <div ref="categoryChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :span="8">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>订单状态</span>
              </div>
            </template>
            <div ref="orderStatusChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="16">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>用户增长</span>
              </div>
            </template>
            <div ref="userGrowthChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>热销商品</span>
              </div>
            </template>
            <div ref="hotProductsChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>营收统计</span>
              </div>
            </template>
            <div class="revenue-container">
              <div class="revenue-item" v-for="item in revenueData" :key="item.label">
                <div class="revenue-label">{{ item.label }}</div>
                <div class="revenue-value">{{ item.value }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, markRaw, nextTick } from 'vue'
import * as echarts from 'echarts'
import { User, Goods, List, Grid } from '@element-plus/icons-vue'
import {
  getOverview,
  getSalesTrend,
  getCategoryDistribution,
  getOrderStatus,
  getUserGrowth,
  getHotProducts,
  getRevenue
} from '@/api/statistics'

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

const salesChartRef = ref(null)
const categoryChartRef = ref(null)
const orderStatusChartRef = ref(null)
const userGrowthChartRef = ref(null)
const hotProductsChartRef = ref(null)

let salesChart = null
let categoryChart = null
let orderStatusChart = null
let userGrowthChart = null
let hotProductsChart = null

const money = value => `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`

const disposeChart = chart => {
  chart?.dispose()
}

const initSalesChart = data => {
  disposeChart(salesChart)
  salesChart = echarts.init(salesChartRef.value)
  salesChart.setOption({
    tooltip: { trigger: 'axis', valueFormatter: value => money(value) },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(item => item.name) },
    yAxis: { type: 'value' },
    series: [{
      name: '销售额',
      type: 'line',
      smooth: true,
      data: data.map(item => Number(item.value || 0)),
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(46, 125, 50, 0.5)' },
          { offset: 1, color: 'rgba(46, 125, 50, 0.1)' }
        ])
      },
      lineStyle: { color: '#2e7d32', width: 2 },
      itemStyle: { color: '#2e7d32' }
    }]
  })
}

const initCategoryChart = data => {
  disposeChart(categoryChart)
  categoryChart = echarts.init(categoryChartRef.value)
  categoryChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['60%', '50%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data.map((item, index) => ({
        name: item.name,
        value: Number(item.value || 0),
        itemStyle: {
          color: ['#2e7d32', '#4caf50', '#81c784', '#ffb300', '#ff8f00', '#795548'][index % 6]
        }
      }))
    }]
  })
}

const initOrderStatusChart = data => {
  disposeChart(orderStatusChart)
  orderStatusChart = echarts.init(orderStatusChartRef.value)
  orderStatusChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    series: [{
      type: 'pie',
      radius: '70%',
      center: ['50%', '50%'],
      data: data.map(item => ({
        name: item.name,
        value: Number(item.value || 0),
        itemStyle: { color: item.color }
      })),
      label: { formatter: '{b}\n{d}%' },
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.35)' }
      }
    }]
  })
}

const initUserGrowthChart = data => {
  disposeChart(userGrowthChart)
  userGrowthChart = echarts.init(userGrowthChartRef.value)
  userGrowthChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '累计用户'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: data.map(item => item.month) },
    yAxis: [
      { type: 'value', name: '新增用户' },
      { type: 'value', name: '累计用户' }
    ],
    series: [
      {
        name: '新增用户',
        type: 'bar',
        data: data.map(item => Number(item.newUsers || 0)),
        itemStyle: { color: '#2e7d32' }
      },
      {
        name: '累计用户',
        type: 'line',
        yAxisIndex: 1,
        data: data.map(item => Number(item.totalUsers || 0)),
        lineStyle: { color: '#81c784' },
        itemStyle: { color: '#81c784' }
      }
    ]
  })
}

const initHotProductsChart = data => {
  disposeChart(hotProductsChart)
  hotProductsChart = echarts.init(hotProductsChartRef.value)
  hotProductsChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: data.map(item => item.name).reverse() },
    series: [{
      name: '销量',
      type: 'bar',
      data: data.map(item => Number(item.sales || 0)).reverse(),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#2e7d32' },
          { offset: 1, color: '#81c784' }
        ])
      },
      barWidth: 20
    }]
  })
}

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

    await nextTick()
    initSalesChart(salesRes.data || [])
    initCategoryChart(categoryRes.data || [])
    initOrderStatusChart(orderRes.data || [])
    initUserGrowthChart(userRes.data || [])
    initHotProductsChart(hotRes.data || [])

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
  salesChart?.resize()
  categoryChart?.resize()
  orderStatusChart?.resize()
  userGrowthChart?.resize()
  hotProductsChart?.resize()
}

onMounted(() => {
  loadOverview()
  loadCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeChart(salesChart)
  disposeChart(categoryChart)
  disposeChart(orderStatusChart)
  disposeChart(userGrowthChart)
  disposeChart(hotProductsChart)
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.chart-row {
  margin-bottom: 20px;
}

.chart-container {
  height: 300px;
}

.revenue-container {
  height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  padding: 20px;
}

.revenue-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: var(--color-bg-section);
  border-radius: 8px;
}

.revenue-label {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.revenue-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--color-primary);
}
</style>
