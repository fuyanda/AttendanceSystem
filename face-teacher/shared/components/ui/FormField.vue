<template>
  <view class="form-field">
    <text v-if="label" class="form-field__label">{{ label }}</text>
    <slot />
  </view>
</template>
<script setup>
defineProps({
  label: { type: String, default: '' }
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.form-field {
  margin-bottom: $space-24;
}
.form-field__label {
  display: block;
  margin-bottom: $space-8;
  color: $color-text-body;
  font-size: 26rpx;
}
</style>
