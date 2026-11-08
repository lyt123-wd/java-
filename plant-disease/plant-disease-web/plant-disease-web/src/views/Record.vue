<template>
  <div style="max-width: 1200px; margin: 0 auto; padding: 20px;">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: bold; font-size: 16px">识别历史记录</span>
          <el-button type="success" size="small" @click="exportRecords">
            导出 CSV
          </el-button>
        </div>
      </template>

      <el-table :data="recordList" border stripe style="width: 100%">
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <img
              v-if="row.imageUrl"
              :src="imgBase + row.imageUrl"
              style="width: 80px; height: 80px; object-fit: cover; border-radius: 4px; cursor: pointer"
              @click="handleViewDetail(row)"
            />
            <span v-else>无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="cropName" label="作物" width="100" />
        <el-table-column prop="diseaseName" label="病害名称" width="150" />
        <el-table-column label="置信度" width="110">
          <template #default="{ row }">
            {{ (row.confidence * 100).toFixed(2) }}%
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="识别时间" width="180" />
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">详情</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :total="total"
        layout="total, prev, pager, next, jumper"
        style="margin-top: 16px; text-align: right"
        @current-change="fetchList"
        @size-change="fetchList"
      />
    </el-card>

    <!-- 识别详情弹窗 -->
    <el-dialog v-model="detailVisible" title="识别详情" width="650px">
      <div v-if="detailData">
        <div style="text-align: center; margin-bottom: 20px">
          <img
            :src="imgBase + detailData.imageUrl"
            style="max-width: 320px; max-height: 320px; border-radius: 8px"
          />
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="作物名称">{{ detailData.cropName }}</el-descriptions-item>
          <el-descriptions-item label="病害名称">{{ detailData.diseaseName }}</el-descriptions-item>
          <el-descriptions-item label="置信度">{{ (detailData.confidence * 100).toFixed(2) }}%</el-descriptions-item>
          <el-descriptions-item label="识别时间">{{ detailData.createTime }}</el-descriptions-item>
        </el-descriptions>

        <div style="margin-top: 20px">
          <h4 style="margin-bottom: 12px; color: #333">防治建议</h4>
          <el-collapse v-model="activeNames">
            <el-collapse-item title="发病特征" name="symptom">
              {{ detailData.symptom || '暂无数据' }}
            </el-collapse-item>
            <el-collapse-item title="发病诱因" name="cause">
              {{ detailData.cause || '暂无数据' }}
            </el-collapse-item>
            <el-collapse-item title="防治药剂" name="pesticide">
              {{ detailData.pesticide || '暂无数据' }}
            </el-collapse-item>
            <el-collapse-item title="农事操作" name="farming">
              {{ detailData.farming || '暂无数据' }}
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

// 图片访问基地址：开发环境由 .env.development 提供，生产环境留空走同源反向代理
const imgBase = import.meta.env.VITE_IMG_BASE_URL || ''

const recordList = ref([])
const total = ref(0)
const query = ref({
  pageNum: 1,
  pageSize: 5
})
const detailVisible = ref(false)
const detailData = ref(null)
const activeNames = ref(['symptom'])

// 获取分页列表
const fetchList = async () => {
  try {
    const res = await request.get('/record/page', {
      params: {
        pageNum: query.value.pageNum,
        pageSize: query.value.pageSize
      }
    })
    if (res.code === 200) {
      recordList.value = res.data.records
      total.value = res.data.total
    }
  } catch (err) {
    console.error('获取记录失败：', err)
  }
}

// 导出 CSV
const exportRecords = async () => {
  try {
    const blob = await request.get('/record/export', { responseType: 'blob' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '识别记录.csv'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (err) {
    console.error('导出失败：', err)
    ElMessage.error('导出失败')
  }
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await request.get(`/record/${row.id}`)
    if (res.code === 200) {
      detailData.value = res.data
      detailVisible.value = true
    }
  } catch {
    ElMessage.error('获取详情失败')
  }
}

// 删除记录
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该记录吗？', '提示', { type: 'warning' })
    const res = await request.delete(`/record/${id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      // 当前页只剩这条数据且不是第一页时回退一页，避免停留在空页
      if (recordList.value.length <= 1 && query.value.pageNum > 1) {
        query.value.pageNum -= 1
      }
      fetchList()
    }
  } catch {
    // 取消删除不处理
  }
}

onMounted(() => {
  fetchList()
})
</script>
