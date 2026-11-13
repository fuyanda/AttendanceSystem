<template>
  <view
    class="nav-header"
    :class="[`nav-header--${variant}`]"
    :style="headerStyle"
  >
    <view class="nav-header__side">
      <button v-if="showBack" class="nav-header__back" @click="$emit('back')">{{ backText }}</button>
      <view v-else class="nav-header__placeholder"></view>
    </view>
    <view class="nav-header__title">{{ title }}</view>
    <view class="nav-header__side nav-header__side--right">
      <slot name="action">
        <view class="nav-header__placeholder"></view>
      </slot>
    </view>
  </view>
</template>
<script setup>
import { computed } from 'vue'
import { useLayoutInsets } from '@/shared/composables/useLayoutInsets.js'

defineProps({
  title: { type: String, default: '' },
  showBack: { type: Boolean, default: false },
  backText: { type: String, default: '←' },
  variant: { type: String, default: 'default' }
})
defineEmits(['back'])

const { statusBarHeightPx } = useLayoutInsets()
const headerStyle = computed(() => ({
  paddingTop: `${statusBarHeightPx.value + uni.upx2px(16)}px`
}))
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-left: $space-32;
  padding-right: $space-32;
  padding-bottom: $space-24;
  background: $color-bg-card;
  border-bottom: 1px solid $color-card-border;
}
.nav-header--dark {
  background: transparent;
  color: #fff;
  border-bottom: none;
}
.nav-header__side {
  width: 160rpx;
  display: flex;
  align-items: center;
}
.nav-header__side--right { justify-content: flex-end; }
.nav-header__back {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: $color-bg-page;
  color: $color-text-primary;
  font-size: 32rpx;
  line-height: 72rpx;
  padding: 0;
  margin: 0;
}
.nav-header--dark .nav-header__back {
  width: auto;
  height: auto;
  border-radius: 0;
  background: transparent;
  color: #fff;
  font-size: 28rpx;
  line-height: 1.4;
}
.nav-header__placeholder { width: 72rpx; height: 72rpx; }
.nav-header__title {
  flex: 1;
  text-align: center;
  font-size: 34rpx;
  font-weight: 700;
  color: $color-text-primary;
  letter-spacing: 1rpx;
}
.nav-header--dark .nav-header__title {
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
}
</style>
