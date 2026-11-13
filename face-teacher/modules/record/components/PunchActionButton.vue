<template>
  <view
    class="punch-action"
    :class="{ 'punch-action--inline': inline }"
    hover-class="punch-action--hover"
    @click="$emit('click')"
  >
    <view class="punch-action__dot" />
    <text class="punch-action__label">{{ label }}</text>
  </view>
</template>
<script setup>
defineProps({
  label: { type: String, default: '开始签到' },
  /** 文档流布局（不悬浮），用于首页列表页避免遮挡 */
  inline: { type: Boolean, default: false }
})
defineEmits(['click'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.punch-action {
  position: fixed;
  left: $space-32;
  right: $space-32;
  bottom: calc($tabbar-height + 20rpx + env(safe-area-inset-bottom));
  height: 92rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
  box-shadow: $shadow-float;
  z-index: 20;
}
.punch-action--inline {
  position: relative;
  left: auto;
  right: auto;
  bottom: auto;
  z-index: auto;
  flex-shrink: 0;
  margin: 0 $space-32 $space-16;
}
.punch-action--hover { opacity: 0.92; transform: scale(0.99); }
.punch-action__dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 0 6rpx rgba(255, 255, 255, 0.22);
}
.punch-action__label {
  font-size: 30rpx;
  font-weight: 700;
  color: #fff;
  letter-spacing: 2rpx;
}
</style>
