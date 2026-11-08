<template>
  <div style="max-width: 1200px; margin: 0 auto; padding: 20px;">
    <el-card>
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span style="font-weight: bold; font-size: 16px">病害知识库</span>
          <el-select
            v-model="cropFilter"
            placeholder="按作物筛选"
            style="width: 160px"
            clearable
          >
            <el-option v-for="crop in cropOptions" :key="crop" :label="crop" :value="crop" />
          </el-select>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :span="8" v-for="item in displayList" :key="item.id" style="margin-bottom: 16px">
          <el-card shadow="hover" style="height: 100%">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span style="font-weight: bold; font-size: 15px">{{ item.diseaseName }}</span>
                <el-tag size="small" type="info">{{ item.cropName }}</el-tag>
              </div>
            </template>
            <p style="color: #666; font-size: 14px; line-height: 1.6; min-height: 60px; margin: 0">
              {{ item.symptom ? item.symptom.slice(0, 60) + '...' : '暂无发病特征描述' }}
            </p>
            <div style="margin-top: 12px; text-align: right">
              <el-button type="primary" link style="padding: 0" @click="handleViewDetail(item)">
                查看详情
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="displayList.length === 0" description="暂无该作物病害数据" />
    </el-card>

    <!-- 病害详情弹窗 -->
    <el-dialog v-model="detailVisible" title="病害详情" width="600px">
      <div v-if="currentDetail">
        <el-descriptions :column="2" border style="margin-bottom: 16px">
          <el-descriptions-item label="作物名称">{{ currentDetail.cropName }}</el-descriptions-item>
          <el-descriptions-item label="病害名称">{{ currentDetail.diseaseName }}</el-descriptions-item>
        </el-descriptions>
        <el-collapse v-model="activeNames">
          <el-collapse-item title="发病特征" name="symptom">
            {{ currentDetail.symptom || '暂无数据' }}
          </el-collapse-item>
          <el-collapse-item title="发病诱因" name="cause">
            {{ currentDetail.cause || '暂无数据' }}
          </el-collapse-item>
          <el-collapse-item title="防治药剂" name="pesticide">
            {{ currentDetail.pesticide || '暂无数据' }}
          </el-collapse-item>
          <el-collapse-item title="农事操作" name="farming">
            {{ currentDetail.farming || '暂无数据' }}
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../utils/request'

const allList = ref([])
const cropFilter = ref('')
const detailVisible = ref(false)
const currentDetail = ref(null)
const activeNames = ref(['symptom'])

// 作物筛选项：从全量数据动态提取，数据库新增作物后自动出现
const cropOptions = computed(() => [...new Set(allList.value.map(i => i.cropName))])

// 数据量小（几十条），直接前端过滤，无需每次筛选都请求后端
const displayList = computed(() =>
  cropFilter.value ? allList.value.filter(i => i.cropName === cropFilter.value) : allList.value
)

// 获取病害列表（全量）
const fetchDiseaseList = async () => {
  try {
    const res = await request.get('/disease/list')
    if (res.code === 200) {
      allList.value = res.data
    }
  } catch (err) {
    console.error('获取病害列表失败', err)
  }
}

// 查看详情
const handleViewDetail = async (item) => {
  try {
    const res = await request.get(`/disease/${item.id}`)
    if (res.code === 200) {
      currentDetail.value = res.data
      detailVisible.value = true
    }
  } catch {
    // 错误已由拦截器处理
  }
}

onMounted(() => {
  fetchDiseaseList()
})
</script>
