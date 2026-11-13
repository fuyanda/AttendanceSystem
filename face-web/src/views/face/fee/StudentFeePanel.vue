<template>
  <div>
    <el-row :gutter="10" class="mb8" v-if="showToolbar">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['face:fee:add']">新增缴费</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="feeList">
      <el-table-column v-if="showStudentColumn" label="学生" align="center" prop="studentName" min-width="100" />
      <el-table-column label="科目" align="center" prop="subject" width="90">
        <template #default="scope">
          <dict-tag :options="cf_fee_subject" :value="scope.row.subject" />
        </template>
      </el-table-column>
      <el-table-column label="课时" align="center" width="100">
        <template #default="scope">
          {{ scope.row.usedHours || 0 }}/{{ scope.row.totalHours || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="剩余" align="center" width="80">
        <template #default="scope">
          <el-tag :type="remainingTagType(scope.row.remainingHours)" size="small">
            {{ scope.row.remainingHours }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="课时费" align="center" prop="totalAmount" width="100" />
      <el-table-column label="任课老师" align="center" prop="teacherName" width="100" />
      <el-table-column label="上课时间" align="center" prop="scheduleSummary" min-width="160" show-overflow-tooltip />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === '0' ? 'success' : 'info'" size="small">
            {{ scope.row.status === '0' ? '有效' : '关闭' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="240" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-hasPermi="['face:fee:edit']">编辑</el-button>
          <el-button
            v-if="scope.row.status === '0' && scope.row.remainingHours > 0"
            link
            type="warning"
            icon="RefreshLeft"
            @click="handleRefund(scope.row)"
            v-hasPermi="['face:fee:refund']"
          >退费</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['face:fee:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-if="showPagination && total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="loadList"
    />

    <FeeFormDialog
      ref="formDialogRef"
      :fixed-student-id="studentId"
      :fixed-student-name="studentName"
      :dept-id="deptId"
      @success="loadList"
    />
    <FeeRefundDialog ref="refundDialogRef" @success="loadList" />
  </div>
</template>

<script setup>
import { getCurrentInstance, onMounted, reactive, ref, watch } from 'vue'
import { delFee, listFee, listFeeByStudent } from '@/api/face/fee'
import FeeFormDialog from './components/FeeFormDialog.vue'
import FeeRefundDialog from './components/FeeRefundDialog.vue'

const props = defineProps({
  studentId: { type: Number, default: null },
  studentName: { type: String, default: '' },
  deptId: { type: Number, default: null },
  showStudentColumn: { type: Boolean, default: true },
  showToolbar: { type: Boolean, default: true },
  showPagination: { type: Boolean, default: true },
  externalQuery: { type: Object, default: null }
})

const { proxy } = getCurrentInstance()
const { cf_fee_subject } = proxy.useDict('cf_fee_subject')

const loading = ref(false)
const feeList = ref([])
const total = ref(0)
const formDialogRef = ref(null)
const refundDialogRef = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  studentName: null,
  subject: null,
  status: null
})

function remainingTagType(n) {
  if (n > 3) return 'success'
  if (n >= 1) return 'warning'
  return 'danger'
}

async function loadList() {
  loading.value = true
  try {
    if (props.studentId) {
      const res = await listFeeByStudent(props.studentId)
      feeList.value = res.data || []
      total.value = feeList.value.length
    } else {
      const params = { ...queryParams, ...(props.externalQuery || {}) }
      const res = await listFee(params)
      feeList.value = res.rows || []
      total.value = res.total || 0
    }
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  formDialogRef.value?.openAdd()
}

function handleEdit(row) {
  formDialogRef.value?.openEdit(row.id)
}

function handleRefund(row) {
  refundDialogRef.value?.open(row)
}

function handleDelete(row) {
  proxy.$modal.confirm(`确认删除「${row.studentName || props.studentName}」的缴费记录？`).then(() => {
    return delFee(row.id)
  }).then(() => {
    loadList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

watch(() => props.studentId, () => {
  loadList()
})

watch(() => props.externalQuery, () => {
  if (!props.studentId) {
    queryParams.pageNum = 1
    loadList()
  }
}, { deep: true })

onMounted(() => {
  loadList()
})

defineExpose({ loadList, handleAdd })
</script>
