<template>
  <view class="form-footer">
    <button
      class="form-footer__save"
      :loading="saving"
      :disabled="saving"
      @click="$emit('save')"
    >
      {{ saving ? '保存中...' : '保存' }}
    </button>
    <button v-if="isEdit" class="form-footer__archive" @click="$emit('archive')">
      归档学生
    </button>
  </view>
</template>
<script setup>
defineProps({
  saving: { type: Boolean, default: false },
  isEdit: { type: Boolean, default: false }
})
defineEmits(['save', 'archive'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.form-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 20;
  padding: $space-24 $space-32 calc($space-24 + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid $color-border;
  box-shadow: 0 -8rpx 24rpx rgba(15, 23, 42, 0.04);
}
.form-footer__save {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  margin: 0;
  padding: 0;
  border-radius: 999rpx;
  background: $color-primary;
  color: #fff;
  font-size: 30rpx;
  font-weight: 700;
  box-shadow: 0 10rpx 24rpx rgba(34, 197, 94, 0.28);
}
.form-footer__save[disabled] {
  opacity: 0.7;
}
.form-footer__archive {
  width: 100%;
  height: 72rpx;
  line-height: 72rpx;
  margin: $space-16 0 0;
  padding: 0;
  background: transparent;
  color: $color-danger;
  font-size: 26rpx;
}
</style>
