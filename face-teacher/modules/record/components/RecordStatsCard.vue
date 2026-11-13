<template>
  <view class="stats-card">
    <view class="stats-card__header" @click="toggleCollapsed">
      <view class="stats-card__header-left">
        <text class="stats-card__title">{{ stats.title || '今日统计' }}</text>
        <view v-if="stats.total > 0 && !collapsed" class="stats-card__rate">
          {{ completionRate }}%
        </view>
      </view>
      <view class="stats-card__header-right">
        <text v-if="collapsed" class="stats-card__summary">
          {{ stats.signed }}/{{ stats.total }}
        </text>
        <text class="stats-card__chevron" :class="{ 'is-collapsed': collapsed }">›</text>
      </view>
    </view>

    <view v-if="!collapsed" class="stats-card__body">
      <!-- 三项总数 -->
      <view class="stats-card__row">
        <view class="stats-card__item stats-card__item--total">
          <view class="stats-card__value">{{ stats.total }}</view>
          <view class="stats-card__label">{{ stats.totalLabel || '总人数' }}</view>
        </view>
        <view class="stats-card__divider" />
        <view class="stats-card__item stats-card__item--success">
          <view class="stats-card__value">{{ stats.signed }}</view>
          <view class="stats-card__label">{{ stats.signedLabel }}</view>
        </view>
        <view class="stats-card__divider" />
        <view
          class="stats-card__item stats-card__item--pending"
          :class="{ 'stats-card__item--clickable': unsignedClickable }"
          @click.stop="onUnsignedClick"
        >
          <view class="stats-card__value">{{ stats.unsigned }}</view>
          <view class="stats-card__label">{{ stats.unsignedLabel }}</view>
        </view>
      </view>

      <!-- 进度条 -->
      <view v-if="stats.total > 0" class="stats-card__progress-wrap">
        <view
          class="stats-card__progress-bar"
          :style="{ width: completionRate + '%' }"
        />
      </view>

      <!-- 各校区统计 -->
      <view v-if="statsLoading" class="stats-card__dept-loading">
        <text>加载校区统计...</text>
      </view>
      <view v-else-if="deptStats && deptStats.length" class="stats-card__dept-section">
        <view class="stats-card__dept-title">{{ stats.deptSectionTitle || '各校区' }}</view>
        <view class="stats-card__dept-grid">
          <view
            v-for="item in deptStats"
            :key="item.deptId"
            class="stats-card__dept-item"
          >
            <text class="stats-card__dept-count">{{ item.count }}</text>
            <text class="stats-card__dept-name">{{ item.deptName || '未分配' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>
<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  stats: {
    type: Object,
    default: () => ({
      total: 0,
      signed: 0,
      unsigned: 0,
      totalLabel: '总人数',
      signedLabel: '已签到',
      unsignedLabel: '未签到',
      deptSectionTitle: '各校区'
    })
  },
  deptStats: { type: Array, default: () => [] },
  statsLoading: { type: Boolean, default: false },
  unsignedClickable: { type: Boolean, default: false }
})

const emit = defineEmits(['layout-change', 'unsigned-click'])

const collapsed = ref(true)

function toggleCollapsed() {
  collapsed.value = !collapsed.value
  emit('layout-change')
}

function onUnsignedClick() {
  if (!props.unsignedClickable) return
  emit('unsigned-click')
}

const completionRate = computed(() => {
  if (!props.stats.total) return 0
  return Math.round((props.stats.signed / props.stats.total) * 100)
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';

.stats-card {
  margin: 0 $space-32 12rpx;
  background: $color-bg-card;
  border-radius: $radius-md;
  box-shadow: $shadow-card;
  border: 1px solid $color-card-border;
  overflow: hidden;
}
.stats-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
}
.stats-card__header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.stats-card__header-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.stats-card__title {
  font-size: 24rpx;
  font-weight: 700;
  color: $color-text-secondary;
}
.stats-card__rate {
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  background: $color-primary-soft;
  color: $color-primary-dark;
  font-size: 20rpx;
  font-weight: 700;
}
.stats-card__summary {
  font-size: 22rpx;
  color: $color-text-muted;
}
.stats-card__chevron {
  font-size: 28rpx;
  color: $color-text-muted;
  display: inline-block;
  transform: rotate(90deg);
  &.is-collapsed { transform: rotate(0deg); }
}
.stats-card__body {
  padding: 0 24rpx 16rpx;
}
.stats-card__row {
  display: flex;
  align-items: stretch;
  border-radius: $radius-md;
  background: $color-bg-inset;
  border: 1px solid $color-card-border;
  overflow: hidden;
}
.stats-card__item {
  flex: 1;
  padding: 12rpx 8rpx;
  text-align: center;
}
.stats-card__divider {
  width: 1px;
  background: $color-border;
  margin: 14rpx 0;
}
.stats-card__value {
  font-size: 34rpx;
  font-weight: 700;
  line-height: 1.2;
}
.stats-card__label {
  margin-top: 6rpx;
  font-size: 20rpx;
  color: $color-text-muted;
}
.stats-card__item--total .stats-card__value { color: $color-text-primary; }
.stats-card__item--success .stats-card__value { color: $color-success; }
.stats-card__item--pending .stats-card__value { color: $color-warning; }

/* 进度条 */
.stats-card__progress-wrap {
  height: 8rpx;
  background: $color-bg-inset;
  border-radius: 999rpx;
  margin-top: 16rpx;
  overflow: hidden;
}
.stats-card__progress-bar {
  height: 100%;
  background: linear-gradient(90deg, $color-primary 0%, $color-primary-dark 100%);
  border-radius: 999rpx;
  transition: width 0.4s ease;
  min-width: 0;
}

/* 校区统计 */
.stats-card__dept-section {
  margin-top: 20rpx;
  padding-top: 16rpx;
  border-top: 1px solid $color-card-border;
}
.stats-card__dept-title {
  font-size: 22rpx;
  color: $color-text-muted;
  margin-bottom: 14rpx;
  text-transform: uppercase;
  letter-spacing: 2rpx;
}
.stats-card__dept-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12rpx;
}
.stats-card__dept-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 110rpx;
  padding: 14rpx 16rpx;
  background: $color-bg-inset;
  border-radius: $radius-md;
  border: 1px solid $color-card-border;
}
.stats-card__dept-count {
  font-size: 32rpx;
  font-weight: 700;
  color: $color-success;
  line-height: 1.2;
}
.stats-card__dept-name {
  font-size: 20rpx;
  color: $color-text-muted;
  margin-top: 4rpx;
  text-align: center;
  line-height: 1.3;
}
.stats-card__dept-loading {
  margin-top: 16rpx;
  padding-top: 16rpx;
  border-top: 1px solid $color-card-border;
  text-align: center;
  font-size: 24rpx;
  color: $color-text-muted;
}
</style>
