<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="90px">
      <el-form-item label="时间段名称" prop="name">
        <el-input
          v-model="queryParams.name"
          placeholder="请输入时间段名称"
          clearable
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="启用状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 150px">
          <el-option
            v-for="dict in punch_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
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
          v-hasPermi="['face:punch:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['face:punch:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['face:punch:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="punchList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="时间段名称" align="center" prop="name" />
      <el-table-column label="开始时间" align="center" prop="startTime" width="180">
        <template #default="scope">
          <span>{{ scope.row.startTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="结束时间" align="center" prop="endTime" width="180">
        <template #default="scope">
          <span>{{ scope.row.endTime }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <dict-tag :options="punch_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['face:punch:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['face:punch:remove']">删除</el-button>
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

    <!-- 添加或修改打卡设置对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="punchRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="时间段名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入时间段名称" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-time-select clearable
              v-model="form.startTime"
              start="07:00"
              step="00:5"
              end="23:59"
              placeholder="请选择开始时间"
          />

        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-time-select clearable
                          v-model="form.endTime"
                          start="07:00"
                          step="00:5"
                          end="23:59"
                          placeholder="请选择结束时间"
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in punch_status"
              :key="dict.value"
              :value="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
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

<script setup name="Punch">
import { listPunch, getPunch, delPunch, addPunch, updatePunch } from "@/api/face/punch";
import {getGenTable} from "@/api/tool/gen.js";
import {optionselect as getDictOptionselect} from "@/api/system/dict/type.js";

const route = useRoute();

const { proxy } = getCurrentInstance();
const { punch_status } = proxy.useDict('punch_status');

const punchList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");

const data = reactive({
  form: {
    status: 0
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    deptId: null,
    name: null,
    startTime: null,
    endTime: null,
    status: null
  },
  rules: {
    deptId: [
      { required: true, message: "校区主键不能为空", trigger: "change" }
    ],
    name: [
      { required: true, message: "时间段名称不能为空", trigger: "blur" }
    ],
    startTime: [
      { required: true, message: "开始时间不能为空", trigger: "blur" }
    ],
    endTime: [
      { required: true, message: "结束时间不能为空", trigger: "blur" }
    ],
    status: [
      { required: true, message: "启用状态", trigger: "change" }
    ]
  }
});

const { queryParams, form, rules } = toRefs(data);

/** 查询打卡设置列表 */
function getList() {
  loading.value = true;
  listPunch(queryParams.value).then(response => {
    punchList.value = response.rows;
    total.value = response.total;
    loading.value = false;
  });
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  form.value = {
    id: null,
    deptId: null,
    name: null,
    startTime: null,
    endTime: null,
    status: "1"
  };
  proxy.resetForm("punchRef");
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
  const deptId = route.params && route.params.deptId;
  if(deptId){
    form.value.deptId = deptId
  }
  else {
    proxy.$modal.msgError("未绑定所属校区，请关闭重试");
    return
  }
  open.value = true;
  title.value = "添加打卡设置";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getPunch(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改打卡设置";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["punchRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updatePunch(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addPunch(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value = false;
          getList();
        });
      }
    }
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const _ids = row.id || ids.value;
  proxy.$modal.confirm('是否确认删除打卡设置编号为"' + _ids + '"的数据项？').then(function() {
    return delPunch(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {});
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('face/punch/export', {
    ...queryParams.value
  }, `punch_${new Date().getTime()}.xlsx`)
}

(() => {
  const tableId = route.params && route.params.deptId;
  if (tableId) {
    // 获取表详细信息
    queryParams.value.deptId = tableId;
  }
  getList();
})();
</script>
