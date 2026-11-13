<template>
  <view class="face-capture-panel">
    <view class="face-capture-panel__header">
      <text v-if="studentName" class="face-capture-panel__name">{{ studentName }}</text>
      <text v-if="hasExistingFace" class="face-capture-panel__tag">已有人脸，重新采集将覆盖</text>
    </view>

    <text v-if="tips" class="face-capture-panel__tips">{{ tips }}</text>

    <view class="face-capture-panel__preview-area">
      <image
        v-if="previewPath"
        class="face-capture-panel__preview-img"
        :src="previewPath"
        mode="aspectFill"
      />
      <view class="face-capture-panel__guide face-capture-panel__guide--preview">
        <view class="face-capture-panel__guide-frame">
          <view class="face-capture-panel__guide-oval" />
        </view>
        <text class="face-capture-panel__guide-label">请确认面部在框内且清晰</text>
      </view>
    </view>

    <FaceCaptureConfirmCard
      :visible="true"
      :submitting="submitting"
      @retake="$emit('capture')"
      @confirm="$emit('confirm')"
    />
  </view>
</template>
<script setup>
import FaceCaptureConfirmCard from '@/modules/students/components/FaceCaptureConfirmCard.vue'

defineProps({
  studentName: { type: String, default: '' },
  previewPath: { type: String, default: '' },
  tips: { type: String, default: '' },
  submitting: { type: Boolean, default: false },
  hasExistingFace: { type: Boolean, default: false }
})

defineEmits(['capture', 'confirm'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.face-capture-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.face-capture-panel__header {
  text-align: center;
  padding: 0 $space-32 $space-16;
}
.face-capture-panel__name {
  color: #f8fafc;
  font-size: 32rpx;
  font-weight: 700;
}
.face-capture-panel__tag {
  margin-top: $space-8;
  display: inline-block;
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
  background: rgba(34, 197, 94, 0.15);
  color: #86efac;
  font-size: 22rpx;
}
.face-capture-panel__tips {
  margin: 0 $space-32 $space-16;
  text-align: center;
  color: #e2e8f0;
  font-size: 28rpx;
}
.face-capture-panel__preview-area {
  position: relative;
  height: 680rpx;
  margin: 0 $space-24;
  border-radius: $radius-lg;
  overflow: hidden;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
}
.face-capture-panel__preview-img {
  width: 100%;
  height: 100%;
}
.face-capture-panel__guide {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
.face-capture-panel__guide-frame {
  position: relative;
  width: 520rpx;
  height: 640rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.face-capture-panel__guide-oval {
  width: 520rpx;
  height: 640rpx;
  border: 4rpx dashed rgba(34, 197, 94, 0.95);
  border-radius: 50%;
  background: rgba(34, 197, 94, 0.06);
}
.face-capture-panel__guide-label {
  margin-top: $space-24;
  color: #ffffff;
  font-size: 28rpx;
  text-align: center;
}
.face-capture-panel__guide--preview .face-capture-panel__guide-oval {
  border-style: solid;
  border-color: rgba(250, 204, 21, 0.95);
  background: rgba(250, 204, 21, 0.08);
}
.face-capture-panel__guide--preview .face-capture-panel__guide-label {
  color: #fde68a;
}
</style>
