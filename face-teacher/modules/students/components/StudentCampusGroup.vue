<template>
  <view class="campus-group">
    <view class="campus-group__header" hover-class="campus-group__header--hover" @click="$emit('toggle')">
      <view class="campus-group__left">
        <text class="campus-group__chevron" :class="{ 'is-collapsed': collapsed }">›</text>
        <text class="campus-group__name">{{ name }}</text>
      </view>
      <text class="campus-group__count">{{ total > 0 ? `${total} 人` : (loading ? '加载中' : count + ' 人') }}</text>
    </view>
    <view v-if="!collapsed" class="campus-group__body">
      <view v-if="loading && count === 0" class="campus-group__loading">
        <text class="campus-group__loading-text">加载中...</text>
      </view>
      <slot />
      <view v-if="!loading && !finished && count > 0" class="campus-group__loadmore" @click.stop="$emit('load-more')">
        <text class="campus-group__loadmore-text">加载更多</text>
      </view>
      <view v-if="loading && count > 0" class="campus-group__loading">
        <text class="campus-group__loading-text">加载中...</text>
      </view>
    </view>
  </view>
</template>
<script setup>
defineProps({
  name: { type: String, default: '未分配校区' },
  count: { type: Number, default: 0 },
  total: { type: Number, default: 0 },
  collapsed: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  finished: { type: Boolean, default: false }
})
defineEmits(['toggle', 'load-more'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.campus-group {
  margin-bottom: $space-16;
}
.campus-group__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $space-16 $space-24;
  border-radius: $radius-lg;
  background: $color-bg-inset;
  border: 1px solid $color-card-border;
}
.campus-group__header--hover {
  opacity: 0.88;
}
.campus-group__left {
  display: flex;
  align-items: center;
  gap: $space-8;
  min-width: 0;
}
.campus-group__chevron {
  width: 28rpx;
  color: $color-text-muted;
  font-size: 32rpx;
  line-height: 1;
  transform: rotate(90deg);
  transition: transform 0.18s ease;
}
.campus-group__chevron.is-collapsed {
  transform: rotate(0deg);
}
.campus-group__name {
  font-size: 28rpx;
  font-weight: 700;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.campus-group__count {
  flex-shrink: 0;
  margin-left: $space-16;
  font-size: 24rpx;
  color: $color-text-muted;
}
.campus-group__body {
  padding-top: $space-16;
}
.campus-group__loading {
  padding: $space-24 0;
  text-align: center;
}
.campus-group__loading-text {
  font-size: 24rpx;
  color: $color-text-muted;
}
.campus-group__loadmore {
  padding: $space-24 0;
  text-align: center;
}
.campus-group__loadmore-text {
  font-size: 26rpx;
  color: $color-primary;
}
</style>
