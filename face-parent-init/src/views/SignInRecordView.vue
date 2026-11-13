<template>
  <div class="record-container">
    <el-card class="record-card" v-loading="loading">
      <div v-if="!recordData.name || !recordData.date" class="empty-state">
        <el-empty :description="emptyText" image-size="150" />
      </div>

      <div v-else class="record-content">
        <div class="basic-info">
          <div class="info-item">
            <label class="label">{{ t.recordPerson }}</label>
            <span class="value">{{ recordData.name }}</span>
          </div>
          <div class="info-item">
            <label class="label">{{ t.recordDate }}</label>
            <span class="value">{{ formatDate(recordData.date) }}</span>
          </div>
          <div class="info-item">
            <label class="label">{{ t.recordTime }}</label>
            <span class="value">{{ recordData.time }}</span>
          </div>
        </div>

        <div class="advanced-info">
          <div class="info-item">
            <label class="label">{{ t.recordType }}</label>
            <el-tag :type="recordData.type === 'signin' ? 'success' : 'warning'">
              {{ recordData.type === 'signin' ? t.signin : t.checkout }}
            </el-tag>
          </div>
          <div class="info-item">
            <label class="label">{{ t.recordProject }}</label>
            <el-tag type="info">{{ recordData.project }}</el-tag>
          </div>
          <div class="info-item">
            <label class="label">{{ t.recordLocation }}</label>
            <span class="value">{{ recordData.location }}</span>
          </div>
        </div>

        <div v-if="recordData.photoUrl" class="photo-section">
          <label class="section-title">{{ t.recordPhoto }}</label>
          <div class="photo-container">
            <el-image
              :src="recordData.photoUrl"
              fit="contain"
              class="record-photo"
              :preview-src-list="[recordData.photoUrl]"
            >
              <template #placeholder>
                <div class="image-placeholder">{{ t.loading }}</div>
              </template>
              <template #error>
                <div class="image-error">{{ t.photoLoadFailed }}</div>
              </template>
            </el-image>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getParentRecord } from '@/api/record'
import t from '@/i18n/zh-CN'

const route = useRoute()
const loading = ref(false)
const emptyText = ref(t.noRecord)
const recordData = ref({
  name: '',
  date: '',
  time: '',
  type: 'signin',
  project: '',
  location: '',
  photoUrl: ''
})

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
}

const clearRecord = () => {
  recordData.value = {
    name: '',
    date: '',
    time: '',
    type: 'signin',
    project: '',
    location: '',
    photoUrl: ''
  }
}

const mapRecord = (data, recordType) => {
  const [date = '', time = ''] = (data.createTime || '').split(' ')
  return {
    name: data.studentName || '',
    date,
    time,
    type: recordType === 'checkout' ? 'checkout' : 'signin',
    project: data.project || data.checkType || t.punchDefault,
    location: data.school || '',
    photoUrl: data.avatar || ''
  }
}

const loadRecord = async () => {
  const id = route.query.id
  const type = route.query.type || 'checkin'
  const exp = route.query.exp
  const sign = route.query.sign

  if (!id || !exp || !sign) {
    emptyText.value = t.invalidLink
    clearRecord()
    return
  }

  loading.value = true
  try {
    const response = await getParentRecord({ id, type, exp, sign })
    const { data } = response

    if (data?.code === 200 && data.data) {
      recordData.value = mapRecord(data.data, type)
    } else {
      clearRecord()
      emptyText.value = t.expiredRecord
      ElMessage.warning(t.expiredRecord)
    }
  } catch {
    clearRecord()
    emptyText.value = t.expiredRecord
    ElMessage.warning(t.expiredRecord)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecord()
})
</script>

<style scoped>
.record-container {
  min-height: 100vh;
  padding: 20px;
  background: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%);
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.record-card {
  width: 100%;
  max-width: 800px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.record-content {
  padding: 20px 0;
}

.basic-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.advanced-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.label {
  font-weight: 600;
  color: #606266;
  min-width: 80px;
}

.value {
  color: #303133;
  font-size: 16px;
}

.photo-section {
  margin-top: 30px;
}

.section-title {
  display: block;
  font-weight: 600;
  color: #606266;
  margin-bottom: 16px;
  font-size: 16px;
}

.photo-container {
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.record-photo {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.image-placeholder,
.image-error {
  width: 400px;
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.empty-state {
  padding: 60px 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

@media (max-width: 768px) {
  .record-container {
    padding: 10px;
  }

  .basic-info,
  .advanced-info {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .record-photo {
    max-height: 300px;
  }

  .image-placeholder,
  .image-error {
    width: 100%;
    height: 250px;
  }
}
</style>
