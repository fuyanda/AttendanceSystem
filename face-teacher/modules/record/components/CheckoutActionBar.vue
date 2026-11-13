<template>
  <view class="checkout-bar" :class="{ 'checkout-bar--inline': inline }">
    <view
      class="checkout-bar__btn checkout-bar__btn--secondary"
      hover-class="checkout-bar__btn--hover"
      @click="$emit('manual')"
    >
      <text class="checkout-bar__icon">📝</text>
      <text class="checkout-bar__label">手动签退</text>
    </view>
    <view
      class="checkout-bar__btn checkout-bar__btn--primary"
      hover-class="checkout-bar__btn--hover"
      @click="$emit('punch')"
    >
      <view class="checkout-bar__dot" />
      <text class="checkout-bar__label">{{ punchLabel }}</text>
    </view>
  </view>
</template>
<script setup>
defineProps({
  punchLabel: { type: String, default: '拍照签退' },
  /** 文档流布局（不悬浮），用于首页列表页避免遮挡 */
  inline: { type: Boolean, default: false }
})
defineEmits(['manual', 'punch'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.checkout-bar {
  position: fixed;
  left: $space-32;
  right: $space-32;
  bottom: calc($tabbar-height + 20rpx + env(safe-area-inset-bottom));
  z-index: 20;
  display: flex;
  gap: $space-16;
}
.checkout-bar--inline {
  position: relative;
  left: auto;
  right: auto;
  bottom: auto;
  z-index: auto;
  flex-shrink: 0;
  margin: 0 $space-32 $space-16;
}
.checkout-bar__btn {
  flex: 1;
  height: 92rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10rpx;
}
.checkout-bar__btn--primary {
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 100%);
  box-shadow: $shadow-float;
}
.checkout-bar__btn--secondary {
  background: $color-bg-card;
  border: 1.5px solid $color-primary-border;
}
.checkout-bar__btn--hover { opacity: 0.88; }
.checkout-bar__icon {
  font-size: 28rpx;
}
.checkout-bar__label {
  font-size: 28rpx;
  font-weight: 600;
}
.checkout-bar__btn--primary .checkout-bar__label { color: #fff; }
.checkout-bar__btn--secondary .checkout-bar__label { color: $color-primary-dark; }
.checkout-bar__dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 0 5rpx rgba(255, 255, 255, 0.25);
}
</style>
