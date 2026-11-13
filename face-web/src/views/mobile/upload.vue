<template>
  <div class="app-container mobile-container">
    <el-form ref="uploadForm" :model="form" label-width="80px">
      <el-form-item label="学生标识">
        <el-input v-model="form.studentId" readonly />
      </el-form-item>
      <el-form-item label="上传照片">
        <el-upload
          :action="`${baseApi}/face/student/upload?studentId=${form.studentId}`"
          :show-file-list="false"
          :on-success="handleUploadSuccess"
          :before-upload="beforeUpload"
          accept="image/png, image/jpeg"
          capture="camera"
        >
          <el-button type="primary">拍照/选择图片</el-button>
        </el-upload>
        <div class="mt4">支持直接拍照或从相册选择图片（仅支持png/jpg格式，大小不超过20MB）</div>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup name="MobileUpload">
import { useRoute } from 'vue-router';

const route = useRoute();
const { proxy } = getCurrentInstance();
const baseApi = computed(() => import.meta.env.VITE_APP_BASE_API);

const form = reactive({ studentId: route.query.studentId });

const beforeUpload = (file) => {
  const isImage = file.type === 'image/png' || file.type === 'image/jpeg';
  const isLt20M = file.size / 1024 / 1024 < 20;

  if (!isImage) {
    proxy.$modal.msgError('只能上传png/jpg格式的图片！');
  }
  if (!isLt20M) {
    proxy.$modal.msgError('图片大小不能超过20MB！');
  }
  return isImage && isLt20M;
};

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    proxy.$modal.msgSuccess('图片上传成功！');
    // 可添加逻辑通知PC端刷新（如WebSocket或轮询）
  } else {
    proxy.$modal.msgError(response.msg || '上传失败，请重试');
  }
};
</script>

<style scoped>
.mobile-container {
  padding: 20px;
}
</style>