<template>
  <div class="review-page">
    <div class="review-page__inner">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="上课时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="YYYY-MM-DD"
            range-separator="-"
            start-placeholder="开始"
            end-placeholder="结束"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" clearable placeholder="全部" style="width: 100%">
            <el-option label="草稿" value="0" />
            <el-option label="已发送" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        </el-form-item>
      </el-form>

      <div v-loading="loading" class="session-list">
        <div v-for="row in sessionList" :key="row.id" class="session-card" @click="goEdit(row)">
          <div class="session-card__head">
            <dict-tag :options="cf_fee_subject" :value="row.subject" />
            <el-tag :type="row.status === '1' ? 'success' : 'warning'" size="small">
              {{ row.status === '1' ? '已发送' : '草稿' }}
            </el-tag>
          </div>
          <div class="session-card__time">{{ row.classTime }}</div>
          <div class="session-card__meta">
            <span>{{ row.teacherName }}</span>
            <span>{{ row.studentCount || 0 }} 名学员</span>
          </div>
        </div>
        <el-empty v-if="!loading && !sessionList.length" description="暂无点评记录" />
      </div>

      <pagination
        v-show="total > 0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </div>
  </div>
</template>

<script setup name="ClassReview">
import { getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listReviewSession } from '@/api/face/review'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { cf_fee_subject } = proxy.useDict('cf_fee_subject')

const loading = ref(false)
const sessionList = ref([])
const total = ref(0)
const dateRange = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  status: null
})

function buildQuery() {
  return proxy.addDateRange({ ...queryParams }, dateRange.value)
}

function getList() {
  loading.value = true
  listReviewSession(buildQuery()).then(res => {
    sessionList.value = res.rows || []
    total.value = res.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function goEdit(row) {
  const route = router.getRoutes().find(r => r.path.endsWith('/classReview/edit') || r.path.includes('classReview/edit'))
  if (route) {
    router.push({ path: route.path, query: { sessionId: row.id } })
  } else {
    router.push({ path: '/classReview/edit', query: { sessionId: row.id } })
  }
}

onMounted(getList)
</script>

<style scoped>
.review-page {
  min-height: calc(100vh - 84px);
  background: #f5f7fa;
  padding: 12px;
}
.review-page__inner {
  max-width: 480px;
  margin: 0 auto;
}
.filter-form :deep(.el-form-item) {
  width: 100%;
  margin-right: 0;
}
.session-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 8px;
}
.session-card {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  cursor: pointer;
}
.session-card__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.session-card__time {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.session-card__meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 13px;
}
</style>
