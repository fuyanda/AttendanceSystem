<template>
  <view class="page-section" :class="{ 'page-section--card': card }">
    <view v-if="title" class="page-section__title">{{ title }}</view>
    <slot />
  </view>
</template>
<script setup>
defineProps({
  title: { type: String, default: '' },
  card: { type: Boolean, default: false }
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.page-section {
  padding: $space-24 $space-32;
}
.page-section--card {
  background: $color-bg-card;
  border-radius: $radius-lg;
  box-shadow: $shadow-card;
  border: 1px solid $color-card-border;
  margin: $space-16 $space-32;
  padding: $space-24 $space-32;
}
.page-section__title {
  font-size: 28rpx;
  font-weight: 700;
  color: $color-text-secondary;
  margin-bottom: $space-16;
  letter-spacing: 1rpx;
}
</style>
