<template>
  <view class="menu-item" hover-class="menu-item--hover" @click="$emit('click')">
    <view class="menu-item__icon" :class="`menu-item__icon--${tone}`">{{ icon }}</view>
    <view class="menu-item__main">
      <view class="menu-item__title">{{ title }}</view>
      <view v-if="desc" class="menu-item__desc">{{ desc }}</view>
    </view>
    <text class="menu-item__arrow">›</text>
  </view>
</template>
<script setup>
defineProps({
  title: { type: String, required: true },
  desc: { type: String, default: '' },
  icon: { type: String, default: '•' },
  tone: { type: String, default: 'primary' }
})
defineEmits(['click'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.menu-item {
  display: flex;
  align-items: center;
  gap: $space-24;
  padding: 24rpx 0;
  border-bottom: 1px solid $color-card-border;
  transition: opacity 0.15s;
}
.menu-item:last-child { border-bottom: none; }
.menu-item--hover { opacity: 0.72; }
.menu-item__icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  flex-shrink: 0;
}
.menu-item__icon--primary { background: $color-primary-soft; color: $color-primary; }
.menu-item__icon--blue { background: #eff6ff; color: #2563eb; }
.menu-item__icon--orange { background: $color-warning-soft; color: $color-warning; }
.menu-item__main { flex: 1; min-width: 0; }
.menu-item__title { font-size: 30rpx; font-weight: 600; color: $color-text-primary; }
.menu-item__desc { margin-top: 6rpx; font-size: 24rpx; color: $color-text-secondary; line-height: 1.4; }
.menu-item__arrow { font-size: 34rpx; color: $color-text-muted; line-height: 1; }
</style>
