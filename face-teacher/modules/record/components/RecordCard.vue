<template>
  <view class="record-card" hover-class="record-card--hover" @click="$emit('open', item)">
    <image class="record-card__avatar" :src="item.photo" mode="aspectFill" lazy-load />
    <view class="record-card__main">
      <text class="record-card__name">{{ item.name }}</text>
      <text class="record-card__time">{{ item.displayTime }}</text>
      <text class="record-card__action-hint">课堂点评</text>
    </view>
    <view class="record-card__aside">
      <view
        class="record-card__badge"
        :class="item.canRePush ? 'record-card__badge--unpushed' : `record-card__badge--${item.status}`"
      >
        {{ item.canRePush ? '未推送' : item.statusText }}
      </view>
      <button
        v-if="item.canRePush"
        class="record-card__repush"
        :class="{ 'record-card__repush--loading': rePushingId === item.id }"
        :disabled="!!rePushingId"
        hover-class="record-card__repush--hover"
        @click.stop="$emit('re-push', item)"
      >
        {{ rePushingId === item.id ? '推送中...' : item.rePushText }}
      </button>
    </view>
  </view>
</template>
<script setup>
defineProps({
  item: { type: Object, required: true },
  rePushingId: { type: [Number, String], default: null }
})
defineEmits(['re-push', 'open'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';

.record-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: $color-bg-card;
  border-radius: $radius-md;
  padding: 20rpx 20rpx;
  border: 1px solid $color-card-border;
}
.record-card--hover {
  background: darken(#ffffff, 1.5%);
}
.record-card__avatar {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: $color-bg-inset;
  flex-shrink: 0;
  border: 2px solid $color-card-border;
}
.record-card__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.record-card__name {
  font-size: 28rpx;
  font-weight: 700;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.record-card__time {
  font-size: 22rpx;
  color: $color-text-muted;
}
.record-card__action-hint {
  font-size: 20rpx;
  color: $color-primary-dark;
  font-weight: 600;
}
.record-card__aside {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10rpx;
  flex-shrink: 0;
}
.record-card__badge {
  padding: 5rpx 14rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 600;
  white-space: nowrap;
}
.record-card__badge--checked_in {
  background: $color-success-soft;
  color: $color-success;
}
.record-card__badge--checked_out {
  background: $color-accent-blue-soft;
  color: $color-accent-blue;
}
.record-card__badge--unpushed {
  background: $color-warning-soft;
  color: $color-warning;
}
.record-card__repush {
  margin: 0;
  padding: 0 16rpx;
  height: 44rpx;
  line-height: 44rpx;
  font-size: 20rpx;
  font-weight: 600;
  color: $color-accent-blue;
  background: $color-accent-blue-soft;
  border: 1px solid rgba(37, 99, 235, 0.18);
  border-radius: 999rpx;
}
.record-card__repush::after { border: none; }
.record-card__repush--loading { opacity: 0.55; }
.record-card__repush--hover { opacity: 0.82; }
</style>
