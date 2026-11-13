<template>
  <PageContainer variant="dark">
    <NavPageHeader :title="pageTitle" variant="dark" show-back back-text="返回" @back="goBack" />

    <view v-if="processing && !matchResult" class="punch-loading">
      <text>{{ tips || '正在识别...' }}</text>
    </view>

    <template v-else>
      <view v-if="tips" class="punch-tips">{{ tips }}</view>

      <PunchCameraPanel v-if="hasPreview" :preview-path="previewPath" />

      <view v-if="hasPreview && !matchResult && !processing" class="punch-actions">
        <AppButton type="secondary" @click="resetCapture">重拍</AppButton>
      </view>
    </template>

    <PunchConfirmCard
      :result="matchResult"
      :submitting="submitting"
      :submit-text="submitText"
      :is-checkin="isCheckin"
      :fee-options="feeOptions"
      :selected-fee-id="selectedFeeId"
      :fee-loading="feeLoading"
      @select-fee="onSelectFee"
      @cancel="resetCapture"
      @confirm="confirmSubmit"
    />
  </PageContainer>
</template>
<script setup>
import NavPageHeader from '@/shared/components/layout/NavPageHeader.vue'
import PunchCameraPanel from '@/modules/punch/components/PunchCameraPanel.vue'
import PunchConfirmCard from '@/modules/punch/components/PunchConfirmCard.vue'
import { usePunch } from '@/modules/punch/composables/usePunch.js'

const {
  tips, previewPath, processing, submitting, matchResult,
  pageTitle, submitText, hasPreview,
  isCheckin, feeOptions, selectedFeeId, feeLoading,
  goBack, resetCapture, confirmSubmit
} = usePunch()

function onSelectFee(id) {
  selectedFeeId.value = id
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.punch-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 28rpx;
}
.punch-tips {
  margin: 0 $space-32 $space-16;
  text-align: center;
  color: #e2e8f0;
  font-size: 28rpx;
}
.punch-actions {
  padding: $space-32;
}
.punch-actions :deep(.app-button) {
  margin-top: 0;
}
:deep(.page-container) {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
</style>
