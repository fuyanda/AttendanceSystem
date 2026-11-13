<template>
  <div class="review-container">
    <el-card v-loading="loading" class="review-card">
      <el-empty v-if="!info.studentName" :description="emptyText" />
      <div v-else class="review-content">
        <h2 class="title">{{ info.studentName }} 的课堂点评</h2>
        <div class="meta">
          <p><strong>科目：</strong>{{ info.subjectLabel || '--' }}</p>
          <p><strong>老师：</strong>{{ info.teacherName || '--' }}</p>
          <p><strong>时间：</strong>{{ formatClassTime(info.classTime) }}</p>
        </div>

        <div v-if="info.classContent" class="block">
          <h3>上课内容</h3>
          <p class="text">{{ info.classContent }}</p>
          <div v-if="classImages.length" class="img-grid">
            <el-image
              v-for="(url, idx) in classImages"
              :key="'c-' + idx"
              :src="url"
              fit="cover"
              class="img"
              :preview-src-list="classImages"
              :initial-index="idx"
            />
          </div>
        </div>

        <div class="block">
          <h3>课堂表现</h3>
          <p>纪律专注：{{ info.disciplineScore || 0 }} 星</p>
          <p>活跃参与：{{ info.participationScore || 0 }} 星</p>
        </div>

        <div v-if="info.content" class="block">
          <h3>课堂评语</h3>
          <p class="text">{{ info.content }}</p>
          <div v-if="itemImages.length" class="img-grid">
            <el-image
              v-for="(url, idx) in itemImages"
              :key="'i-' + idx"
              :src="url"
              fit="cover"
              class="img"
              :preview-src-list="itemImages"
              :initial-index="idx"
            />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchClassReview } from '../api/review'

const API_BASE = (import.meta.env.VITE_API_BASE || '').replace(/\/$/, '')

const route = useRoute()
const loading = ref(false)
const emptyText = ref('记录不存在或已过期')
const info = ref({})

const toFullUrl = (path) => {
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  return `${API_BASE}${path}`
}

const normalizeImages = (raw) => {
  if (!raw) return []
  if (Array.isArray(raw)) return raw.filter(Boolean).map(toFullUrl)
  return []
}

const classImages = computed(() => normalizeImages(info.value.classImages))
const itemImages = computed(() => normalizeImages(info.value.images))

const formatClassTime = (value) => {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 19)
}

onMounted(async () => {
  const { id, exp, sign } = route.query
  if (!id || !exp || !sign) {
    emptyText.value = '链接无效，请从微信通知重新打开'
    return
  }
  loading.value = true
  try {
    const response = await fetchClassReview({ id, exp, sign })
    const body = response?.data
    if (body?.code === 200 && body.data) {
      info.value = body.data
    } else {
      info.value = {}
      emptyText.value = body?.msg || '记录不存在或已过期'
    }
  } catch (e) {
    info.value = {}
    emptyText.value = e?.response?.data?.msg || '网络异常，请稍后重试'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.review-container {
  min-height: 100vh;
  padding: 16px;
  background: #f5f7fa;
}
.review-card {
  max-width: 480px;
  margin: 0 auto;
}
.review-content {
  color: #303133;
}
.title {
  font-size: 18px;
  margin: 0 0 12px;
}
.meta p {
  margin: 6px 0;
  color: #606266;
  font-size: 14px;
}
.block {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}
.block h3 {
  font-size: 15px;
  margin: 0 0 8px;
}
.text {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.6;
  color: #606266;
  font-size: 14px;
}
.img-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-top: 10px;
}
.img {
  width: 100%;
  height: 96px;
  border-radius: 8px;
  overflow: hidden;
  background: #f2f3f5;
}
</style>
