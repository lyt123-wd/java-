<template>
  <div style="max-width: 1200px; margin: 0 auto; padding: 20px;">
    <el-row :gutter="20">
      <!-- 左侧：上传区域 -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <span style="font-weight: bold">图片上传</span>
          </template>
          <el-upload
            drag
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            accept="image/jpg,image/png,image/jpeg"
            :on-change="handleFileChange"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">将图片拖到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 jpg/png 格式，大小不超过 5MB</div>
            </template>
          </el-upload>
          <div v-if="imageUrl" style="margin-top: 20px; text-align: center">
            <img :src="imageUrl" style="max-width: 100%; max-height: 300px; border-radius: 8px" />
          </div>
          <el-button
            type="primary"
            style="width: 100%; margin-top: 20px"
            :loading="loading"
            :disabled="!imageFile"
            @click="handleRecognize"
          >
            {{ loading ? '识别中...' : '开始识别' }}
          </el-button>
        </el-card>
      </el-col>

      <!-- 右侧：识别结果 -->
      <el-col :span="14">
        <el-card v-if="result">
          <template #header>
            <span style="font-weight: bold">识别结果</span>
            <el-tag type="success" style="margin-left: 10px">识别完成</el-tag>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="作物名称">{{ result.cropName }}</el-descriptions-item>
            <el-descriptions-item label="病害名称">{{ result.diseaseName }}</el-descriptions-item>
            <el-descriptions-item label="置信度">{{ (result.confidence * 100).toFixed(2) }}%</el-descriptions-item>
            <!-- 调用格式化方法显示时间 -->
            <el-descriptions-item label="识别时间">{{ formatTime(result.createTime) }}</el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">防治建议</el-divider>
          <el-collapse v-model="activeNames">
            <el-collapse-item title="发病特征" name="symptom">
              <p>{{ result.symptom || '暂无数据' }}</p>
            </el-collapse-item>
            <el-collapse-item title="发病诱因" name="cause">
              <p>{{ result.cause || '暂无数据' }}</p>
            </el-collapse-item>
            <el-collapse-item title="防治药剂" name="pesticide">
              <p>{{ result.pesticide || '暂无数据' }}</p>
            </el-collapse-item>
            <el-collapse-item title="农事操作" name="farming">
              <p>{{ result.farming || '暂无数据' }}</p>
            </el-collapse-item>
          </el-collapse>
        </el-card>
        <el-card v-else>
          <el-empty description="上传图片后点击识别查看结果" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import request from '../utils/request'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'

const imageFile = ref(null)
const imageUrl = ref('')
const result = ref(null)
const loading = ref(false)
const activeNames = ref(['symptom'])

// 时间格式化：把 2026-10-26T12:53:22 转成 2026-10-26 12:53:22
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

// 文件校验（在 handleFileChange 中调用；auto-upload 为 false 时 el-upload 的 before-upload 钩子不会触发）
const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/jpg'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('仅支持 JPG/PNG 格式图片！')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB！')
    return false
  }
  return true
}

const handleFileChange = (file) => {
  if (!beforeUpload(file.raw)) return
  imageFile.value = file.raw
  // 本地预览
  const reader = new FileReader()
  reader.onload = (e) => {
    imageUrl.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
  result.value = null
}

const handleRecognize = async () => {
  if (!imageFile.value) {
    ElMessage.warning('请先上传图片')
    return
  }
  loading.value = true
  try {
    const formData = new FormData()
    formData.append('file', imageFile.value)
    const res = await request.post('/recognize/upload', formData)
    if (res.code === 200) {
      result.value = res.data
      ElMessage.success('识别完成')
    } else {
      ElMessage.error(res.msg || '识别失败')
    }
  } catch (err) {
    console.error('识别异常：', err)
    ElMessage.error('识别异常，请检查后端服务')
  } finally {
    loading.value = false
  }
}
</script>
