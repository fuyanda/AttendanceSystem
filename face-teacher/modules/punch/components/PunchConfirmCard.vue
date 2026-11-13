<template>
  <view v-if="result" class="punch-confirm">
    <view class="punch-confirm__label">识别结果</view>
    <view class="punch-confirm__name">{{ result.studentName }}</view>
    <view class="punch-confirm__score">置信度 {{ scoreText }}</view>

    <view v-if="isCheckin && feeLoading" class="punch-confirm__fee-tip">正在加载科目...</view>
    <view v-else-if="isCheckin && feeOptions.length" class="punch-confirm__fee">
      <view class="punch-confirm__fee-label">选择签到科目</view>
      <view
        v-for="item in feeOptions"
        :key="item.feeId"
        class="punch-confirm__fee-item"
        :class="{ 'is-active': selectedFeeId === item.feeId }"
        @click="$emit('select-fee', item.feeId)"
      >
        <view class="punch-confirm__fee-main">
          <text class="punch-confirm__fee-subject">{{ item.subjectLabel || item.subject }}</text>
          <text class="punch-confirm__fee-remain">剩余 {{ item.remainingHours }} 节</text>
        </view>
        <view class="punch-confirm__fee-meta">
          <text v-if="item.scheduledToday" class="punch-confirm__fee-tag">今日排课</text>
          <text v-if="item.teacherName" class="punch-confirm__fee-teacher">{{ item.teacherName }}</text>
        </view>
      </view>
    </view>

    <view class="punch-confirm__actions">
      <AppButton type="secondary" :block="false" @click="$emit('cancel')">取消</AppButton>
      <AppButton type="primary" :block="false" :loading="submitting" @click="$emit('confirm')">{{ submitText }}</AppButton>
    </view>
  </view>
</template>
<script setup>
import { computed } from 'vue'
const props = defineProps({
  result: { type: Object, default: null },
  submitting: { type: Boolean, default: false },
  submitText: { type: String, default: '确认' },
  isCheckin: { type: Boolean, default: false },
  feeOptions: { type: Array, default: () => [] },
  selectedFeeId: { type: Number, default: null },
  feeLoading: { type: Boolean, default: false }
})
defineEmits(['cancel', 'confirm', 'select-fee'])
const scoreText = computed(() => {
  const score = props.result?.score
  return score == null ? '-' : Number(score).toFixed(1)
})
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.punch-confirm {
  margin: 0 $space-32 $space-32;
  padding: $space-32;
  border-radius: $radius-lg;
  background: rgba(255, 255, 255, 0.08);
}
.punch-confirm__label { color: #cbd5e1; font-size: 26rpx; }
.punch-confirm__name { margin-top: 12rpx; font-size: 40rpx; font-weight: 700; }
.punch-confirm__score { margin-top: $space-8; color: #93c5fd; font-size: 26rpx; }
.punch-confirm__fee-tip {
  margin-top: $space-24;
  color: #94a3b8;
  font-size: 26rpx;
}
.punch-confirm__fee {
  margin-top: $space-24;
}
.punch-confirm__fee-label {
  color: #cbd5e1;
  font-size: 26rpx;
  margin-bottom: $space-12;
}
.punch-confirm__fee-item {
  padding: $space-16 $space-24;
  margin-bottom: $space-12;
  border-radius: $radius-md;
  border: 2rpx solid rgba(148, 163, 184, 0.35);
  background: rgba(15, 23, 42, 0.35);
}
.punch-confirm__fee-item.is-active {
  border-color: #60a5fa;
  background: rgba(37, 99, 235, 0.18);
}
.punch-confirm__fee-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.punch-confirm__fee-subject {
  font-size: 30rpx;
  font-weight: 600;
}
.punch-confirm__fee-remain {
  font-size: 26rpx;
  color: #93c5fd;
}
.punch-confirm__fee-meta {
  display: flex;
  gap: $space-12;
  margin-top: $space-8;
  font-size: 24rpx;
  color: #94a3b8;
}
.punch-confirm__fee-tag {
  color: #4ade80;
}
.punch-confirm__actions {
  display: flex;
  gap: $space-16;
  margin-top: 28rpx;
}
.punch-confirm__actions :deep(.app-button) {
  flex: 1;
  margin-top: 0;
}
</style>
