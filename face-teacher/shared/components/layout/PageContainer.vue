<template>
  <view class="page-container" :class="[`page-container--${variant}`]">
    <slot />
  </view>
</template>
<script setup>
defineProps({
  variant: { type: String, default: 'default' }
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.page-container {
  min-height: 100vh;
  box-sizing: border-box;
  background: $color-bg-page;
}
.page-container--gradient {
  background: linear-gradient(180deg, $color-primary-soft 0%, $color-bg-page 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $space-32;
}
.page-container--dark {
  background: $color-bg-dark;
  color: #fff;
}
</style>
