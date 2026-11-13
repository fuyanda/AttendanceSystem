<template>
  <view class="face-hero" @click="$emit('capture')">
    <view class="face-hero__avatar-wrap">
      <image
        v-if="photo"
        class="face-hero__avatar"
        :src="photo"
        mode="aspectFill"
      />
      <view v-else class="face-hero__avatar face-hero__avatar--placeholder">
        {{ placeholderText }}
      </view>
      <view class="face-hero__badge">
        <text class="face-hero__badge-icon">+</text>
      </view>
    </view>
    <view class="face-hero__title">{{ photo ? '重新拍照采集' : '拍照采集人脸' }}</view>
    <view class="face-hero__hint">用于打卡签到、签退人脸识别</view>
  </view>
</template>
<script setup>
import { computed } from 'vue'

const props = defineProps({
  photo: { type: String, default: '' },
  name: { type: String, default: '' }
})

defineEmits(['capture'])

const placeholderText = computed(() => {
  const text = (props.name || '').trim()
  return text ? text.slice(0, 1) : '人'
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.face-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $space-32 $space-24 $space-24;
}
.face-hero__avatar-wrap {
  position: relative;
  width: 176rpx;
  height: 176rpx;
}
.face-hero__avatar {
  width: 176rpx;
  height: 176rpx;
  border-radius: 50%;
  background: $color-bg-page;
  border: 4rpx solid $color-primary-border;
  box-sizing: border-box;
}
.face-hero__avatar--placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-success-soft;
  color: $color-success;
  font-size: 64rpx;
  font-weight: 700;
}
.face-hero__badge {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: $color-primary;
  border: 4rpx solid $color-bg-card;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 16rpx rgba(34, 197, 94, 0.35);
}
.face-hero__badge-icon {
  font-size: 34rpx;
  line-height: 1;
  color: #fff;
  font-weight: 700;
}
.face-hero__title {
  margin-top: $space-24;
  font-size: 30rpx;
  font-weight: 700;
  color: $color-text-primary;
}
.face-hero__hint {
  margin-top: $space-8;
  font-size: 24rpx;
  color: $color-text-muted;
}
</style>
