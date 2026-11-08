<template>
  <el-card>
    <div class="repair-toolbar">
      <el-select
        v-model="query.status"
        placeholder="工单状态"
        clearable
        style="width: 150px"
        @change="loadData"
      >
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-input
        v-model="query.type"
        placeholder="报修类型"
        clearable
        style="width: 150px"
        @keyup.enter="loadData"
      />
      <el-input
        v-model="query.keyword"
        placeholder="标题/描述关键词"
        clearable
        style="width: 220px"
        @keyup.enter="loadData"
      />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
    >
      <el-table-column prop="id" label="工单号" width="90" />
      <el-table-column prop="type" label="类型" width="100" />
      <el-table-column
        prop="title"
        label="标题"
        min-width="180"
        show-overflow-tooltip
      />
      <el-table-column prop="roomId" label="房间id" width="90" />
      <el-table-column prop="userId" label="用户id" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column
        prop="handleRemark"
        label="处理备注"
        min-width="160"
        show-overflow-tooltip
      />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            :disabled="!canHandle(row.status)"
            @click="openHandle(row)"
          >
            处理
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="repair-pagination"
      v-model:current-page="query.pageNum"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="handleSizeChange"
      @current-change="loadData"
    />

    <el-dialog v-model="dialogVisible" title="处理报修工单" width="480px">
      <el-form label-width="90px">
        <el-form-item label="工单标题">
          <span>{{ currentRow?.title }}</span>
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="handleForm.status" style="width: 100%">
            <el-option label="处理中" :value="1" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.handleRemark"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="填写处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitHandle">
          提交
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getRepairOrderPage,
  handleRepairOrder,
  RepairOrderInterface,
} from '@/api/repair'

const statusOptions = [
  { label: '待处理', value: 0 },
  { label: '处理中', value: 1 },
  { label: '已完成', value: 2 },
  { label: '已取消', value: 3 },
]

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<RepairOrderInterface[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const currentRow = ref<RepairOrderInterface | null>(null)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  status: undefined as number | undefined,
  type: '',
  keyword: '',
})

const handleForm = reactive({
  status: 1,
  handleRemark: '',
})

const statusLabel = (status: number) =>
  statusOptions.find((item) => item.value === status)?.label ?? '未知'

const statusTagType = (status: number) => {
  if (status === 1) return 'warning'
  if (status === 2) return 'success'
  if (status === 3) return 'info'
  return 'danger'
}

const canHandle = (status: number) => status === 0 || status === 1

const formatTime = (value?: string) =>
  value ? value.replace('T', ' ').slice(0, 19) : '-'

async function loadData() {
  loading.value = true
  try {
    const res = await getRepairOrderPage({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      status: query.status,
      type: query.type || undefined,
      keyword: query.keyword || undefined,
    })
    tableData.value = res.data?.records ?? []
    total.value = res.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.status = undefined
  query.type = ''
  query.keyword = ''
  query.pageNum = 1
  loadData()
}

function handleSizeChange(size: number) {
  query.pageSize = size
  query.pageNum = 1
  loadData()
}

function openHandle(row: RepairOrderInterface) {
  currentRow.value = row
  handleForm.status = row.status === 1 ? 2 : 1
  handleForm.handleRemark = row.handleRemark ?? ''
  dialogVisible.value = true
}

async function submitHandle() {
  if (!currentRow.value) return
  submitting.value = true
  try {
    await handleRepairOrder(currentRow.value.id, {
      status: handleForm.status,
      handleRemark: handleForm.handleRemark,
    })
    ElMessage.success('处理成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.repair-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.repair-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
