<template>
  <view class="date-filter" :class="{ 'date-filter--compact': compact }">
    <picker mode="date" :value="modelValue" :end="today" @change="onChange">
      <view class="date-filter__trigger" hover-class="date-filter__trigger--hover">
        <text class="date-filter__icon">📅</text>
        <text class="date-filter__text">{{ displayText }}</text>
        <text class="date-filter__arrow">▼</text>
      </view>
    </picker>
    <view
      v-if="!isToday"
      class="date-filter__reset"
      hover-class="date-filter__reset--hover"
      @click="$emit('reset')"
    >
      今天
    </view>
  </view>
</template>
<script setup>
import { computed } from 'vue'
import { formatDate } from '@/modules/record/api/index.js'

const props = defineProps({
  modelValue: { type: String, required: true },
  compact: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'change', 'reset'])

const today = formatDate(new Date())
const isToday = computed(() => props.modelValue === today)

const displayText = computed(() => {
  if (!props.modelValue) return '选择日期'
  if (isToday.value) return '今天'
  return formatDisplay(props.modelValue)
})

function formatDisplay(day) {
  const parts = String(day).split('-')
  if (parts.length !== 3) return day
  return `${Number(parts[1])}/${Number(parts[2])}`
}

function onChange(e) {
  const value = (e.detail && e.detail.value) || ''
  if (!value || value === props.modelValue) return
  emit('update:modelValue', value)
  emit('change', value)
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.date-filter {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin: 0 $space-32 $space-16;
}
.date-filter--compact {
  margin: 0;
  flex-shrink: 0;
}
.date-filter__trigger {
  min-width: 180rpx;
  height: 68rpx;
  padding: 0 20rpx;
  border-radius: 999rpx;
  background: $color-bg-card;
  border: 1px solid $color-card-border;
  display: flex;
  align-items: center;
  gap: 8rpx;
}
.date-filter__trigger--hover { opacity: 0.88; }
.date-filter__icon { font-size: 24rpx; }
.date-filter__text {
  font-size: 24rpx;
  font-weight: 600;
  color: $color-text-primary;
}
.date-filter__arrow {
  font-size: 16rpx;
  color: $color-text-muted;
}
.date-filter__reset {
  flex-shrink: 0;
  height: 68rpx;
  padding: 0 18rpx;
  border-radius: 999rpx;
  background: $color-primary-soft;
  border: 1px solid $color-primary-border;
  color: $color-primary-dark;
  font-size: 22rpx;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}
.date-filter__reset--hover { opacity: 0.85; }
</style>
