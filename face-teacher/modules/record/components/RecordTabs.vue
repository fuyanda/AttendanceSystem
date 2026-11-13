<template>
  <view class="record-tabs">
    <view
      v-for="item in tabs"
      :key="item.value"
      class="record-tabs__item"
      :class="{ 'record-tabs__item--active': modelValue === item.value }"
      @click="$emit('update:modelValue', item.value)"
    >
      <text class="record-tabs__icon">{{ item.icon }}</text>
      <text class="record-tabs__label">{{ item.label }}</text>
    </view>
  </view>
</template>
<script setup>
defineProps({ modelValue: { type: String, default: 'checkin' } })
defineEmits(['update:modelValue'])
const tabs = [
  { label: '签到记录', value: 'checkin', icon: '✅' },
  { label: '签退记录', value: 'checkout', icon: '🚪' }
]
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.record-tabs {
  display: flex;
  margin: 12rpx $space-32 12rpx;
  padding: 4rpx;
  border-radius: $radius-md;
  background: $color-bg-inset;
  border: 1px solid $color-card-border;
  gap: 4rpx;
}
.record-tabs__item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  padding: 12rpx 0;
  border-radius: $radius-md;
  color: $color-text-muted;
  font-size: 24rpx;
  font-weight: 600;
  transition: all 0.15s;
}
.record-tabs__icon {
  font-size: 24rpx;
  line-height: 1;
}
.record-tabs__label {
  font-size: 24rpx;
}
.record-tabs__item--active {
  background: $color-bg-card;
  color: $color-primary-dark;
  box-shadow: 0 2rpx 12rpx rgba(15, 23, 42, 0.06);
  border: 1px solid $color-primary-border;
}
</style>
