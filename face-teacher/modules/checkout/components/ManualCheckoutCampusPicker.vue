<template>
  <view class="campus-picker">
    <view class="campus-picker__label">选择校区</view>
    <picker
      mode="selector"
      :range="options"
      range-key="label"
      :value="pickerIndex"
      :disabled="!options.length"
      @change="onChange"
    >
      <view class="campus-picker__control">
        <text :class="{ 'campus-picker__placeholder': !currentLabel }">
          {{ currentLabel || '请选择校区' }}
        </text>
        <text class="campus-picker__chevron">›</text>
      </view>
    </picker>
  </view>
</template>
<script setup>
import { computed } from 'vue'

const props = defineProps({
  options: { type: Array, default: () => [] },
  modelValue: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue', 'change'])

const pickerIndex = computed(() => {
  const idx = props.options.findIndex((item) => item.id === props.modelValue)
  return idx >= 0 ? idx : 0
})

const currentLabel = computed(() => {
  const current = props.options.find((item) => item.id === props.modelValue)
  return current?.label || ''
})

function onChange(e) {
  const index = Number(e.detail.value)
  const option = props.options[index]
  if (!option) return
  emit('update:modelValue', option.id)
  emit('change', option.id)
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.campus-picker {
  display: flex;
  align-items: center;
  gap: $space-16;
  margin: 0 $space-32 $space-16;
  padding: 20rpx $space-24;
  background: $color-bg-card;
  border-radius: $radius-lg;
  border: 1px solid $color-card-border;
  box-shadow: $shadow-card;
}
.campus-picker__label {
  flex-shrink: 0;
  font-size: 26rpx;
  font-weight: 600;
  color: $color-text-secondary;
}
.campus-picker__control {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-12;
  padding: 14rpx $space-16;
  border-radius: $radius-md;
  background: $color-bg-inset;
  border: 1px solid $color-card-border;
}
.campus-picker__placeholder {
  color: $color-text-muted;
}
.campus-picker__chevron {
  color: $color-text-muted;
  font-size: 28rpx;
}
</style>
