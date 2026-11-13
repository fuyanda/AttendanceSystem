<template>
  <div class="page">
    <el-card class="card">
      <div class="success-icon">&#10003;</div>
      <h2 class="title">{{ t.homeTitle }}</h2>
      <p class="subtitle">{{ t.homeSubtitle }}</p>

      <div v-if="studentNames.length" class="children-section">
        <h3 class="section-title">{{ t.childrenSection }}</h3>
        <ul class="children-list">
          <li v-for="name in studentNames" :key="name" class="child-item">
            <span class="child-name">{{ name }}</span>
          </li>
        </ul>
      </div>

      <div v-if="savedPassword" class="password-section">
        <h3 class="section-title">{{ t.homePasswordSection }}</h3>
        <div class="password-row">
          <code class="password-value">{{ savedPassword }}</code>
          <el-button size="small" type="primary" plain @click="copyPassword">
            {{ t.copyPassword }}
          </el-button>
        </div>
        <p class="password-hint">{{ t.homePasswordHint }}</p>
      </div>

      <div class="actions">
        <el-button type="primary" plain class="action-btn" @click="goBindMore">
          {{ t.bindMoreBtn }}
        </el-button>
        <p class="action-hint">{{ t.bindMoreTip }}</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useParentStore } from '@/stores/parent'
import t from '@/i18n/zh-CN'

const router = useRouter()
const { studentNames, hasSession, password } = useParentStore()
const savedPassword = password

onMounted(() => {
  if (!hasSession.value) {
    router.replace('/login')
  }
})

async function copyPassword() {
  if (!savedPassword.value) return
  try {
    await navigator.clipboard.writeText(savedPassword.value)
    ElMessage.success(t.copySuccess)
  } catch {
    ElMessage.warning(t.copyFailed)
  }
}

function goBindMore() {
  router.push('/bind-more')
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px 16px;
  background: linear-gradient(145deg, #e8f4fc 0%, #f0f7ff 50%, #e3f0ff 100%);
}

.card {
  width: 100%;
  max-width: 440px;
  padding: 32px 24px;
  text-align: center;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(64, 158, 255, 0.12);
}

.success-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  border-radius: 50%;
  background: #67c23a;
  color: #fff;
  font-size: 28px;
  line-height: 56px;
  font-weight: bold;
}

.title {
  margin: 0 0 8px;
  color: #303133;
  font-size: 22px;
  font-weight: 600;
}

.subtitle {
  margin: 0 0 24px;
  color: #909399;
  font-size: 14px;
}

.children-section {
  text-align: left;
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f9ff;
  border-radius: 8px;
  border: 1px solid #e4edf9;
}

.password-section {
  text-align: left;
  margin-bottom: 24px;
  padding: 16px;
  background: #fffbeb;
  border-radius: 8px;
  border: 1px solid #faecd8;
}

.password-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.password-value {
  flex: 1;
  min-width: 120px;
  padding: 10px 12px;
  background: #fff;
  border-radius: 6px;
  border: 1px solid #ebeef5;
  font-size: 16px;
  font-weight: 600;
  color: #e6a23c;
  word-break: break-all;
}

.password-hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

.section-title {
  margin: 0 0 12px;
  font-size: 15px;
  color: #606266;
  font-weight: 500;
}

.children-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.child-item {
  padding: 10px 12px;
  background: #fff;
  border-radius: 6px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
}

.child-item:last-child {
  margin-bottom: 0;
}

.child-name {
  color: #303133;
  font-size: 16px;
  font-weight: 500;
}

.actions {
  margin-top: 8px;
}

.action-btn {
  width: 100%;
}

.action-hint {
  margin: 12px 0 0;
  font-size: 13px;
  color: #909399;
  line-height: 1.5;
}
</style>
