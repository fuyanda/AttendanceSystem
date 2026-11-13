<template>
  <view class="attend-header" :style="headerStyle">
    <view class="attend-header__inner">
      <view class="attend-header__left">
        <view class="attend-header__icon">
          <text class="attend-header__icon-text">📋</text>
        </view>
        <view>
          <view class="attend-header__title">打卡管理</view>
          <view v-if="deptName" class="attend-header__dept">{{ deptName }}</view>
        </view>
      </view>
      <view class="attend-header__date">{{ dateLabel }}</view>
    </view>
  </view>
</template>
<script setup>
import { computed } from 'vue'
import { useLayoutInsets } from '@/shared/composables/useLayoutInsets.js'

defineProps({
  deptName: { type: String, default: '' }
})

const { statusBarHeightPx } = useLayoutInsets()
const headerStyle = computed(() => ({
  paddingTop: `${statusBarHeightPx.value + uni.upx2px(8)}px`
}))

const dateLabel = computed(() => {
  const now = new Date()
  const m = now.getMonth() + 1
  const d = now.getDate()
  const days = ['日', '一', '二', '三', '四', '五', '六']
  return `${m}月${d}日 周${days[now.getDay()]}`
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.attend-header {
  padding-left: $space-32;
  padding-right: $space-32;
  padding-bottom: 12rpx;
  background: $color-bg-card;
  border-bottom: 1px solid $color-card-border;
}
.attend-header__inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 64rpx;
}
.attend-header__left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.attend-header__icon {
  width: 56rpx;
  height: 56rpx;
  border-radius: 14rpx;
  background: $color-primary-soft;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.attend-header__icon-text {
  font-size: 28rpx;
}
.attend-header__title {
  font-size: 28rpx;
  font-weight: 700;
  color: $color-text-primary;
  line-height: 1.2;
}
.attend-header__dept {
  margin-top: 2rpx;
  font-size: 20rpx;
  color: $color-primary-dark;
  font-weight: 600;
}
.attend-header__date {
  font-size: 22rpx;
  color: $color-text-muted;
  background: $color-bg-inset;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  border: 1px solid $color-border;
}
</style>
