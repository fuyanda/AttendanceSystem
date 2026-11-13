<template>
  <div class="app-container schedule-page">
    <el-card shadow="never" class="toolbar-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="老师">
          <el-select
            v-model="teacherUserId"
            filterable
            clearable
            placeholder="全部老师"
            style="width: 220px"
            @change="loadCalendar"
          >
            <el-option
              v-for="t in teachers"
              :key="t.userId"
              :label="t.nickName || t.userName"
              :value="t.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Plus" v-hasPermi="['face:schedule:add']" @click="openCreate()">新增排课</el-button>
          <el-button icon="Refresh" @click="loadCalendar">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="calendar-card" v-loading="loading">
      <el-calendar v-model="calendarDate">
        <template #date-cell="{ data }">
          <div
            v-for="daySessions in [sessionsOfDay(data.day)]"
            :key="data.day"
            class="cal-cell"
            @click.stop="onDayClick(data)"
          >
            <div class="cal-day">
              <span>{{ dayNum(data.day) }}</span>
              <span v-if="daySessions.length > 0" class="cal-count">{{ daySessions.length }}</span>
            </div>
            <div class="cal-list" @click.stop>
              <div
                v-for="item in daySessions"
                :key="item.id"
                class="cal-item"
                :title="formatSessionFull(item)"
                @click.stop="openEdit(item)"
              >
                {{ formatSessionLine(item) }}
              </div>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" :width="form.id ? '640px' : '880px'" append-to-body destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="老师" prop="teacherUserId">
              <el-select v-model="form.teacherUserId" filterable style="width: 100%">
                <el-option
                  v-for="t in teachers"
                  :key="t.userId"
                  :label="t.nickName || t.userName"
                  :value="t.userId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科目" prop="subject">
              <el-select v-model="form.subject" placeholder="请选择科目" style="width: 100%">
                <el-option v-for="dict in cf_fee_subject" :key="dict.value" :label="dict.label" :value="dict.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="学生" prop="studentIds">
          <ScheduleStudentPicker v-model="form.studentIds" />
        </el-form-item>
        <template v-if="form.id">
          <el-form-item label="上课日期" prop="classDate">
            <el-date-picker v-model="form.classDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
          </el-form-item>
          <el-form-item label="时间" required>
            <div class="time-row">
              <el-form-item prop="startHm" class="time-item">
                <el-time-select v-model="form.startHm" start="07:00" step="00:15" end="22:00" placeholder="开始" style="width: 140px" />
              </el-form-item>
              <span class="time-sep">至</span>
              <el-form-item prop="endHm" class="time-item">
                <el-time-select v-model="form.endHm" start="07:00" step="00:15" end="22:00" placeholder="结束" style="width: 140px" />
              </el-form-item>
              <el-tag v-if="periodHint" type="info" effect="plain" class="period-tag">{{ periodHint }}</el-tag>
            </div>
          </el-form-item>
        </template>
        <el-form-item v-else label="上课时段" required>
          <div class="slots-block">
            <div v-for="(slot, index) in timeSlots" :key="index" class="slot-row">
              <el-date-picker
                v-model="slot.classDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="日期"
                style="width: 150px"
              />
              <el-time-select v-model="slot.startHm" start="07:00" step="00:15" end="22:00" placeholder="开始" style="width: 120px" />
              <span class="time-sep">至</span>
              <el-time-select v-model="slot.endHm" start="07:00" step="00:15" end="22:00" placeholder="结束" style="width: 120px" />
              <el-button v-if="timeSlots.length > 1" link type="danger" icon="Delete" @click="removeTimeSlot(index)" />
            </div>
            <div class="slots-actions">
              <el-button link type="primary" icon="Plus" @click="addTimeSlot">添加时段</el-button>
              <el-button link type="primary" icon="CopyDocument" @click="duplicateLastSlot">复制上一行</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="form.id" type="danger" plain v-hasPermi="['face:schedule:remove']" @click="handleDelete">删除</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ScheduleTeacher">
import { getCurrentInstance, onMounted, reactive, ref, watch, computed } from 'vue'
import useUserStore from '@/store/modules/user'
import ScheduleStudentPicker from './components/ScheduleStudentPicker.vue'
import { batchSchedule, delSchedule, getSchedule, listScheduleTeachers, teacherCalendar, updateSchedule } from '@/api/face/schedule'

const { proxy } = getCurrentInstance()
const { cf_fee_subject } = proxy.useDict('cf_fee_subject')
const userStore = useUserStore()

/** 角色键含 teacher 时默认只看本人；不用 auth.hasRole，避免 admin 被当成任意角色 */
const isTeacherRole = computed(() => (userStore.roles || []).includes('teacher'))
const showAllTeachers = computed(() => teacherUserId.value == null)

const teachers = ref([])
const teacherUserId = ref(null)
const calendarDate = ref(new Date())
const loading = ref(false)
const sessions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增排课')
const formRef = ref()
const timeSlots = ref([])

const form = reactive({
  id: null,
  teacherUserId: null,
  subject: null,
  classDate: null,
  startHm: null,
  endHm: null,
  studentIds: [],
  remark: ''
})

const rules = computed(() => {
  const base = {
    teacherUserId: [{ required: true, message: '请选择老师', trigger: 'change' }],
    subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
    studentIds: [{ required: true, type: 'array', min: 1, message: '请选择学生', trigger: 'change' }]
  }
  if (form.id) {
    base.classDate = [{ required: true, message: '请选择日期', trigger: 'change' }]
    base.startHm = [{ required: true, message: '请选择开始时间', trigger: 'change' }]
    base.endHm = [{ required: true, message: '请选择结束时间', trigger: 'change' }]
  }
  return base
})

const periodHint = computed(() => {
  if (!form.startHm) return ''
  const h = Number(String(form.startHm).split(':')[0])
  return h < 12 ? '上午' : '下午'
})

watch(calendarDate, () => {
  loadCalendar()
})

function createEmptySlot(classDate) {
  return { classDate: classDate || null, startHm: '14:00', endHm: '15:00' }
}

function resetTimeSlots(classDate) {
  timeSlots.value = [createEmptySlot(classDate)]
}

function addTimeSlot() {
  const last = timeSlots.value[timeSlots.value.length - 1]
  timeSlots.value.push(createEmptySlot(last?.classDate))
}

function duplicateLastSlot() {
  const last = timeSlots.value[timeSlots.value.length - 1]
  if (!last) {
    addTimeSlot()
    return
  }
  timeSlots.value.push({ ...last })
}

function removeTimeSlot(index) {
  if (timeSlots.value.length <= 1) {
    return
  }
  timeSlots.value.splice(index, 1)
}

function dayNum(day) {
  return String(day).slice(8)
}

function monthRange(date) {
  const d = date instanceof Date ? date : new Date(date)
  const y = d.getFullYear()
  const m = d.getMonth()
  const begin = new Date(y, m, 1)
  const end = new Date(y, m + 1, 0)
  const fmt = (x) => {
    const mm = String(x.getMonth() + 1).padStart(2, '0')
    const dd = String(x.getDate()).padStart(2, '0')
    return `${x.getFullYear()}-${mm}-${dd}`
  }
  return { beginDate: fmt(begin), endDate: fmt(end) }
}

const sessionsByDay = computed(() => {
  const map = {}
  for (const s of sessions.value) {
    const day = String(s.startTime || '').slice(0, 10)
    if (!map[day]) {
      map[day] = []
    }
    map[day].push(s)
  }
  return map
})

function sessionsOfDay(day) {
  return sessionsByDay.value[day] || []
}

function formatHm(dt) {
  if (!dt) return ''
  const t = String(dt)
  return t.length >= 16 ? t.slice(11, 16) : t
}

function formatSessionBrief(item) {
  const period = item.periodLabel || ''
  return `${period}${formatHm(item.startTime)}`.trim()
}

function formatSessionLine(item) {
  const subject = item.subjectLabel || item.subject || ''
  if (showAllTeachers.value) {
    return `${formatSessionBrief(item)} · ${item.teacherName || '-'} · ${subject}`
  }
  return `${formatSessionBrief(item)} · ${subject}`
}

function formatSessionFull(item) {
  const period = item.periodLabel || ''
  const range = `${formatHm(item.startTime)}-${formatHm(item.endTime)}`
  const subject = item.subjectLabel || item.subject || ''
  const teacher = item.teacherName || '-'
  const count = item.studentCount || 0
  return `${period} ${range} · ${teacher} · ${subject} · ${count}人`.trim()
}

async function loadTeachers() {
  const res = await listScheduleTeachers()
  teachers.value = res.data || []
  applyDefaultTeacher()
}

function applyDefaultTeacher() {
  if (isTeacherRole.value) {
    const selfId = Number(userStore.id)
    const self = teachers.value.find((t) => Number(t.userId) === selfId)
    teacherUserId.value = self ? self.userId : selfId || null
  } else {
    teacherUserId.value = null
  }
}

async function loadCalendar() {
  loading.value = true
  try {
    const range = monthRange(calendarDate.value)
    const params = {
      beginDate: range.beginDate,
      endDate: range.endDate
    }
    if (teacherUserId.value != null && teacherUserId.value !== '') {
      params.teacherUserId = teacherUserId.value
    }
    const res = await teacherCalendar(params)
    sessions.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  form.id = null
  form.teacherUserId = teacherUserId.value || (isTeacherRole.value ? Number(userStore.id) : null)
  form.subject = null
  form.classDate = null
  form.startHm = null
  form.endHm = null
  form.studentIds = []
  form.remark = ''
}

function openCreate(day) {
  resetForm()
  resetTimeSlots(day || null)
  dialogTitle.value = '新增排课'
  dialogVisible.value = true
}

function onDayClick(data) {
  if (data.type !== 'current-month') return
  openCreate(data.day)
}

async function openEdit(item) {
  resetForm()
  dialogTitle.value = '编辑排课'
  dialogVisible.value = true
  const res = await getSchedule(item.id)
  const detail = res.data || {}
  form.id = detail.id
  form.teacherUserId = detail.teacherUserId
  form.subject = detail.subject
  form.classDate = String(detail.startTime || '').slice(0, 10)
  form.startHm = formatHm(detail.startTime)
  form.endHm = formatHm(detail.endTime)
  form.studentIds = detail.studentIds || []
  form.remark = detail.remark || ''
}

function buildBatchSlots() {
  const slots = []
  for (const slot of timeSlots.value) {
    if (!slot.classDate || !slot.startHm || !slot.endHm) {
      continue
    }
    slots.push({
      startTime: `${slot.classDate} ${slot.startHm}:00`,
      endTime: `${slot.classDate} ${slot.endHm}:00`
    })
  }
  return slots
}

function buildPayload() {
  return {
    id: form.id,
    teacherUserId: form.teacherUserId,
    subject: form.subject,
    startTime: `${form.classDate} ${form.startHm}:00`,
    endTime: `${form.classDate} ${form.endHm}:00`,
    studentIds: form.studentIds,
    remark: form.remark
  }
}

function submitForm() {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (form.id) {
      if (form.endHm <= form.startHm) {
        proxy.$modal.msgError('结束时间须晚于开始时间')
        return
      }
      const payload = buildPayload()
      await updateSchedule(payload)
      proxy.$modal.msgSuccess('修改成功')
      dialogVisible.value = false
      if (payload.teacherUserId) {
        teacherUserId.value = payload.teacherUserId
      }
      loadCalendar()
      return
    }
    const slots = buildBatchSlots()
    if (!slots.length) {
      proxy.$modal.msgError('请至少填写一个完整上课时段')
      return
    }
    for (const slot of slots) {
      if (slot.endTime <= slot.startTime) {
        proxy.$modal.msgError('存在结束时间不晚于开始时间的时段')
        return
      }
    }
    const batchPayload = {
      teacherUserId: form.teacherUserId,
      subject: form.subject,
      remark: form.remark,
      studentIds: form.studentIds,
      slots
    }
    const res = await batchSchedule(batchPayload)
    const result = res.data || {}
    if (result.failCount > 0) {
      proxy.$modal.msgWarning(`成功 ${result.successCount} 条，失败 ${result.failCount} 条`)
    } else {
      proxy.$modal.msgSuccess(`成功创建 ${result.successCount || slots.length} 条排课`)
    }
    dialogVisible.value = false
    if (form.teacherUserId) {
      teacherUserId.value = form.teacherUserId
    }
    loadCalendar()
  })
}

function handleDelete() {
  proxy.$modal.confirm('确认删除该排课吗？').then(async () => {
    await delSchedule(form.id)
    proxy.$modal.msgSuccess('删除成功')
    dialogVisible.value = false
    loadCalendar()
  }).catch(() => {})
}

onMounted(async () => {
  await loadTeachers()
  await loadCalendar()
})
</script>

<style scoped>
.schedule-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.toolbar-card :deep(.el-card__body) {
  padding: 16px 16px 0;
}
.calendar-card :deep(.el-calendar-table .el-calendar-day) {
  height: 128px;
  padding: 6px;
  vertical-align: top;
  box-sizing: border-box;
}
.calendar-card :deep(.el-calendar-table td.is-selected .el-calendar-day),
.calendar-card :deep(.el-calendar-table td .el-calendar-day:hover) {
  background-color: #f5f7fa;
}
.cal-cell {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  cursor: pointer;
}
.cal-day {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  font-weight: 600;
  margin-bottom: 4px;
  line-height: 1.2;
}
.cal-count {
  min-width: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #409eff;
  color: #fff;
  font-size: 11px;
  font-weight: 500;
  text-align: center;
  line-height: 16px;
}
.cal-list {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
  overflow-x: hidden;
  overflow-y: auto;
  padding-right: 2px;
}
.cal-list::-webkit-scrollbar {
  width: 4px;
}
.cal-list::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 2px;
}
.cal-item {
  flex-shrink: 0;
  background: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 11px;
  line-height: 1.45;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cal-item:hover {
  background: #d9ecff;
}
.time-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.time-item {
  margin-bottom: 0;
}
.time-sep {
  color: #909399;
}
.period-tag {
  margin-left: 4px;
}

.slots-block {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.slot-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.slots-actions {
  display: flex;
  gap: 12px;
}
</style>
