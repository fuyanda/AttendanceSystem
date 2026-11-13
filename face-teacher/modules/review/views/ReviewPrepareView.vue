<template>
  <PageContainer :style="fillStyle">
    <NavPageHeader title="课堂点评" show-back @back="goBack" />
    <view class="prepare">
      <view class="prepare__hero">
        <view class="prepare__avatar">
          <text class="prepare__avatar-text">{{ nameInitial }}</text>
        </view>
        <view class="prepare__info">
          <text class="prepare__name">{{ studentName || '学员' }}</text>
          <text class="prepare__hint">选择科目后开始点评，无需报课或消课</text>
        </view>
      </view>

      <view class="prepare__card">
        <view class="prepare__card-head">
          <text class="prepare__card-title">选择科目</text>
          <text class="prepare__card-sub">用于家长通知展示</text>
        </view>
        <view v-if="loading" class="prepare__state">加载科目中...</view>
        <view v-else-if="!subjects.length" class="prepare__state">暂无科目配置</view>
        <view v-else class="subject-grid">
          <view
            v-for="s in subjects"
            :key="s.value"
            class="subject-chip"
            :class="{ 'is-active': selectedSubject === s.value }"
            hover-class="subject-chip--hover"
            @click="selectedSubject = s.value"
          >
            <text class="subject-chip__label">{{ s.label || s.value }}</text>
            <text v-if="selectedSubject === s.value" class="subject-chip__check">✓</text>
          </view>
        </view>
      </view>

      <view class="prepare__footer">
        <AppButton type="primary" :loading="submitting" :disabled="!canSubmit" @click="confirmCreate">
          开始点评
        </AppButton>
      </view>
    </view>
  </PageContainer>
</template>
<script setup>
import { computed } from 'vue'
import { pageFillStyle } from '@/shared/composables/useLayoutInsets.js'
import { useReviewPrepare } from '@/modules/review/composables/useReviewPrepare.js'

const fillStyle = pageFillStyle()
const {
  studentName,
  subjects,
  selectedSubject,
  loading,
  submitting,
  canSubmit,
  confirmCreate,
  goBack
} = useReviewPrepare()

const nameInitial = computed(() => {
  const name = (studentName.value || '学').trim()
  return name ? name.slice(0, 1) : '学'
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.prepare {
  padding: $space-16 $space-24 $space-32;
  padding-bottom: calc(180rpx + env(safe-area-inset-bottom));
}
.prepare__hero {
  display: flex;
  align-items: center;
  gap: $space-24;
  padding: $space-24;
  margin-bottom: $space-16;
  border-radius: $radius-xl;
  background: linear-gradient(135deg, $color-primary-soft 0%, #ffffff 70%);
  border: 1px solid $color-primary-border;
  box-shadow: $shadow-card;
}
.prepare__avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, $color-primary 0%, $color-primary-dark 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 8rpx 20rpx rgba(34, 197, 94, 0.28);
}
.prepare__avatar-text {
  color: #fff;
  font-size: 40rpx;
  font-weight: 800;
}
.prepare__info {
  flex: 1;
  min-width: 0;
}
.prepare__name {
  display: block;
  font-size: 34rpx;
  font-weight: 800;
  color: $color-text-primary;
}
.prepare__hint {
  display: block;
  margin-top: $space-8;
  font-size: 24rpx;
  color: $color-text-secondary;
  line-height: 1.45;
}
.prepare__card {
  background: $color-bg-card;
  border: 1px solid $color-card-border;
  border-radius: $radius-xl;
  padding: $space-24;
  box-shadow: $shadow-card;
}
.prepare__card-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: $space-24;
}
.prepare__card-title {
  font-size: 30rpx;
  font-weight: 750;
  color: $color-text-primary;
}
.prepare__card-sub {
  font-size: 22rpx;
  color: $color-text-muted;
}
.prepare__state {
  padding: $space-32 0;
  text-align: center;
  color: $color-text-muted;
  font-size: 26rpx;
}
.subject-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $space-16;
}
.subject-chip {
  position: relative;
  min-width: 148rpx;
  padding: 22rpx 28rpx;
  border-radius: 999rpx;
  border: 1px solid $color-border;
  background: $color-bg-inset;
  text-align: center;
}
.subject-chip.is-active {
  border-color: $color-primary;
  background: $color-primary-soft;
  box-shadow: 0 6rpx 16rpx rgba(34, 197, 94, 0.12);
}
.subject-chip--hover { opacity: 0.9; }
.subject-chip__label {
  font-size: 28rpx;
  font-weight: 650;
  color: $color-text-primary;
}
.subject-chip.is-active .subject-chip__label {
  color: $color-primary-dark;
}
.subject-chip__check {
  position: absolute;
  top: -8rpx;
  right: -4rpx;
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: $color-primary;
  color: #fff;
  font-size: 20rpx;
  line-height: 32rpx;
  text-align: center;
}
.prepare__footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: $space-16 $space-24;
  padding-bottom: calc($space-16 + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid $color-border;
  box-shadow: 0 -8rpx 24rpx rgba(15, 23, 42, 0.04);
}
.prepare__footer :deep(.app-button) {
  margin-top: 0;
  height: 88rpx;
  line-height: 88rpx;
}
</style>
