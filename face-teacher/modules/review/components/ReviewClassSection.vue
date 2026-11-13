<template>
  <view class="section-card">
    <view class="section-card__head">
      <view class="section-card__icon section-card__icon--green">课</view>
      <view class="section-card__titles">
        <text class="section-card__title">上课内容</text>
        <text class="section-card__desc">全班可见，家长端会一并展示</text>
      </view>
    </view>
    <textarea
      class="review-textarea"
      :value="classContent"
      :disabled="readonly"
      maxlength="500"
      placeholder="记录今天的上课内容、知识点或课堂亮点..."
      @input="$emit('update:classContent', $event.detail.value)"
    />
    <view class="review-count-row">
      <text class="review-count-row__hint">支持配图，最多 6 张</text>
      <text class="review-count-row__num">{{ (classContent || '').length }}/500</text>
    </view>
    <ReviewImagePicker
      :model-value="classImages"
      :disabled="readonly"
      @update:model-value="$emit('update:classImages', $event)"
    />
  </view>
</template>
<script setup>
import ReviewImagePicker from './ReviewImagePicker.vue'

defineProps({
  classContent: { type: String, default: '' },
  classImages: { type: Array, default: () => [] },
  readonly: { type: Boolean, default: false }
})
defineEmits(['update:classContent', 'update:classImages'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.section-card {
  background: $color-bg-card;
  border: 1px solid $color-card-border;
  border-radius: $radius-xl;
  padding: $space-24;
  margin-bottom: $space-16;
  box-shadow: $shadow-card;
}
.section-card__head {
  display: flex;
  align-items: center;
  gap: $space-16;
  margin-bottom: $space-24;
}
.section-card__icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 800;
  flex-shrink: 0;
}
.section-card__icon--green {
  background: $color-primary-soft;
  color: $color-primary-dark;
}
.section-card__titles {
  flex: 1;
  min-width: 0;
}
.section-card__title {
  display: block;
  font-size: 30rpx;
  font-weight: 750;
  color: $color-text-primary;
}
.section-card__desc {
  display: block;
  margin-top: 4rpx;
  font-size: 22rpx;
  color: $color-text-muted;
}
.review-textarea {
  width: 100%;
  min-height: 200rpx;
  padding: $space-16 $space-24;
  box-sizing: border-box;
  background: $color-bg-inset;
  border-radius: $radius-md;
  border: 1px solid $color-border;
  font-size: 28rpx;
  color: $color-text-primary;
  line-height: 1.6;
}
.review-count-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: $space-8;
}
.review-count-row__hint,
.review-count-row__num {
  font-size: 22rpx;
  color: $color-text-muted;
}
</style>
