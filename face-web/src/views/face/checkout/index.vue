<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="学生姓名" prop="studentName">
        <el-input
            v-model="queryParams.studentName"
            placeholder="请输入学生姓名"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="已推送" prop="pushFlag">
        <el-select v-model="queryParams.pushFlag" placeholder="请选择" clearable style="width: 150px;">
          <el-option
              v-for="dict in num_yes_no"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="打卡时间">
        <el-date-picker
            v-model="dateRange"
            value-format="YYYY-MM-DD"
            type="daterange"
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8" v-show="0">
      <el-col :span="1.5">
        <el-button
            type="primary"
            plain
            icon="Plus"
            @click="handleAdd"
            v-hasPermi="['face:checkout:add']"
        >新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="success"
            plain
            icon="Edit"
            :disabled="single"
            @click="handleUpdate"
            v-hasPermi="['face:checkout:edit']"
        >修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="danger"
            plain
            icon="Delete"
            :disabled="multiple"
            @click="handleDelete"
            v-hasPermi="['face:checkout:remove']"
        >删除
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
            type="warning"
            plain
            icon="Download"
            @click="handleExport"
            v-hasPermi="['face:checkout:export']"
        >导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="checkoutList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"/>
      <el-table-column label="学生姓名" align="center" prop="studentName"/>
      <el-table-column label="签退时间" align="center" prop="createTime"/>
      <el-table-column label="人脸照片" align="center" prop="avatar">
        <template #default="scope">
          <ImagePreview
              :src="`${scope.row.avatar}`"
              :width="100"
              :height="60"
          />
        </template>
      </el-table-column>
      <el-table-column label="推送状态" align="center" prop="pushFlag">
        <template #default="scope">
          <dict-tag :options="num_yes_no" :value="scope.row.pushFlag" v-if="scope.row.pushFlag == '1'"/>
          <el-button link type="danger" icon="phone" @click="handleRePush(scope.row)" v-else>手动推送</el-button>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" v-if="0">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['face:checkout:edit']">修改
          </el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['face:checkout:remove']">删除
          </el-button>
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

    <!-- 添加或修改签退对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="checkoutRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学生姓名" prop="studentId">
          <el-input v-model="form.studentId" placeholder="请输入学生姓名"/>
        </el-form-item>
        <el-form-item label="人脸照片" prop="avatar">
          <el-input v-model="form.avatar" placeholder="请输入人脸照片"/>
        </el-form-item>
        <el-form-item label="是否推送" prop="pushFlag">
          <el-select v-model="form.pushFlag" placeholder="请选择是否推送">
            <el-option
                v-for="dict in num_yes_no"
                :key="dict.value"
                :label="dict.label"
                :value="dict.value"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="删除标志" prop="delFlag">
          <el-input v-model="form.delFlag" placeholder="请输入删除标志"/>
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

<script setup name="Checkout">
import {addCheckout, delCheckout, getCheckout,rePush, listCheckout, updateCheckout} from "@/api/face/checkout";

const {proxy} = getCurrentInstance();
const {num_yes_no} = proxy.useDict('num_yes_no');

const checkoutList = ref([]);
const open = ref(false);
const loading = ref(true);
const showSearch = ref(true);
const ids = ref([]);
const single = ref(true);
const multiple = ref(true);
const total = ref(0);
const title = ref("");
const dateRange = ref([]);

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    studentName: null,
    pushFlag: null,
  },
  rules: {}
});

const {queryParams, form, rules} = toRefs(data);

/** 查询签退列表 */
function getList() {
  loading.value = true;
  listCheckout(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    checkoutList.value = response.rows;
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
    studentId: null,
    avatar: null,
    pushFlag: null,
    delFlag: null,
    createBy: null,
    createTime: null,
    updateBy: null,
    updateTime: null
  };
  proxy.resetForm("checkoutRef");
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
  open.value = true;
  title.value = "添加签退";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const _id = row.id || ids.value
  getCheckout(_id).then(response => {
    form.value = response.data;
    open.value = true;
    title.value = "修改签退";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["checkoutRef"].validate(valid => {
    if (valid) {
      if (form.value.id != null) {
        updateCheckout(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功");
          open.value = false;
          getList();
        });
      } else {
        addCheckout(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除签退编号为"' + _ids + '"的数据项？').then(function () {
    return delCheckout(_ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {
  });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.download('face/checkout/export', {
    ...queryParams.value
  }, `checkout_${new Date().getTime()}.xlsx`)
}


/** 重新推送 */
function handleRePush(row) {
  const _id = row.id
  rePush(_id).then(response => {
    if (response.code === 200){
      proxy.$modal.msgSuccess("重新签退成功！");
      getList();
    }
    else{
      proxy.$modal.msgError("重新签退失败！");
    }
  });
}

getList();
</script>
