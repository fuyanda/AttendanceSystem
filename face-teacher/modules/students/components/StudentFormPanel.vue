<template>
  <view class="student-form">
    <view class="student-form__card student-form__card--hero">
      <StudentFormFaceHero
        :photo="form.photo"
        :name="form.name"
        @capture="$emit('capture-face')"
      />
    </view>

    <view class="student-form__section-title">基本信息</view>
    <view class="student-form__card">
      <view class="student-form__cell">
        <view class="student-form__label">归属校区</view>
        <picker
          mode="selector"
          :range="deptOptions"
          range-key="label"
          :value="deptIndex"
          @change="$emit('dept-change', $event)"
        >
          <view class="student-form__control student-form__control--picker">
            <text :class="{ 'student-form__placeholder': !form.deptId }">{{ deptLabel }}</text>
            <text class="student-form__chevron">›</text>
          </view>
        </picker>
      </view>
      <view class="student-form__cell">
        <view class="student-form__label">学生姓名</view>
        <input
          :value="form.name"
          class="student-form__control student-form__control--input"
          placeholder="请输入姓名"
          placeholder-class="student-form__placeholder"
          @input="onNameInput"
        />
      </view>
      <view class="student-form__cell">
        <view class="student-form__label">学生电话</view>
        <input
          :value="form.phone"
          class="student-form__control student-form__control--input"
          type="number"
          placeholder="请输入学生电话"
          placeholder-class="student-form__placeholder"
          @input="onPhoneInput"
        />
      </view>
      <view class="student-form__cell student-form__cell--last">
        <view class="student-form__label">缴费截止日期</view>
        <picker mode="date" :value="form.deadline" @change="$emit('deadline-change', $event)">
          <view class="student-form__control student-form__control--picker">
            <text :class="{ 'student-form__placeholder': !form.deadline }">
              {{ form.deadline || '请选择缴费截止日期' }}
            </text>
            <text class="student-form__chevron">›</text>
          </view>
        </picker>
      </view>
    </view>

    <template v-if="isEdit">
      <view class="student-form__section-title">家长绑定</view>
      <view class="student-form__card">
        <view class="student-form__bind-code">{{ form.bindCode || '暂无绑定码' }}</view>
        <view class="student-form__bind-tip">家长输入绑定码，或微信扫下方二维码，即可关联该学生</view>
        <view class="student-form__bind-actions">
          <button class="student-form__chip student-form__chip--primary" @click="$emit('regenerate-bind-code')">
            重新生成
          </button>
          <button class="student-form__chip student-form__chip--success" @click="$emit('copy-bind-code')">
            复制绑定码
          </button>
        </view>
        <StudentBindQr :bind-code="form.bindCode" />
      </view>
    </template>

    <template v-if="isEdit && form.parents.length">
      <view class="student-form__section-title">已绑定家长</view>
      <view class="student-form__card student-form__card--flat">
        <view
          v-for="(parent, index) in form.parents"
          :key="index"
          class="student-form__parent"
          :class="{ 'student-form__parent--last': index === form.parents.length - 1 }"
        >
          <view class="student-form__parent-avatar">{{ (parent.name || '家').slice(0, 1) }}</view>
          <view class="student-form__parent-main">
            <view class="student-form__parent-name">{{ parent.name || '未填写姓名' }}</view>
            <view class="student-form__parent-meta">{{ parent.phone || '-' }}</view>
          </view>
          <view v-if="parent.isPrimary === 1" class="student-form__parent-tag">主联系人</view>
        </view>
      </view>
    </template>

    <view class="student-form__section-title">备注</view>
    <view class="student-form__card">
      <textarea
        :value="form.remark"
        class="student-form__textarea"
        placeholder="请输入备注信息（选填）"
        placeholder-class="student-form__placeholder"
        @input="onRemarkInput"
      />
    </view>
  </view>
</template>
<script setup>
import StudentFormFaceHero from '@/modules/students/components/StudentFormFaceHero.vue'
import StudentBindQr from '@/modules/students/components/StudentBindQr.vue'

defineProps({
  form: { type: Object, required: true },
  deptOptions: { type: Array, default: () => [] },
  deptIndex: { type: Number, default: 0 },
  deptLabel: { type: String, default: '' },
  isEdit: { type: Boolean, default: false }
})

const emit = defineEmits([
  'dept-change',
  'deadline-change',
  'regenerate-bind-code',
  'copy-bind-code',
  'capture-face',
  'update:name',
  'update:phone',
  'update:remark'
])

function onNameInput(e) {
  emit('update:name', e.detail.value)
}
function onPhoneInput(e) {
  emit('update:phone', e.detail.value)
}
function onRemarkInput(e) {
  emit('update:remark', e.detail.value)
}
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.student-form {
  padding: 0 $space-32 calc(32rpx + env(safe-area-inset-bottom));
}
.student-form__section-title {
  margin: $space-8 0 $space-16;
  padding-left: 4rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: $color-text-primary;
}
.student-form__card {
  background: $color-bg-card;
  border-radius: $radius-xl;
  box-shadow: $shadow-card;
  padding: $space-24 $space-32;
  margin-bottom: $space-24;
}
.student-form__card--hero {
  padding: 0;
  overflow: hidden;
}
.student-form__card--flat {
  padding: $space-16 $space-24;
}
.student-form__cell {
  margin-bottom: $space-24;
}
.student-form__cell--last {
  margin-bottom: 0;
}
.student-form__label {
  margin-bottom: 12rpx;
  font-size: 26rpx;
  color: $color-text-secondary;
}
.student-form__control {
  min-height: $input-height;
  padding: 0 28rpx;
  border-radius: 999rpx;
  background: $color-bg-page;
  box-sizing: border-box;
  font-size: 28rpx;
  color: $color-text-primary;
}
.student-form__control--input {
  width: 100%;
  height: $input-height;
  line-height: $input-height;
}
.student-form__control--picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: $input-height;
  line-height: $input-height;
}
.student-form__placeholder {
  color: $color-text-muted;
}
.student-form__chevron {
  color: $color-text-muted;
  font-size: 36rpx;
  line-height: 1;
}
.student-form__bind-code {
  padding: 24rpx 28rpx;
  border-radius: $radius-lg;
  background: $color-bg-page;
  font-size: 40rpx;
  font-weight: 700;
  letter-spacing: 4rpx;
  text-align: center;
  color: $color-text-primary;
}
.student-form__bind-tip {
  margin-top: $space-16;
  font-size: 24rpx;
  line-height: 1.6;
  color: $color-text-muted;
  text-align: center;
}
.student-form__bind-actions {
  display: flex;
  gap: $space-16;
  margin-top: $space-24;
}
.student-form__chip {
  flex: 1;
  height: 72rpx;
  line-height: 72rpx;
  margin: 0;
  padding: 0;
  border-radius: 999rpx;
  font-size: 26rpx;
  font-weight: 600;
}
.student-form__chip--primary {
  background: $color-primary-soft;
  color: $color-primary;
}
.student-form__chip--success {
  background: $color-success-soft;
  color: $color-success;
}
.student-form__parent {
  display: flex;
  align-items: center;
  gap: $space-16;
  padding: 20rpx 8rpx;
  border-bottom: 1px solid $color-border;
}
.student-form__parent--last {
  border-bottom: none;
}
.student-form__parent-avatar {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: $color-success-soft;
  color: $color-success;
  font-size: 30rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.student-form__parent-main {
  flex: 1;
  min-width: 0;
}
.student-form__parent-name {
  font-size: 28rpx;
  font-weight: 700;
  color: $color-text-primary;
}
.student-form__parent-meta {
  margin-top: 6rpx;
  font-size: 24rpx;
  color: $color-text-secondary;
}
.student-form__parent-tag {
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  background: $color-primary-soft;
  color: $color-primary;
  font-size: 22rpx;
  font-weight: 600;
  flex-shrink: 0;
}
.student-form__textarea {
  width: 100%;
  min-height: 180rpx;
  padding: 24rpx 28rpx;
  box-sizing: border-box;
  border-radius: $radius-lg;
  background: $color-bg-page;
  font-size: 28rpx;
  color: $color-text-primary;
  line-height: 1.6;
}
</style>
