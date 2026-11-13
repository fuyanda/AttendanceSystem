<template>
  <el-dialog :title="title" v-model="visible" width="720px" append-to-body @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="学生" prop="studentId" v-if="!fixedStudentId">
        <el-select
          v-model="form.studentId"
          filterable
          remote
          reserve-keyword
          placeholder="搜索学生姓名"
          :remote-method="searchStudents"
          :loading="studentLoading"
          style="width: 100%"
          @change="handleStudentChange"
        >
          <el-option
            v-for="item in studentOptions"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-else label="学生">
        <el-input :model-value="fixedStudentName" disabled />
      </el-form-item>
      <el-form-item label="科目" prop="subject">
        <el-select v-model="form.subject" placeholder="请选择科目" style="width: 100%">
          <el-option
            v-for="dict in cf_fee_subject"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="购买课时" prop="totalHours">
        <el-input-number v-model="form.totalHours" :min="1" :max="9999" />
      </el-form-item>
      <el-form-item label="课时费" prop="totalAmount">
        <el-input-number v-model="form.totalAmount" :min="0" :precision="2" :step="100" />
      </el-form-item>
      <el-form-item label="任课老师" prop="teacherUserId">
        <el-select
          v-model="form.teacherUserId"
          placeholder="请选择任课老师"
          filterable
          :disabled="!canSelectTeacher"
          style="width: 100%"
        >
          <el-option
            v-for="t in teachers"
            :key="t.userId"
            :label="t.nickName || t.userName"
            :value="t.userId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status" v-if="form.id">
        <el-radio-group v-model="form.status">
          <el-radio value="0">有效</el-radio>
          <el-radio value="1">关闭</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="已消课时" v-if="form.id">
        <el-input :model-value="`${form.usedHours || 0} / ${form.totalHours || 0}`" disabled />
      </el-form-item>
      <el-form-item label="排课时间">
        <div class="schedule-block">
          <div v-for="(row, index) in form.schedules" :key="index" class="schedule-row">
            <el-select v-model="row.weekDay" placeholder="星期" style="width: 120px">
              <el-option v-for="w in weekDayOptions" :key="w.value" :label="w.label" :value="w.value" />
            </el-select>
            <el-time-select
              v-model="row.startTime"
              start="07:00"
              step="00:15"
              end="22:00"
              placeholder="开始"
              style="width: 120px; margin-left: 8px"
            />
            <span class="schedule-sep">至</span>
            <el-time-select
              v-model="row.endTime"
              start="07:00"
              step="00:15"
              end="22:00"
              placeholder="结束"
              style="width: 120px"
            />
            <el-button link type="danger" icon="Delete" @click="removeSchedule(index)" />
          </div>
          <el-button type="primary" link icon="Plus" @click="addSchedule">添加排课</el-button>
        </div>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" type="textarea" placeholder="备注" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submit">确 定</el-button>
      <el-button @click="visible = false">取 消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { getCurrentInstance, reactive, ref, watch } from 'vue'
import { addFee, getFee, getFeeTeacherSelect, updateFee } from '@/api/face/fee'
import { listStudent } from '@/api/face/student'

const weekDayOptions = [
  { value: 1, label: '周一' },
  { value: 2, label: '周二' },
  { value: 3, label: '周三' },
  { value: 4, label: '周四' },
  { value: 5, label: '周五' },
  { value: 6, label: '周六' },
  { value: 7, label: '周日' }
]

const props = defineProps({
  fixedStudentId: { type: Number, default: null },
  fixedStudentName: { type: String, default: '' },
  deptId: { type: Number, default: null }
})

const emit = defineEmits(['success'])

const { proxy } = getCurrentInstance()
const { cf_fee_subject } = proxy.useDict('cf_fee_subject')

const visible = ref(false)
const title = ref('')
const formRef = ref(null)
const teachers = ref([])
const canSelectTeacher = ref(true)
const studentOptions = ref([])
const studentLoading = ref(false)
const selectedDeptId = ref(null)

const form = reactive({
  id: null,
  studentId: null,
  subject: null,
  totalHours: 10,
  usedHours: 0,
  totalAmount: 0,
  teacherUserId: null,
  status: '0',
  remark: '',
  schedules: []
})

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
  totalHours: [{ required: true, message: '请填写购买课时', trigger: 'blur' }],
  totalAmount: [{ required: true, message: '请填写课时费', trigger: 'blur' }],
  teacherUserId: [{ required: true, message: '请选择任课老师', trigger: 'change' }]
}

watch(() => props.deptId, (val) => {
  if (val) {
    selectedDeptId.value = val
    loadTeachers(val)
  }
})

function resetForm() {
  form.id = null
  form.studentId = props.fixedStudentId || null
  form.subject = null
  form.totalHours = 10
  form.usedHours = 0
  form.totalAmount = 0
  form.teacherUserId = null
  form.status = '0'
  form.remark = ''
  form.schedules = []
  proxy.resetForm('formRef')
}

function addSchedule() {
  form.schedules.push({ weekDay: 1, startTime: '14:00', endTime: '15:00' })
}

function removeSchedule(index) {
  form.schedules.splice(index, 1)
}

async function loadTeachers(deptId) {
  const res = await getFeeTeacherSelect(deptId)
  const data = res.data || {}
  canSelectTeacher.value = data.canSelectTeacher !== false
  teachers.value = data.teachers || []
  if (!form.teacherUserId && data.defaultTeacherUserId) {
    form.teacherUserId = data.defaultTeacherUserId
  }
}

async function searchStudents(query) {
  if (!query) {
    studentOptions.value = []
    return
  }
  studentLoading.value = true
  try {
    const res = await listStudent({ name: query, pageNum: 1, pageSize: 20 })
    studentOptions.value = res.rows || []
  } finally {
    studentLoading.value = false
  }
}

function handleStudentChange(studentId) {
  const found = studentOptions.value.find(s => s.id === studentId)
  if (found?.deptId) {
    selectedDeptId.value = found.deptId
    loadTeachers(found.deptId)
  }
}

async function openAdd() {
  resetForm()
  title.value = '新增缴费'
  if (props.fixedStudentId) {
    form.studentId = props.fixedStudentId
    studentOptions.value = [{ id: props.fixedStudentId, name: props.fixedStudentName }]
    if (props.deptId) {
      await loadTeachers(props.deptId)
    }
  } else {
    await loadTeachers(null)
  }
  visible.value = true
}

async function openEdit(id) {
  resetForm()
  title.value = '编辑缴费'
  const res = await getFee(id)
  Object.assign(form, res.data)
  if (!form.schedules?.length) {
    form.schedules = []
  }
  if (props.fixedStudentId) {
    form.studentId = props.fixedStudentId
  }
  await loadTeachers(props.deptId || selectedDeptId.value)
  visible.value = true
}

function handleClosed() {
  resetForm()
}

function submit() {
  formRef.value.validate(valid => {
    if (!valid) return
    const payload = { ...form, schedules: form.schedules.filter(s => s.weekDay && s.startTime && s.endTime) }
    const req = form.id ? updateFee(payload) : addFee(payload)
    req.then(() => {
      proxy.$modal.msgSuccess(form.id ? '修改成功' : '新增成功')
      visible.value = false
      emit('success')
    })
  })
}

defineExpose({ openAdd, openEdit })
</script>

<style scoped>
.schedule-block {
  width: 100%;
}
.schedule-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.schedule-sep {
  margin: 0 8px;
  color: #909399;
}
</style>
