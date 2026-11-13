<template>
  <div class="review-edit-page">
    <div class="review-edit-page__inner" v-loading="loading">
      <div class="page-header">
        <el-button link icon="ArrowLeft" @click="goBack">返回</el-button>
        <span class="page-header__title">课堂点评</span>
        <span />
      </div>

      <div v-if="session.id" class="class-banner">
        <dict-tag :options="cf_fee_subject" :value="session.subject" />
        <div class="class-banner__time">{{ session.classTime }}</div>
        <div class="class-banner__teacher">{{ session.teacherName }}</div>
      </div>

      <section class="section-card">
        <div class="section-title">上课内容</div>
        <el-input
          v-model="form.classContent"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="这里是上课内容..."
          :disabled="readonly"
        />
        <div class="upload-block">
          <ReviewImageUpload
            v-model="classImagesStr"
            :limit="6"
            :file-size="10"
            :disabled="readonly"
          />
        </div>
      </section>

      <section
        v-for="item in form.items"
        :key="item.id"
        class="section-card student-card"
      >
        <div class="student-card__head">
          <div class="student-card__name">{{ item.studentName }}</div>
          <el-tag size="small" type="success">到课</el-tag>
        </div>
        <div class="student-card__stats">
          <span>剩余 {{ item.remainingHours || 0 }} 节</span>
          <span>本次扣 {{ item.consumeHours || 0 }} 节</span>
        </div>

        <div class="score-row">
          <span>纪律专注</span>
          <el-rate v-model="item.disciplineScore" :disabled="readonly" />
        </div>
        <div class="score-row">
          <span>活跃参与</span>
          <el-rate v-model="item.participationScore" :disabled="readonly" />
        </div>

        <el-input
          v-model="item.content"
          type="textarea"
          :rows="3"
          maxlength="1000"
          show-word-limit
          placeholder="这里输入课堂评语，仅针对此学员可见..."
          :disabled="readonly"
        />
        <div class="upload-block">
          <ReviewImageUpload
            v-model="item.imagesStr"
            :limit="6"
            :file-size="10"
            :disabled="readonly"
          />
        </div>
      </section>

      <div class="footer-actions" v-if="session.id && !readonly">
        <el-button class="footer-btn" @click="handleSave" :loading="saving">存草稿</el-button>
        <el-button class="footer-btn" type="primary" @click="handleSend" :loading="sending">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup name="ClassReviewEdit">
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ReviewImageUpload from './components/ReviewImageUpload.vue'
import { getReviewSession, saveReviewDraft, sendReview } from '@/api/face/review'

const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()
const { cf_fee_subject } = proxy.useDict('cf_fee_subject')

const loading = ref(false)
const saving = ref(false)
const sending = ref(false)
const session = ref({})
const classImagesStr = ref('')

const form = reactive({
  sessionId: null,
  classContent: '',
  items: []
})

const readonly = computed(() => session.value.status === '1')

function parseImages(str) {
  if (!str) return []
  if (Array.isArray(str)) return str
  return String(str).split(',').filter(Boolean)
}

function imagesToJson(str) {
  const list = parseImages(str)
  return list.length ? JSON.stringify(list) : ''
}

function loadDetail() {
  const sessionId = route.query.sessionId
  if (!sessionId) {
    proxy.$modal.msgWarning('缺少点评批次')
    return
  }
  loading.value = true
  getReviewSession(sessionId).then(res => {
    session.value = res.data || {}
    form.sessionId = session.value.id
    form.classContent = session.value.classContent || ''
    classImagesStr.value = parseImagesFromJson(session.value.classImages).join(',')
    form.items = (session.value.items || []).map(it => ({
      ...it,
      disciplineScore: it.disciplineScore || 5,
      participationScore: it.participationScore || 5,
      imagesStr: parseImagesFromJson(it.images).join(',')
    }))
  }).finally(() => {
    loading.value = false
  })
}

function parseImagesFromJson(json) {
  if (!json) return []
  try {
    const arr = JSON.parse(json)
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

function buildPayload() {
  return {
    sessionId: form.sessionId,
    classContent: form.classContent,
    classImages: imagesToJson(classImagesStr.value),
    items: form.items.map(it => ({
      id: it.id,
      disciplineScore: it.disciplineScore,
      participationScore: it.participationScore,
      content: it.content,
      images: imagesToJson(it.imagesStr)
    }))
  }
}

function handleSave() {
  saving.value = true
  saveReviewDraft(buildPayload()).then(() => {
    proxy.$modal.msgSuccess('草稿已保存')
  }).finally(() => {
    saving.value = false
  })
}

function handleSend() {
  proxy.$modal.confirm('确认发送课堂点评？家长将收到微信服务号通知。').then(() => {
    sending.value = true
    return saveReviewDraft(buildPayload())
  }).then(() => sendReview(form.sessionId)).then(() => {
    proxy.$modal.msgSuccess('发送成功')
    loadDetail()
  }).catch(() => {}).finally(() => {
    sending.value = false
  })
}

function goBack() {
  router.back()
}

onMounted(loadDetail)
</script>

<style scoped>
.review-edit-page {
  min-height: calc(100vh - 84px);
  background: #f0f2f5;
  padding-bottom: 80px;
}
.review-edit-page__inner {
  max-width: 480px;
  margin: 0 auto;
  padding: 12px;
}
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.page-header__title {
  font-size: 17px;
  font-weight: 600;
}
.class-banner {
  background: linear-gradient(135deg, #ecf5ff, #fff);
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
}
.class-banner__time {
  font-size: 16px;
  font-weight: 600;
  margin: 8px 0 4px;
}
.class-banner__teacher {
  color: #606266;
  font-size: 13px;
}
.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}
.section-title {
  font-weight: 600;
  margin-bottom: 12px;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}
.upload-block {
  margin-top: 12px;
}
.student-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.student-card__name {
  font-size: 16px;
  font-weight: 600;
  background: #ecf5ff;
  padding: 4px 12px;
  border-radius: 16px;
}
.student-card__stats {
  display: flex;
  gap: 16px;
  color: #909399;
  font-size: 12px;
  margin-bottom: 12px;
}
.score-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}
.footer-actions {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  max-width: 480px;
  margin: 0 auto;
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
}
.footer-btn {
  flex: 1;
  height: 44px;
}
</style>
