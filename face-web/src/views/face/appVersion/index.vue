<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="版本名" prop="versionName">
        <el-input
          v-model="queryParams.versionName"
          placeholder="请输入版本名"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="强制更新" prop="forceUpdate">
        <el-select v-model="queryParams.forceUpdate" placeholder="全部" clearable style="width: 120px">
          <el-option label="可选" value="0" />
          <el-option label="强制" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="停用" value="0" />
          <el-option label="启用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['face:appVersion:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['face:appVersion:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['face:appVersion:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="versionList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="平台" align="center" prop="platform" width="100" />
      <el-table-column label="版本名" align="center" prop="versionName" width="120" />
      <el-table-column label="版本号" align="center" prop="versionCode" width="100" />
      <el-table-column label="强制更新" align="center" prop="forceUpdate" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.forceUpdate === '1' ? 'danger' : 'info'">
            {{ scope.row.forceUpdate === '1' ? '强制' : '可选' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === '1' ? 'success' : 'info'">
            {{ scope.row.status === '1' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="APK" align="center" prop="apkUrl" min-width="180" show-overflow-tooltip />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['face:appVersion:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['face:appVersion:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" v-model="open" width="640px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="平台" prop="platform">
          <el-input v-model="form.platform" disabled />
        </el-form-item>
        <el-form-item label="版本名" prop="versionName">
          <el-input v-model="form.versionName" placeholder="如 1.1.4，需与打包 manifest 一致" />
        </el-form-item>
        <el-form-item label="版本号" prop="versionCode">
          <el-input-number v-model="form.versionCode" :min="1" :step="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="APK文件" prop="apkUrl">
          <file-upload
            v-model="form.apkUrl"
            action="/common/upload/apk"
            :limit="1"
            :file-size="200"
            :file-type="['apk']"
          />
        </el-form-item>
        <el-form-item label="强制更新" prop="forceUpdate">
          <el-radio-group v-model="form.forceUpdate">
            <el-radio value="0">可选</el-radio>
            <el-radio value="1">强制</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">停用</el-radio>
            <el-radio value="1">启用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="更新说明" prop="changelog">
          <el-input v-model="form.changelog" type="textarea" :rows="4" placeholder="展示给教师端的更新内容" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AppVersion">
import {
  listAppVersion,
  getAppVersion,
  addAppVersion,
  updateAppVersion,
  delAppVersion
} from '@/api/face/appVersion'

const { proxy } = getCurrentInstance()

const versionList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    versionName: undefined,
    forceUpdate: undefined,
    status: undefined,
    platform: 'android'
  },
  rules: {
    versionName: [{ required: true, message: '版本名不能为空', trigger: 'blur' }],
    versionCode: [{ required: true, message: '版本号不能为空', trigger: 'blur' }],
    apkUrl: [{ required: true, message: '请上传 APK', trigger: 'change' }],
    forceUpdate: [{ required: true, message: '请选择是否强制更新', trigger: 'change' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listAppVersion(queryParams.value).then((response) => {
    versionList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    id: undefined,
    platform: 'android',
    versionName: undefined,
    versionCode: undefined,
    apkUrl: undefined,
    forceUpdate: '0',
    changelog: undefined,
    status: '1',
    remark: undefined
  }
  proxy.resetForm('formRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增 App 版本'
}

function handleUpdate(row) {
  reset()
  const id = row?.id || ids.value
  getAppVersion(id).then((response) => {
    form.value = response.data
    open.value = true
    title.value = '修改 App 版本'
  })
}

function submitForm() {
  proxy.$refs['formRef'].validate((valid) => {
    if (!valid) return
    const req = form.value.id != null ? updateAppVersion(form.value) : addAppVersion(form.value)
    req.then(() => {
      proxy.$modal.msgSuccess(form.value.id != null ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const _ids = row?.id || ids.value
  proxy.$modal
    .confirm('是否确认删除选中的 App 版本？')
    .then(() => delAppVersion(_ids))
    .then(() => {
      getList()
      proxy.$modal.msgSuccess('删除成功')
    })
    .catch(() => {})
}

getList()
</script>
