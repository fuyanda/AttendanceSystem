<template>
  <view class="manual-list">
    <view class="manual-list__toolbar">
      <view class="manual-list__summary">
        <text v-if="campusLabel">{{ campusLabel }} · </text>
        共 {{ candidates.length }} 人待签退
      </view>
      <view
        class="manual-list__select-all"
        :class="{ 'manual-list__select-all--disabled': !candidates.length || submitting }"
        @click="$emit('toggle-all')"
      >
        {{ allSelected ? '取消全选' : '全选' }}
      </view>
    </view>
    <scroll-view class="manual-list__scroll" scroll-y :style="scrollFillStyle">
      <view v-if="!candidates.length && !loading" class="manual-list__empty">
        该校区暂无待签退学生
      </view>
      <view
        v-for="item in candidates"
        :key="item.studentId"
        class="manual-card"
        :class="{ 'manual-card--selected': isSelected(item.studentId) }"
        @click="$emit('toggle', item.studentId)"
      >
        <view class="manual-card__check" :class="{ 'manual-card__check--on': isSelected(item.studentId) }">
          <text v-if="isSelected(item.studentId)">✓</text>
        </view>
        <image class="manual-card__avatar" :src="item.photo" mode="aspectFill" />
        <view class="manual-card__main">
          <view class="manual-card__name">{{ item.name }}</view>
          <view class="manual-card__meta">签到: {{ item.displayTime }}</view>
        </view>
      </view>
      <view v-if="loading" class="manual-list__hint">加载中...</view>
    </scroll-view>
  </view>
</template>
<script setup>
import { computed } from 'vue'
import { useScrollFillHeight } from '@/shared/composables/useScrollFillHeight.js'

defineProps({
  candidates: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false },
  allSelected: { type: Boolean, default: false },
  campusLabel: { type: String, default: '' },
  isSelected: { type: Function, required: true }
})
defineEmits(['toggle', 'toggle-all'])

const { scrollHeightPx } = useScrollFillHeight({
  anchorSelector: '.manual-list__toolbar',
  bottomReserveRpx: 180
})
const scrollFillStyle = computed(() => ({ height: `${scrollHeightPx.value}px` }))
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.manual-list {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 0 $space-32;
}
.manual-list__toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $space-16;
  flex-shrink: 0;
}
.manual-list__summary {
  font-size: 24rpx;
  color: $color-text-secondary;
}
.manual-list__select-all {
  font-size: 26rpx;
  font-weight: 600;
  color: $color-primary;
}
.manual-list__select-all--disabled {
  opacity: 0.5;
}
.manual-list__scroll {
  width: 100%;
  box-sizing: border-box;
}
.manual-card {
  display: flex;
  align-items: center;
  gap: $space-16;
  background: $color-bg-card;
  border-radius: $radius-xl;
  padding: 24rpx;
  margin-bottom: $space-16;
  box-shadow: $shadow-card;
  border: 2rpx solid transparent;
}
.manual-card--selected {
  border-color: $color-primary-border;
  background: $color-primary-soft;
}
.manual-card__check {
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  border: 2rpx solid $color-border;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24rpx;
  flex-shrink: 0;
}
.manual-card__check--on {
  background: $color-primary;
  border-color: $color-primary;
}
.manual-card__avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: $color-border;
  flex-shrink: 0;
}
.manual-card__main {
  flex: 1;
  min-width: 0;
}
.manual-card__name {
  font-size: 32rpx;
  font-weight: 700;
  color: $color-text-primary;
}
.manual-card__meta {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: $color-text-secondary;
}
.manual-list__empty,
.manual-list__hint {
  text-align: center;
  color: $color-text-muted;
  padding: 48rpx 0;
  font-size: 26rpx;
}
</style>
