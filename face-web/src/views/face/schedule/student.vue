<template>
  <div class="app-container schedule-page">
    <el-card shadow="never" class="toolbar-card">
      <el-form :inline="true" @submit.prevent>
        <el-form-item label="学生">
          <el-select
            v-model="studentId"
            filterable
            remote
            clearable
            reserve-keyword
            placeholder="搜索学生姓名"
            :remote-method="searchStudents"
            :loading="studentLoading"
            style="width: 240px"
            @change="loadCalendar"
          >
            <el-option v-for="s in studentOptions" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button icon="Refresh" :disabled="!studentId" @click="loadCalendar">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="calendar-card" v-loading="loading">
      <el-calendar v-model="calendarDate">
        <template #date-cell="{ data }">
          <div v-for="daySessions in [sessionsOfDay(data.day)]" :key="data.day" class="cal-cell">
            <div class="cal-day">
              <span>{{ dayNum(data.day) }}</span>
              <span v-if="daySessions.length > 0" class="cal-count">{{ daySessions.length }}</span>
            </div>
            <div class="cal-list">
              <div
                v-for="item in daySessions"
                :key="item.id"
                class="cal-item"
                :title="formatSessionFull(item)"
                @click.stop="openDetail(item)"
              >
                {{ formatSessionBrief(item) }} · {{ item.teacherName || '-' }} · {{ item.subjectLabel || item.subject }}
              </div>
            </div>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <el-dialog title="课表详情" v-model="detailVisible" width="520px" append-to-body>
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="老师">{{ detail.teacherName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="科目">{{ detail.subjectLabel || detail.subject }}</el-descriptions-item>
        <el-descriptions-item label="时段">{{ detail.periodLabel }} {{ formatHm(detail.startTime) }}-{{ formatHm(detail.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="日期">{{ String(detail.startTime || '').slice(0, 10) }}</el-descriptions-item>
        <el-descriptions-item label="学员">
          {{ (detail.students || []).map(s => s.studentName).filter(Boolean).join('、') || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ScheduleStudent">
import { computed, onMounted, ref, watch } from 'vue'
import { listStudent } from '@/api/face/student'
import { getSchedule, studentCalendar } from '@/api/face/schedule'

const studentId = ref(null)
const studentOptions = ref([])
const studentLoading = ref(false)
const calendarDate = ref(new Date())
const loading = ref(false)
const sessions = ref([])
const detailVisible = ref(false)
const detail = ref(null)

watch(calendarDate, () => {
  loadCalendar()
})

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

function formatSessionFull(item) {
  const period = item.periodLabel || ''
  const range = `${formatHm(item.startTime)}-${formatHm(item.endTime)}`
  const teacher = item.teacherName || '-'
  const subject = item.subjectLabel || item.subject || ''
  return `${period} ${range} · ${teacher} · ${subject}`.trim()
}

async function searchStudents(query) {
  if (!query) return
  studentLoading.value = true
  try {
    const res = await listStudent({ name: query, pageNum: 1, pageSize: 20 })
    studentOptions.value = res.rows || []
  } finally {
    studentLoading.value = false
  }
}

async function loadCalendar() {
  if (!studentId.value) {
    sessions.value = []
    return
  }
  loading.value = true
  try {
    const range = monthRange(calendarDate.value)
    const res = await studentCalendar({
      studentId: studentId.value,
      beginDate: range.beginDate,
      endDate: range.endDate
    })
    sessions.value = res.data || []
  } finally {
    loading.value = false
  }
}

async function openDetail(item) {
  const res = await getSchedule(item.id)
  detail.value = res.data || item
  detailVisible.value = true
}


onMounted(() => {
  searchStudents(' ')
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
  background: #67c23a;
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
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 4px;
  padding: 1px 6px;
  font-size: 11px;
  line-height: 1.45;
  cursor: pointer;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.cal-item:hover {
  background: #e1f3d8;
}
</style>
