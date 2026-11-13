<template>
  <div class="app-container fee-ledger-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="业务时间">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item label="业务类型" prop="bizType">
        <el-select v-model="queryParams.bizType" placeholder="全部" clearable style="width: 120px">
          <el-option label="缴费" value="pay" />
          <el-option label="消课" value="consume" />
          <el-option label="退费" value="refund" />
        </el-select>
      </el-form-item>
      <el-form-item label="学生姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入学生姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="科目" prop="subject">
        <el-select v-model="queryParams.subject" placeholder="全部" clearable style="width: 130px">
          <el-option v-for="dict in cf_fee_subject" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="never" class="summary-card summary-card--pay">
          <div class="summary-card__label">缴费笔数</div>
          <div class="summary-card__value">{{ summary.payCount }}</div>
          <div class="summary-card__sub">{{ summary.payHours }} 节 / ¥{{ formatMoney(summary.payAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="never" class="summary-card summary-card--consume">
          <div class="summary-card__label">消课笔数</div>
          <div class="summary-card__value">{{ summary.consumeCount }}</div>
          <div class="summary-card__sub">{{ summary.consumeHours }} 节 / ¥{{ formatMoney(summary.consumeAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="never" class="summary-card summary-card--refund">
          <div class="summary-card__label">退费笔数</div>
          <div class="summary-card__value">{{ summary.refundCount }}</div>
          <div class="summary-card__sub">{{ summary.refundHours }} 节 / ¥{{ formatMoney(summary.refundAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="4">
        <el-card shadow="never" class="summary-card summary-card--net">
          <div class="summary-card__label">净收入</div>
          <div class="summary-card__value">¥{{ formatMoney(summary.netAmount) }}</div>
          <div class="summary-card__sub">缴费 − 退费</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="10" class="mb8">
      <right-toolbar v-model:showSearch="showSearch" @queryTable="handleQuery" />
    </el-row>

    <el-table v-loading="loading" :data="ledgerList">
      <el-table-column label="业务时间" align="center" prop="bizTime" width="170" />
      <el-table-column label="类型" align="center" prop="bizTypeLabel" width="90">
        <template #default="scope">
          <el-tag :type="bizTagType(scope.row.bizType)" size="small">{{ scope.row.bizTypeLabel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="学生" align="center" prop="studentName" min-width="100" />
      <el-table-column label="科目" align="center" prop="subject" width="90">
        <template #default="scope">
          <dict-tag :options="cf_fee_subject" :value="scope.row.subject" />
        </template>
      </el-table-column>
      <el-table-column label="任课老师" align="center" prop="teacherName" width="100" />
      <el-table-column label="课时" align="center" prop="hours" width="80" />
      <el-table-column label="金额" align="center" width="110">
        <template #default="scope">
          <span :class="amountClass(scope.row.bizType)">{{ formatAmount(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作人" align="center" prop="operator" width="100" />
      <el-table-column label="备注" align="center" prop="remark" min-width="140" show-overflow-tooltip />
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="FeeLedger">
import { getCurrentInstance, onMounted, reactive, ref } from 'vue'
import { getFeeLedgerSummary, listFeeLedger } from '@/api/face/fee'

const { proxy } = getCurrentInstance()
const { cf_fee_subject } = proxy.useDict('cf_fee_subject')

const showSearch = ref(true)
const loading = ref(false)
const ledgerList = ref([])
const total = ref(0)
const dateRange = ref(defaultDateRange())

const summary = reactive({
  payCount: 0,
  payHours: 0,
  payAmount: 0,
  consumeCount: 0,
  consumeHours: 0,
  consumeAmount: 0,
  refundCount: 0,
  refundHours: 0,
  refundAmount: 0,
  netAmount: 0
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  studentName: null,
  subject: null,
  bizType: null
})

function defaultDateRange() {
  const now = new Date()
  const start = new Date(now.getFullYear(), now.getMonth(), 1)
  const fmt = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  return [fmt(start), fmt(now)]
}

function buildQuery() {
  return proxy.addDateRange({ ...queryParams }, dateRange.value)
}

function formatMoney(val) {
  const n = Number(val || 0)
  return n.toFixed(2)
}

function bizTagType(bizType) {
  if (bizType === 'pay') return 'success'
  if (bizType === 'refund') return 'warning'
  return 'info'
}

function amountClass(bizType) {
  if (bizType === 'pay') return 'amount--in'
  if (bizType === 'refund') return 'amount--out'
  return ''
}

function formatAmount(row) {
  const val = formatMoney(row.amount)
  if (row.bizType === 'pay') return `+${val}`
  if (row.bizType === 'refund') return `-${val}`
  return val
}

async function loadSummary() {
  const res = await getFeeLedgerSummary(buildQuery())
  Object.assign(summary, res.data || {})
}

function getList() {
  loading.value = true
  Promise.all([
    listFeeLedger(buildQuery()),
    loadSummary()
  ]).then(([listRes]) => {
    ledgerList.value = listRes.rows || []
    total.value = listRes.total || 0
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = defaultDateRange()
  proxy.resetForm('queryRef')
  handleQuery()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.summary-row {
  margin-bottom: 16px;
}
.summary-card {
  margin-bottom: 16px;
  border-radius: 8px;
}
.summary-card__label {
  color: #909399;
  font-size: 13px;
  margin-bottom: 8px;
}
.summary-card__value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  line-height: 1.2;
}
.summary-card__sub {
  margin-top: 8px;
  color: #606266;
  font-size: 12px;
}
.summary-card--pay { border-left: 4px solid #67c23a; }
.summary-card--consume { border-left: 4px solid #409eff; }
.summary-card--refund { border-left: 4px solid #e6a23c; }
.summary-card--net { border-left: 4px solid #303133; }
.amount--in { color: #67c23a; font-weight: 500; }
.amount--out { color: #e6a23c; font-weight: 500; }
</style>
