<template>
  <view class="punch-list">
    <view v-if="!settings.length && !loading" class="punch-list__empty">暂无打卡时段，请点击下方新增</view>
    <view v-for="item in settings" :key="item.id" class="punch-card">
      <view class="punch-card__main" @click="$emit('edit', item)">
        <view class="punch-card__name">{{ item.name }}</view>
        <view class="punch-card__time">{{ item.startTime }} - {{ item.endTime }}</view>
      </view>
      <view class="punch-card__actions">
        <view class="punch-card__badge" :class="item.enabled ? 'punch-card__badge--on' : ''">
          {{ item.enabled ? '启用' : '停用' }}
        </view>
        <view class="punch-card__delete" @click.stop="$emit('delete', item)">删除</view>
      </view>
    </view>
    <view v-if="loading" class="punch-list__hint">加载中...</view>
  </view>
</template>
<script setup>
defineProps({
  settings: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})
defineEmits(['edit', 'delete'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.punch-list__empty,
.punch-list__hint {
  text-align: center;
  color: $color-text-muted;
  padding: 40rpx 0;
  font-size: 26rpx;
}
.punch-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-16;
  padding: 24rpx;
  margin-bottom: $space-16;
  background: $color-bg-inset;
  border-radius: $radius-lg;
  border: 1px solid $color-card-border;
}
.punch-card:last-of-type { margin-bottom: 0; }
.punch-card__main { flex: 1; min-width: 0; }
.punch-card__name {
  font-size: 30rpx;
  font-weight: 700;
  color: $color-text-primary;
}
.punch-card__time {
  margin-top: 8rpx;
  font-size: 26rpx;
  color: $color-text-secondary;
}
.punch-card__actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 14rpx;
  flex-shrink: 0;
}
.punch-card__badge {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 600;
  background: $color-bg-card;
  color: $color-text-muted;
  border: 1px solid $color-card-border;
}
.punch-card__badge--on {
  background: $color-success-soft;
  color: $color-success;
  border-color: transparent;
}
.punch-card__delete {
  padding: 4rpx 16rpx;
  font-size: 24rpx;
  color: $color-text-muted;
  border-radius: 999rpx;
  border: 1px solid $color-card-border;
  background: $color-bg-card;
}
</style>
