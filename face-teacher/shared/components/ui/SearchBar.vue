<template>
  <view class="search-bar">
    <view class="search-bar__icon">⌕</view>
    <input
      class="search-bar__input"
      :value="modelValue"
      :placeholder="placeholder"
      confirm-type="search"
      @input="onInput"
      @confirm="onConfirm"
    />
  </view>
</template>
<script setup>
defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '搜索...' }
})
const emit = defineEmits(['update:modelValue', 'search'])
function onInput(e) {
  emit('update:modelValue', e.detail.value)
}
function onConfirm(e) {
  emit('search', e.detail.value)
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.search-bar {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 0 $space-32 12rpx;
  padding: 0 $space-24;
  height: 68rpx;
  border-radius: 999rpx;
  background: $color-bg-inset;
  border: 1px solid $color-card-border;
}
.search-bar__icon {
  color: $color-text-muted;
  font-size: 26rpx;
}
.search-bar__input {
  flex: 1;
  font-size: 26rpx;
  color: $color-text-primary;
}
</style>
