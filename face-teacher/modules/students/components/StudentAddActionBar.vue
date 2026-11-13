<template>
  <view class="student-add-action" hover-class="student-add-action--hover" @click="$emit('click')">
    <text>{{ label }}</text>
  </view>
</template>
<script setup>
defineProps({
  label: { type: String, default: '+ 新增学生' }
})
defineEmits(['click'])
</script>
<style lang="scss" scoped>
@import '@/shared/styles/tokens.scss';
.student-add-action {
  position: fixed;
  left: $space-32;
  right: $space-32;
  bottom: calc($tabbar-height + 20rpx + env(safe-area-inset-bottom));
  height: 92rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
  box-shadow: $shadow-float;
  z-index: 20;
}
.student-add-action--hover {
  opacity: 0.94;
}
</style>
