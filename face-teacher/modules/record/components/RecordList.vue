<template>
  <view class="record-section">
    <view class="record-section__header">
      <text class="record-section__title">{{ title }}</text>
      <text v-if="!campusLoading && !isSearching" class="record-section__count">
        共 {{ totalCount }} 条
      </text>
    </view>

    <scroll-view
      class="record-list"
      scroll-y
      :style="scrollFillStyle"
      @scrolltolower="$emit('load-more')"
    >
      <view class="record-list__content">
      <!-- 校区加载中 -->
      <view v-if="campusLoading" class="record-list__state">
        <text class="record-list__state-text">加载校区中...</text>
      </view>

      <!-- 搜索模式：平铺 -->
      <template v-else-if="isSearching">
        <view v-if="!searchRecords.length && !loading" class="record-list__empty">
          <text class="record-list__empty-icon">🔍</text>
          <text class="record-list__empty-text">未找到匹配记录</text>
        </view>
        <RecordCard
          v-for="item in searchRecords"
          :key="item.id + item.time"
          :item="item"
          :re-pushing-id="rePushingId"
          @re-push="$emit('re-push', $event)"
          @open="$emit('open', $event)"
        />
        <view v-if="loading" class="record-list__state">
          <text class="record-list__state-text">加载中...</text>
        </view>
      </template>

      <!-- 校区分组模式 -->
      <template v-else>
        <view v-if="!campusGroups.length && !campusLoading" class="record-list__empty">
          <text class="record-list__empty-icon">📋</text>
          <text class="record-list__empty-text">今日暂无记录</text>
          <text class="record-list__empty-hint">完成打卡后记录将显示在这里</text>
        </view>

        <view
          v-for="group in campusGroups"
          :key="group.key"
          class="campus-group"
        >
          <view
            class="campus-group__header"
            hover-class="campus-group__header--active"
            @click="$emit('toggle-campus', group.key)"
          >
            <view class="campus-group__left">
              <text
                class="campus-group__chevron"
                :class="{ 'is-collapsed': isCampusCollapsed(group.key) }"
              >›</text>
              <text class="campus-group__name">{{ group.name }}</text>
            </view>
            <view class="campus-group__right">
              <view v-if="group.total > 0" class="campus-group__badge">
                {{ group.total }} 条
              </view>
              <text v-else-if="group.loading" class="campus-group__meta">加载中</text>
              <text v-else class="campus-group__meta">{{ group.records.length }} 条</text>
            </view>
          </view>

          <view v-if="!isCampusCollapsed(group.key)" class="campus-group__body">
            <view v-if="group.loading && group.records.length === 0" class="campus-group__loading">
              <text class="campus-group__loading-text">加载中...</text>
            </view>

            <view
              v-for="item in group.records"
              :key="item.id + item.time"
              class="campus-group__card-wrap"
            >
              <RecordCard
                :item="item"
                :re-pushing-id="rePushingId"
                @re-push="$emit('re-push', $event)"
                @open="$emit('open', $event)"
              />
            </view>

            <view v-if="group.records.length === 0 && !group.loading" class="campus-group__empty">
              <text class="campus-group__empty-text">今日暂无记录</text>
            </view>

            <view
              v-if="!group.loading && !group.finished && group.records.length > 0"
              class="campus-group__loadmore"
              hover-class="campus-group__loadmore--active"
              @click.stop="$emit('campus-load-more', group.key)"
            >
              <text class="campus-group__loadmore-text">加载更多</text>
            </view>
            <view v-if="group.loading && group.records.length > 0" class="campus-group__loading">
              <text class="campus-group__loading-text">加载中...</text>
            </view>
          </view>
        </view>
      </template>
      </view>
    </scroll-view>
  </view>
</template>
<script setup>
import { computed, watch } from 'vue'
import { useScrollFillHeight } from '@/shared/composables/useScrollFillHeight.js'
import RecordCard from './RecordCard.vue'

const props = defineProps({
  title: { type: String, default: '签到记录' },
  campusGroups: { type: Array, default: () => [] },
  searchRecords: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  campusLoading: { type: Boolean, default: false },
  isSearching: { type: Boolean, default: false },
  isCampusCollapsed: { type: Function, default: () => false },
  rePushingId: { type: [Number, String], default: null }
})
defineEmits(['load-more', 're-push', 'open', 'toggle-campus', 'campus-load-more'])

const totalCount = computed(() => {
  if (props.isSearching) return props.searchRecords.length
  return props.campusGroups.reduce((s, g) => s + (g.total || g.records.length), 0)
})

// 按父容器实际高度计算，底部按钮已在文档流中，不再叠加预留
const { scrollHeightPx, measure } = useScrollFillHeight({
  containerSelector: '.record-section',
  anchorSelector: '.record-section__header',
  bottomReserveRpx: 0
})
const scrollFillStyle = computed(() => ({ height: `${scrollHeightPx.value}px` }))

watch(
  () => [props.campusLoading, props.campusGroups.length, props.isSearching],
  () => {
    setTimeout(measure, 80)
    setTimeout(measure, 320)
  }
)

defineExpose({ remeasure: measure })
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';

.record-section {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 0 $space-32;
  overflow: hidden;
}
.record-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
  flex-shrink: 0;
}
.record-section__title {
  font-size: 26rpx;
  font-weight: 700;
  color: $color-text-secondary;
}
.record-section__count {
  font-size: 22rpx;
  color: $color-text-muted;
}
.record-list {
  flex: 1;
  width: 100%;
  height: 0;
  box-sizing: border-box;
}
.record-list__content {
  padding-bottom: $space-24;
}

/* ── 状态提示 ─────────────────────────── */
.record-list__state {
  display: flex;
  justify-content: center;
  padding: 48rpx 0;
}
.record-list__state-text {
  font-size: 26rpx;
  color: $color-text-muted;
}
.record-list__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80rpx 32rpx;
  text-align: center;
}
.record-list__empty-icon { font-size: 64rpx; opacity: 0.3; }
.record-list__empty-text {
  margin-top: $space-16;
  font-size: 28rpx;
  font-weight: 600;
  color: $color-text-secondary;
}
.record-list__empty-hint {
  margin-top: 8rpx;
  font-size: 24rpx;
  color: $color-text-muted;
}

/* ── 校区分组 ─────────────────────────── */
.campus-group {
  margin-bottom: $space-16;
  background: $color-bg-card;
  border-radius: $radius-lg;
  border: 1px solid $color-card-border;
  box-shadow: $shadow-card;
  overflow: hidden;
}
.campus-group__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22rpx 24rpx;
  background: $color-bg-inset;
  border-bottom: 1px solid $color-card-border;
}
.campus-group__header--active {
  background: darken(#f8fafc, 2%);
}
.campus-group__left {
  display: flex;
  align-items: center;
  gap: 14rpx;
}
.campus-group__chevron {
  font-size: 28rpx;
  color: $color-text-muted;
  display: inline-block;
  transform: rotate(90deg);
  &.is-collapsed { transform: rotate(0deg); }
}
.campus-group__name {
  font-size: 26rpx;
  font-weight: 700;
  color: $color-text-primary;
}
.campus-group__right {
  display: flex;
  align-items: center;
}
.campus-group__badge {
  padding: 4rpx 16rpx;
  background: $color-success-soft;
  color: $color-success;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 700;
}
.campus-group__meta {
  font-size: 22rpx;
  color: $color-text-muted;
}
.campus-group__body {
  padding: $space-16;
}
.campus-group__card-wrap {
  margin-bottom: $space-12;
  &:last-child { margin-bottom: 0; }
}
.campus-group__loading, .campus-group__empty {
  display: flex;
  justify-content: center;
  padding: 32rpx 0;
}
.campus-group__loading-text, .campus-group__empty-text {
  font-size: 24rpx;
  color: $color-text-muted;
}
.campus-group__loadmore {
  margin-top: $space-12;
  padding: 18rpx 0;
  display: flex;
  justify-content: center;
  border-top: 1px solid $color-card-border;
}
.campus-group__loadmore--active {
  background: $color-bg-inset;
}
.campus-group__loadmore-text {
  font-size: 24rpx;
  color: $color-primary;
  font-weight: 600;
}
</style>
