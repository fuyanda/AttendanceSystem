<template>
  <view class="app-tabbar">
    <view
      v-for="item in tabs"
      :key="item.path"
      class="app-tabbar__item"
      :class="{ 'app-tabbar__item--active': current === item.key }"
      @click="switchTab(item)"
    >
      <view class="app-tabbar__icon-wrap" :class="{ 'app-tabbar__icon-wrap--active': current === item.key }">
        <TabBarIcon :name="item.key" />
      </view>
      <view class="app-tabbar__text">{{ item.text }}</view>
    </view>
  </view>
</template>
<script setup>
import TabBarIcon from '@/shared/components/ui/TabBarIcon.vue'

const props = defineProps({ current: { type: String, default: 'home' } })
const tabs = [
  { key: 'home', text: '打卡', path: '/pages/home/home' },
  { key: 'students', text: '学生', path: '/pages/students/students' },
  { key: 'settings', text: '设置', path: '/pages/settings/settings' }
]
function switchTab(item) {
  if (props.current === item.key) return
  uni.reLaunch({ url: item.path })
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.app-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  height: calc($tabbar-height + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid $color-card-border;
  display: flex;
  backdrop-filter: blur(12px);
  z-index: 30;
}
.app-tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  color: $color-text-muted;
}
.app-tabbar__item--active {
  color: $color-success;
}
.app-tabbar__icon-wrap {
  width: 56rpx;
  height: 44rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $color-text-muted;
}
.app-tabbar__icon-wrap--active {
  background: $color-primary-soft;
  color: $color-success;
}
.app-tabbar__text {
  font-size: 22rpx;
  font-weight: 600;
}
</style>
