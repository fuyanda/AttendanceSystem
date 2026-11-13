<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="学生姓名" prop="studentName">
        <el-input v-model="queryParams.studentName" placeholder="请输入学生姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="科目" prop="subject">
        <el-select v-model="queryParams.subject" placeholder="请选择科目" clearable style="width: 150px">
          <el-option v-for="dict in cf_fee_subject" :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
          <el-option label="有效" value="0" />
          <el-option label="关闭" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['face:fee:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="Edit" @click="goBatchConsume" v-hasPermi="['face:fee:consume']">批量消课</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="DataAnalysis" @click="goLedger" v-hasPermi="['face:fee:ledger']">缴费台账</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="handleQuery" />
    </el-row>

    <StudentFeePanel ref="panelRef" :external-query="queryParams" :show-toolbar="false" />
  </div>
</template>

<script setup name="Fee">
import { getCurrentInstance, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import StudentFeePanel from './StudentFeePanel.vue'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { cf_fee_subject } = proxy.useDict('cf_fee_subject')

const showSearch = ref(true)
const panelRef = ref(null)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  studentName: null,
  subject: null,
  status: null
})

function handleQuery() {
  queryParams.pageNum = 1
  panelRef.value?.loadList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleAdd() {
  panelRef.value?.handleAdd()
}

function goBatchConsume() {
  const target = router.getRoutes().find(r =>
    r.path === '/feeConsume' || r.path.endsWith('/feeConsume')
  )
  if (target) {
    router.push(target.path)
  } else {
    proxy.$modal.msgWarning('未找到批量消课路由，请执行菜单修复 SQL 后重新登录')
  }
}

function goLedger() {
  const target = router.getRoutes().find(r =>
    r.path === '/feeLedger' || r.path.endsWith('/feeLedger')
  )
  if (target) {
    router.push(target.path)
  } else {
    proxy.$modal.msgWarning('未找到缴费台账路由，请执行 sql/cf_student_fee_ledger.sql 后重新登录')
  }
}
</script>
