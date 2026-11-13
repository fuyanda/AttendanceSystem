<template>
  <div class="app-container fee-consume-page">
    <el-card shadow="never" class="filter-card">
      <template #header>
        <span>消课信息</span>
      </template>
      <el-form :inline="true" label-width="88px">
        <el-form-item v-if="context.canSelectTeacher" label="任课老师">
          <el-select
            v-model="form.teacherUserId"
            placeholder="请选择老师"
            filterable
            clearable
            style="width: 200px"
            @change="handleTeacherChange"
          >
            <el-option
              v-for="t in teachers"
              :key="t.userId"
              :label="t.nickName || t.userName"
              :value="t.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="消课时间">
          <el-date-picker
            v-model="form.consumeTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择消课时间"
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="消课科目">
          <el-select
            v-model="form.subject"
            placeholder="请选择科目"
            style="width: 150px"
            @change="loadStudents"
          >
            <el-option
              v-for="s in subjectOptions"
              :key="s.value"
              :label="s.label"
              :value="s.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="消课节数">
          <el-input-number v-model="form.hours" :min="1" :max="10" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="可选" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadStudents">查询学生</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="student-card">
      <template #header>
        <div class="student-card__header">
          <span>报读学生（可多选，支持班课）</span>
          <el-button
            type="primary"
            :disabled="!selectedFeeIds.length"
            :loading="submitting"
            v-hasPermi="['face:fee:consume']"
            @click="handleBatchConsume"
          >统一消课（已选 {{ selectedFeeIds.length }} 人）</el-button>
        </div>
      </template>
      <el-table
        v-loading="loading"
        :data="studentList"
        @selection-change="handleSelectionChange"
        :empty-text="emptyTableText"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="学生" prop="studentName" min-width="100" />
        <el-table-column label="科目" prop="subjectLabel" width="90" />
        <el-table-column label="课时" width="100" align="center">
          <template #default="scope">
            {{ scope.row.usedHours || 0 }}/{{ scope.row.totalHours || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="剩余" width="80" align="center">
          <template #default="scope">
            <el-tag :type="remainingTagType(scope.row.remainingHours)" size="small">
              {{ scope.row.remainingHours }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上课时间" prop="scheduleSummary" min-width="180" show-overflow-tooltip />
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="FeeConsume">
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  batchConsumeFee,
  getConsumeContext,
  listConsumeStudents,
  listConsumeSubjects
} from '@/api/face/fee'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { cf_fee_subject } = proxy.useDict('cf_fee_subject')

const loading = ref(false)
const submitting = ref(false)
const context = ref({ canSelectTeacher: false, defaultTeacherUserId: null, deptId: null, nickName: '' })
const teachers = ref([])
const subjectOptions = ref([])
const studentList = ref([])
const selectedFeeIds = ref([])

const form = reactive({
  teacherUserId: null,
  consumeTime: '',
  subject: null,
  hours: 1,
  remark: ''
})

const emptyTableText = computed(() =>
  context.value.canSelectTeacher ? '请先选择老师和科目，再查询学生' : '请先选择科目，再查询学生'
)

function remainingTagType(n) {
  if (n > 3) return 'success'
  if (n >= 1) return 'warning'
  return 'danger'
}

function buildSubjectOptions(subjects) {
  const dictMap = Object.fromEntries((cf_fee_subject.value || []).map(d => [d.value, d.label]))
  subjectOptions.value = (subjects || []).map(v => ({ value: v, label: dictMap[v] || v }))
  if (subjectOptions.value.length && !form.subject) {
    form.subject = subjectOptions.value[0].value
  }
}

async function initPage() {
  const res = await getConsumeContext()
  context.value = res.data || {}
  form.consumeTime = proxy.parseTime(new Date(), '{y}-{m}-{d} {h}:{i}:{s}')

  teachers.value = context.value.teachers || []

  if (context.value.canSelectTeacher) {
    form.teacherUserId = null
  } else {
    form.teacherUserId = context.value.defaultTeacherUserId
    await loadSubjects()
    await loadStudents()
  }
}

async function loadSubjects() {
  if (!form.teacherUserId) return
  const res = await listConsumeSubjects(form.teacherUserId)
  buildSubjectOptions(res.data)
}

async function loadStudents() {
  if (!form.teacherUserId || !form.subject) {
    studentList.value = []
    return
  }
  loading.value = true
  selectedFeeIds.value = []
  try {
    const res = await listConsumeStudents(form.teacherUserId, form.subject)
    studentList.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function handleTeacherChange() {
  form.subject = null
  subjectOptions.value = []
  studentList.value = []
  await loadSubjects()
  if (form.subject) {
    await loadStudents()
  }
}

function handleSelectionChange(rows) {
  selectedFeeIds.value = rows.map(r => r.feeId)
}

function handleBatchConsume() {
  if (!form.consumeTime) {
    proxy.$modal.msgWarning('请选择消课时间')
    return
  }
  if (!selectedFeeIds.value.length) {
    proxy.$modal.msgWarning('请选择要消课的学生')
    return
  }
  proxy.$modal.confirm(`确认为 ${selectedFeeIds.value.length} 名学生各消课 ${form.hours} 节？`).then(() => {
    submitting.value = true
    return batchConsumeFee({
      teacherUserId: form.teacherUserId,
      subject: form.subject,
      consumeTime: form.consumeTime,
      hours: form.hours,
      remark: form.remark,
      feeIds: selectedFeeIds.value
    })
  }).then((res) => {
    proxy.$modal.msgSuccess('消课成功')
    const sessionId = res.data?.sessionId
    if (sessionId) {
      proxy.$modal.confirm('消课成功，是否立即进行课堂点评？').then(() => {
        const target = router.getRoutes().find(r =>
          r.path.endsWith('/classReview/edit') || r.path.includes('classReview/edit')
        )
        const path = target ? target.path : '/classReview/edit'
        router.push({ path, query: { sessionId } })
      }).catch(() => {
        loadStudents()
      })
    } else {
      loadStudents()
    }
  }).finally(() => {
    submitting.value = false
  })
}

onMounted(() => {
  initPage()
})
</script>

<style scoped>
.fee-consume-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.filter-card :deep(.el-card__body) {
  padding-bottom: 8px;
}
.student-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
