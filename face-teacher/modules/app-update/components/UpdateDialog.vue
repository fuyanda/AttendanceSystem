<template>
  <view v-if="visible" class="update-mask" @touchmove.stop.prevent>
    <view class="update-dialog" @click.stop>
      <view class="update-dialog__title">发现新版本 {{ info.versionName }}</view>
      <scroll-view class="update-dialog__body" scroll-y>
        <text class="update-dialog__changelog">{{ info.changelog || '优化体验与稳定性' }}</text>
      </scroll-view>
      <view v-if="downloading" class="update-dialog__progress">
        <view class="update-dialog__bar">
          <view class="update-dialog__bar-inner" :style="{ width: progress + '%' }" />
        </view>
        <text class="update-dialog__progress-text">下载中 {{ progress }}%</text>
      </view>
      <view class="update-dialog__actions">
        <view
          v-if="!info.forceUpdate && !downloading"
          class="update-dialog__btn update-dialog__btn--ghost"
          @click="$emit('later')"
        >稍后</view>
        <view
          class="update-dialog__btn update-dialog__btn--primary"
          :class="{ 'update-dialog__btn--disabled': downloading }"
          @click="$emit('confirm')"
        >{{ downloading ? '下载中...' : '立即更新' }}</view>
      </view>
    </view>
  </view>
</template>
<script setup>
defineProps({
  visible: { type: Boolean, default: false },
  info: {
    type: Object,
    default: () => ({
      versionName: '',
      changelog: '',
      forceUpdate: false
    })
  },
  downloading: { type: Boolean, default: false },
  progress: { type: Number, default: 0 }
})
defineEmits(['later', 'confirm'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.update-mask {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $space-32;
  box-sizing: border-box;
}
.update-dialog {
  width: 100%;
  max-width: 620rpx;
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: $space-32;
  box-shadow: $shadow-card;
}
.update-dialog__title {
  font-size: 34rpx;
  font-weight: 700;
  color: $color-text-primary;
  text-align: center;
}
.update-dialog__body {
  margin-top: $space-24;
  max-height: 360rpx;
}
.update-dialog__changelog {
  font-size: 26rpx;
  line-height: 1.6;
  color: $color-text-secondary;
  white-space: pre-wrap;
}
.update-dialog__progress {
  margin-top: $space-24;
}
.update-dialog__bar {
  height: 12rpx;
  border-radius: 999rpx;
  background: $color-bg-inset;
  overflow: hidden;
}
.update-dialog__bar-inner {
  height: 100%;
  background: $color-primary;
  border-radius: 999rpx;
  transition: width 0.2s ease;
}
.update-dialog__progress-text {
  display: block;
  margin-top: $space-8;
  text-align: center;
  font-size: 24rpx;
  color: $color-text-muted;
}
.update-dialog__actions {
  display: flex;
  gap: $space-16;
  margin-top: $space-32;
}
.update-dialog__btn {
  flex: 1;
  height: 80rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
}
.update-dialog__btn--ghost {
  background: $color-bg-inset;
  color: $color-text-secondary;
}
.update-dialog__btn--primary {
  background: $color-primary;
  color: #fff;
}
.update-dialog__btn--disabled {
  opacity: 0.7;
}
</style>
