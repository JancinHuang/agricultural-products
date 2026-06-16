<template>
  <el-card>
    <template #header>
      <div class="card-header">
        <span>{{ title }}</span>
      </div>
    </template>
    <div ref="chartRef" class="dashboard-chart-card__chart"></div>
  </el-card>
</template>

<script setup>
import { onBeforeUnmount, ref, watch, nextTick } from 'vue'
import { use, init } from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([
  BarChart,
  LineChart,
  PieChart,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  CanvasRenderer
])

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  option: {
    type: Object,
    default: null
  }
})

const chartRef = ref(null)
let chart = null

const render = async () => {
  if (!props.option) return
  await nextTick()
  if (!chartRef.value) return
  if (!chart) chart = init(chartRef.value)
  chart.setOption(props.option, true)
}

const resize = () => chart?.resize()

watch(() => props.option, render, { immediate: true })

onBeforeUnmount(() => {
  chart?.dispose()
  chart = null
})

defineExpose({ resize })
</script>

<style scoped>
.dashboard-chart-card__chart {
  height: 300px;
}
</style>
