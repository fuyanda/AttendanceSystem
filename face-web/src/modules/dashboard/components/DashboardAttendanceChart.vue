<template>
  <el-card shadow="never" class="dashboard-panel">
    <template #header><span class="dashboard-panel__title">今日出勤概览</span></template>
    <div ref="chartRef" class="dashboard-chart" />
  </el-card>
</template>
<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
const props = defineProps({
  chartData: { type: Object, default: () => ({ checkin: { signed: 0, unsigned: 0 }, checkout: { signed: 0, unsigned: 0 } }) }
})
const chartRef = ref(null)
let chartInstance = null
function renderChart() {
  if (!chartRef.value) return
  if (!chartInstance) chartInstance = echarts.init(chartRef.value)
  chartInstance.setOption({
    color: ['#22c55e', '#f59e0b'],
    tooltip: { trigger: 'axis' },
    legend: { top: 0 },
    grid: { left: 24, right: 24, bottom: 24, top: 40, containLabel: true },
    xAxis: { type: 'category', data: ['签到', '签退'] },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '已完成', type: 'bar', barWidth: 28, data: [props.chartData.checkin.signed, props.chartData.checkout.signed] },
      { name: '未完成', type: 'bar', barWidth: 28, data: [props.chartData.checkin.unsigned, props.chartData.checkout.unsigned] }
    ]
  })
}
function resizeChart() { chartInstance?.resize() }
onMounted(() => { renderChart(); window.addEventListener('resize', resizeChart) })
onBeforeUnmount(() => { window.removeEventListener('resize', resizeChart); chartInstance?.dispose(); chartInstance = null })
watch(() => props.chartData, renderChart, { deep: true })
</script>
<style scoped lang="scss">
.dashboard-panel { border: 1px solid var(--el-border-color-lighter); margin-bottom: 16px; }
.dashboard-panel__title { font-size: 15px; font-weight: 700; color: #1e293b; }
.dashboard-chart { width: 100%; height: 280px; }
</style>
