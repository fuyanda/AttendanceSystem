<template>
  <div class="student-picker" v-loading="loading">
    <div class="student-picker__toolbar">
      <el-input
        v-model="keyword"
        clearable
        placeholder="搜索学生姓名或校区"
        prefix-icon="Search"
        class="student-picker__search"
      />
      <div class="student-picker__actions">
        <el-button link type="primary" @click="selectFiltered">全选当前</el-button>
        <el-button link type="danger" @click="clearSelected">清空已选</el-button>
        <span class="student-picker__count">已选 {{ modelValue.length }} 人</span>
      </div>
    </div>
    <div class="student-picker__list">
      <el-checkbox-group :model-value="modelValue" @change="emit('update:modelValue', $event)">
        <section
          v-for="group in groupedStudents"
          :key="group.deptName"
          class="student-picker__group"
        >
          <div class="student-picker__group-head">
            <span class="student-picker__group-title">{{ group.deptName }}</span>
            <span class="student-picker__group-meta">{{ group.items.length }} 人</span>
            <el-button link type="primary" size="small" @click.stop="selectGroup(group)">全选本组</el-button>
          </div>
          <el-checkbox
            v-for="item in group.items"
            :key="item.id"
            :label="item.id"
            class="student-picker__item"
          >
            {{ item.name }}
          </el-checkbox>
        </section>
      </el-checkbox-group>
      <el-empty v-if="!loading && !groupedStudents.length" description="暂无学生" :image-size="48" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { listScheduleStudents } from '@/api/face/schedule'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const loading = ref(false)
const keyword = ref('')
const students = ref([])

const UNASSIGNED_DEPT = '未分配校区'

function compareName(a, b) {
  return (a.name || '').localeCompare(b.name || '', 'zh-CN')
}

function compareDept(a, b) {
  return (a.deptName || UNASSIGNED_DEPT).localeCompare(b.deptName || UNASSIGNED_DEPT, 'zh-CN')
}

const filteredStudents = computed(() => {
  const q = keyword.value.trim().toLowerCase()
  let list = students.value
  if (q) {
    list = list.filter((s) => {
      const name = (s.name || '').toLowerCase()
      const dept = (s.deptName || UNASSIGNED_DEPT).toLowerCase()
      return name.includes(q) || dept.includes(q)
    })
  }
  return [...list].sort((a, b) => {
    const deptCmp = compareDept(a, b)
    return deptCmp !== 0 ? deptCmp : compareName(a, b)
  })
})

const groupedStudents = computed(() => {
  const map = new Map()
  for (const student of filteredStudents.value) {
    const deptName = student.deptName || UNASSIGNED_DEPT
    if (!map.has(deptName)) {
      map.set(deptName, [])
    }
    map.get(deptName).push(student)
  }
  return Array.from(map.entries())
    .sort(([a], [b]) => a.localeCompare(b, 'zh-CN'))
    .map(([deptName, items]) => ({
      deptName,
      items: [...items].sort(compareName)
    }))
})

async function loadStudents() {
  loading.value = true
  try {
    const res = await listScheduleStudents()
    students.value = res.data || []
  } finally {
    loading.value = false
  }
}

function mergeIds(ids) {
  const merged = new Set([...props.modelValue, ...ids])
  emit('update:modelValue', Array.from(merged))
}

function selectFiltered() {
  mergeIds(filteredStudents.value.map((s) => s.id))
}

function selectGroup(group) {
  mergeIds(group.items.map((s) => s.id))
}

function clearSelected() {
  emit('update:modelValue', [])
}

onMounted(loadStudents)
</script>

<style scoped>
.student-picker {
  width: 100%;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 12px;
  background: #fafafa;
}
.student-picker__toolbar {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 8px;
}
.student-picker__search {
  width: 100%;
}
.student-picker__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.student-picker__count {
  margin-left: auto;
  color: #606266;
  font-size: 13px;
}
.student-picker__list {
  max-height: 260px;
  overflow-y: auto;
  padding: 4px 0;
}
.student-picker__list :deep(.el-checkbox-group) {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.student-picker__group {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  background: #fff;
  overflow: hidden;
}
.student-picker__group-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}
.student-picker__group-title {
  font-weight: 600;
  color: #303133;
  font-size: 13px;
}
.student-picker__group-meta {
  color: #909399;
  font-size: 12px;
}
.student-picker__group-head .el-button {
  margin-left: auto;
}
.student-picker__item {
  display: flex;
  align-items: center;
  margin: 0;
  width: 100%;
  height: auto;
  padding: 6px 12px;
  box-sizing: border-box;
}
.student-picker__item:hover {
  background: #f0f2f5;
}
.student-picker__item :deep(.el-checkbox__label) {
  font-weight: 500;
}
</style>
