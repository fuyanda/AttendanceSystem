<template>
  <view class="checkin-bar" :class="{ 'checkin-bar--inline': inline }">
    <view
      class="checkin-bar__btn checkin-bar__btn--secondary"
      hover-class="checkin-bar__btn--hover"
      @click="$emit('live')"
    >
      <text class="checkin-bar__icon">📷</text>
      <text class="checkin-bar__label">常驻签到</text>
    </view>
    <view
      class="checkin-bar__btn checkin-bar__btn--primary"
      hover-class="checkin-bar__btn--hover"
      @click="$emit('punch')"
    >
      <view class="checkin-bar__dot" />
      <text class="checkin-bar__label">{{ punchLabel }}</text>
    </view>
  </view>
</template>
<script setup>
defineProps({
  punchLabel: { type: String, default: '开始签到' },
  /** 文档流布局（不悬浮），用于首页列表页避免遮挡 */
  inline: { type: Boolean, default: false }
})
defineEmits(['live', 'punch'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.checkin-bar {
  position: fixed;
  left: $space-32;
  right: $space-32;
  bottom: calc($tabbar-height + 20rpx + env(safe-area-inset-bottom));
  z-index: 20;
  display: flex;
  gap: $space-16;
}
.checkin-bar--inline {
  position: relative;
  left: auto;
  right: auto;
  bottom: auto;
  z-index: auto;
  flex-shrink: 0;
  margin: 0 $space-32 $space-16;
}
.checkin-bar__btn {
  flex: 1;
  height: 92rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
}
.checkin-bar__btn--primary {
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 100%);
  box-shadow: $shadow-float;
}
.checkin-bar__btn--secondary {
  background: $color-bg-card;
  border: 1.5px solid $color-primary-border;
}
.checkin-bar__btn--hover { opacity: 0.88; }
.checkin-bar__icon {
  font-size: 28rpx;
}
.checkin-bar__label {
  font-size: 28rpx;
  font-weight: 600;
}
.checkin-bar__btn--primary .checkin-bar__label { color: #fff; }
.checkin-bar__btn--secondary .checkin-bar__label { color: $color-primary-dark; }
.checkin-bar__dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 0 5rpx rgba(255, 255, 255, 0.25);
}
</style>
