<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="学生姓名" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入姓名"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="学生电话" prop="phone">
        <el-input
            v-model="queryParams.phone"
            placeholder="请输入学生电话"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="缴费日期">
        <el-date-picker
            v-model="dateRange"
            value-format="YYYY-MM-DD"
            type="daterange"
            range-separator="-"
            start-placeholder="截止日期起"
            end-placeholder="截止日期至"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="家长电话" prop="parentTel">
        <el-input
            v-model="queryParams.parentTel"
            placeholder="请输入家长电话"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['face:student:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['face:student:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['face:student:remove']"
        >归档</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['face:student:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Delete"
          @click="handleCleanupLocalFaces"
          v-hasPermi="['face:student:edit']"
        >清理人脸照片</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="studentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="人脸照片" align="center" width="150">
        <template #default="scope">
          <ImagePreview
            :src="`${scope.row.avatar}`"
            :width="100"
            :height="60"
          />
        </template>
      </el-table-column>
      <el-table-column label="姓名" align="center" prop="name" />
      <el-table-column label="绑定码" align="center" prop="bindCode" width="100" />
      <el-table-column label="学生电话" align="center" prop="phone" />
      <el-table-column label="家长姓名" align="center" prop="family" />
      <el-table-column label="家长电话" align="center" prop="parentTel" />
      <el-table-column label="校区" align="center" prop="deptName" />
      <el-table-column label="缴费截止日期至" align="center" prop="deadline" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.deadline, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['face:student:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['face:student:remove']">归档</el-button>
          <el-button link type="primary" icon="Key" @click="handlePay(scope.row)" v-hasPermi="['face:fee:list']">缴费</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total>0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改学生管理对话框 -->
    <el-dialog :title="title" v-model="open" width="980px" append-to-body>
      <el-form ref="studentRef" :model="form" :rules="rules" label-width="100" :inline="true" class="form-inline">
        <el-form-item label="归属校区" prop="deptId">
          <el-tree-select
              v-model="form.deptId"
              :data="deptOptions"
              :props="{ value: 'id', label: 'label', children: 'children' }"
              value-key="id"
              placeholder="请选择归属校区"
              check-strictly
          />
        </el-form-item>
        <el-form-item label="学生姓名" prop="name">
          <el-input ref="nameInputRef" v-model="form.name" placeholder="请输入姓名" @keyup.enter="handleNameEnter" />
        </el-form-item>
        <!--<el-form-item label="学生年龄" prop="age">
          <el-input-number v-model="form.age" :min="3" :max="20" placeholder="请输入年龄"/>
        </el-form-item>-->
        <el-form-item label="学生电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入学生电话" />
        </el-form-item>
        <el-form-item label="绑定码" prop="bindCode">
          <div class="bind-code-row">
            <el-input
              v-model="form.bindCode"
              placeholder="留空则系统自动生成"
              maxlength="16"
              :disabled="!!form.id && !bindCodeEditable"
            />
            <el-button v-if="form.id" type="primary" plain @click="handleRegenerateBindCode">重新生成</el-button>
            <el-button v-if="form.bindCode" type="success" plain @click="copyBindCode">复制</el-button>
          </div>
          <div class="bind-code-tip">家长注册时输入此绑定码，或微信扫下方二维码，即可关联该学生</div>
          <BindQrCode :bind-code="form.bindCode" :student-name="form.name" />
        </el-form-item>
        <el-form-item v-if="form.id && form.parents && form.parents.length" label="已绑定家长" class="parent-bind-item">
          <el-table :data="form.parents" border size="small" class="parent-bind-table">
            <el-table-column label="家长电话" prop="phone" min-width="120" />
            <el-table-column label="家长姓名" prop="name" min-width="100" />
            <el-table-column label="关系" width="90" align="center">
              <template #default="scope">
                {{ formatRelationType(scope.row.relationType) }}
              </template>
            </el-table-column>
            <el-table-column label="主联系人" width="90" align="center">
              <template #default="scope">
                <el-tag v-if="scope.row.isPrimary === 1" type="success" size="small">是</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template #default="scope">
                <el-button
                  v-if="scope.row.isPrimary !== 1"
                  link
                  type="primary"
                  v-hasPermi="['face:student:edit']"
                  @click="handleSetPrimaryParent(scope.row)"
                >设为主联系人</el-button>
                <el-button
                  link
                  type="danger"
                  v-hasPermi="['face:student:edit']"
                  @click="handleUnbindParent(scope.row)"
                >解除绑定</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="bind-code-tip">错误绑定可直接解除；解绑主联系人时，若仍有其他家长将自动切换主联系人。</div>
        </el-form-item>
        <el-form-item label="缴费截止日期" prop="deadline">
          <el-date-picker clearable
            v-model="form.deadline"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择缴费截止日期">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="学生照片" prop="avatar">
          <el-row :gutter="10">
            <!--<el-col :span="12">-->
              <ImageUpload v-model="form.avatar" :limit="1" :fileSize="20" :fileType="['png', 'jpg', 'jpeg']" @success="handleUploadSuccess" />
            <!--</el-col>
            <el-col :span="12" style="text-align: center;">
              <ImagePreview
                  :src="form.avatar"
                  width="69%"
              />
              <div class="mt4">扫描二维码上传手机照片</div>
            </el-col>-->
          </el-row>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" wi/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm(false)">确 定</el-button>
          <el-button v-if="!form.id" type="success" plain @click="submitForm(true)">保存并继续</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="feeDialogOpen" :title="feeDialogTitle" width="960px" append-to-body destroy-on-close>
      <StudentFeePanel
        v-if="feeDialogOpen"
        :student-id="feeStudent.id"
        :student-name="feeStudent.name"
        :dept-id="feeStudent.deptId"
        :show-student-column="false"
        :show-pagination="false"
      />
    </el-dialog>
  </div>
</template>

<script setup name="Student">
import { ElMessageBox } from 'element-plus'
import { listStudent, getStudent, delStudent, addStudent, updateStudent, regenerateBindCode, cleanupLocalFaces } from "@/api/face/student";
import { unbindStudentParent, setPrimaryStudentParent } from "@/api/face/studentParent";
import BindQrCode from '@/components/BindQrCode/index.vue';
import { deptTreeSelect } from "@/api/system/user";
import StudentFeePanel from '@/views/face/fee/StudentFeePanel.vue'


const { proxy } = getCurrentInstance();

const baseApi = computed(() => import.meta.env.VITE_APP_BASE_API);

const studentList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const deptOptions = ref(undefined);
const dateRange = ref([]);
const bindCodeEditable = ref(false);
const feeDialogOpen = ref(false);
const feeStudent = ref({ id: null, name: '', deptId: null });
const feeDialogTitle = computed(() => feeStudent.value.name ? `${feeStudent.value.name} - 缴费管理` : '缴费管理');

const LAST_ADD_STORAGE_KEY = 'face:student:lastAddDefaults'
const nameInputRef = ref(null)

function loadLastAddDefaults() {
  try {
    const raw = localStorage.getItem(LAST_ADD_STORAGE_KEY)
    return raw ? JSON.parse(raw) : {}
  } catch {
    return {}
  }
}

function saveLastAddDefaults() {
  const payload = {
    deptId: form.value.deptId ?? null,
    deadline: form.value.deadline ?? null
  }
  localStorage.setItem(LAST_ADD_STORAGE_KEY, JSON.stringify(payload))
}

function applyLastAddDefaults() {
  const saved = loadLastAddDefaults()
  if (saved.deptId != null) {
    form.value.deptId = saved.deptId
  }
  if (saved.deadline) {
    form.value.deadline = saved.deadline
  }
}

function resetForNextEntry() {
  const kept = {
    deptId: form.value.deptId,
    deadline: form.value.deadline
  }
  reset()
  form.value.deptId = kept.deptId
  form.value.deadline = kept.deadline
  nextTick(() => {
    nameInputRef.value?.focus?.()
  })
}

const handleUploadSuccess = () => {
  getList();
};

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: null,
    age: null,
    sex: null,
    avatar: null,
    deptId: null,
    family: null,
    deadline: null,
    parentTel: null,
    phone: null,
    bindCode: null,
  },
  rules: {
    deptId: [{ required: true, message: "请选择归属校区", trigger: "blur" }],
    name: [{ required: true, message: "请输入姓名", trigger: "blur" }]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询学生管理列表 */
function getList() {
  loading.value = true;
  listStudent(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    studentList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

/** 查询校区下拉树结构 */
function getDeptTree() {
  deptTreeSelect().then(response => {
    deptOptions.value = response.data;
  });
};

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// // 根据家长电话查询家长姓名
// async function getFamilyByParentTel() {
//   if (form.parentTel) {
//     const res = await getFamilyByParentTel(form.parentTel);
//     if (res.code === 200) {
//       form.family = res.data?.family || '';
//     } else {
//       proxy.$modal.msgError(res.msg);
//       form.family = '';
//     }
//   }
// }

// 表单重置
function reset() {
  form.value = {
    id: null,
    name: null,
    age: null,
    sex: null,
    avatar: null,
    deptId: null,
    family: null,
    deadline: null,
    parentTel: null,
    phone: null,
    bindCode: null,
    parents: [],
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null,
    remark: null
  };
  bindCodeEditable.value = false;
  proxy.resetForm("studentRef");
}

function copyBindCode() {
  if (!form.value.bindCode) {
    return;
  }
  navigator.clipboard.writeText(form.value.bindCode).then(() => {
    proxy.$modal.msgSuccess("绑定码已复制");
  });
}

function handleRegenerateBindCode() {
  regenerateBindCode(form.value.id).then(response => {
    const data = response.data;
    form.value.bindCode = typeof data === 'string' ? data : (data?.bindCode || '');
    proxy.$modal.msgSuccess("绑定码已重新生成");
  });
}

function formatRelationType(type) {
  const map = {
    parent: '家长',
    father: '父亲',
    mother: '母亲',
    grandpa: '爷爷',
    grandma: '奶奶',
    other: '其他'
  }
  return map[type] || type || '-'
}

function refreshBoundParents() {
  return getStudent(form.value.id).then(response => {
    const data = response.data || {}
    form.value.parents = data.parents || []
    form.value.parentTel = data.parentTel || null
    form.value.family = data.family || null
  })
}

function handleSetPrimaryParent(row) {
  if (!row?.userId || !form.value.id) {
    proxy.$modal.msgError('缺少家长信息，无法设置主联系人')
    return
  }
  proxy.$modal.confirm(`确认将「${row.name || row.phone}」设为主联系人？`).then(() => {
    return setPrimaryStudentParent(form.value.id, row.userId)
  }).then(() => {
    proxy.$modal.msgSuccess('已设为主联系人')
    return refreshBoundParents()
  }).catch(() => {})
}

function handleUnbindParent(row) {
  if (!row?.userId || !form.value.id) {
    proxy.$modal.msgError('缺少家长信息，无法解绑')
    return
  }
  const isPrimary = row.isPrimary === 1
  const tip = isPrimary
    ? `「${row.name || row.phone}」当前为主联系人，确认解除绑定？若还有其他家长将自动切换主联系人。`
    : `确认解除「${row.name || row.phone}」与该学生的绑定？`
  proxy.$modal.confirm(tip).then(() => {
    return unbindStudentParent(form.value.id, row.userId)
  }).then(() => {
    proxy.$modal.msgSuccess('解绑成功')
    return refreshBoundParents()
  }).then(() => {
    getList()
  }).catch(() => {})
}

function buildSaveMessage(data) {
  if (data && data.bindCode) {
    return `学生已保存<br><br>绑定码：<strong>${data.bindCode}</strong><br><br>请将此绑定码告知家长，家长注册时输入即可自动关联。`;
  }
  return "保存成功";
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef");
  handleQuery();
}

// 多选框选中数据
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.id);
  single.value = selection.length != 1;
  multiple.value = !selection.length;
}

/** 新增按钮操作 */
function handleAdd() {
  reset();
  applyLastAddDefaults();
  open.value = true;
  title.value = "添加学生管理";
  nextTick(() => {
    nameInputRef.value?.focus?.()
  })
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getStudent(_id).then(response => {
    form.value = response.data;
    if (!form.value.parents) {
      form.value.parents = [];
    }
    open.value = true;
    title.value = "修改学生管理";
  });
}

/** 提交按钮 */
function submitForm(continueAdd = false) {
  proxy.$refs["studentRef"].validate(valid => {
    if (!valid) {
      return;
    }
    const isEdit = form.value.id != null
    const request = isEdit ? updateStudent(form.value) : addStudent(form.value);
    request.then(response => {
      const payload = response.data || form.value;
      if (payload.bindCode) {
        form.value.bindCode = payload.bindCode;
      }
      if (!isEdit) {
        saveLastAddDefaults()
      }
      getList();
      if (!isEdit && continueAdd) {
        const bindCodeTip = payload.bindCode ? `，绑定码：${payload.bindCode}` : ''
        proxy.$modal.msgSuccess(`学生已添加${bindCodeTip}`)
        resetForNextEntry()
        return
      }
      ElMessageBox.alert(
          buildSaveMessage(payload),
          isEdit ? '修改成功' : '请保存绑定码',
          {
            dangerouslyUseHTMLString: true,
          }
      );
      open.value = false;
    });
  });
}

function handleNameEnter() {
  if (!form.value.id) {
    submitForm(true)
  }
}

/** 删除按钮操作 */
function handleDelete(row) {
  console.log(row)
  const _ids = row.id || ids.value;
  const msg = row.id?'确认归档【' + row.name + '】吗？':'确认归档选中学生吗？'

  proxy.$modal.confirm(msg).then(function() {
    return delStudent(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("归档成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('face/student/export', {
    ...queryParams.value
  }, `student_${new Date().getTime()}.xlsx`)
}

/** 清理本地多余人脸照片 */
function handleCleanupLocalFaces() {
  proxy.$modal.confirm('将按数据库在用头像，为每位学生只保留一张本地人脸，并删除多余与无引用文件。是否继续？').then(() => {
    return cleanupLocalFaces()
  }).then((res) => {
    const data = res.data || {}
    proxy.$modal.msgSuccess(
      `清理完成：扫描 ${data.scannedFiles || 0}，保留 ${data.kept || 0}，删除 ${data.deleted || 0}，修复 ${data.repaired || 0}，缺失 ${data.missing || 0}`
    )
    getList()
  }).catch(() => {})
}

function handlePay(row) {
  feeStudent.value = { id: row.id, name: row.name, deptId: row.deptId };
  feeDialogOpen.value = true;
}

getList();
getDeptTree();
</script>
<style>
.form-inline .el-input,
.form-inline .el-input-number,
.form-inline .el-date-picker,
.form-inline .el-select{
  --el-input-width: 250px;
  width: 250px;
}

.form-inline .el-textarea{
  width: 631px;
  min-width: 250px;
}

.parent-bind-panel {
  width: 631px;
}

.parent-bind-item {
  width: 100%;
}

.parent-bind-table {
  width: 631px;
}

.bind-code-row {
  display: flex;
  gap: 8px;
  width: 631px;
}

.bind-code-row .el-input {
  flex: 1;
}

.bind-code-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
  max-width: 631px;
}

.mt8 {
  margin-top: 8px;
}

.mt4{
  text-align: center;
  color: #f56c6c;
}
</style>
