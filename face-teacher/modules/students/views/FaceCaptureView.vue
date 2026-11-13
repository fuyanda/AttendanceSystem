<template>
  <PageContainer variant="dark">
    <NavPageHeader title="人脸采集" variant="dark" show-back back-text="返回" @back="goBack" />
    <StudentFaceCapturePanel
      :student-name="studentName"
      :preview-path="previewPath"
      :tips="tips"
      :submitting="submitting"
      :has-existing-face="hasExistingFace"
      @capture="capturePhoto"
      @confirm="confirmSave"
    />
  </PageContainer>
</template>
<script setup>
import NavPageHeader from '@/shared/components/layout/NavPageHeader.vue'
import StudentFaceCapturePanel from '@/modules/students/components/StudentFaceCapturePanel.vue'
import { useFaceCapture } from '@/modules/students/composables/useFaceCapture.js'

const {
  studentName,
  previewPath,
  submitting,
  tips,
  hasExistingFace,
  goBack,
  capturePhoto,
  confirmSave
} = useFaceCapture()
</script>
<style lang="scss" scoped>
:deep(.page-container) {
  display: flex;
  flex-direction: column;
  height: 100vh;
}
</style>
