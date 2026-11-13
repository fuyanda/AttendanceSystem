<template>
  <view class="score-row">
    <view class="score-row__left">
      <text class="score-row__label">{{ label }}</text>
      <text class="score-row__value">{{ modelValue }}/5</text>
    </view>
    <view class="score-row__stars">
      <view
        v-for="n in 5"
        :key="n"
        class="score-row__star-hit"
        @click="onPick(n)"
      >
        <text
          class="score-row__star"
          :class="{ 'is-on': n <= modelValue, 'is-disabled': disabled }"
        >★</text>
      </view>
    </view>
  </view>
</template>
<script setup>
const props = defineProps({
  label: { type: String, required: true },
  modelValue: { type: Number, default: 5 },
  disabled: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue'])

function onPick(n) {
  if (props.disabled) return
  emit('update:modelValue', n)
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.score-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-16;
  padding: $space-16 0;
}
.score-row__left {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 140rpx;
}
.score-row__label {
  font-size: 26rpx;
  font-weight: 650;
  color: $color-text-body;
}
.score-row__value {
  font-size: 22rpx;
  color: $color-warning;
  font-weight: 700;
}
.score-row__stars {
  display: flex;
  align-items: center;
}
.score-row__star-hit {
  padding: 4rpx 6rpx;
}
.score-row__star {
  font-size: 42rpx;
  color: #e2e8f0;
  line-height: 1;
}
.score-row__star.is-on {
  color: #f59e0b;
  text-shadow: 0 2rpx 8rpx rgba(245, 158, 11, 0.28);
}
.score-row__star.is-disabled {
  opacity: 0.75;
}
</style>
