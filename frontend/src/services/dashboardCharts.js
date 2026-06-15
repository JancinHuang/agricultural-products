import * as echarts from 'echarts'

export const money = value => `¥${Number(value || 0).toLocaleString('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
})}`

export const createSalesOption = data => ({
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

export const createCategoryOption = data => ({
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

export const createOrderStatusOption = data => ({
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

export const createUserGrowthOption = data => ({
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

export const createHotProductsOption = data => ({
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
