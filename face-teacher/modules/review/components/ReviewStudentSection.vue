<template>
  <view class="section-card">
    <view class="section-card__head">
      <image
        v-if="avatarUrl"
        class="section-card__avatar"
        :src="avatarUrl"
        mode="aspectFill"
      />
      <view v-else class="section-card__icon section-card__icon--blue">
        {{ nameInitial }}
      </view>
      <view class="section-card__titles">
        <view class="section-card__title-row">
          <text class="section-card__title">{{ item.studentName || '学员点评' }}</text>
          <text class="section-card__tag">到课</text>
        </view>
        <text class="section-card__desc">{{ metaText }}</text>
      </view>
    </view>

    <view class="score-board">
      <ReviewScoreRow
        label="纪律专注"
        :model-value="item.disciplineScore"
        :disabled="readonly"
        @update:model-value="onScore('disciplineScore', $event)"
      />
      <view class="score-board__line" />
      <ReviewScoreRow
        label="活跃参与"
        :model-value="item.participationScore"
        :disabled="readonly"
        @update:model-value="onScore('participationScore', $event)"
      />
    </view>

    <view class="field-label">课堂评语</view>
    <textarea
      class="review-textarea"
      :value="item.content"
      :disabled="readonly"
      maxlength="1000"
      placeholder="针对此学员的表现评语，仅该学员家长可见..."
      @input="onContent"
    />
    <view class="review-count-row">
      <text class="review-count-row__hint">可上传课堂表现图片</text>
      <text class="review-count-row__num">{{ (item.content || '').length }}/1000</text>
    </view>
    <ReviewImagePicker
      :model-value="item.images"
      :disabled="readonly"
      @update:model-value="onImages"
    />
  </view>
</template>
<script setup>
import { computed } from 'vue'
import { getHost } from '@/shared/config/host.js'
import ReviewScoreRow from './ReviewScoreRow.vue'
import ReviewImagePicker from './ReviewImagePicker.vue'

const props = defineProps({
  item: { type: Object, required: true },
  readonly: { type: Boolean, default: false }
})

const showHours = computed(() => {
  const it = props.item || {}
  return !!(it.feeId || it.consumeId || it.consumeHours)
})

const metaText = computed(() => {
  if (showHours.value) {
    return `剩余 ${props.item.remainingHours || 0} 节 · 本次扣 ${props.item.consumeHours || 0} 节`
  }
  return '独立点评，未关联消课'
})

const nameInitial = computed(() => {
  const name = (props.item.studentName || '学').trim()
  return name ? name.slice(0, 1) : '学'
})

const avatarUrl = computed(() => {
  const path = props.item.studentAvatar || ''
  if (!path) return ''
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  return `${getHost()}${path}`
})

function onScore(key, val) {
  props.item[key] = val
}

function onContent(e) {
  props.item.content = e.detail.value
}

function onImages(list) {
  props.item.images = list
}
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
.section-card__avatar,
.section-card__icon {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  flex-shrink: 0;
}
.section-card__avatar {
  background: $color-bg-inset;
  border: 2px solid $color-primary-border;
}
.section-card__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 800;
}
.section-card__icon--blue {
  background: $color-accent-blue-soft;
  color: $color-accent-blue;
}
.section-card__titles {
  flex: 1;
  min-width: 0;
}
.section-card__title-row {
  display: flex;
  align-items: center;
  gap: $space-12;
}
.section-card__title {
  font-size: 30rpx;
  font-weight: 750;
  color: $color-text-primary;
}
.section-card__tag {
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: $color-success-soft;
  color: $color-success;
  font-size: 20rpx;
  font-weight: 700;
}
.section-card__desc {
  display: block;
  margin-top: 6rpx;
  font-size: 22rpx;
  color: $color-text-muted;
}
.score-board {
  background: $color-bg-inset;
  border: 1px solid $color-border;
  border-radius: $radius-md;
  padding: $space-8 $space-16;
  margin-bottom: $space-24;
}
.score-board__line {
  height: 1px;
  background: $color-border;
  margin: 0 $space-8;
}
.field-label {
  font-size: 26rpx;
  font-weight: 650;
  color: $color-text-body;
  margin-bottom: $space-12;
}
.review-textarea {
  width: 100%;
  min-height: 180rpx;
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
