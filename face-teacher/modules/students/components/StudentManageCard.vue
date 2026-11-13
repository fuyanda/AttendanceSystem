<template>
  <view class="student-card" hover-class="student-card--hover" @click="$emit('edit', student.id)">
    <image
      v-if="student.photo"
      class="student-card__avatar"
      :src="student.photo"
      mode="aspectFill"
    />
    <view v-else class="student-card__avatar student-card__avatar--placeholder">
      {{ student.name.slice(0, 1) }}
    </view>
    <view class="student-card__main">
      <view class="student-card__name">{{ student.name }}</view>
      <view class="student-card__meta">学号 {{ student.id }}</view>
    </view>
    <text class="student-card__arrow">›</text>
  </view>
</template>
<script setup>
defineProps({
  student: { type: Object, required: true }
})
defineEmits(['open', 'edit'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.student-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: $color-bg-card;
  border-radius: $radius-lg;
  padding: 20rpx 24rpx;
  margin-bottom: $space-16;
  box-shadow: $shadow-card;
  border: 1px solid $color-card-border;
}
.student-card--hover { background: $color-bg-inset; }
.student-card__avatar {
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  background: $color-border;
  flex-shrink: 0;
}
.student-card__avatar--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-primary-soft;
  color: $color-primary;
  font-size: 30rpx;
  font-weight: 700;
}
.student-card__main { flex: 1; min-width: 0; }
.student-card__name {
  font-size: 30rpx;
  font-weight: 700;
  color: $color-text-primary;
}
.student-card__meta {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: $color-text-muted;
}
.student-card__arrow {
  color: $color-text-muted;
  font-size: 36rpx;
  line-height: 1;
  padding-left: 8rpx;
}
</style>
