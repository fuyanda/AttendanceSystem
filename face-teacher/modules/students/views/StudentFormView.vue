<template>
  <PageContainer>
    <NavPageHeader :title="pageTitle" show-back @back="goBack" />
    <view v-if="!loading" class="student-form-scroll-wrap">
      <scroll-view class="student-form-scroll" scroll-y :show-scrollbar="false">
        <StudentFormPanel
          :form="form"
          :dept-options="deptOptions"
          :dept-index="deptIndex"
          :dept-label="deptLabel"
          :is-edit="isEdit"
          @dept-change="onDeptChange"
          @deadline-change="onDeadlineChange"
          @regenerate-bind-code="handleRegenerateBindCode"
          @copy-bind-code="copyBindCode"
          @capture-face="goFaceCapture"
          @update:name="form.name = $event"
          @update:phone="form.phone = $event"
          @update:remark="form.remark = $event"
        />
      </scroll-view>
    </view>
    <view v-else class="student-form-view__loading">加载中...</view>
    <StudentFormActions
      v-if="!loading"
      :saving="saving"
      :is-edit="isEdit"
      @save="submitForm"
      @archive="handleArchive"
    />
  </PageContainer>
</template>
<script setup>
import NavPageHeader from '@/shared/components/layout/NavPageHeader.vue'
import StudentFormPanel from '@/modules/students/components/StudentFormPanel.vue'
import StudentFormActions from '@/modules/students/components/StudentFormActions.vue'
import { useStudentForm } from '@/modules/students/composables/useStudentForm.js'

const {
  form,
  deptOptions,
  deptIndex,
  deptLabel,
  saving,
  loading,
  isEdit,
  pageTitle,
  onDeptChange,
  onDeadlineChange,
  submitForm,
  handleRegenerateBindCode,
  copyBindCode,
  handleArchive,
  goFaceCapture,
  goBack
} = useStudentForm()
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
:deep(.page-container) {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: $color-bg-page;
}
.student-form-scroll-wrap {
  flex: 1;
  min-height: 0;
  width: 100%;
}
.student-form-scroll {
  width: 100%;
  height: calc(100vh - 120rpx - env(safe-area-inset-top) - 240rpx - env(safe-area-inset-bottom));
  box-sizing: border-box;
}
.student-form-view__loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-text-muted;
  font-size: 28rpx;
}
</style>
