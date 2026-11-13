<template>
  <view class="page-header">
    <view class="page-header__main">
      <view v-if="title" class="page-header__title">{{ title }}</view>
      <view v-if="subtitle" class="page-header__subtitle">{{ subtitle }}</view>
    </view>
    <view v-if="$slots.action" class="page-header__action">
      <slot name="action" />
    </view>
  </view>
</template>
<script setup>
defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' }
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $space-32;
  background: $color-bg-card;
  border-bottom: 1px solid $color-border;
}
.page-header__title {
  font-size: 36rpx;
  font-weight: 700;
  color: $color-text-primary;
}
.page-header__subtitle {
  margin-top: $space-8;
  font-size: 26rpx;
  color: $color-text-secondary;
}
</style>
