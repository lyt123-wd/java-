<template>
  <div style="max-width: 1200px; margin: 0 auto; padding: 20px;">
    <!-- 核心指标卡 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6" v-for="item in statCards" :key="item.key">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-card-icon" :style="{ background: item.bgColor }">
            <el-icon :size="28" :color="'#fff'">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="stat-card-info">
            <div class="stat-card-value">{{ overview[item.key] || 0 }}</div>
            <div class="stat-card-label">{{ item.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区：左折线趋势 + 右作物分类柱状 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span style="font-weight: bold">近7天识别趋势</span>
          </template>
          <div ref="lineChartRef" style="width: 100%; height: 360px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span style="font-weight: bold">按作物分类识别量</span>
          </template>
          <div ref="cropChartRef" style="width: 100%; height: 360px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { DataAnalysis, Document, Calendar, TrendCharts } from '@element-plus/icons-vue'

const overview = reactive({
  totalCount: 0,
  diseaseTypeCount: 0,
  todayCount: 0,
  weekCount: 0
})

const statCards = [
  { key: 'totalCount', label: '累计识别次数', icon: DataAnalysis, bgColor: '#2e7d32' },
  { key: 'diseaseTypeCount', label: '覆盖病害种类', icon: Document, bgColor: '#1565c0' },
  { key: 'todayCount', label: '今日识别次数', icon: Calendar, bgColor: '#e65100' },
  { key: 'weekCount', label: '近7天识别量', icon: TrendCharts, bgColor: '#7b1fa2' }
]

const lineChartRef = ref(null)
const cropChartRef = ref(null)
let lineChart = null
let cropChart = null

onMounted(async () => {
  await nextTick()
  window.addEventListener('resize', handleResize)

  // 加载概览指标
  try {
    const res = await request.get('/statistics/overview')
    if (res.code === 200) {
      Object.assign(overview, res.data)
    } else {
      ElMessage.error(res.msg || '获取概览数据失败')
    }
  } catch {
    // 网络错误已由拦截器统一提示
  }

  // 折线图：近7天趋势（失败时用空数据渲染，避免页面静默显示全 0）
  const lineRes = await request.get('/statistics/dailyCount').catch(() => null)
  const lineData = lineRes && lineRes.code === 200 ? lineRes.data : null
  if (!lineData) {
    ElMessage.error('获取近7天趋势数据失败')
  }
  lineChart = echarts.init(lineChartRef.value)
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: lineData ? lineData.dates : [] },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '识别次数',
      type: 'line',
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(46, 125, 50, 0.3)' },
          { offset: 1, color: 'rgba(46, 125, 50, 0.05)' }
        ])
      },
      lineStyle: { width: 2, color: '#2e7d32' },
      itemStyle: { color: '#2e7d32' },
      data: lineData ? lineData.counts : []
    }]
  })

  // 柱状图：按作物分类
  const cropRes = await request.get('/statistics/cropCount').catch(() => null)
  const cropData = cropRes && cropRes.code === 200 ? cropRes.data : []
  if (!cropRes || cropRes.code !== 200) {
    ElMessage.error('获取作物分类数据失败')
  }
  cropChart = echarts.init(cropChartRef.value)
  cropChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: cropData.map(i => i.name) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      name: '识别次数',
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#66bb6a' },
          { offset: 1, color: '#2e7d32' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      data: cropData.map(i => i.value)
    }]
  })
})

const handleResize = () => {
  lineChart?.resize()
  cropChart?.resize()
}

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  lineChart?.dispose()
  cropChart?.dispose()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-card-info {
  flex: 1;
}
.stat-card-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  line-height: 1.2;
  margin-bottom: 4px;
}
.stat-card-label {
  font-size: 14px;
  color: #666;
}
</style>
