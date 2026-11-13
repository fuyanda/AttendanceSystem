<template>
  <view class="tab-bar-icon">
    <view v-if="name === 'home'" class="tab-bar-icon__svg">
      <view class="tab-bar-icon__ring" />
      <view class="tab-bar-icon__check" />
    </view>
    <view v-else-if="name === 'students'" class="tab-bar-icon__svg">
      <view class="tab-bar-icon__head tab-bar-icon__head--left" />
      <view class="tab-bar-icon__head tab-bar-icon__head--right" />
      <view class="tab-bar-icon__body tab-bar-icon__body--left" />
      <view class="tab-bar-icon__body tab-bar-icon__body--right" />
    </view>
    <view v-else-if="name === 'settings'" class="tab-bar-icon__svg">
      <view class="tab-bar-icon__gear-tooth tab-bar-icon__gear-tooth--top" />
      <view class="tab-bar-icon__gear-tooth tab-bar-icon__gear-tooth--bottom" />
      <view class="tab-bar-icon__gear-tooth tab-bar-icon__gear-tooth--left" />
      <view class="tab-bar-icon__gear-tooth tab-bar-icon__gear-tooth--right" />
      <view class="tab-bar-icon__gear-outer" />
      <view class="tab-bar-icon__gear-dot" />
    </view>
  </view>
</template>
<script setup>
defineProps({
  name: { type: String, required: true }
})
</script>
<style lang="scss" scoped>
.tab-bar-icon {
  width: 40rpx;
  height: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.tab-bar-icon__svg {
  position: relative;
  width: 36rpx;
  height: 36rpx;
  color: inherit;
}

.tab-bar-icon__ring {
  position: absolute;
  inset: 0;
  border: 3rpx solid currentColor;
  border-radius: 50%;
  box-sizing: border-box;
}
.tab-bar-icon__check {
  position: absolute;
  left: 9rpx;
  top: 10rpx;
  width: 14rpx;
  height: 8rpx;
  border-left: 3rpx solid currentColor;
  border-bottom: 3rpx solid currentColor;
  transform: rotate(-45deg);
  box-sizing: border-box;
}

.tab-bar-icon__head {
  position: absolute;
  width: 12rpx;
  height: 12rpx;
  border: 2.5rpx solid currentColor;
  border-radius: 50%;
  box-sizing: border-box;
  top: 2rpx;
}
.tab-bar-icon__head--left { left: 4rpx; }
.tab-bar-icon__head--right { right: 4rpx; }
.tab-bar-icon__body {
  position: absolute;
  width: 16rpx;
  height: 10rpx;
  border: 2.5rpx solid currentColor;
  border-bottom: none;
  border-radius: 10rpx 10rpx 0 0;
  box-sizing: border-box;
  bottom: 2rpx;
}
.tab-bar-icon__body--left { left: 2rpx; }
.tab-bar-icon__body--right { right: 2rpx; }

.tab-bar-icon__gear-outer {
  position: absolute;
  left: 8rpx;
  top: 8rpx;
  width: 20rpx;
  height: 20rpx;
  border: 2.5rpx solid currentColor;
  border-radius: 50%;
  box-sizing: border-box;
}
.tab-bar-icon__gear-dot {
  position: absolute;
  left: 14rpx;
  top: 14rpx;
  width: 8rpx;
  height: 8rpx;
  border: 2rpx solid currentColor;
  border-radius: 50%;
  box-sizing: border-box;
}
.tab-bar-icon__gear-tooth {
  position: absolute;
  background: currentColor;
  border-radius: 2rpx;
}
.tab-bar-icon__gear-tooth--top {
  left: 16rpx;
  top: 2rpx;
  width: 4rpx;
  height: 6rpx;
  margin-left: -2rpx;
}
.tab-bar-icon__gear-tooth--bottom {
  left: 16rpx;
  bottom: 2rpx;
  width: 4rpx;
  height: 6rpx;
  margin-left: -2rpx;
}
.tab-bar-icon__gear-tooth--left {
  left: 2rpx;
  top: 16rpx;
  width: 6rpx;
  height: 4rpx;
  margin-top: -2rpx;
}
.tab-bar-icon__gear-tooth--right {
  right: 2rpx;
  top: 16rpx;
  width: 6rpx;
  height: 4rpx;
  margin-top: -2rpx;
}
</style>
