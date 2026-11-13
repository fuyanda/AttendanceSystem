<template>
  <div class="login">
    <h3 class="title">学生人脸采集</h3>
    <el-form ref="loginRef" :model="studentForm" :rules="studentRules" class="login-form">
      <el-form-item label="学生照片" prop="avatar">
        <ImageUpload v-model="studentForm.avatar" :limit="1" :fileSize="20" :fileType="['png', 'jpg', 'jpeg']"/>
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleLogin"
        >
          确定
        </el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>

const studentForm = ref({
  id: 0,
  avatar: ""
});

const studentRules = {
  avatar: [{ required: true, trigger: "blur", message: "请上传人脸照片" }]
};


function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      userStore.login(studentForm.value).then(() => {
        const query = route.query;
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur];
          }
          return acc;
        }, {});
      }).catch(() => {

      });
    }
  });
}
</script>

<style lang='scss' scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
}
.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 40px;
  padding-left: 12px;
}
</style>
