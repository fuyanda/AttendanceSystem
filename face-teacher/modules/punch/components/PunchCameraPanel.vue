<template>
  <view class="punch-camera">
    <image
      class="punch-camera__preview"
      :src="previewPath"
      mode="aspectFill"
    />
    <view class="punch-camera__guide punch-camera__guide--preview">
      <view class="punch-camera__oval" />
      <text class="punch-camera__hint">已拍摄，请确认识别结果</text>
    </view>
  </view>
</template>
<script setup>
defineProps({
  previewPath: { type: String, default: '', required: true }
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.punch-camera {
  position: relative;
  height: 720rpx;
  margin: 0 $space-32;
  border-radius: $radius-lg;
  overflow: hidden;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
}
.punch-camera__preview {
  width: 100%;
  height: 100%;
  display: block;
}
.punch-camera__guide {
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
.punch-camera__oval {
  width: 520rpx;
  height: 640rpx;
  border: 4rpx solid rgba(250, 204, 21, 0.95);
  border-radius: 50%;
  background: rgba(250, 204, 21, 0.08);
}
.punch-camera__hint {
  margin-top: $space-24;
  color: #fde68a;
  font-size: 28rpx;
  text-align: center;
}
</style>
