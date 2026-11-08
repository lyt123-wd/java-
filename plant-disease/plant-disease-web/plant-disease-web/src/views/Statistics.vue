<template>
  <div style="max-width: 1200px; margin: 0 auto; padding: 20px;">
    <el-row :gutter="20">
      <!-- 左：病害分布饼图 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span style="font-weight: bold">病害类型占比</span>
          </template>
          <div ref="pieChartRef" style="width: 100%; height: 380px"></div>
        </el-card>
      </el-col>
      <!-- 右：高发病害TOP5 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span style="font-weight: bold">高发病害 TOP5</span>
          </template>
          <div ref="topChartRef" style="width: 100%; height: 380px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'
import { ElMessage } from 'element-plus'

const pieChartRef = ref(null)
const topChartRef = ref(null)
let pieChart = null
let topChart = null

onMounted(async () => {
  await nextTick()
  const pieRes = await request.get('/statistics/diseaseType').catch(() => null)
  const pieData = pieRes && pieRes.code === 200 ? pieRes.data : []
  if (!pieRes || pieRes.code !== 200) {
    ElMessage.error('获取病害统计数据失败')
  }

  // 饼图：病害占比（请求失败时渲染空数据，并已提示错误）
  pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}次 ({d}%)' },
    legend: { bottom: '5%', left: 'center' },
    color: ['#2e7d32', '#66bb6a', '#1565c0', '#e65100', '#7b1fa2'],
    series: [{
      name: '病害分布',
      type: 'pie',
      radius: ['40%', '70%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: pieData.map(i => ({ name: i.name, value: i.value }))
    }]
  })

  // TOP5：横向条形图（取前5名倒序展示）
  const topData = [...pieData].sort((a, b) => b.value - a.value).slice(0, 5).reverse()
  topChart = echarts.init(topChartRef.value)
  topChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', minInterval: 1 },
    yAxis: { type: 'category', data: topData.map(i => i.name) },
    series: [{
      name: '识别次数',
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: '#4caf50',
        borderRadius: [0, 4, 4, 0]
      },
      label: { show: true, position: 'right' },
      data: topData.map(i => i.value)
    }]
  })

  window.addEventListener('resize', handleResize)
})

const handleResize = () => {
  pieChart?.resize()
  topChart?.resize()
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  topChart?.dispose()
})
</script>
