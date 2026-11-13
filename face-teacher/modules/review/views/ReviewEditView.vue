<template>
  <PageContainer :style="fillStyle">
    <NavPageHeader title="课堂点评" show-back @back="goBack" />
    <view v-if="loading" class="edit-state">
      <view class="edit-state__dot" />
      <text class="edit-state__text">正在加载点评...</text>
    </view>
    <scroll-view v-else class="edit-scroll" scroll-y>
      <view class="edit-inner">
        <view class="hero">
          <view class="hero__top">
            <view class="hero__subject-wrap">
              <text class="hero__subject">{{ subjectLabel || '课堂点评' }}</text>
            </view>
            <text v-if="readonly" class="hero__badge hero__badge--sent">已发送</text>
            <text v-else class="hero__badge hero__badge--draft">草稿</text>
          </view>
          <view class="hero__meta">
            <view class="hero__meta-item hero__meta-item--time">
              <text class="hero__meta-label">上课时间</text>
              <view v-if="readonly" class="hero__meta-value">{{ classTimeDisplay }}</view>
              <view v-else class="hero__time-pickers">
                <picker mode="date" :value="classDate" @change="onClassDateChange">
                  <view class="hero__picker">
                    <text class="hero__picker-text">{{ classDate }}</text>
                    <text class="hero__picker-chevron">›</text>
                  </view>
                </picker>
                <picker mode="time" :value="classClock" @change="onClassTimeChange">
                  <view class="hero__picker">
                    <text class="hero__picker-text">{{ classClock }}</text>
                    <text class="hero__picker-chevron">›</text>
                  </view>
                </picker>
              </view>
            </view>
            <view class="hero__divider" />
            <view class="hero__meta-item">
              <text class="hero__meta-label">任课老师</text>
              <text class="hero__meta-value">{{ teacherName || '--' }}</text>
            </view>
          </view>
          <text class="hero__tip">填写上课内容与学员表现，保存草稿或发送给家长</text>
        </view>

        <ReviewClassSection
          :class-content="classContent"
          :class-images="classImages"
          :readonly="readonly"
          @update:class-content="classContent = $event"
          @update:class-images="classImages = $event"
        />

        <ReviewStudentSection :item="item" :readonly="readonly" />
      </view>
    </scroll-view>

    <view v-if="!loading && !readonly" class="edit-footer">
      <AppButton type="ghost" :loading="saving" :disabled="sending" @click="handleSave">存草稿</AppButton>
      <AppButton type="primary" :loading="sending" :disabled="saving" @click="handleSend">发送家长</AppButton>
    </view>
  </PageContainer>
</template>
<script setup>
import { pageFillStyle } from '@/shared/composables/useLayoutInsets.js'
import ReviewClassSection from '@/modules/review/components/ReviewClassSection.vue'
import ReviewStudentSection from '@/modules/review/components/ReviewStudentSection.vue'
import { useReviewEdit } from '@/modules/review/composables/useReviewEdit.js'

const fillStyle = pageFillStyle()
const {
  loading,
  saving,
  sending,
  readonly,
  subjectLabel,
  classDate,
  classClock,
  classTimeDisplay,
  teacherName,
  classContent,
  classImages,
  item,
  onClassDateChange,
  onClassTimeChange,
  handleSave,
  handleSend,
  goBack
} = useReviewEdit()
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
:deep(.page-container) {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.edit-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: $space-16;
  padding: 80rpx;
}
.edit-state__dot {
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: $color-primary;
  opacity: 0.75;
}
.edit-state__text {
  font-size: 26rpx;
  color: $color-text-muted;
}
.edit-scroll {
  flex: 1;
  min-height: 0;
  height: 0;
}
.edit-inner {
  padding: $space-16 $space-24;
  padding-bottom: calc(200rpx + env(safe-area-inset-bottom));
}
.hero {
  position: relative;
  overflow: hidden;
  background: linear-gradient(145deg, #ecfdf5 0%, #ffffff 55%, #f0fdf4 100%);
  border: 1px solid $color-primary-border;
  border-radius: $radius-xl;
  padding: $space-24 $space-24 $space-32;
  margin-bottom: $space-16;
  box-shadow: $shadow-card;
}
.hero__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $space-16;
}
.hero__subject-wrap {
  flex: 1;
  min-width: 0;
}
.hero__subject {
  font-size: 36rpx;
  font-weight: 800;
  color: $color-text-primary;
  letter-spacing: 1rpx;
}
.hero__badge {
  flex-shrink: 0;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
}
.hero__badge--draft {
  background: $color-warning-soft;
  color: $color-warning;
}
.hero__badge--sent {
  background: $color-accent-blue-soft;
  color: $color-accent-blue;
}
.hero__meta {
  display: flex;
  align-items: stretch;
  margin-top: $space-24;
  padding: $space-16 $space-24;
  background: rgba(255, 255, 255, 0.82);
  border-radius: $radius-md;
  border: 1px solid rgba(187, 247, 208, 0.7);
}
.hero__meta-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.hero__meta-label {
  font-size: 22rpx;
  color: $color-text-muted;
}
.hero__meta-value {
  font-size: 26rpx;
  font-weight: 650;
  color: $color-text-body;
}
.hero__divider {
  width: 1px;
  margin: 0 $space-16;
  background: $color-primary-border;
}
.hero__tip {
  display: block;
  margin-top: $space-16;
  font-size: 22rpx;
  color: $color-text-secondary;
  line-height: 1.5;
}

.hero__meta-item--time {
  flex: 1.35;
}
.hero__time-pickers {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  margin-top: 4rpx;
}
.hero__picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 48rpx;
  padding: 0 12rpx;
  border-radius: 12rpx;
  background: rgba(236, 253, 245, 0.95);
  border: 1px solid rgba(187, 247, 208, 0.9);
}
.hero__picker-text {
  font-size: 24rpx;
  font-weight: 650;
  color: $color-text-body;
}
.hero__picker-chevron {
  font-size: 28rpx;
  color: $color-primary-dark;
  transform: rotate(90deg);
  line-height: 1;
}
.edit-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  gap: $space-16;
  padding: $space-16 $space-24;
  padding-bottom: calc($space-16 + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid $color-border;
  box-shadow: 0 -8rpx 24rpx rgba(15, 23, 42, 0.04);
}
.edit-footer :deep(.app-button) {
  flex: 1;
  margin-top: 0;
  height: 88rpx;
  line-height: 88rpx;
}
</style>
