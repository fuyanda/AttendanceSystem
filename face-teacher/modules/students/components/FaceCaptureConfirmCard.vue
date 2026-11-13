<template>
  <view v-if="visible" class="face-confirm">
    <view class="face-confirm__label">照片预览</view>
    <view class="face-confirm__hint">请对照上方黄色框线，确认正脸居中、五官清晰、无遮挡后再保存</view>
    <view class="face-confirm__actions">
      <AppButton type="secondary" :block="false" @click="$emit('retake')">重拍</AppButton>
      <AppButton type="primary" :block="false" :loading="submitting" @click="$emit('confirm')">
        {{ submitting ? '保存中...' : '确认保存' }}
      </AppButton>
    </view>
  </view>
</template>
<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false }
})
defineEmits(['retake', 'confirm'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.face-confirm {
  margin: 0 $space-24 $space-24;
  padding: $space-32;
  border-radius: $radius-lg;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.face-confirm__label {
  color: #e2e8f0;
  font-size: 28rpx;
  font-weight: 600;
}
.face-confirm__hint {
  margin-top: $space-8;
  color: #94a3b8;
  font-size: 24rpx;
  line-height: 1.5;
}
.face-confirm__actions {
  display: flex;
  gap: $space-16;
  margin-top: $space-24;
}
.face-confirm__actions :deep(.app-button) {
  flex: 1;
  margin-top: 0;
}
</style>
